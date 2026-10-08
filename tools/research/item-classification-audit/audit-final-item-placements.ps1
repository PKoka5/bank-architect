param(
	[string] $PlacementPath = "build/reports/item-classification/final-placements.tsv",
	[string] $ReferencePath = "src/test/resources/com/pkoka5/ironmanbankarchitect/research/reviewed-item-roles.tsv",
	[string] $SourcePath = "tools/research/item-classification-audit/cache/effective-with-source-facts.tsv",
	[string] $SourceRevision = "Wiki bulk snapshot 2026-07-15; historical identity evidence",
	[string] $SourceDate = "2026-07-15",
	[string] $OutputDir = "build/reports/item-classification",
	[string] $AsOfDate = "",
	[int] $MaxSourceAgeDays = 30,
	[switch] $FailOnReviewedMismatch,
	[switch] $RequireFreshSource
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
if ($MaxSourceAgeDays -lt 0) { throw "MaxSourceAgeDays must not be negative." }
$asOf = if ($AsOfDate) { [datetime]::ParseExact($AsOfDate, "yyyy-MM-dd", $null) } else { [datetime]::UtcNow.Date }
$sourceDateValue = [datetime]::ParseExact($SourceDate, "yyyy-MM-dd", $null)
if ($sourceDateValue -gt $asOf) { throw "SourceDate is in the future." }
if (!$SourceRevision.Trim()) { throw "SourceRevision is required." }
foreach ($outputName in @("review-ledger.tsv", "priority-review.tsv", "summary.md"))
{
	$outputPath = [IO.Path]::GetFullPath((Join-Path $OutputDir $outputName))
	foreach ($inputPath in @($PlacementPath, $ReferencePath, $SourcePath))
	{
		if ($outputPath -eq [IO.Path]::GetFullPath($inputPath)) { throw "Output would overwrite an input: $outputPath" }
	}
}

function Read-Table([string] $path, [string[]] $required)
{
	if (!(Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing input: $path" }
	$rows = @(Import-Csv -LiteralPath $path -Delimiter "`t")
	if (!$rows.Count) { throw "Empty input: $path" }
	foreach ($column in $required)
	{
		if ($rows[0].PSObject.Properties.Name -notcontains $column) { throw "Missing column $column in $path" }
	}
	return $rows
}

$placements = @(Read-Table $PlacementPath @("itemId", "name", "preset", "scenario", "catalogCategory", "catalogSubcategory", "effectiveCategory", "effectiveSubcategory", "layoutTag", "finalTab", "mapperContext", "gearStatsContext", "itemValueContext", "upgradeContext", "playerChoicesContext", "bankabilityContext", "reviewContext", "status", "error"))
$references = @(Read-Table $ReferencePath @("itemId", "name", "catalogCategory", "catalogSubcategory", "ironmanTag", "mainTag", "ironmanTab", "mainTab", "sourceUrl", "sourceRevision", "reviewedOn", "rationale"))
$referenceById = @{}
foreach ($reference in $references)
{
	$id = 0
	if (![int]::TryParse($reference.itemId, [ref] $id) -or $id -le 0 -or $referenceById.ContainsKey($id)) { throw "Invalid or duplicate reviewed ID: $($reference.itemId)" }
	foreach ($field in @("name", "catalogCategory", "catalogSubcategory", "ironmanTag", "mainTag", "sourceUrl", "sourceRevision", "reviewedOn", "rationale"))
	{
		if (![string] $reference.$field -or !([string] $reference.$field).Trim()) { throw "Missing reviewed field $field for $id" }
	}
	if ($reference.sourceUrl -notmatch '^https://') { throw "Reviewed source must be an HTTPS URL: $id" }
	$reviewedDate = [datetime]::ParseExact($reference.reviewedOn, "yyyy-MM-dd", $null)
	if ($reviewedDate -gt $asOf) { throw "Review date is in the future: $id" }
	foreach ($field in @("ironmanTab", "mainTab"))
	{
		if ([string] $reference.$field -notmatch '^[0-9]$') { throw "Reviewed tab must be zero-based 0..9: $id/$field" }
	}
	$referenceById[$id] = $reference
}

$sourceById = @{}
$sourcePresent = Test-Path -LiteralPath $SourcePath -PathType Leaf
if ($sourcePresent)
{
	foreach ($source in @(Read-Table $SourcePath @("ItemId", "WikiStatus", "WikiPage", "QuestItem", "Equipable", "CombatStatMagnitude", "Minigame", "Tutorial", "Unobtainable")))
	{
		$id = 0
		if (![int]::TryParse($source.ItemId, [ref] $id) -or $id -le 0 -or $sourceById.ContainsKey($id)) { throw "Invalid or duplicate source ID: $($source.ItemId)" }
		$sourceById[$id] = $source
	}
}
$sourceFreshness = if (!$sourcePresent) { "MISSING" } elseif (($asOf - $sourceDateValue).Days -gt $MaxSourceAgeDays) { "STALE" } else { "WITHIN_AGE_LIMIT_NOT_GAMEPLAY_CERTIFICATION" }
$sourceHash = if ($sourcePresent) { (Get-FileHash -LiteralPath $SourcePath -Algorithm SHA256).Hash.ToLowerInvariant() } else { "MISSING" }
$placementHash = (Get-FileHash -LiteralPath $PlacementPath -Algorithm SHA256).Hash.ToLowerInvariant()
$referenceHash = (Get-FileHash -LiteralPath $ReferencePath -Algorithm SHA256).Hash.ToLowerInvariant()
$expectedContext = @{
	mapperContext = "base-catalog-without-gathering"; gearStatsContext = "unavailable"
	itemValueContext = "unavailable"; upgradeContext = "no-other-owned-items"
	playerChoicesContext = "preset-defaults-no-overrides-or-capture"
	bankabilityContext = "unverified-cache-record"; reviewContext = "unreviewed-observation"
}
$observedKeys = @{}
$observedIds = @{}
$mismatchCount = 0
$failureCount = 0
$ledger = @(
	foreach ($placement in ($placements | Sort-Object @{ Expression = { [int] $_.itemId } }, preset))
	{
		$id = 0
		if (![int]::TryParse($placement.itemId, [ref] $id) -or $id -le 0) { throw "Invalid placement ID: $($placement.itemId)" }
		if ($placement.preset -notin @("IRONMAN", "MAIN") -or $placement.scenario -ne "isolated-owned") { throw "Unsupported placement context for $id" }
		foreach ($field in $expectedContext.Keys)
		{
			if ($placement.$field -cne $expectedContext[$field]) { throw "Unsupported placement context $field for $id" }
		}
		$key = "$id|$($placement.preset)"
		if ($observedKeys.ContainsKey($key)) { throw "Duplicate placement scenario: $key" }
		$observedKeys[$key] = $true
		$observedIds[$id] = $true
		$reference = $referenceById[$id]
		$source = $sourceById[$id]
		$sourceStatus = if (!$sourcePresent) { "MISSING_SOURCE" } elseif ($null -eq $source -or $source.WikiStatus -ne "VERIFIED_WIKI_ID") { "NO_EXACT_SOURCE" } else { "EXACT_CACHED_IDENTITY" }
		$reasons = New-Object System.Collections.Generic.List[string]
		$reviewStatus = "UNREVIEWED"
		if ($placement.status -ne "OK")
		{
			$reviewStatus = "PIPELINE_FAILURE"
			$reasons.Add($placement.error)
			$failureCount++
		}
		elseif ($null -ne $reference)
		{
			$tag = if ($placement.preset -eq "IRONMAN") { $reference.ironmanTag } else { $reference.mainTag }
			$tab = if ($placement.preset -eq "IRONMAN") { $reference.ironmanTab } else { $reference.mainTab }
			foreach ($field in @("name", "catalogCategory", "catalogSubcategory"))
			{
				if ($placement.$field -cne $reference.$field) { $reasons.Add("${field}: $($placement.$field) -> expected $($reference.$field)") }
			}
			if ($placement.effectiveCategory -cne $reference.catalogCategory) { $reasons.Add("effectiveCategory: $($placement.effectiveCategory) -> expected $($reference.catalogCategory)") }
			if ($placement.effectiveSubcategory -cne $reference.catalogSubcategory) { $reasons.Add("effectiveSubcategory: $($placement.effectiveSubcategory) -> expected $($reference.catalogSubcategory)") }
			if ($placement.layoutTag -cne $tag) { $reasons.Add("tag: $($placement.layoutTag) -> expected $tag") }
			if ($placement.finalTab -ne $tab) { $reasons.Add("tab: $($placement.finalTab) -> expected $tab") }
			$reviewStatus = if ($reasons.Count) { $mismatchCount++; "REVIEWED_MISMATCH" } else { "REVIEWED_MATCH" }
		}
		if ($null -ne $source -and $sourceStatus -eq "EXACT_CACHED_IDENTITY")
		{
			if ($source.Minigame -eq "True" -or $source.Tutorial -eq "True" -or $source.Unobtainable -eq "True") { $reasons.Add("restricted-or-legacy-source-review") }
			if ($source.QuestItem -eq "True") { $reasons.Add("quest-lifecycle-not-proven-disposable") }
			if ($placement.layoutTag -eq "gear" -and $source.Equipable -eq "False") { $reasons.Add("non-equipable-gear-check-function-cannon-ammo-may-be-valid") }
		}
		[pscustomobject] [ordered] @{
			itemId = $id; name = $placement.name; preset = $placement.preset
			catalogCategory = $placement.catalogCategory; catalogSubcategory = $placement.catalogSubcategory
			effectiveCategory = $placement.effectiveCategory; effectiveSubcategory = $placement.effectiveSubcategory
			layoutTag = $placement.layoutTag; finalTab = $placement.finalTab
			reviewStatus = $reviewStatus; sourceStatus = $sourceStatus; sourceFreshness = $sourceFreshness
			bankability = "UNVERIFIED"; context = "isolated-owned-no-runtime-stats-prices-upgrades-player-choices"
			reviewSource = if ($null -ne $reference) { $reference.sourceUrl } else { "" }
			reviewRevision = if ($null -ne $reference) { $reference.sourceRevision } else { "" }
			reviewedOn = if ($null -ne $reference) { $reference.reviewedOn } else { "" }
			cachedWikiPage = if ($null -ne $source) { $source.WikiPage } else { "" }
			reasons = $reasons -join "; "
		}
	}
)
foreach ($id in $observedIds.Keys)
{
	foreach ($preset in @("IRONMAN", "MAIN"))
	{
		if (!$observedKeys.ContainsKey("$id|$preset")) { throw "Placement counterpart missing from export: $id/$preset" }
	}
}
foreach ($id in $referenceById.Keys)
{
	foreach ($preset in @("IRONMAN", "MAIN"))
	{
		if (!$observedKeys.ContainsKey("$id|$preset")) { throw "Reviewed scenario missing from export: $id/$preset" }
	}
}
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$ledger | Export-Csv -LiteralPath (Join-Path $OutputDir "review-ledger.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$ledger | Where-Object { $_.reviewStatus -eq "PIPELINE_FAILURE" -or $_.reviewStatus -eq "REVIEWED_MISMATCH" -or ($_.reviewStatus -eq "UNREVIEWED" -and $_.reasons) } |
	Export-Csv -LiteralPath (Join-Path $OutputDir "priority-review.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$summary = New-Object System.Collections.Generic.List[string]
$summary.Add("# Final item placement audit")
$summary.Add("")
$summary.Add("As of $($asOf.ToString('yyyy-MM-dd')); $($ledger.Count) isolated preset scenarios. Registry/cache records are not a bankable-item count.")
$summary.Add("Source: $SourceRevision ($SourceDate), freshness: $sourceFreshness. Exact identity is not a verified functional role.")
$summary.Add("Input SHA-256: source=$sourceHash; placements=$placementHash; reference=$referenceHash. Source date/revision are supplied provenance, not a verified fetch manifest.")
$summary.Add("Reviewed independent reference: $($references.Count) item IDs. Mismatches: $mismatchCount; pipeline failures: $failureCount.")
$summary.Add("")
foreach ($group in ($ledger | Group-Object reviewStatus, sourceStatus | Sort-Object Name)) { $summary.Add("- $($group.Name): $($group.Count)") }
$summary.Add("")
$summary.Add("Every unreviewed record stays in review-ledger.tsv, including missing facts. priority-review.tsv contains contradictions and review signals, never automatic corrections. Context-dependent Alch, mixed banks and saved player layouts require separate regression tests.")
$summary | Set-Content -LiteralPath (Join-Path $OutputDir "summary.md") -Encoding UTF8
Write-Host ($summary -join "`n")
if ($failureCount -gt 0 -or ($FailOnReviewedMismatch -and $mismatchCount -gt 0)) { throw "Placement review failed; inspect the ledger." }
if ($RequireFreshSource -and $sourceFreshness -ne "WITHIN_AGE_LIMIT_NOT_GAMEPLAY_CERTIFICATION") { throw "Fresh source required; source is $sourceFreshness." }
