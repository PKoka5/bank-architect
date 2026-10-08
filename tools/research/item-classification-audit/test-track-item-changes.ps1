Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$scriptPath = Join-Path $PSScriptRoot 'track-item-changes.ps1'
$repositoryRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
$testBuildRoot = Join-Path $repositoryRoot 'build'
$testRoot = Join-Path $testBuildRoot ('item-change-tests-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testRoot -Force | Out-Null

function Assert-True([bool] $condition, [string] $message) { if (-not $condition) { throw $message } }
function Write-Registry([string] $path, [string[]] $rows) { $rows | Set-Content -LiteralPath $path -Encoding UTF8 }
function Write-Facts([string] $path, [object[]] $rows) { $rows | Export-Csv -LiteralPath $path -Delimiter "`t" -NoTypeInformation -Encoding UTF8 }
function Fact([int] $id, [string] $equipable, [string] $examine) {
	return [pscustomobject][ordered]@{ ItemId = $id; WikiStatus = 'VERIFIED_WIKI_ID'; WikiPage = "Item $id"; Equipable = $equipable; Examine = $examine }
}
function Assert-Throws([scriptblock] $operation, [string] $label) {
	$failed = $false
	try { & $operation | Out-Null } catch { $failed = $true }
	Assert-True $failed "Expected validation failure: $label"
}

try {
	$registry = Join-Path $testRoot 'registry.tsv'
	$facts = Join-Path $testRoot 'facts.tsv'
	$baseline = Join-Path $testRoot 'baseline.json'
	$review = Join-Path $testRoot 'review.tsv'
	$report = Join-Path $testRoot 'report.md'
	$argsBase = @{
		RegistryPath = $registry; RegistrySource = 'original test registry'; RegistryRevision = 'fixture-a'; RegistryDate = '2026-10-06'
		SourcePath = $facts; SourceIdentifier = 'independent test facts'; SourceRevision = 'fixture-facts-a'; SourceDate = '2026-10-06'
		SourceCompleteness = 'Complete'; SnapshotPath = $baseline; ReviewPath = $review; ReportPath = $report; AsOfDate = '2026-10-06'
	}
	Write-Registry $registry @("3`tDummy`tUNKNOWN`tDUMMY_SLOT", "2`tOld item`tUNKNOWN`tOLD_ITEM", "1`tTool`tTOOL`tTOOL")
	Write-Facts $facts @((Fact 2 'False' 'Previous function.'), (Fact 1 'False' 'A tool.'))
	& $scriptPath @argsBase | Out-Null
	Assert-True (-not (Test-Path -LiteralPath $baseline)) 'Comparison created an implicit baseline.'
	& $scriptPath @argsBase -WriteSnapshot | Out-Null
	$initialHash = (Get-FileHash -LiteralPath $baseline).Hash
	$initial = Get-Content -LiteralPath $baseline -Raw | ConvertFrom-Json
	Assert-True (($initial.records.itemId -join ',') -eq '1,2,3') 'Snapshot ID order must be deterministic.'
	$cacheChanges = @(Import-Csv -LiteralPath $review -Delimiter "`t" | Where-Object { $_.itemId -eq '3' })
	Assert-True (@($cacheChanges | Where-Object { $_.actionability -ne 'CACHE_CONTEXT_ONLY' }).Count -eq 0) 'Cache records must retain changes with explicit cache actionability.'
	& $scriptPath @argsBase -WriteSnapshot | Out-Null
	Assert-True ((Get-FileHash -LiteralPath $baseline).Hash -eq $initialHash) 'Identical inputs produced a different snapshot.'

	Write-Registry $registry @("4`tNew item`tUNKNOWN`tNEW_ITEM", "2`tRenamed item`tGEAR`tNEW_CONSTANT", "1`tTool`tTOOL`tTOOL")
	Write-Facts $facts @((Fact 1 'False' 'A tool.'), (Fact 2 'True' 'New function.'), (Fact 4 'False' 'New item.'))
	$comparison = $argsBase.Clone()
	$comparison.PreviousSnapshot = $baseline
	& $scriptPath @comparison | Out-Null
	$changes = @(Import-Csv -LiteralPath $review -Delimiter "`t")
	foreach ($expected in @('4:NEW_ID', '3:REMOVED_ID', '2:NAME_CHANGED', '2:REGISTRY_CLASSIFICATION_CHANGED', '2:CONSTANT_CHANGED', '2:SOURCE_FACTS_CHANGED')) {
		Assert-True ($expected -in @($changes | ForEach-Object { $_.itemId + ':' + $_.changeType })) "Missing change $expected."
	}
	$changedFacts = @($changes | Where-Object { $_.itemId -eq '2' -and $_.changeType -eq 'SOURCE_FACTS_CHANGED' })
	Assert-True ($changedFacts[0].details -match 'Equipable: False -> True' -and $changedFacts[0].details -match 'Examine: Previous function. -> New function.') 'Existing-ID function evidence must include changed fields.'
	Assert-True ((Get-FileHash -LiteralPath $baseline).Hash -eq $initialHash) 'Comparison modified the previous snapshot.'
	Assert-True (($changes.itemId -join ',') -eq '2,2,2,2,3,4') 'Review rows are not ordered by ID and change type.'

	# Missing records and blank cells are incomplete evidence, not function removal.
	Write-Facts $facts @((Fact 1 '' ''))
	$partialSnapshot = Join-Path $testRoot 'partial.json'
	$comparison.SnapshotPath = $partialSnapshot
	$comparison.SourceCompleteness = 'Partial'
	$comparison.SourceDate = '2026-07-15'
	& $scriptPath @comparison -WriteSnapshot | Out-Null
	$partial = Get-Content -LiteralPath $partialSnapshot -Raw | ConvertFrom-Json
	$tool = @($partial.records | Where-Object { $_.itemId -eq 1 })[0]
	$renamed = @($partial.records | Where-Object { $_.itemId -eq 2 })[0]
	Assert-True ($tool.facts.Equipable.value -eq 'False' -and $tool.facts.Examine.value -eq 'A tool.') 'Blank current cells erased prior facts.'
	$originalTool = @($initial.records | Where-Object { $_.itemId -eq 1 })[0]
	Assert-True ($tool.facts.Examine.provenance -eq $originalTool.facts.Examine.provenance) 'Retained facts lost their original source fingerprint.'
	Assert-True ($renamed.facts.Examine.value -eq 'Previous function.') 'Missing source records erased prior facts.'
	Assert-True ('SOURCE_STALE' -in $partial.warnings -and 'SOURCE_COMPLETENESS_PARTIAL' -in $partial.warnings -and 'PREVIOUS_FACTS_RETAINED' -in $partial.warnings) 'Partial and stale source warnings were concealed.'
	$changes = @(Import-Csv -LiteralPath $review -Delimiter "`t")
	Assert-True (@($changes | Where-Object { $_.changeType -eq 'SOURCE_FACTS_CHANGED' }).Count -eq 0) 'Missing facts were presented as function changes.'
	Assert-True (@($changes | Where-Object { $_.changeType -eq 'SOURCE_FACTS_UNAVAILABLE' -and $_.itemId -eq '2' }).Count -eq 1) 'Missing facts must remain review items.'

	$missing = $comparison.Clone()
	foreach ($key in @('SourcePath', 'SourceIdentifier', 'SourceRevision', 'SourceDate')) { $missing.Remove($key) }
	$missing.SnapshotPath = Join-Path $testRoot 'missing.json'
	& $scriptPath @missing -WriteSnapshot | Out-Null
	$missingData = Get-Content -LiteralPath $missing.SnapshotPath -Raw | ConvertFrom-Json
	Assert-True ('SOURCE_NOT_SUPPLIED' -in $missingData.warnings) 'Missing-source warning was concealed.'
	Assert-True (@($missingData.records | Where-Object { $_.itemId -eq 1 })[0].facts.Examine.value -eq 'A tool.') 'Absent source file erased prior facts.'
	$missing.AsOfDate = '2026-12-06'
	& $scriptPath @missing -WriteSnapshot | Out-Null
	$missingData = Get-Content -LiteralPath $missing.SnapshotPath -Raw | ConvertFrom-Json
	Assert-True ('RETAINED_FACTS_STALE' -in $missingData.warnings -and $missingData.source.retainedStaleFactCount -gt 0) 'Retained old evidence must still produce a stale warning.'

	# Reduced schemas and unverified identity matches also retain previous facts.
	Write-Facts $facts @([pscustomobject]@{ ItemId = 1; WikiStatus = 'VERIFIED_WIKI_ID'; WikiPage = 'Item 1' })
	& $scriptPath @comparison -WriteSnapshot | Out-Null
	$reduced = Get-Content -LiteralPath $partialSnapshot -Raw | ConvertFrom-Json
	Assert-True ('SOURCE_SCHEMA_INCOMPLETE' -in $reduced.warnings) 'A reduced fact schema was concealed.'
	Write-Facts $facts @([pscustomobject]@{ ItemId = 1; WikiStatus = 'UNVERIFIED'; Equipable = 'True'; Examine = 'Wrong namesake' })
	& $scriptPath @comparison -WriteSnapshot | Out-Null
	$unverified = Get-Content -LiteralPath $partialSnapshot -Raw | ConvertFrom-Json
	Assert-True (@($unverified.records | Where-Object { $_.itemId -eq 1 })[0].facts.Examine.value -eq 'A tool.') 'Unverified namesake facts replaced exact-ID facts.'

	Write-Registry $registry @("1`tTool`tTOOL`tTOOL", "1`tDuplicate`tTOOL`tDUPLICATE")
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'duplicate registry ID'
	Write-Registry $registry @("x`tBad`tUNKNOWN`tBAD")
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'malformed registry ID'
	Write-Registry $registry @("1`tMissing column`tTOOL")
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'malformed registry columns'
	Write-Registry $registry @("1`tTool`tTOOL`tTOOL")
	Write-Facts $facts @((Fact 1 'False' 'A'), (Fact 1 'True' 'B'))
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'duplicate source ID'
	Write-Facts $facts @([pscustomobject]@{ ItemId = 'x'; Examine = 'Malformed' })
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'malformed source ID'
	Write-Facts $facts @((Fact 1 'False' 'A'))
	$badProvenance = $argsBase.Clone()
	$badProvenance.SourceRevision = ''
	Assert-Throws { & $scriptPath @badProvenance -WriteSnapshot } 'missing source revision'
	$badDate = $argsBase.Clone()
	$badDate.RegistryDate = '2026-10-07'
	Assert-Throws { & $scriptPath @badDate -WriteSnapshot } 'future registry date'
	$badDate = $argsBase.Clone()
	$badDate.SourceDate = '2026-10-07'
	Assert-Throws { & $scriptPath @badDate -WriteSnapshot } 'future source date'
	$staleRegistry = $argsBase.Clone()
	$staleRegistry.RegistryDate = '2026-07-15'
	$staleRegistry.SnapshotPath = Join-Path $testRoot 'stale-registry.json'
	& $scriptPath @staleRegistry -WriteSnapshot | Out-Null
	$staleData = Get-Content -LiteralPath $staleRegistry.SnapshotPath -Raw | ConvertFrom-Json
	Assert-True ('REGISTRY_STALE' -in $staleData.warnings) 'Stale registry was presented as current.'

	# Final-placement exporter metadata is an observation of the classifier, never independent evidence.
	$observedFields = @('preset', 'scenario', 'catalogCategory', 'catalogSubcategory', 'mappedCategoryKey',
		'effectiveCategory', 'effectiveSubcategory', 'layoutTag', 'finalTab', 'usageTags', 'registryName', 'finalTabName',
		'mapperContext', 'gearStatsContext', 'itemValueContext', 'upgradeContext', 'playerChoicesContext',
		'bankabilityContext', 'reviewContext', 'status', 'error')
	$merged = Fact 1 'False' 'Independent description.'
	foreach ($field in $observedFields) { $merged | Add-Member -NotePropertyName $field -NotePropertyValue 'CLASSIFIER_OBSERVATION' }
	Write-Facts $facts @($merged)
	$mergedArgs = $argsBase.Clone()
	$mergedArgs.SnapshotPath = Join-Path $testRoot 'merged-facts.json'
	& $scriptPath @mergedArgs -WriteSnapshot | Out-Null
	$mergedData = Get-Content -LiteralPath $mergedArgs.SnapshotPath -Raw | ConvertFrom-Json
	Assert-True (@($mergedData.source.fields | Where-Object { $_ -in $observedFields }).Count -eq 0) 'Final-placement observations were persisted as source facts.'
	$mergedRecord = @($mergedData.records | Where-Object { $_.itemId -eq 1 })[0]
	Assert-True ($mergedRecord.facts.Examine.value -eq 'Independent description.' -and @($mergedRecord.facts.PSObject.Properties.Name | Where-Object { $_ -in $observedFields }).Count -eq 0) 'Merged facts did not preserve the evidence boundary.'
	$derivedOnly = [pscustomobject][ordered]@{ ItemId = 1; Name = 'Tool'; ItemCategory = 'TOOL'; Subcategory = 'tool' }
	foreach ($field in $observedFields) { $derivedOnly | Add-Member -NotePropertyName $field -NotePropertyValue 'CLASSIFIER_OBSERVATION' }
	Write-Facts $facts @($derivedOnly)
	Assert-Throws { & $scriptPath @argsBase -WriteSnapshot } 'derived-only classification source'
	Write-Facts $facts @((Fact 1 'False' 'A'))
	Assert-True ((Get-FileHash -LiteralPath $baseline).Hash -eq $initialHash) 'Validation failures modified the baseline.'

	# A new local RuneLite source index can reveal new IDs without changing production data.
	$index = Join-Path $testRoot 'research-index.tsv'
	$indexHeader = "item_id`tnamespace`tconstant_name`tdisplay_name`tinferred_label`tcategory`tconfidence`tflags`ttags"
	$indexRows = @($indexHeader,
		"0`tTOP_LEVEL`tNULL_SLOT`tNull`tNull Slot`tUNKNOWN`tLOW`texclude-main-catalog`t",
		"7`tPLACEHOLDER`tNEW_PLACEHOLDER`tNew placeholder`tNew Placeholder`tGEAR`tLOW`texclude-main-catalog`t",
		"6`tCERT`tNEW_NOTE`tNew note`tNew Note`tGEAR`tLOW`texclude-main-catalog`t",
		"5`tTOP_LEVEL`tNEW_REWARD`t`tNew Reward`tUNKNOWN`tLOW`tmissing-direct-javadoc`t",
		"1`tTOP_LEVEL`tTOOL`tTool`tTool`tTOOL`tLOW`t`t")
	$indexRows | Set-Content -LiteralPath $index -Encoding UTF8
	$indexArgs = $comparison.Clone()
	$indexArgs.Remove('RegistryPath')
	$indexArgs.ResearchIndexPath = $index
	$indexArgs.SnapshotPath = Join-Path $testRoot 'index.json'
	& $scriptPath @indexArgs -WriteSnapshot | Out-Null
	$indexSnapshot = Get-Content -LiteralPath $indexArgs.SnapshotPath -Raw | ConvertFrom-Json
	Assert-True (($indexSnapshot.records.itemId -join ',') -eq '1,5,6,7' -and $indexSnapshot.registry.excludedNonPositiveRecords -eq 1) 'Research index ID selection is incorrect.'
	$newReward = @($indexSnapshot.records | Where-Object { $_.itemId -eq 5 })[0]
	Assert-True ($newReward.nameOrigin -eq 'INFERRED_CONSTANT_LABEL' -and $newReward.classification -eq 'UNKNOWN') 'Missing direct names were presented as verified names.'
	$indexChanges = @(Import-Csv -LiteralPath $review -Delimiter "`t")
	Assert-True (@($indexChanges | Where-Object { $_.itemId -eq '5' -and $_.changeType -eq 'NEW_ID' -and $_.actionability -eq 'REVIEW_REQUIRED' }).Count -eq 1) 'New RuneLite TOP_LEVEL ID was not detected.'
	Assert-True (@($indexChanges | Where-Object { $_.itemId -in @('6', '7') -and $_.actionability -ne 'CACHE_CONTEXT_ONLY' }).Count -eq 0) 'CERT/PLACEHOLDER index records must remain explicit cache context.'
	$conflictingInputs = $indexArgs.Clone()
	$conflictingInputs.RegistryPath = $registry
	Assert-Throws { & $scriptPath @conflictingInputs } 'conflicting registry and research index'
	@($indexHeader, $indexRows[5], $indexRows[5]) | Set-Content -LiteralPath $index -Encoding UTF8
	Assert-Throws { & $scriptPath @indexArgs } 'duplicate research index ID'
	Write-Output 'PASS: item-change tracking, provenance, partial evidence, deterministic outputs, and explicit baseline writes.'
} finally {
	# Delete only the verified test directory inside this repository's build directory.
	$resolved = [IO.Path]::GetFullPath($testRoot)
	$buildRoot = [IO.Path]::GetFullPath($testBuildRoot) + [IO.Path]::DirectorySeparatorChar
	if (-not $resolved.StartsWith($buildRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe test cleanup path.' }
	if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}
