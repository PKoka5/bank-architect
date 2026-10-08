Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$scriptPath = Join-Path $PSScriptRoot "audit-final-item-placements.ps1"
$testDir = Join-Path (Get-Location) "build/tmp/final-placement-audit-tests"
New-Item -ItemType Directory -Force -Path $testDir | Out-Null
$placementPath = Join-Path $testDir "placements.tsv"
$referencePath = Join-Path $testDir "reference.tsv"
$sourcePath = Join-Path $testDir "source.tsv"
$outputDir = Join-Path $testDir "report"
$placementHeader = "itemId`tname`tpreset`tscenario`tcatalogCategory`tcatalogSubcategory`tlayoutTag`tfinalTab`tstatus`terror"
$placementRows = @(
	"1`tCoins`tIRONMAN`tisolated-owned`tCURRENCY`tcurrency`tcurrency`t0`tOK`t",
	"1`tCoins`tMAIN`tisolated-owned`tCURRENCY`tcurrency`tcurrency`t0`tOK`t",
	"2`tUnknown thing`tIRONMAN`tisolated-owned`tCLEANUP`tcleanup`tcleanup`t9`tOK`t",
	"2`tUnknown thing`tMAIN`tisolated-owned`tCLEANUP`tcleanup`tcleanup`t9`tOK`t"
)
$placementHeader += "`teffectiveCategory`teffectiveSubcategory`tmapperContext`tgearStatsContext`titemValueContext`tupgradeContext`tplayerChoicesContext`tbankabilityContext`treviewContext"
$context = "`tbase-catalog-without-gathering`tunavailable`tunavailable`tno-other-owned-items`tpreset-defaults-no-overrides-or-capture`tunverified-cache-record`tunreviewed-observation"
$placementRows = @(
	($placementRows[0] + "`tCURRENCY`tcurrency" + $context),
	($placementRows[1] + "`tCURRENCY`tcurrency" + $context),
	($placementRows[2] + "`tCLEANUP`tcleanup" + $context),
	($placementRows[3] + "`tCLEANUP`tcleanup" + $context)
)
@($placementHeader) + $placementRows | Set-Content -LiteralPath $placementPath -Encoding UTF8
@(
	"itemId`tname`tcatalogCategory`tcatalogSubcategory`tironmanTag`tmainTag`tironmanTab`tmainTab`tsourceUrl`tsourceRevision`treviewedOn`trationale",
	"1`tCoins`tCURRENCY`tcurrency`tcurrency`tcurrency`t0`t0`thttps://oldschool.runescape.wiki/w/Coins`tIndependent test fixture`t2026-10-06`tSpendable money"
) | Set-Content -LiteralPath $referencePath -Encoding UTF8
@(
	"ItemId`tWikiStatus`tWikiPage`tQuestItem`tEquipable`tCombatStatMagnitude`tMinigame`tTutorial`tUnobtainable",
	"1`tVERIFIED_WIKI_ID`tCoins`tFalse`tFalse`t0`tFalse`tFalse`tFalse"
) | Set-Content -LiteralPath $sourcePath -Encoding UTF8
$arguments = @{
	PlacementPath = $placementPath; ReferencePath = $referencePath; SourcePath = $sourcePath
	SourceRevision = "Independent test fixture"; SourceDate = "2026-07-15"
	AsOfDate = "2026-10-06"; OutputDir = $outputDir; FailOnReviewedMismatch = $true
}
function Assert-True([bool] $condition, [string] $message) { if (!$condition) { throw $message } }
function Assert-Fails([scriptblock] $action, [string] $expected)
{
	$didFail = $false
	try { & $action | Out-Null } catch { $didFail = $true; Assert-True ($_.Exception.Message -match $expected) "Unexpected failure: $_" }
	Assert-True $didFail "Expected failure: $expected"
}
& $scriptPath @arguments | Out-Null
$ledgerPath = Join-Path $outputDir "review-ledger.tsv"
$first = [IO.File]::ReadAllText($ledgerPath)
$rows = @(Import-Csv -LiteralPath $ledgerPath -Delimiter "`t")
Assert-True ($rows.Count -eq 4) "Every scenario, including missing source, must remain visible."
Assert-True (@($rows | Where-Object reviewStatus -eq REVIEWED_MATCH).Count -eq 2) "Both presets must use independent reviewed expectations."
Assert-True (@($rows | Where-Object sourceStatus -eq NO_EXACT_SOURCE).Count -eq 2) "Missing source must not disappear."
Assert-True (@($rows | Where-Object sourceFreshness -ne STALE).Count -eq 0) "Old sources must remain visibly stale."
& $scriptPath @arguments | Out-Null
Assert-True ([IO.File]::ReadAllText($ledgerPath) -ceq $first) "Identical inputs must produce identical ledgers."
Assert-Fails { & $scriptPath @arguments -RequireFreshSource } "Fresh source required"
$arguments.SourcePath = Join-Path $testDir "not-present.tsv"
& $scriptPath @arguments | Out-Null
$rows = @(Import-Csv -LiteralPath $ledgerPath -Delimiter "`t")
Assert-True (@($rows | Where-Object sourceStatus -ne MISSING_SOURCE).Count -eq 0) "Missing source must be explicit."
$arguments.SourcePath = $sourcePath
@($placementHeader) + @($placementRows[0] -replace "`tcurrency`t0`tOK", "`tgear`t1`tOK") + $placementRows[1..3] |
	Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Placement review failed"
@($placementHeader) + $placementRows + @($placementRows[0]) | Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Duplicate placement scenario"
@($placementHeader) + $placementRows[1..3] | Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Placement counterpart missing"
@($placementHeader) + $placementRows[0..2] | Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Placement counterpart missing"
@($placementHeader) + @($placementRows | ForEach-Object { $_ -replace "without-gathering`tunavailable", "without-gathering`tinjected-test-source" }) |
	Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Unsupported placement context"
@($placementHeader) + @($placementRows | ForEach-Object { $_ -replace "OK`t`tCURRENCY", "OK`t`tGEAR" }) |
	Set-Content -LiteralPath $placementPath -Encoding UTF8
Assert-Fails { & $scriptPath @arguments } "Placement review failed"
@($placementHeader) + $placementRows | Set-Content -LiteralPath $placementPath -Encoding UTF8
$aliasedSource = Join-Path $outputDir "review-ledger.tsv"
$arguments.SourcePath = $aliasedSource
Assert-Fails { & $scriptPath @arguments } "Output would overwrite an input"
$arguments.SourcePath = $sourcePath
$arguments.AsOfDate = "2026-01-01"
Assert-Fails { & $scriptPath @arguments } "SourceDate is in the future"
Write-Host "Final-placement audit tests passed: complete ledger, independent mismatch guard, missing/stale source, deterministic output, malformed context."
