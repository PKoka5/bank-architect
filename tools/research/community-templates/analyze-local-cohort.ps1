param(
	[string] $InputDir = "tools/research/community-templates/cache/local-imports",
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $MetadataPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-sort-metadata.tsv",
	[string] $OutputPath = "tools/research/community-templates/cache/local-imports/aggregate-analysis.json"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

foreach ($requiredPath in @($InputDir, $RegistryPath, $MetadataPath))
{
	if (!(Test-Path -LiteralPath $requiredPath))
	{
		throw "Required research input not found: $requiredPath"
	}
}

$registry = @{}
Get-Content -LiteralPath $RegistryPath |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Name, Category, ConstantName |
	ForEach-Object { $registry[[int] $_.ItemId] = $_ }

$metadata = @{}
Get-Content -LiteralPath $MetadataPath |
	Where-Object { $_ -and !$_.StartsWith("#") } |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Family, VariantKind, VariantValue, FoodRole, HealModel, ImmediateHealMin, ImmediateHealMax, SecondaryHeal, AreaRestriction, SourceKey |
	ForEach-Object { $metadata[[int] $_.ItemId] = $_ }

$files = @(Get-ChildItem -LiteralPath $InputDir -File -Filter "*.normalized.json" | Sort-Object Name)
if ($files.Count -eq 0)
{
	throw "No normalized template files found in: $InputDir"
}

$categoryCounts = @{}
$tabDominantCategories = @{}
$pairStats = @{ Horizontal = @{}; Vertical = @{} }
$blockObservations = New-Object System.Collections.Generic.List[object]
$sameFamily = [ordered] @{ Horizontal = 0; Vertical = 0 }
$doseDirection = [ordered] @{
	HorizontalDescending = 0
	HorizontalAscending = 0
	HorizontalOther = 0
	VerticalDescending = 0
	VerticalAscending = 0
	VerticalOther = 0
}
$positivePlacementCount = 0
$registryMatchCount = 0
$metadataMatchCount = 0
$tabCount = 0

function Add-Count([hashtable] $table, [string] $key, [int] $amount = 1)
{
	if (!$table.ContainsKey($key))
	{
		$table[$key] = 0
	}
	$table[$key] += $amount
}

function Add-Pair([hashtable] $table, [int] $leftItemId, [int] $rightItemId, [int] $templateId)
{
	$key = "$leftItemId>$rightItemId"
	if (!$table.ContainsKey($key))
	{
		$table[$key] = [pscustomobject] @{
			LeftItemId = $leftItemId
			RightItemId = $rightItemId
			Occurrences = 0
			TemplateIds = New-Object System.Collections.Generic.HashSet[int]
		}
	}
	$table[$key].Occurrences++
	[void] $table[$key].TemplateIds.Add($templateId)
}

function Measure-CategoryBlocks([object[]] $placements, [int] $columns, [int] $templateId)
{
	$categorized = @($placements | ForEach-Object {
		$itemId = [int] $_.ItemId
		if ($registry.ContainsKey($itemId) -and [string] $registry[$itemId].Category -ne "UNKNOWN")
		{
			[pscustomobject] @{
				Position = [int] $_.AbsolutePosition
				Row = [int] $_.Row
				Column = [int] $_.Column
				Category = [string] $registry[$itemId].Category
			}
		}
	})

	foreach ($categoryGroup in @($categorized | Group-Object Category))
	{
		if ($categoryGroup.Count -lt 8)
		{
			continue
		}

		$byPosition = @{}
		$unseen = New-Object System.Collections.Generic.HashSet[int]
		foreach ($placement in $categoryGroup.Group)
		{
			$position = [int] $placement.Position
			$byPosition[$position] = $placement
			[void] $unseen.Add($position)
		}

		while ($unseen.Count -gt 0)
		{
			$seed = [int] ($unseen | Select-Object -First 1)
			$queue = New-Object System.Collections.Generic.Queue[int]
			$queue.Enqueue($seed)
			[void] $unseen.Remove($seed)
			$component = New-Object System.Collections.Generic.List[object]

			while ($queue.Count -gt 0)
			{
				$position = [int] $queue.Dequeue()
				$component.Add($byPosition[$position])
				$column = $position % $columns
				$neighbors = @(([int] $position - $columns), ([int] $position + $columns))
				if ($column -gt 0) { $neighbors += ([int] $position - 1) }
				if ($column -lt $columns - 1) { $neighbors += ([int] $position + 1) }
				foreach ($neighbor in $neighbors)
				{
					if ($unseen.Remove([int] $neighbor))
					{
						$queue.Enqueue([int] $neighbor)
					}
				}
			}

			if ($component.Count -lt 8)
			{
				continue
			}

			$minRow = ($component.Row | Measure-Object -Minimum).Minimum
			$maxRow = ($component.Row | Measure-Object -Maximum).Maximum
			$minColumn = ($component.Column | Measure-Object -Minimum).Minimum
			$maxColumn = ($component.Column | Measure-Object -Maximum).Maximum
			$width = [int] $maxColumn - [int] $minColumn + 1
			$height = [int] $maxRow - [int] $minRow + 1
			$rowShapes = New-Object System.Collections.Generic.List[object]

			foreach ($row in $minRow..$maxRow)
			{
				$occupiedColumns = @($component | Where-Object { $_.Row -eq $row } |
					ForEach-Object { [int] $_.Column } | Sort-Object)
				if ($occupiedColumns.Count -eq 0)
				{
					continue
				}
				$span = $occupiedColumns[-1] - $occupiedColumns[0] + 1
				$rowShapes.Add([pscustomobject] @{
					Count = $occupiedColumns.Count
					Start = $occupiedColumns[0]
					Dense = $span -eq $occupiedColumns.Count
				})
			}

			$priorMaximum = $rowShapes[0].Count
			if ($rowShapes.Count -gt 1)
			{
				$priorMaximum = ($rowShapes | Select-Object -First ($rowShapes.Count - 1) |
					Measure-Object Count -Maximum).Maximum
			}
			[pscustomobject] @{
				Category = [string] $categoryGroup.Name
				TemplateId = $templateId
				Width = $width
				Density = [Math]::Round($component.Count / ($width * $height), 4)
				Rows = $rowShapes.Count
				DenseRows = @($rowShapes | Where-Object Dense).Count
				SameStartRows = @($rowShapes | Where-Object { $_.Start -eq $minColumn }).Count
				LeftAligned = $minColumn -eq 0
				ShortLastRow = $rowShapes.Count -gt 1 -and $rowShapes[-1].Count -lt $priorMaximum
			}
		}
	}
}

function Get-Median([double[]] $values)
{
	$sorted = @($values | Sort-Object)
	if ($sorted.Count -eq 0) { return 0 }
	$middle = [Math]::Floor($sorted.Count / 2)
	if ($sorted.Count % 2 -eq 1) { return $sorted[$middle] }
	return ($sorted[$middle - 1] + $sorted[$middle]) / 2
}

foreach ($file in $files)
{
	$document = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
	$templateId = [int] $document.TemplateId
	$columns = [int] $document.Columns
	if ($columns -le 0)
	{
		throw "Template $templateId has invalid column count: $columns"
	}

	foreach ($tab in $document.Tabs)
	{
		$tabCount++
		$tabIndex = [int] $tab.TabIndex
		$positions = @{}
		$knownCategoryCounts = @{}
		$placements = @($document.Placements | Where-Object {
			[int] $_.TabIndex -eq $tabIndex -and $_.State -eq "item"
		})
		foreach ($observation in @(Measure-CategoryBlocks $placements $columns $templateId))
		{
			$blockObservations.Add($observation)
		}

		foreach ($placement in $placements)
		{
			$itemId = [int] $placement.ItemId
			$position = [int] $placement.AbsolutePosition
			$positions[$position] = $itemId
			$positivePlacementCount++

			if ($registry.ContainsKey($itemId))
			{
				$registryMatchCount++
				$category = [string] $registry[$itemId].Category
				Add-Count $categoryCounts $category
				if ($category -ne "UNKNOWN")
				{
					Add-Count $knownCategoryCounts $category
				}
			}
			if ($metadata.ContainsKey($itemId))
			{
				$metadataMatchCount++
			}
		}

		if ($knownCategoryCounts.Count -gt 0)
		{
			$dominant = $knownCategoryCounts.GetEnumerator() |
				Sort-Object -Property @{ Expression = "Value"; Descending = $true }, @{ Expression = "Name"; Ascending = $true } |
				Select-Object -First 1
			$key = [string] $tabIndex
			if (!$tabDominantCategories.ContainsKey($key))
			{
				$tabDominantCategories[$key] = @{}
			}
			Add-Count $tabDominantCategories[$key] ([string] $dominant.Name)
		}

		foreach ($position in @($positions.Keys))
		{
			$leftItemId = [int] $positions[$position]
			foreach ($axis in @(
				[pscustomobject] @{ Name = "Horizontal"; Delta = 1 },
				[pscustomobject] @{ Name = "Vertical"; Delta = $columns }
			))
			{
				$rightPosition = [int] $position + [int] $axis.Delta
				if (!$positions.ContainsKey($rightPosition))
				{
					continue
				}
				if ($axis.Name -eq "Horizontal" -and
					[Math]::Floor([int] $position / $columns) -ne [Math]::Floor($rightPosition / $columns))
				{
					continue
				}

				$rightItemId = [int] $positions[$rightPosition]
				Add-Pair $pairStats[$axis.Name] $leftItemId $rightItemId $templateId

				if (!$metadata.ContainsKey($leftItemId) -or !$metadata.ContainsKey($rightItemId))
				{
					continue
				}
				$leftMetadata = $metadata[$leftItemId]
				$rightMetadata = $metadata[$rightItemId]
				if ([string] $leftMetadata.Family -ne [string] $rightMetadata.Family)
				{
					continue
				}

				$sameFamily[$axis.Name]++
				if ([string] $leftMetadata.VariantKind -ne "DOSE" -or
					[string] $rightMetadata.VariantKind -ne "DOSE")
				{
					continue
				}

				$leftValue = [int] $leftMetadata.VariantValue
				$rightValue = [int] $rightMetadata.VariantValue
				$direction = if ($rightValue -lt $leftValue) { "Descending" } elseif ($rightValue -gt $leftValue) { "Ascending" } else { "Other" }
				$doseDirection["$($axis.Name)$direction"]++
			}
		}
	}
}

function Convert-PairStats([hashtable] $table)
{
	return @($table.Values |
		ForEach-Object {
			[pscustomobject] @{
				LeftItemId = $_.LeftItemId
				LeftName = if ($registry.ContainsKey($_.LeftItemId)) { $registry[$_.LeftItemId].Name } else { "Unknown item $($_.LeftItemId)" }
				RightItemId = $_.RightItemId
				RightName = if ($registry.ContainsKey($_.RightItemId)) { $registry[$_.RightItemId].Name } else { "Unknown item $($_.RightItemId)" }
				Occurrences = $_.Occurrences
				TemplateSupport = $_.TemplateIds.Count
			}
		} |
		Where-Object { $_.TemplateSupport -ge 3 } |
		Sort-Object -Property @{ Expression = "TemplateSupport"; Descending = $true }, @{ Expression = "Occurrences"; Descending = $true }, LeftItemId, RightItemId |
		Select-Object -First 50)
}

$categoryOutput = @($categoryCounts.GetEnumerator() |
	Sort-Object -Property @{ Expression = "Value"; Descending = $true }, @{ Expression = "Name"; Ascending = $true } |
	ForEach-Object {
		[pscustomobject] @{ Category = $_.Name; Placements = $_.Value; Share = [Math]::Round($_.Value / $positivePlacementCount, 4) }
	})
$tabOutput = @($tabDominantCategories.Keys | Sort-Object { [int] $_ } | ForEach-Object {
	$tabIndex = [int] $_
	$distribution = @($tabDominantCategories[$_].GetEnumerator() |
		Sort-Object -Property @{ Expression = "Value"; Descending = $true }, @{ Expression = "Name"; Ascending = $true } |
		ForEach-Object { [pscustomobject] @{ Category = $_.Name; Templates = $_.Value } })
	[pscustomobject] @{ TabIndex = $tabIndex; DominantKnownCategoryDistribution = $distribution }
})
$blockOutput = @($blockObservations | Group-Object Category | ForEach-Object {
	$observations = @($_.Group)
	$templateSupport = @($observations.TemplateId | Sort-Object -Unique).Count
	if ($templateSupport -lt 3) { return }
	$widthDistribution = @($observations | Group-Object Width | Sort-Object { [int] $_.Name } |
		ForEach-Object {
			[pscustomobject] @{
				Width = [int] $_.Name
				Blocks = $_.Count
				TemplateSupport = @($_.Group.TemplateId | Sort-Object -Unique).Count
			}
		})
	[pscustomobject] @{
		Category = $_.Name
		Blocks = $observations.Count
		TemplateSupport = $templateSupport
		WidthDistribution = $widthDistribution
		MedianDensity = [Math]::Round((Get-Median $observations.Density), 4)
		LeftAlignedBlocks = @($observations | Where-Object LeftAligned).Count
		MostlyDenseBlocks = @($observations | Where-Object {
			$_.DenseRows -ge [Math]::Ceiling($_.Rows * 0.75)
		}).Count
		MostlySameStartBlocks = @($observations | Where-Object {
			$_.SameStartRows -ge [Math]::Ceiling($_.Rows * 0.75)
		}).Count
		ShortLastRowBlocks = @($observations | Where-Object ShortLastRow).Count
	}
} | Sort-Object Category)
$includedCategories = @($blockOutput.Category)
$includedBlockObservations = @($blockObservations | Where-Object {
	$includedCategories -contains $_.Category
})
$globalBlockWidthOutput = @($includedBlockObservations | Group-Object Width |
	Sort-Object { [int] $_.Name } |
	ForEach-Object {
		[pscustomobject] @{
			Width = [int] $_.Name
			Blocks = $_.Count
			TemplateSupport = @($_.Group.TemplateId | Sort-Object -Unique).Count
		}
	})

$result = [ordered] @{
	SchemaVersion = 2
	Input = "git-ignored normalized local imports"
	TemplateCount = $files.Count
	TabCount = $tabCount
	PositivePlacementCount = $positivePlacementCount
	CatalogCoverage = [ordered] @{
		RegistryMatches = $registryMatchCount
		RegistryShare = [Math]::Round($registryMatchCount / $positivePlacementCount, 4)
		SortMetadataMatches = $metadataMatchCount
		SortMetadataShare = [Math]::Round($metadataMatchCount / $positivePlacementCount, 4)
	}
	CategoryDistribution = $categoryOutput
	SameFamilyAdjacentOccurrences = $sameFamily
	DoseVariantDirection = $doseDirection
	DominantKnownCategoryByTabIndex = $tabOutput
	CategoryBlockSignals = $blockOutput
	GlobalBlockWidthDistribution = $globalBlockWidthOutput
	HorizontalPairsWithAtLeastThreeTemplateSupport = Convert-PairStats $pairStats.Horizontal
	VerticalPairsWithAtLeastThreeTemplateSupport = Convert-PairStats $pairStats.Vertical
}

$outputDirectory = Split-Path -Parent $OutputPath
if ($outputDirectory)
{
	New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($OutputPath, ($result | ConvertTo-Json -Depth 8), $utf8)

Write-Host "Analyzed $($files.Count) templates, $tabCount tabs, and $positivePlacementCount positive placements."
Write-Host "Registry coverage: $registryMatchCount/$positivePlacementCount; sort metadata coverage: $metadataMatchCount/$positivePlacementCount."
Write-Host "Aggregate output: $OutputPath"
