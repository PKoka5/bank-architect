param(
	[string] $RegistryPath = 'src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv',
	[string] $ResearchIndexPath,
	[Parameter(Mandatory = $true)][string] $RegistrySource,
	[Parameter(Mandatory = $true)][string] $RegistryRevision,
	[Parameter(Mandatory = $true)][string] $RegistryDate,
	[string] $SourcePath,
	[string] $SourceIdentifier,
	[string] $SourceRevision,
	[string] $SourceDate,
	[ValidateSet('Unknown', 'Partial', 'Complete')][string] $SourceCompleteness = 'Unknown',
	[string] $PreviousSnapshot,
	[string] $SnapshotPath = 'build/item-classification-snapshot.json',
	[string] $ReviewPath = 'build/item-changes-review.tsv',
	[string] $ReportPath = 'build/item-changes-report.md',
	[string] $AsOfDate = (Get-Date -Format 'yyyy-MM-dd'),
	[ValidateRange(0, 36500)][int] $MaxSourceAgeDays = 30,
	[switch] $WriteSnapshot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Read-Date([string] $value, [string] $label) {
	$result = [datetime]::MinValue
	if (-not [datetime]::TryParseExact($value, 'yyyy-MM-dd', [cultureinfo]::InvariantCulture,
		[globalization.DateTimeStyles]::None, [ref] $result)) { throw "$label must be yyyy-MM-dd." }
	return $result
}
function Get-Hash([string] $value) {
	$sha = [security.cryptography.SHA256]::Create()
	try { return ([bitconverter]::ToString($sha.ComputeHash([text.encoding]::UTF8.GetBytes($value)))).Replace('-', '').ToLowerInvariant() }
	finally { $sha.Dispose() }
}
function Ensure-Parent([string] $path) {
	$parent = Split-Path -Parent ([IO.Path]::GetFullPath($path))
	New-Item -ItemType Directory -Force -Path $parent | Out-Null
}
function Get-RecordKind([string] $name, [string] $constant, [string] $namespace) {
	if ($namespace -in @('CERT', 'PLACEHOLDER') -or $constant -match '(^|_)(INTERFACE|PLACEHOLDER|DUMMY|NULL)(_|$)' -or $name -match '^null$') {
		return 'EXPLICIT_CACHE_RECORD'
	}
	return 'ITEM_BANKABILITY_UNVERIFIED'
}
function Add-Change([int] $id, [string] $name, [string] $kind, [string] $type, [string] $details) {
	$changes.Add([pscustomobject][ordered]@{
		itemId = $id; name = $name; changeType = $type; recordKind = $kind
		actionability = $(if ($kind -eq 'EXPLICIT_CACHE_RECORD') { 'CACHE_CONTEXT_ONLY' } else { 'REVIEW_REQUIRED' })
		details = $details
	})
}

$asOf = Read-Date $AsOfDate 'AsOfDate'
$registryDay = Read-Date $RegistryDate 'RegistryDate'
if ($registryDay -gt $asOf) { throw 'RegistryDate must not be after AsOfDate.' }
if ($ResearchIndexPath -and $PSBoundParameters.ContainsKey('RegistryPath')) { throw 'Supply RegistryPath or ResearchIndexPath, not both.' }
$registryInputPath = $(if ($ResearchIndexPath) { $ResearchIndexPath } else { $RegistryPath })
$registryFormat = $(if ($ResearchIndexPath) { 'RUNELITE_RESEARCH_INDEX' } else { 'FOUR_COLUMN_REGISTRY' })
foreach ($value in @($RegistrySource, $RegistryRevision)) {
	if ([string]::IsNullOrWhiteSpace($value) -or $value -match '[\r\n]') { throw 'Registry provenance must be nonempty and single-line.' }
}
if ($SourcePath) {
	foreach ($value in @($SourceIdentifier, $SourceRevision)) {
		if ([string]::IsNullOrWhiteSpace($value) -or $value -match '[\r\n]') { throw 'SourceIdentifier and SourceRevision are required with SourcePath.' }
	}
	$sourceDay = Read-Date $SourceDate 'SourceDate'
	if ($sourceDay -gt $asOf) { throw 'SourceDate must not be after AsOfDate.' }
} elseif ($SourceIdentifier -or $SourceRevision -or $SourceDate) { throw 'Source provenance requires SourcePath.' }

# Validate all inputs before writing outputs. Comparing never modifies its baseline.
$inputPaths = @($registryInputPath, $SourcePath, $PreviousSnapshot) | Where-Object { $_ }
$outputPaths = @($ReviewPath, $ReportPath)
if ($WriteSnapshot) { $outputPaths += $SnapshotPath }
for ($i = 0; $i -lt $outputPaths.Count; $i++) {
	$target = [IO.Path]::GetFullPath($outputPaths[$i])
	foreach ($inputPath in $inputPaths) {
		if ($target -eq [IO.Path]::GetFullPath($inputPath) -and
			-not ($WriteSnapshot -and $outputPaths[$i] -eq $SnapshotPath -and $inputPath -eq $PreviousSnapshot)) {
			throw "Output would overwrite an input: $target"
		}
	}
	for ($j = 0; $j -lt $i; $j++) {
		if ($target -eq [IO.Path]::GetFullPath($outputPaths[$j])) { throw 'Output paths must be distinct.' }
	}
}

$registry = @{}
$lineNumber = 0
$excludedNonPositive = 0
$indexSeen = @{}
foreach ($line in (Get-Content -LiteralPath $registryInputPath -Encoding UTF8)) {
	$lineNumber++
	$clean = $line.TrimStart([char]0xfeff)
	if (-not $clean.Trim() -or $clean.StartsWith('#')) { continue }
	$cells = $clean.Split([char]9)
	$id = 0
	$namespace = 'TOP_LEVEL'
	$nameOrigin = 'SUPPLIED_REGISTRY_NAME'
	if ($ResearchIndexPath) {
		if ($lineNumber -eq 1) {
			if (($cells -join "`t") -ne "item_id`tnamespace`tconstant_name`tdisplay_name`tinferred_label`tcategory`tconfidence`tflags`ttags") { throw 'Unsupported research index header.' }
			continue
		}
		if ($cells.Count -ne 9 -or -not [int]::TryParse($cells[0], [ref]$id) -or
			$cells[1] -notin @('TOP_LEVEL', 'CERT', 'PLACEHOLDER') -or [string]::IsNullOrWhiteSpace($cells[2]) -or
			[string]::IsNullOrWhiteSpace($cells[5])) { throw "Malformed research index row $lineNumber." }
		if ($indexSeen.ContainsKey($id)) { throw "Duplicate research index ID $id." }
		$indexSeen[$id] = $true
		if ($id -lt 1) { $excludedNonPositive++; continue }
		$namespace = $cells[1]
		$name = $cells[3]
		$nameOrigin = 'DIRECT_JAVADOC_NAME'
		if ([string]::IsNullOrWhiteSpace($name)) { $name = $cells[4]; $nameOrigin = 'INFERRED_CONSTANT_LABEL' }
		$category = $(if ($namespace -eq 'TOP_LEVEL') { $cells[5] } else { 'CACHE_CONTEXT' })
		$cells = @([string]$id, $name, $category, $cells[2])
	}
	if ($cells.Count -ne 4 -or -not [int]::TryParse($cells[0], [ref] $id) -or $id -lt 1 -or
		[string]::IsNullOrWhiteSpace($cells[1]) -or [string]::IsNullOrWhiteSpace($cells[2]) -or
		[string]::IsNullOrWhiteSpace($cells[3])) { throw "Malformed registry row $lineNumber; require positive ID and four nonempty columns." }
	if ($registry.ContainsKey($id)) { throw "Duplicate registry ID $id." }
	$registry[$id] = [pscustomobject]@{ itemId = $id; name = $cells[1]; classification = $cells[2]; constantName = $cells[3]; namespace = $namespace; nameOrigin = $nameOrigin }
}
if ($registry.Count -eq 0) { throw 'Registry has no records.' }

$previous = @{}
$provenance = @{}
$previousFields = @()
if ($PreviousSnapshot) {
	$baseline = Get-Content -LiteralPath $PreviousSnapshot -Raw -Encoding UTF8 | ConvertFrom-Json
	if ($baseline.schemaVersion -ne 1) { throw 'Unsupported snapshot schema.' }
	$previousFields = @($baseline.source.fields)
	foreach ($property in $baseline.factProvenance.PSObject.Properties) {
		$null = Read-Date $property.Value.date 'Snapshot fact provenance date'
		$provenance[$property.Name] = $property.Value
	}
	if (@($baseline.records).Count -ne $baseline.registry.recordCount) { throw 'Snapshot record count does not match its metadata.' }
	foreach ($record in @($baseline.records)) {
		$id = 0
		if (-not [int]::TryParse([string]$record.itemId, [ref]$id) -or $id -lt 1) { throw 'Malformed snapshot ID.' }
		if ($previous.ContainsKey($id)) { throw "Duplicate snapshot ID $id." }
		foreach ($fact in $record.facts.PSObject.Properties) {
			if (-not $provenance.ContainsKey([string]$fact.Value.provenance)) { throw "Missing fact provenance for snapshot ID $id." }
		}
		$previous[$id] = $record
	}
}

$sourceRows = @{}
$sourceFields = @()
$warnings = New-Object 'System.Collections.Generic.List[string]'
if (($asOf - $registryDay).TotalDays -gt $MaxSourceAgeDays) { $warnings.Add('REGISTRY_STALE') }
$sourceKey = ''
$sourceHash = ''
if ($SourcePath) {
	# These columns describe the classifier/export, not independent gameplay facts.
	$observedColumns = @('ItemId', 'Name', 'ItemCategory', 'Subcategory', 'IronmanTabKey', 'WorkflowKey',
		'Tags', 'ConstantName', 'VariantFamilyKey', 'VariantFlags', 'DuplicateNameKey', 'DuplicateNameCount', 'VariantFamilyCount',
		'Preset', 'Scenario', 'CatalogCategory', 'CatalogSubcategory', 'MappedCategoryKey', 'EffectiveCategory',
		'EffectiveSubcategory', 'LayoutTag', 'FinalTab', 'UsageTags', 'RegistryName', 'FinalTabName', 'MapperContext',
		'GearStatsContext', 'ItemValueContext', 'UpgradeContext', 'PlayerChoicesContext', 'BankabilityContext',
		'ReviewContext', 'Status', 'Error')
	$rawRows = @(Import-Csv -LiteralPath $SourcePath -Delimiter "`t" -Encoding UTF8)
	if ($rawRows.Count -eq 0) { throw 'Source TSV has no records.' }
	$headers = @($rawRows[0].PSObject.Properties.Name)
	if ($headers -notcontains 'ItemId') { throw 'Source TSV requires an ItemId column.' }
	$sourceFields = @($headers | Where-Object { $observedColumns -notcontains $_ } | Sort-Object)
	if ($sourceFields.Count -eq 0) { throw 'Source TSV has no independent fact columns.' }
	foreach ($row in $rawRows) {
		$id = 0
		if (-not [int]::TryParse([string]$row.ItemId, [ref]$id) -or $id -lt 1) { throw 'Malformed source ID.' }
		if ($sourceRows.ContainsKey($id)) { throw "Duplicate source ID $id." }
		$sourceRows[$id] = $row
	}
	$sourceHash = (Get-FileHash -LiteralPath $SourcePath -Algorithm SHA256).Hash.ToLowerInvariant()
	$sourceKey = Get-Hash ($SourceIdentifier + "`n" + $SourceRevision + "`n" + $SourceDate + "`n" + $sourceHash)
	$provenance[$sourceKey] = [pscustomobject][ordered]@{ identifier = $SourceIdentifier; revision = $SourceRevision; date = $SourceDate; sha256 = $sourceHash }
	if (($asOf - $sourceDay).TotalDays -gt $MaxSourceAgeDays) { $warnings.Add('SOURCE_STALE') }
	if ($SourceCompleteness -ne 'Complete') { $warnings.Add('SOURCE_COMPLETENESS_' + $SourceCompleteness.ToUpperInvariant()) }
	if (@($previousFields | Where-Object { $sourceFields -notcontains $_ }).Count -gt 0) { $warnings.Add('SOURCE_SCHEMA_INCOMPLETE') }
} else { $warnings.Add('SOURCE_NOT_SUPPLIED') }
if (-not $PreviousSnapshot) { $warnings.Add('NO_PREVIOUS_SNAPSHOT_ALL_IDS_REQUIRE_INITIAL_REVIEW') }

$changes = New-Object 'System.Collections.Generic.List[object]'
$records = New-Object 'System.Collections.Generic.List[object]'
$matched = 0
$available = 0
$retainedCount = 0
$retainedStaleFacts = 0
foreach ($id in @($registry.Keys | Sort-Object)) {
	$item = $registry[$id]
	$old = $previous[$id]
	$kind = Get-RecordKind $item.name $item.constantName $item.namespace
	if ($null -eq $old) { Add-Change $id $item.name $kind 'NEW_ID' 'Not present in the supplied previous snapshot.' }
	else {
		if ($old.name -cne $item.name) { Add-Change $id $item.name $kind 'NAME_CHANGED' ($old.name + ' -> ' + $item.name) }
		if ($old.classification -cne $item.classification) {
			Add-Change $id $item.name $kind 'REGISTRY_CLASSIFICATION_CHANGED' ($old.classification + ' -> ' + $item.classification + '; inferred registry role, not verified usage')
		}
		if ($old.constantName -cne $item.constantName) { Add-Change $id $item.name $kind 'CONSTANT_CHANGED' ($old.constantName + ' -> ' + $item.constantName) }
		if ($old.namespace -cne $item.namespace) { Add-Change $id $item.name $kind 'NAMESPACE_CHANGED' ($old.namespace + ' -> ' + $item.namespace) }
	}
	$facts = [ordered]@{}
	if ($null -ne $old) {
		foreach ($fact in @($old.facts.PSObject.Properties | Sort-Object Name)) { $facts[$fact.Name] = $fact.Value }
	}
	$row = $sourceRows[$id]
	$rowAvailable = $null -ne $row
	if ($rowAvailable) {
		$matched++
		if ($sourceFields -contains 'WikiStatus' -and $row.WikiStatus -ne 'VERIFIED_WIKI_ID') { $rowAvailable = $false }
	}
	$state = 'SOURCE_UNAVAILABLE'
	$retained = New-Object 'System.Collections.Generic.List[string]'
	$changedFields = New-Object 'System.Collections.Generic.List[string]'
	if ($rowAvailable) {
		$available++
		$state = 'SUPPLIED_FACTS_OBSERVED'
		if ($sourceFields -contains 'WikiStatus') { $state = 'EXACT_CACHED_IDENTITY_OBSERVED' }
		foreach ($field in @(@($facts.Keys) + $sourceFields | Sort-Object -Unique)) {
			$value = $(if ($sourceFields -contains $field) { [string]$row.$field } else { '' })
			if ([string]::IsNullOrWhiteSpace($value)) {
				if ($facts.Contains($field)) { $retained.Add($field) }
				continue
			}
			if ($facts.Contains($field) -and $facts[$field].value -cne $value) {
				$changedFields.Add($field + ': ' + $facts[$field].value + ' -> ' + $value)
			} elseif (-not $facts.Contains($field) -and $null -ne $old) { $changedFields.Add($field + ': [previously unavailable] -> ' + $value) }
			$facts[$field] = [pscustomobject][ordered]@{ value = $value; provenance = $sourceKey }
		}
		if ($retained.Count -gt 0) { $state = 'PARTIAL_SOURCE_FIELDS_RETAINED' }
	} else {
		foreach ($field in @($facts.Keys)) { $retained.Add($field) }
	}
	if ($changedFields.Count -gt 0) { Add-Change $id $item.name $kind 'SOURCE_FACTS_CHANGED' ($changedFields -join '; ') }
	if (-not $rowAvailable -or $retained.Count -gt 0) {
		$details = 'Missing/unverified current facts do not establish that an item function was removed.'
		if ($retained.Count -gt 0) { $details += ' Preserved previous fields: ' + ($retained -join ', ') + '.'; $retainedCount++ }
		Add-Change $id $item.name $kind 'SOURCE_FACTS_UNAVAILABLE' $details
	}
	foreach ($field in $retained) {
		$observedDay = Read-Date $provenance[$facts[$field].provenance].date 'Retained fact provenance date'
		if (($asOf - $observedDay).TotalDays -gt $MaxSourceAgeDays) { $retainedStaleFacts++ }
	}
	$orderedFacts = [ordered]@{}
	foreach ($field in @($facts.Keys | Sort-Object)) { $orderedFacts[$field] = $facts[$field] }
	$records.Add([pscustomobject][ordered]@{
		itemId = $id; name = $item.name; classification = $item.classification; constantName = $item.constantName
		namespace = $item.namespace; nameOrigin = $item.nameOrigin
		recordKind = $kind; sourceState = $state; retainedFields = @($retained | Sort-Object); facts = $orderedFacts
	})
}
foreach ($id in @($previous.Keys | Sort-Object)) {
	if (-not $registry.ContainsKey($id)) {
		$old = $previous[$id]
		Add-Change $id $old.name $old.recordKind 'REMOVED_ID' 'Absent from the supplied registry; verify registry completeness before interpreting removal.'
	}
}
if ($available -lt $registry.Count) { $warnings.Add('SOURCE_RECORD_GAPS') }
if ($retainedCount -gt 0) { $warnings.Add('PREVIOUS_FACTS_RETAINED') }
if ($retainedStaleFacts -gt 0) { $warnings.Add('RETAINED_FACTS_STALE') }
$orderedProvenance = [ordered]@{}
foreach ($key in @($provenance.Keys | Sort-Object)) { $orderedProvenance[$key] = $provenance[$key] }
$snapshot = [pscustomobject][ordered]@{
	schemaVersion = 1; asOfDate = $AsOfDate
	registry = [pscustomobject][ordered]@{
		identifier = $RegistrySource; revision = $RegistryRevision; date = $RegistryDate
		format = $registryFormat; excludedNonPositiveRecords = $excludedNonPositive
		sha256 = (Get-FileHash -LiteralPath $registryInputPath -Algorithm SHA256).Hash.ToLowerInvariant(); recordCount = $registry.Count
		classificationMeaning = 'INFERRED_REGISTRY_ROLE_NOT_VERIFIED_USAGE'
	}
	source = [pscustomobject][ordered]@{
		identifier = $SourceIdentifier; revision = $SourceRevision; date = $SourceDate; sha256 = $sourceHash
		completenessDeclaration = $(if ($SourcePath) { $SourceCompleteness } else { 'NotSupplied' })
		fields = $sourceFields; suppliedRows = $sourceRows.Count; matchedRegistryRows = $matched; availableRegistryRows = $available
		registryCoverage = [math]::Round($available / [double]$registry.Count, 6); retainedRecordCount = $retainedCount; retainedStaleFactCount = $retainedStaleFacts
	}
	warnings = @($warnings | Sort-Object -Unique); factProvenance = $orderedProvenance; records = $records.ToArray()
}

$sortedChanges = @($changes | Sort-Object itemId, changeType)
Ensure-Parent $ReviewPath
if ($sortedChanges.Count -gt 0) { $sortedChanges | Export-Csv -LiteralPath $ReviewPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8 }
else { '"itemId"' + "`t" + '"name"' + "`t" + '"changeType"' + "`t" + '"recordKind"' + "`t" + '"actionability"' + "`t" + '"details"' | Set-Content -LiteralPath $ReviewPath -Encoding UTF8 }
$report = @('# Offline item changes', '',
	"Registry: $RegistrySource / $RegistryRevision ($RegistryDate). Records: $($registry.Count).",
	"Input format: $registryFormat. Excluded nonpositive research records: $excludedNonPositive.",
	"Source: $SourceIdentifier / $SourceRevision ($SourceDate). Available exact-ID rows: $available/$($registry.Count).",
	"Review rows: $($sortedChanges.Count). Registry roles remain inferred; cache identity does not verify item function or bankability.", '',
	'| Change | Rows |', '| --- | ---: |')
foreach ($group in @($sortedChanges | Group-Object changeType | Sort-Object Name)) { $report += "| $($group.Name) | $($group.Count) |" }
$report += @('', 'Warnings: ' + ($snapshot.warnings -join ', '), '',
	'Only supplied fields are compared. A game function change with identical supplied data cannot be detected.',
	'Blank, missing or unverified source fields retain previous facts and remain review items.',
	'Explicit cache records, including CERT/PLACEHOLDER index namespaces, are retained in the diff with CACHE_CONTEXT_ONLY; other records are not asserted bankable.',
	'Stale source data requires refresh/review even when the diff is empty. No network requests or runtime changes occurred.',
	'Snapshot write: ' + $(if ($WriteSnapshot) { 'explicitly requested; this does not certify the facts' } else { 'disabled; baseline untouched' }))
Ensure-Parent $ReportPath
$report | Set-Content -LiteralPath $ReportPath -Encoding UTF8
if ($WriteSnapshot) {
	Ensure-Parent $SnapshotPath
	$tempPath = [IO.Path]::GetFullPath($SnapshotPath) + '.' + [guid]::NewGuid().ToString('N') + '.tmp'
	try {
		$snapshot | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $tempPath -Encoding UTF8
		Move-Item -LiteralPath $tempPath -Destination $SnapshotPath -Force
	} finally { if (Test-Path -LiteralPath $tempPath) { Remove-Item -LiteralPath $tempPath -Force } }
}
Write-Output "Compared $($registry.Count) registry records: $($sortedChanges.Count) review rows; $available exact-ID source rows; snapshot written: $([bool]$WriteSnapshot)."
