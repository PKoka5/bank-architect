param(
	[string] $EffectivePath = 'build/classifications-final-audit.tsv',
	[string] $SourcePath = 'tools/research/item-classification-audit/cache/effective-with-source-facts.tsv',
	[string] $OutputPath = 'build/item-role-coverage.tsv',
	[string] $ReportPath = 'build/item-role-coverage.md'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Development-only, offline comparison. Source identities do not prove current
# gameplay eligibility or the optimal bank destination. Never edits runtime data.
$sources = @{}
foreach ($fact in (Import-Csv -LiteralPath $SourcePath -Delimiter "`t")) {
	if ($sources.ContainsKey($fact.ItemId)) { throw "Duplicate source ID $($fact.ItemId)" }
	$sources[$fact.ItemId] = $fact
}
$seen = @{}
$rows = @(foreach ($item in (Import-Csv -LiteralPath $EffectivePath -Delimiter "`t")) {
	if ($seen.ContainsKey($item.itemId)) { throw "Duplicate effective ID $($item.itemId)" }
	$seen[$item.itemId] = $true
	$fact = $sources[$item.itemId]
	$state = 'NO_EXACT_SOURCE'
	$reason = 'Missing or ambiguous cached identity; retain manual review.'
	$page = ''
	if ($null -ne $fact -and $fact.WikiStatus -eq 'VERIFIED_WIKI_ID') {
		$page = $fact.WikiPage
		$state = 'EXACT_CACHED_IDENTITY'
		$reason = 'Identity matched in July 15 snapshot; usage and lifecycle are separate.'
		if ($fact.Minigame -eq 'True' -or $fact.Tutorial -eq 'True' -or $fact.Unobtainable -eq 'True' -or
			$page -match 'Last Man Standing|Deadman|beta|Tournament') {
			$state = 'RESTRICTED_OR_LEGACY_REVIEW'
			$reason = 'Inspect exact source context; never promote a normal-item namesake.'
		} elseif ($fact.QuestItem -eq 'True') {
			$state = 'QUEST_LIFECYCLE_REVIEW'
			$reason = 'Quest label alone does not establish whether removal is safe.'
		} elseif ($item.itemCategory -eq 'CLEANUP' -and $fact.Equipable -eq 'True' -and
			$fact.CombatStatMagnitude -and [int]$fact.CombatStatMagnitude -gt 0) {
			$state = 'EQUIPMENT_DESTINATION_REVIEW'
			$reason = 'Nonzero cached stats may include penalties or cosmetics; runtime stats may recover gear.'
		}
	}
	[pscustomobject]@{
		itemId = $item.itemId; name = $item.name; category = $item.itemCategory
		destination = $item.ironmanTabKey; usageTags = $item.tags; auditStatus = $state
		wikiPage = $page; reason = $reason
	}
})
foreach ($path in @($OutputPath, $ReportPath)) {
	$parent = Split-Path -Parent $path
	if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
}
$rows | Export-Csv -LiteralPath $OutputPath -Delimiter "`t" -NoTypeInformation -Encoding UTF8
$report = @(
	'# Offline item-role coverage', '',
	"Effective records: **$($rows.Count)**. These include legacy/special records, not just bankable items.",
	"Records with usage facts: **$(@($rows | Where-Object { $_.usageTags }).Count)**.", '',
	'| Audit status | Records |', '| --- | ---: |'
)
foreach ($group in ($rows | Group-Object auditStatus | Sort-Object Name)) {
	$report += "| $($group.Name) | $($group.Count) |"
}
$report += @('', 'Source facts were collected on 2026-07-15. This pass does not claim current gameplay verification,',
	'complete semantic coverage or player-specific disposal eligibility. Unknowns remain review items.',
	'No runtime data was changed, and no network requests were made.')
$report | Set-Content -LiteralPath $ReportPath -Encoding UTF8
Write-Output "Audited $($rows.Count) effective records offline; outputs: $OutputPath, $ReportPath"
