param(
	[string] $SourceDir = (Join-Path $HOME ".runelite/bank-templates"),
	[string] $RepoIds = "70,127,209",
	[string] $OutputDir = "tools/research/community-templates/cache/local-imports"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $SourceDir))
{
	throw "Bank Templates source directory not found: $SourceDir"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)
$requestedIds = @(($RepoIds -split "[,\s]+") |
	Where-Object { ![string]::IsNullOrWhiteSpace($_) } |
	ForEach-Object { [int] $_ } |
	Sort-Object -Unique)
$templates = New-Object System.Collections.Generic.List[object]
$failures = New-Object System.Collections.Generic.List[object]
$duplicateImports = New-Object System.Collections.Generic.List[object]
$seenByRepoId = @{}

function Get-TextSha256([string] $value)
{
	$algorithm = [System.Security.Cryptography.SHA256]::Create()
	try
	{
		$bytes = [System.Text.Encoding]::UTF8.GetBytes($value)
		return ([System.BitConverter]::ToString($algorithm.ComputeHash($bytes))).Replace("-", "")
	}
	finally
	{
		$algorithm.Dispose()
	}
}

foreach ($path in (Get-ChildItem -LiteralPath $SourceDir -File -Filter "*.json" | Sort-Object Name))
{
	if ($path.Name -eq "owned-banks.json")
	{
		continue
	}

	try
	{
		$raw = [System.IO.File]::ReadAllText($path.FullName)
		$template = $raw | ConvertFrom-Json
		if ($null -eq $template.repoId -or $requestedIds -notcontains [int] $template.repoId)
		{
			continue
		}

		$repoId = [int] $template.repoId
		$columns = [int] $template.columns
		if ($columns -le 0)
		{
			throw "Template $repoId has invalid column count: $columns"
		}
		if ($null -eq $template.tabs -or @($template.tabs).Count -eq 0)
		{
			throw "Template $repoId has no tabs"
		}
		$sourceHash = (Get-FileHash -LiteralPath $path.FullName -Algorithm SHA256).Hash
		$layoutHash = Get-TextSha256 ($template.tabs | ConvertTo-Json -Depth 8 -Compress)
		$repoKey = [string] $repoId
		if ($seenByRepoId.ContainsKey($repoKey))
		{
			$kept = $seenByRepoId[$repoKey]
			$duplicateImports.Add([pscustomobject] @{
				TemplateId = $repoId
				KeptSourceFileName = $kept.SourceFileName
				DuplicateSourceFileName = $path.Name
				IdenticalLayout = $kept.LayoutSha256 -eq $layoutHash
			})
			continue
		}

		$placements = New-Object System.Collections.Generic.List[object]
		$tabSummaries = New-Object System.Collections.Generic.List[object]
		$positiveItemIds = New-Object System.Collections.Generic.List[int]
		$sentinelCounts = @{}

		foreach ($tab in $template.tabs)
		{
			$tabIndex = [int] $tab.tab
			$layout = @($tab.layout)
			$positiveCount = 0
			$sentinelCount = 0

			for ($slotIndex = 0; $slotIndex -lt $layout.Count; $slotIndex++)
			{
				$itemId = [int] $layout[$slotIndex]
				$state = if ($itemId -gt 0) { "item" } else { "sentinel" }
				if ($itemId -gt 0)
				{
					$positiveItemIds.Add($itemId)
					$positiveCount++
				}
				else
				{
					$sentinelKey = [string] $itemId
					if (!$sentinelCounts.ContainsKey($sentinelKey))
					{
						$sentinelCounts[$sentinelKey] = 0
					}
					$sentinelCounts[$sentinelKey]++
					$sentinelCount++
				}

				$placements.Add([pscustomobject] @{
					TemplateId = $repoId
					TabIndex = $tabIndex
					AbsolutePosition = $slotIndex
					Row = [Math]::Floor($slotIndex / $columns)
					Column = $slotIndex % $columns
					ItemId = $itemId
					State = $state
				})
			}

			$tabSummaries.Add([pscustomobject] @{
				TabIndex = $tabIndex
				CustomIconItemId = [int] $tab.customIconId
				SlotCount = $layout.Count
				RowCount = [Math]::Ceiling($layout.Count / [double] $columns)
				PositivePlacementCount = $positiveCount
				SentinelCount = $sentinelCount
			})
		}

		$uniqueItemIds = @($positiveItemIds | Sort-Object -Unique)
		$normalized = [ordered] @{
			SchemaVersion = 1
			Source = "local-manual-import"
			SourceFileName = $path.Name
			SourceSha256 = $sourceHash
			LayoutSha256 = $layoutHash
			TemplateId = $repoId
			Name = [string] $template.name
			Description = [string] $template.description
			Columns = $columns
			TabCount = @($template.tabs).Count
			PositivePlacementCount = $positiveItemIds.Count
			UniqueItemIdCount = $uniqueItemIds.Count
			DuplicatePlacementCount = $positiveItemIds.Count - $uniqueItemIds.Count
			SentinelCounts = $sentinelCounts
			Tabs = $tabSummaries.ToArray()
			Placements = $placements.ToArray()
		}

		$outputPath = Join-Path $OutputDir ("{0}.normalized.json" -f $repoId)
		[System.IO.File]::WriteAllText($outputPath, ($normalized | ConvertTo-Json -Depth 8), $utf8)
		$templates.Add([pscustomobject] @{
			TemplateId = $repoId
			SourceFileName = $path.Name
			SourceSha256 = $sourceHash
			LayoutSha256 = $layoutHash
			Columns = $columns
			TabCount = @($template.tabs).Count
			PositivePlacementCount = $positiveItemIds.Count
			UniqueItemIdCount = $uniqueItemIds.Count
			DuplicatePlacementCount = $positiveItemIds.Count - $uniqueItemIds.Count
			SentinelCount = ($sentinelCounts.Values | Measure-Object -Sum).Sum
			OutputFileName = [System.IO.Path]::GetFileName($outputPath)
		})
		$seenByRepoId[$repoKey] = [pscustomobject] @{
			SourceFileName = $path.Name
			LayoutSha256 = $layoutHash
		}
	}
	catch
	{
		$failures.Add([pscustomobject] @{
			SourceFileName = $path.Name
			Error = $_.Exception.Message
		})
	}
}

$foundIds = @($templates | Select-Object -ExpandProperty TemplateId | Sort-Object -Unique)
$missingIds = @($requestedIds | Where-Object { $foundIds -notcontains $_ })
$coverage = [ordered] @{
	GeneratedAtUtc = [DateTimeOffset]::UtcNow.ToString("o")
	SourceDirectory = $SourceDir
	RequestedTemplateIds = $requestedIds
	FoundTemplateIds = $foundIds
	MissingTemplateIds = $missingIds
	SuccessfulTemplates = $templates.Count
	FailedFiles = $failures.Count
	DuplicateImportFiles = $duplicateImports.Count
	Templates = $templates.ToArray()
	DuplicateImports = $duplicateImports.ToArray()
	Failures = $failures.ToArray()
}

[System.IO.File]::WriteAllText((Join-Path $OutputDir "coverage.json"),
	($coverage | ConvertTo-Json -Depth 8), $utf8)

$coverage | Format-List

if ($missingIds.Count -gt 0 -or $failures.Count -gt 0)
{
	exit 1
}
