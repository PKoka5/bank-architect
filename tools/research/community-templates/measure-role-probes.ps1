param(
	[string] $InputDir = "tools/research/community-templates/cache/local-imports",
	[string] $ProbePath = "tools/research/community-templates/main-role-probes.json",
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $RepoIds = "8,14,26,55",
	[string] $OutputPath = "tools/research/community-templates/cache/main-2026-10-05/role-probe-analysis.json"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($RepoIds)) { throw "Select an explicit complete cohort." }
. (Join-Path $PSScriptRoot "select-cohort.ps1")
$files = @(Get-SelectedCommunityTemplateFiles $InputDir $RepoIds)
$spec = Get-Content -Raw -LiteralPath $ProbePath | ConvertFrom-Json
if ($spec.SchemaVersion -ne 1 -or !$spec.Cases.Count) { throw "Unsupported or empty probe specification." }

$registry = @{}
Get-Content -LiteralPath $RegistryPath |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Name, Category, ConstantName |
	ForEach-Object { $registry[[int] $_.ItemId] = [string] $_.Name }
$caseIds = @{}
foreach ($case in $spec.Cases)
{
	if (!$case.Id -or $caseIds.ContainsKey([string] $case.Id)) { throw "Missing or repeated probe ID." }
	$caseIds[[string] $case.Id] = $true
	foreach ($side in @("LeftItemIds", "RightItemIds"))
	{
		$ids = @($case.$side)
		if (!$ids.Count -or @($ids | Sort-Object -Unique).Count -ne $ids.Count) { throw "Empty or repeated probe members: $($case.Id)." }
		foreach ($id in $ids)
		{
			if ([string] $id -notmatch '^[1-9][0-9]*$' -or !$registry.ContainsKey([int] $id)) { throw "Unknown probe item: $id." }
		}
	}
	if (@($case.LeftItemIds | Where-Object { $_ -in $case.RightItemIds }).Count) { throw "Probe sides overlap: $($case.Id)." }
}

$templates = @($files | ForEach-Object {
	$template = Get-Content -Raw -LiteralPath $_.FullName | ConvertFrom-Json
	if ($_.Name -ne "$($template.TemplateId).normalized.json" -or !$template.LayoutSha256) { throw "Invalid selected input: $($_.Name)." }
	$byItem = @{}
	foreach ($placement in $template.Placements)
	{
		if ($placement.State -ne "item") { continue }
		$id = [int] $placement.ItemId
		if (!$byItem.ContainsKey($id)) { $byItem[$id] = @() }
		$byItem[$id] += [int] $placement.TabIndex
	}
	[pscustomobject] @{ Id = $template.TemplateId; LayoutHash = $template.LayoutSha256; ByItem = $byItem }
})
$selectedCount = $templates.Count
$templates = @($templates | Group-Object LayoutHash | ForEach-Object { $_.Group | Sort-Object Id | Select-Object -First 1 })

$results = @($spec.Cases | ForEach-Object {
	$case = $_
	$coPresent = 0; $ambiguous = 0; $eligible = 0; $anyShared = 0; $allTogether = 0
	foreach ($template in $templates)
	{
		$left = @($case.LeftItemIds | Where-Object { $template.ByItem.ContainsKey([int] $_) })
		$right = @($case.RightItemIds | Where-Object { $template.ByItem.ContainsKey([int] $_) })
		if (!$left.Count -or !$right.Count) { continue }
		$coPresent++
		$observed = @($left) + @($right)
		if (@($observed | Where-Object { $template.ByItem[[int] $_].Count -ne 1 }).Count)
		{
			$ambiguous++
			continue
		}
		$eligible++
		$leftTabs = @($left | ForEach-Object { $template.ByItem[[int] $_][0] } | Sort-Object -Unique)
		$rightTabs = @($right | ForEach-Object { $template.ByItem[[int] $_][0] } | Sort-Object -Unique)
		if (@($leftTabs | Where-Object { $_ -in $rightTabs }).Count) { $anyShared++ }
		if (@((@($leftTabs) + @($rightTabs)) | Sort-Object -Unique).Count -eq 1) { $allTogether++ }
	}
	if ($coPresent -ne $eligible + $ambiguous -or $allTogether -gt $anyShared -or $anyShared -gt $eligible) { throw "Invalid probe counts: $($case.Id)." }
	[pscustomobject] @{
		Id = $case.Id
		Label = $case.Label
		LeftItems = @($case.LeftItemIds | ForEach-Object { [pscustomobject] @{ ItemId = $_; Name = $registry[[int] $_] } })
		RightItems = @($case.RightItemIds | ForEach-Object { [pscustomobject] @{ ItemId = $_; Name = $registry[[int] $_] } })
		CoPresentTemplates = $coPresent
		DuplicateAmbiguousTemplates = $ambiguous
		EligibleTemplates = $eligible
		AnySharedTabTemplates = $anyShared
		AllObservedProbeMembersTogetherTemplates = $allTogether
	}
})

$result = [ordered] @{
	SchemaVersion = 1
	Method = "Selected exact-ID probes; both sides present; repeated relevant IDs excluded; identical layout hashes collapsed."
	SelectedRepoIds = $RepoIds
	SelectedTemplateCount = $selectedCount
	DistinctLayoutTemplateCount = $templates.Count
	SourceLayouts = @($templates | Sort-Object Id | ForEach-Object { [pscustomobject] @{ TemplateId = $_.Id; LayoutSha256 = $_.LayoutHash } })
	ProbeSha256 = (Get-FileHash -LiteralPath $ProbePath -Algorithm SHA256).Hash
	RegistrySha256 = (Get-FileHash -LiteralPath $RegistryPath -Algorithm SHA256).Hash
	AnalyzerSha256 = (Get-FileHash -LiteralPath $PSCommandPath -Algorithm SHA256).Hash
	Cases = $results
}
$directory = Split-Path -Parent $OutputPath
if ($directory) { New-Item -ItemType Directory -Force -Path $directory | Out-Null }
[System.IO.File]::WriteAllText($OutputPath, ($result | ConvertTo-Json -Depth 8), (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Measured $($results.Count) role probes across $($templates.Count) distinct complete layouts."
Write-Host "Output: $OutputPath"
