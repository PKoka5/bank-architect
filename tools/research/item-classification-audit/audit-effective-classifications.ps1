param(
	[string] $CacheDir = "tools/research/item-classification-audit/cache",
	[string] $EffectivePath = "tools/research/item-classification-audit/cache/effective-item-classifications.tsv",
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $Phase0Report = "docs/research/item-classification-audit-phase-0.md",
	[string] $Phase1Report = "docs/research/item-classification-audit-phase-1.md",
	[string] $UserAgent = "BankArchitectResearch/1.0 (offline classification audit)",
	[switch] $Refresh
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$wikiApi = "https://oldschool.runescape.wiki/api.php"
$priceMappingApi = "https://prices.runescape.wiki/api/v1/osrs/mapping"
$retrievedOn = [DateTime]::UtcNow.ToString("yyyy-MM-dd")
$script:LastRequestAt = [DateTime]::MinValue

New-Item -ItemType Directory -Force -Path $CacheDir | Out-Null
if (!(Test-Path -LiteralPath $EffectivePath))
{
	throw "Effective export not found. Run .\gradlew.bat exportEffectiveItemClassifications first."
}

function Normalize([string] $value)
{
	if ($null -eq $value) { return "" }
	return ($value.Trim().ToLowerInvariant() -replace '\s+', ' ')
}

function Invoke-AuditRequest([string] $uri)
{
	$elapsed = ([DateTime]::UtcNow - $script:LastRequestAt).TotalMilliseconds
	if ($elapsed -lt 1000)
	{
		Start-Sleep -Milliseconds ([int] [Math]::Ceiling(1000 - $elapsed))
	}
	$response = Invoke-RestMethod -Uri $uri -Headers @{ "User-Agent" = $UserAgent } -TimeoutSec 90
	$script:LastRequestAt = [DateTime]::UtcNow
	return $response
}

function Read-JsonCache([string] $path, [scriptblock] $fetch)
{
	if (!$Refresh -and (Test-Path -LiteralPath $path))
	{
		$parsed = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
		foreach ($entry in $parsed) { Write-Output $entry }
		return
	}
	$data = @(& $fetch)
	$data | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $path -Encoding UTF8
	return $data
}

function Invoke-BucketQuery([string] $query)
{
	$uri = $wikiApi + "?action=bucket&format=json&formatversion=2&query=" +
		[uri]::EscapeDataString($query)
	$response = Invoke-AuditRequest $uri
	if ($response.PSObject.Properties.Name -contains "error")
	{
		throw "Bucket query failed: $($response.error)"
	}
	return @($response.bucket)
}

function Read-Bucket([string] $name, [string[]] $fields, [string] $path)
{
	return @(Read-JsonCache $path {
		$rows = New-Object System.Collections.Generic.List[object]
		$offset = 0
		$pageSize = 5000
		$selection = ($fields | ForEach-Object { "'$_'" }) -join ","
		do
		{
			$query = "bucket('$name').select($selection).orderBy('page_name','asc').limit($pageSize).offset($offset).run()"
			$page = @(Invoke-BucketQuery $query)
			foreach ($row in $page) { $rows.Add($row) }
			$offset += $page.Count
		}
		while ($page.Count -eq $pageSize)
		$rows.ToArray()
	})
}

function Read-Category([string] $category, [string] $path)
{
	return @(Read-JsonCache $path {
		$titles = New-Object System.Collections.Generic.List[string]
		$continue = $null
		do
		{
			$uri = $wikiApi + "?action=query&format=json&formatversion=2&list=categorymembers&cmlimit=500&cmnamespace=0&cmtitle=" +
				[uri]::EscapeDataString("Category:" + $category)
			if ($null -ne $continue)
			{
				$uri += "&cmcontinue=" + [uri]::EscapeDataString($continue)
			}
			$response = Invoke-AuditRequest $uri
			foreach ($member in @($response.query.categorymembers)) { $titles.Add([string] $member.title) }
			$continue = if ($response.PSObject.Properties.Name -contains "continue")
				{ [string] $response.continue.cmcontinue } else { $null }
		}
		while ($null -ne $continue)
		$titles.ToArray()
	})
}

function To-Set([object[]] $values)
{
	$set = New-Object 'System.Collections.Generic.HashSet[string]'
	foreach ($value in $values) { [void] $set.Add((Normalize ([string] $value))) }
	return $set
}

function Parse-Ids($raw)
{
	$result = New-Object System.Collections.Generic.List[int]
	foreach ($candidate in @($raw))
	{
		$value = 0
		if ([int]::TryParse([string] $candidate, [ref] $value) -and $value -gt 0)
		{
			$result.Add($value)
		}
	}
	return @($result.ToArray())
}

function Number($value)
{
	$number = 0.0
	if ([double]::TryParse([string] $value, [Globalization.NumberStyles]::Any,
		[Globalization.CultureInfo]::InvariantCulture, [ref] $number)) { return $number }
	return 0.0
}

function Property-Value($object, [string] $name, $fallback = "")
{
	if ($null -ne $object -and $object.PSObject.Properties.Name -contains $name)
	{
		return $object.$name
	}
	return $fallback
}

function Wiki-Url([string] $page, [long] $revision)
{
	$title = [uri]::EscapeDataString($page.Replace(' ', '_')).Replace('%2F', '/')
	if ($revision -gt 0) { return "https://oldschool.runescape.wiki/w/${title}?oldid=$revision" }
	return "https://oldschool.runescape.wiki/w/$title"
}

function Read-PageRevisions([string[]] $titles, [string] $path)
{
	$rows = New-Object System.Collections.Generic.List[object]
	if (!$Refresh -and (Test-Path -LiteralPath $path))
	{
		$cached = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json
		foreach ($row in $cached) { $rows.Add($row) }
	}
	$existing = To-Set @($rows | ForEach-Object { $_.Title })
	$missing = @($titles | Where-Object { $_ -and !$existing.Contains((Normalize $_)) } | Sort-Object -Unique)
	for ($offset = 0; $offset -lt $missing.Count; $offset += 50)
	{
		$last = [Math]::Min($offset + 49, $missing.Count - 1)
		$batch = @($missing[$offset..$last])
		$uri = $wikiApi + "?action=query&format=json&formatversion=2&prop=revisions&rvprop=ids%7Ctimestamp&titles=" +
			[uri]::EscapeDataString(($batch -join '|'))
		$response = Invoke-AuditRequest $uri
		foreach ($page in @($response.query.pages))
		{
			$revision = if (@($page.revisions).Count -gt 0) { $page.revisions[0] } else { $null }
			$rows.Add([pscustomobject] @{
				Title = [string] $page.title
				Revision = if ($null -ne $revision) { [long] $revision.revid } else { 0 }
				Timestamp = if ($null -ne $revision) { [string] $revision.timestamp } else { "" }
			})
		}
	}
	$rows.ToArray() | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $path -Encoding UTF8
	return @($rows.ToArray())
}

function Add-Finding(
	[System.Collections.Generic.List[object]] $findings,
	[string] $detector,
	[object] $item,
	[string] $proposed,
	[string] $confidence,
	[string] $reason,
	[string[]] $pages)
{
	$verifiedPages = @($pages | Where-Object { $_ } | Sort-Object -Unique)
	if ($verifiedPages.Count -eq 0)
	{
		$confidence = "UNVERIFIED"
	}
	$findings.Add([pscustomobject] @{
		Detector = $detector
		Family = if ([string]::IsNullOrWhiteSpace([string] $item.variantFamilyKey))
			{ Normalize ([string] $item.name) } else { [string] $item.variantFamilyKey }
		ItemId = [int] $item.itemId
		Name = [string] $item.name
		CurrentTab = [string] $item.ironmanTabKey
		ProposedTab = $proposed
		Confidence = $confidence
		Reason = $reason
		Pages = $verifiedPages
	})
}

# Reporting precedence only. This never changes runtime classification. When one exact ID is
# reported by several detectors, a verified, function-specific detector wins over a generic
# shape/absence detector so the report cannot recommend contradictory destinations.
$detectorPriority = @{
	UNFINISHED_WORKFLOW_OUTSIDE_TARGET = 800
	RESTRICTED_VARIANT_IN_FUNCTIONAL_TAB = 700
	ZERO_COMBAT_STATS_IN_GEAR = 600
	QUEST_IN_FUNCTIONAL_TAB = 500
	NON_HERBLORE_FAMILY_IN_HERBLORE = 400
	VARIANT_FAMILY_SPLIT = 300
	NO_EQUIPABLE_EVIDENCE_IN_GEAR = 200
	UNTRADEABLE_WITHOUT_RECORDED_FUNCTION = 100
}

$detectorReading = [ordered] @{
	NO_EQUIPABLE_EVIDENCE_IN_GEAR = "Current tab is the potentially wrong Gear placement. Proposed route is only a provisional destination: missing equipable evidence is not proof, so this detector remains UNVERIFIED."
	NON_HERBLORE_FAMILY_IN_HERBLORE = "Current tab is the wrong current Herblore placement. Proposed route is the destination to review or, after overlap resolution, the more specific detector's destination."
	QUEST_IN_FUNCTIONAL_TAB = "Current tab is the functional placement being challenged. Proposed route is Cleanup unless the sourced item may retain a real function, in which case explicit retained-function review is required."
	RESTRICTED_VARIANT_IN_FUNCTIONAL_TAB = "Current tab is the wrong functional placement of a restricted cache/minigame/tutorial variant. Proposed route is its review/cleanup destination."
	UNFINISHED_WORKFLOW_OUTSIDE_TARGET = "Current tab is the wrong workflow placement. Proposed route is the confirmed production-workflow destination and has the highest overlap priority."
	UNTRADEABLE_WITHOUT_RECORDED_FUNCTION = "Current tab is only a suspected wrong placement. Proposed route is provisional Cleanup; the absence of recorded function is insufficient evidence, so this detector remains UNVERIFIED."
	VARIANT_FAMILY_SPLIT = "Current tabs show the existing split being reviewed, not automatically an error. Proposed route asks for the split's reason unless a more specific detector supplies a concrete destination."
	ZERO_COMBAT_STATS_IN_GEAR = "Current tab is the wrong Gear placement under the zero-combat-stat rule. Proposed route is the sourced non-combat review destination."
}

$reviewedNonHerbloreHighConfidenceIds = @(2203, 8004, 8005, 8006, 11876, 11887)

$effective = @(Import-Csv -LiteralPath $EffectivePath -Delimiter "`t")

$mappingPath = Join-Path $CacheDir "wiki-price-mapping.json"
$mapping = @(Read-JsonCache $mappingPath { @(Invoke-AuditRequest $priceMappingApi) })
$mappingById = @{}
foreach ($row in $mapping) { $mappingById[[int] $row.id] = $row }

$itemFields = @("page_name", "page_name_sub", "item_name", "item_id", "default_version",
	"release_date", "examine", "high_alchemy_value", "is_members_only", "value")
$wikiItems = @(Read-Bucket "infobox_item" $itemFields (Join-Path $CacheDir "wiki-infobox-item.bucket.json"))

$bonusFields = @("page_name", "page_name_sub", "equipment_slot", "stab_attack_bonus",
	"slash_attack_bonus", "crush_attack_bonus", "range_attack_bonus", "magic_attack_bonus",
	"stab_defence_bonus", "slash_defence_bonus", "crush_defence_bonus", "range_defence_bonus",
	"magic_defence_bonus", "strength_bonus", "ranged_strength_bonus", "prayer_bonus",
	"magic_damage_bonus", "weapon_attack_speed", "combat_style")
$wikiBonuses = @(Read-Bucket "infobox_bonuses" $bonusFields (Join-Path $CacheDir "wiki-infobox-bonuses.bucket.json"))

$categorySpecs = [ordered] @{
	Quest = "Quest_items"
	Equipable = "Equipable_items"
	Tradeable = "Tradeable_items"
	Untradeable = "Untradeable_items"
	Food = "Food"
	Tools = "Tools"
	Minigame = "Minigame_items"
	Tutorial = "Tutorial_Island"
	Unobtainable = "Unobtainable_items"
}
$categories = @{}
foreach ($key in $categorySpecs.Keys)
{
	$slug = $categorySpecs[$key]
	$categories[$key] = To-Set (Read-Category $slug (Join-Path $CacheDir ("category-" + $slug.ToLowerInvariant() + ".json")))
}

$wikiById = @{}
foreach ($row in $wikiItems)
{
	$rawIds = if ($row.PSObject.Properties.Name -contains "item_id") { $row.item_id } else { @() }
	foreach ($id in @(Parse-Ids $rawIds))
	{
		if (!$wikiById.ContainsKey($id)) { $wikiById[$id] = @() }
		$wikiById[$id] += $row
	}
}

$bonusByPageSub = @{}
$bonusByPage = @{}
foreach ($row in $wikiBonuses)
{
	$subKey = Normalize ([string] $row.page_name_sub)
	$pageKey = Normalize ([string] $row.page_name)
	if ($subKey) { $bonusByPageSub[$subKey] = $row }
	if ($pageKey -and !$bonusByPage.ContainsKey($pageKey)) { $bonusByPage[$pageKey] = @() }
	if ($pageKey) { $bonusByPage[$pageKey] += $row }
}

function Wiki-Facts([int] $itemId)
{
	if (!$wikiById.ContainsKey($itemId)) { return @() }
	$facts = @($wikiById[$itemId])
	$defaults = @($facts | Where-Object { $_.default_version -eq $true -or $_.default_version -eq "true" })
	if ($defaults.Count -gt 0) { return $defaults }
	return $facts
}

function Bonus-Fact($wikiFact)
{
	$subKey = Normalize ([string] $wikiFact.page_name_sub)
	if ($bonusByPageSub.ContainsKey($subKey)) { return $bonusByPageSub[$subKey] }
	$pageKey = Normalize ([string] $wikiFact.page_name)
	if ($bonusByPage.ContainsKey($pageKey) -and @($bonusByPage[$pageKey]).Count -eq 1)
	{
		return @($bonusByPage[$pageKey])[0]
	}
	return $null
}

function Combat-Score($bonus)
{
	if ($null -eq $bonus) { return $null }
	$fields = @("stab_attack_bonus", "slash_attack_bonus", "crush_attack_bonus", "range_attack_bonus",
		"magic_attack_bonus", "stab_defence_bonus", "slash_defence_bonus", "crush_defence_bonus",
		"range_defence_bonus", "magic_defence_bonus", "strength_bonus", "ranged_strength_bonus",
		"prayer_bonus", "magic_damage_bonus")
	$total = 0.0
	foreach ($field in $fields)
	{
		$value = if ($bonus.PSObject.Properties.Name -contains $field) { $bonus.$field } else { 0 }
		$total += [Math]::Abs((Number $value))
	}
	return $total
}

$findings = New-Object System.Collections.Generic.List[object]
$joinedRows = New-Object System.Collections.Generic.List[object]
$functionalQuestTabs = @("combat-gear", "skilling-tools", "potions-food", "herblore", "seeds-farming", "resources")
$functionalTabs = @("currency-utilities", "combat-gear", "potions-food", "herblore", "seeds-farming", "skilling-tools", "resources")
$reviewedHerbloreUtilityIds = @(11738, 11739, 13226, 24478, 29996, 29997, 29998, 33135, 33137)

foreach ($item in $effective)
{
	$id = [int] $item.itemId
	$wikiFactsForItem = @(Wiki-Facts $id)
	$pages = @($wikiFactsForItem | ForEach-Object { [string] $_.page_name } | Sort-Object -Unique)
	$pageKeys = @($pages | ForEach-Object { Normalize $_ })
	$isQuest = @($pageKeys | Where-Object { $categories.Quest.Contains($_) }).Count -gt 0
	$isEquipableCategory = @($pageKeys | Where-Object { $categories.Equipable.Contains($_) }).Count -gt 0
	$isUntradeable = @($pageKeys | Where-Object { $categories.Untradeable.Contains($_) }).Count -gt 0
	$isMinigamePage = @($pageKeys | Where-Object { $categories.Minigame.Contains($_) }).Count -gt 0
	$isTutorialPage = @($pageKeys | Where-Object { $categories.Tutorial.Contains($_) }).Count -gt 0
	$isUnobtainable = @($pageKeys | Where-Object { $categories.Unobtainable.Contains($_) }).Count -gt 0
	$bonus = $null
	foreach ($fact in $wikiFactsForItem)
	{
		$bonus = Bonus-Fact $fact
		if ($null -ne $bonus) { break }
	}
	$combatScore = Combat-Score $bonus
	$primaryWiki = if ($wikiFactsForItem.Count -gt 0) { $wikiFactsForItem[0] } else { $null }
	$mappingFact = if ($mappingById.ContainsKey($id)) { $mappingById[$id] } else { $null }
	$joinedRows.Add([pscustomobject] @{
		ItemId = $id
		Name = [string] $item.name
		ItemCategory = [string] $item.itemCategory
		Subcategory = [string] $item.subcategory
		IronmanTabKey = [string] $item.ironmanTabKey
		VariantFamilyKey = [string] $item.variantFamilyKey
		VariantFlags = [string] $item.variantFlags
		WikiStatus = if ($null -ne $primaryWiki) { "VERIFIED_WIKI_ID" } else { "UNVERIFIED" }
		WikiPage = [string] (Property-Value $primaryWiki "page_name")
		WikiName = [string] (Property-Value $primaryWiki "item_name" (Property-Value $mappingFact "name"))
		Members = [string] (Property-Value $primaryWiki "is_members_only" (Property-Value $mappingFact "members"))
		Examine = [string] (Property-Value $primaryWiki "examine" (Property-Value $mappingFact "examine"))
		HighAlch = [string] (Property-Value $primaryWiki "high_alchemy_value" (Property-Value $mappingFact "highalch"))
		ReleaseDate = [string] (Property-Value $primaryWiki "release_date")
		QuestItem = $isQuest
		Equipable = ($isEquipableCategory -or $null -ne $bonus)
		EquipmentSlot = [string] (Property-Value $bonus "equipment_slot")
		CombatStatMagnitude = if ($null -ne $combatScore) { $combatScore } else { "" }
		Tradeable = @($pageKeys | Where-Object { $categories.Tradeable.Contains($_) }).Count -gt 0
		Untradeable = $isUntradeable
		Minigame = $isMinigamePage
		Tutorial = $isTutorialPage
		Unobtainable = $isUnobtainable
	})

	if ($isQuest -and !$isUnobtainable -and
		$functionalQuestTabs -contains [string] $item.ironmanTabKey)
	{
		$proposal = if ([string] $item.ironmanTabKey -eq "combat-gear" -and $null -ne $combatScore -and $combatScore -gt 0)
			{ "REVIEW_RETAINED_FUNCTION" } else { "storage-cleanup" }
		$confidence = if ($proposal -eq "storage-cleanup" -and $null -ne $combatScore -and $combatScore -eq 0)
			{ "HIGH" } else { "MEDIUM" }
		Add-Finding $findings "QUEST_IN_FUNCTIONAL_TAB" $item $proposal $confidence `
			"Wiki quest-item page is routed into a functional Ironman tab; retained quest rewards need explicit review." $pages
	}

	if ([string] $item.ironmanTabKey -eq "combat-gear" -and $wikiFactsForItem.Count -gt 0 -and
		!$isUnobtainable -and !$isEquipableCategory -and $null -eq $bonus)
	{
		Add-Finding $findings "NO_EQUIPABLE_EVIDENCE_IN_GEAR" $item "storage-cleanup" "UNVERIFIED" `
			"No Wiki equipable-category or bonuses record was found; page-level confirmation is required before correction." $pages
	}

	if ([string] $item.ironmanTabKey -eq "combat-gear" -and !$isUnobtainable -and
		$null -ne $combatScore -and $combatScore -eq 0)
	{
		$proposal = if (@($pageKeys | Where-Object { $categories.Tools.Contains($_) }).Count -gt 0)
			{ "skilling-tools" } else { "clues-cosmetics-or-cleanup" }
		Add-Finding $findings "ZERO_COMBAT_STATS_IN_GEAR" $item $proposal "HIGH" `
			"Wiki equipment record exists but all reviewed combat bonuses are zero." $pages
	}

	$text = (([string] $item.name) + " " + ([string] $item.constantName).Replace('_', ' ')).ToLowerInvariant()
	$isUnfinishedBowOrBolt = $text -match 'unfeathered|bolts?\\s*\\(unf\\)|unfinished broad bolt|(?:bow|crossbow|ballista).*\\(u\\)|unstrung.*(?:bow|crossbow|ballista)|(?:bow|crossbow|ballista).*unstrung'
	$isUnfinishedPotion = $text -match 'potion .*\\(unf\\)|unfinished potion'
	if ($wikiFactsForItem.Count -gt 0 -and $isUnfinishedBowOrBolt -and
		[string] $item.ironmanTabKey -ne "resources")
	{
		Add-Finding $findings "UNFINISHED_WORKFLOW_OUTSIDE_TARGET" $item "resources" "HIGH" `
			"Reviewed unfinished bow/bolt family is outside the Resources workflow." $pages
	}
	elseif ($wikiFactsForItem.Count -gt 0 -and $isUnfinishedPotion -and
		[string] $item.ironmanTabKey -ne "herblore")
	{
		Add-Finding $findings "UNFINISHED_WORKFLOW_OUTSIDE_TARGET" $item "herblore" "HIGH" `
			"Reviewed unfinished potion family is outside the Herblore workflow." $pages
	}

	$variantFlags = @(([string] $item.variantFlags) -split ',' | Where-Object { $_ })
	$isRestrictedVariant = @($variantFlags | Where-Object { $_ -in @("battle-royale", "last-man-standing", "bounty-hunter", "corrupted", "tutorial", "league", "deadman", "inactive", "broken") }).Count -gt 0
	if ($wikiFactsForItem.Count -gt 0 -and !$isUnobtainable -and
		($isRestrictedVariant -or $isMinigamePage -or $isTutorialPage) -and
		$functionalTabs -contains [string] $item.ironmanTabKey)
	{
		Add-Finding $findings "RESTRICTED_VARIANT_IN_FUNCTIONAL_TAB" $item "storage-cleanup" "MEDIUM" `
			"Cache/minigame/tutorial/restricted variant is routed into a functional Ironman tab." $pages
	}

	if ([string] $item.ironmanTabKey -eq "herblore" -and
		$reviewedHerbloreUtilityIds -notcontains $id -and
		$text -match 'book|sword|shield|helm|boots|lantern|map|bolts?|pouch|sack|box')
	{
		$confidence = if ($reviewedNonHerbloreHighConfidenceIds -contains $id) { "HIGH" } else { "MEDIUM" }
		Add-Finding $findings "NON_HERBLORE_FAMILY_IN_HERBLORE" $item "REVIEW_BY_FUNCTION" $confidence `
			"A book/tool/equipment-shaped family reaches Herblore without an effective HERBLORE classification." $pages
	}

	if ($isUntradeable -and !$isUnobtainable -and
		$functionalTabs -contains [string] $item.ironmanTabKey -and
		$null -eq $bonus -and [string]::IsNullOrWhiteSpace([string] $item.workflowKey) -and
		[string]::IsNullOrWhiteSpace([string] $item.tags) -and !$isQuest)
	{
		Add-Finding $findings "UNTRADEABLE_WITHOUT_RECORDED_FUNCTION" $item "storage-cleanup" "UNVERIFIED" `
			"Wiki marks the item untradeable, while the effective catalog has no workflow/tags or equipment record." $pages
	}
}

$joinedRows | Export-Csv -LiteralPath (Join-Path $CacheDir "effective-with-source-facts.tsv") `
	-Delimiter "`t" -NoTypeInformation -Encoding UTF8

# Detect mixed routing inside reviewed empty/charged families. These are review signals because some
# splits (for example filled versus empty tomes) are deliberate product decisions.
$variantRows = @($effective | Where-Object {
	[int] $_.variantFamilyCount -gt 1 -and $_.variantFlags -and $wikiById.ContainsKey([int] $_.itemId)
})
foreach ($group in @($variantRows | Group-Object variantFamilyKey))
{
	$tabs = @($group.Group.ironmanTabKey | Sort-Object -Unique)
	$flags = @($group.Group.variantFlags -split ',' | Where-Object { $_ } | Sort-Object -Unique)
	if ($tabs.Count -gt 1 -and @($flags | Where-Object { $_ -in @("empty", "charged", "degraded") }).Count -gt 0)
	{
		foreach ($item in $group.Group)
		{
			$pages = @((Wiki-Facts ([int] $item.itemId)) | ForEach-Object { [string] $_.page_name } | Sort-Object -Unique)
			Add-Finding $findings "VARIANT_FAMILY_SPLIT" $item "REVIEW_EXISTING_REASON" "MEDIUM" `
				("Variant family spans tabs: " + ($tabs -join ', ') + ". Confirm that the split is deliberate.") $pages
		}
	}
}

# Resolve every multi-detector exact-ID overlap before rendering. Verified findings are eligible
# before UNVERIFIED findings; within that set, the most function-specific detector wins. All rows
# remain visible as independent signals, but their ProposedTab becomes one consistent final route.
$duplicateFindingGroups = @($findings | Group-Object ItemId | Where-Object Count -gt 1)
$conflictingFindingGroups = @($duplicateFindingGroups | Where-Object {
	@($_.Group.ProposedTab | Sort-Object -Unique).Count -gt 1
})
foreach ($group in $duplicateFindingGroups)
{
	$rows = @($group.Group)
	$verifiedRows = @($rows | Where-Object Confidence -ne "UNVERIFIED")
	$eligibleRows = if ($verifiedRows.Count -gt 0) { $verifiedRows } else { $rows }
	$winner = @($eligibleRows | Sort-Object `
		@{ Expression = { $detectorPriority[[string] $_.Detector] }; Descending = $true }, `
		@{ Expression = { [string] $_.Detector }; Descending = $false } | Select-Object -First 1)[0]
	foreach ($row in $rows)
	{
		$row.ProposedTab = [string] $winner.ProposedTab
		$row | Add-Member -NotePropertyName ResolutionDetector -NotePropertyValue ([string] $winner.Detector)
	}
}

$priceDocumentationPage = "RuneScape:Real-time Prices"
$findingPages = @($findings | ForEach-Object { $_.Pages }) + @($priceDocumentationPage)
$findingPages = @($findingPages | Sort-Object -Unique)
$revisionRows = @(Read-PageRevisions $findingPages (Join-Path $CacheDir "finding-page-revisions.json"))
$revisionByTitle = @{}
foreach ($row in $revisionRows) { $revisionByTitle[(Normalize ([string] $row.Title))] = $row }

$sourcesPath = Join-Path $CacheDir "sources.tsv"
$priceDocumentationRevision = if ($revisionByTitle.ContainsKey((Normalize $priceDocumentationPage)))
	{ [long] $revisionByTitle[(Normalize $priceDocumentationPage)].Revision } else { 0 }
$priceDocumentationUrl = Wiki-Url $priceDocumentationPage $priceDocumentationRevision
@(
	"# schema=1",
	"# source_key`tsource_url`tretrieved_on`trevision`tlicense",
	"osrs-wiki-price-mapping-docs`t$priceDocumentationUrl`t$retrievedOn`t$priceDocumentationRevision`tCC BY-NC-SA 3.0",
	"osrs-wiki-price-mapping-snapshot`thttps://prices.runescape.wiki/api/v1/osrs/mapping`t$retrievedOn`tdynamic-bulk-snapshot`tCC BY-NC-SA 3.0",
	"osrs-wiki-bucket-infobox-item`thttps://oldschool.runescape.wiki/api.php?action=bucket`t$retrievedOn`tpage-revisions-pinned-in-report`tCC BY-NC-SA 3.0",
	"osrs-wiki-bucket-infobox-bonuses`thttps://oldschool.runescape.wiki/api.php?action=bucket`t$retrievedOn`tpage-revisions-pinned-in-report`tCC BY-NC-SA 3.0"
) | Set-Content -LiteralPath $sourcesPath -Encoding UTF8

$categoryCounts = @($effective | Group-Object itemCategory | Sort-Object Name)
$tabCounts = @($effective | Group-Object ironmanTabKey | Sort-Object Name)
$variantFlagCounts = @($effective | ForEach-Object { ([string] $_.variantFlags) -split ',' } |
	Where-Object { $_ } | Group-Object | Sort-Object Count -Descending)
$duplicateGroups = @($effective | Where-Object { [int] $_.duplicateNameCount -gt 1 } |
	Group-Object duplicateNameKey).Count
$excludedCount = @((Import-Csv -LiteralPath (Join-Path $CacheDir "excluded-cache-records.tsv") -Delimiter "`t")).Count
$rawRegistryCount = @(Get-Content -LiteralPath $RegistryPath | Where-Object { $_ -and !([string] $_).StartsWith('#') }).Count
$nullOrInvalidCount = $rawRegistryCount - $effective.Count - $excludedCount

$phase0 = New-Object System.Collections.Generic.List[string]
$phase0.Add("# Item classification audit - phase 0")
$phase0.Add("")
$phase0.Add("Generated on $retrievedOn from the effective CompositeItemCatalog.DEFAULT result and the IRONMAN preset mapper. Raw per-ID output remains git-ignored under the research cache.")
$phase0.Add("")
$phase0.Add("## Scope")
$phase0.Add("")
$phase0.Add("- Effective records exported: **$($effective.Count)**.")
$phase0.Add("- Clear cache/interface/dummy/placeholder constants excluded: **$excludedCount**.")
$phase0.Add("- Null-name, blank-name, or invalid-ID registry rows omitted: **$nullOrInvalidCount**.")
$phase0.Add("- Exact duplicate display-name groups marked: **$duplicateGroups**.")
$phase0.Add("- No production classification, mapper, layout, UI, or runtime-network code was changed.")
$phase0.Add("")
$phase0.Add("## Effective ItemCategory counts")
$phase0.Add("")
$phase0.Add("| ItemCategory | Count |")
$phase0.Add("|---|---:|")
foreach ($group in $categoryCounts) { $phase0.Add("| $($group.Name) | $($group.Count) |") }
$phase0.Add("")
$phase0.Add("## Effective IRONMAN tab counts")
$phase0.Add("")
$phase0.Add("| Tab key | Count |")
$phase0.Add("|---|---:|")
foreach ($group in $tabCounts) { $phase0.Add("| $($group.Name) | $($group.Count) |") }
$phase0.Add("")
$phase0.Add("## Variant markers")
$phase0.Add("")
$phase0.Add("These markers are reporting facts only; they are never used as production classification suffix heuristics.")
$phase0.Add("")
$phase0.Add("| Marker | Records |")
$phase0.Add("|---|---:|")
foreach ($group in $variantFlagCounts) { $phase0.Add("| $($group.Name) | $($group.Count) |") }
$phase0 | Set-Content -LiteralPath $Phase0Report -Encoding UTF8

$groupedFindings = @($findings | Group-Object Detector, Family | Sort-Object Name)
$reviewFindings = @($findings | Where-Object Confidence -ne "UNVERIFIED")
$groupedReviewFindings = @($reviewFindings | Group-Object Detector, Family | Sort-Object Name)
$phase1 = New-Object System.Collections.Generic.List[string]
$phase1.Add("# Item classification audit - phase 1")
$phase1.Add("")
$phase1.Add("Read-only contradiction report generated on $retrievedOn. Findings are review signals, not approved corrections. UNVERIFIED means the available sources do not justify a production decision.")
$phase1.Add("")
$phase1.Add("## Source coverage")
$phase1.Add("")
$phase1.Add("- Effective Bank Architect records: **$($effective.Count)**.")
$phase1.Add("- OSRS Wiki infobox item variants: **$($wikiItems.Count)**.")
$phase1.Add("- OSRS Wiki equipment/bonuses variants: **$($wikiBonuses.Count)**.")
$phase1.Add("- Wiki price-mapping records: **$($mapping.Count)**.")
$phase1.Add("- Price-mapping documentation: [$priceDocumentationPage]($priceDocumentationUrl).")
$phase1.Add("- Effective IDs with an exact Wiki item record: **$(@($effective | Where-Object { $wikiById.ContainsKey([int] $_.itemId) }).Count)**.")
$phase1.Add("- Effective IDs without an exact Wiki item record: **$(@($effective | Where-Object { !$wikiById.ContainsKey([int] $_.itemId) }).Count)**; these remain UNVERIFIED and are not promoted to detailed contradictions.")
$phase1.Add("- All detector families: **$($groupedFindings.Count)**; detector item IDs: **$(@($findings.ItemId | Sort-Object -Unique).Count)**.")
$phase1.Add("- Source-backed HIGH/MEDIUM review families listed below: **$($groupedReviewFindings.Count)**.")
$phase1.Add("- RuneLite-local facts used: canonical item constants already compiled by this repository. No exhaustive offline ItemComposition snapshot exists, so missing slot/stat facts remain UNVERIFIED.")
$phase1.Add("- The git-ignored joined TSV records Wiki name, members, examine, high-alch, release date, quest/equipable/tradeable status, slot and combat-stat magnitude per exact ID.")
$phase1.Add("- Reviewed Herblore utility containers (herb boxes/sacks and reagent pouches) are excluded from the generic tool-shaped Herblore detector by exact ID.")
$phase1.Add("- Multi-detector exact IDs: **$($duplicateFindingGroups.Count)**; already agreeing: **$($duplicateFindingGroups.Count - $conflictingFindingGroups.Count)**; conflicting routes resolved: **$($conflictingFindingGroups.Count)**.")
$phase1.Add("")
$phase1.Add("## Confidence criteria")
$phase1.Add("")
$phase1.Add("- **HIGH:** an exact-ID Wiki record supplies a direct mechanic or equipment fact that both disproves the current placement and determines the workflow, or a specifically reviewed exact-ID family makes the current category impossible. Destination review may still be explicit when more than one non-functional destination is reasonable.")
$phase1.Add("- **MEDIUM:** an exact-ID Wiki record supports the contradiction, but retained usefulness or the final product destination still requires product judgment.")
$phase1.Add("- **LOW:** a source-backed but indirect or incomplete signal that is useful for prioritising manual review. No current detailed finding needs this level.")
$phase1.Add("- **UNVERIFIED:** no exact Wiki item page or decisive local fact supports a correction. These signals stay in the ignored TSV and are not listed as actionable findings.")
$phase1.Add("")
$phase1.Add("The reviewed rock-climbing-boots/maps group is HIGH for the narrow conclusion that Herblore is wrong: exact Wiki records identify boots/maps rather than a Herblore workflow. Their final destination remains REVIEW_BY_FUNCTION. Unfinished broad bolts are also HIGH, and the more specific unfinished-workflow detector resolves their destination to resources.")
$phase1.Add("")
$phase1.Add("## Overlap resolution")
$phase1.Add("")
$phase1.Add("For the same exact item ID, a source-backed finding wins over UNVERIFIED absence evidence. Among equally source-backed findings, the priority is: UNFINISHED_WORKFLOW_OUTSIDE_TARGET, RESTRICTED_VARIANT_IN_FUNCTIONAL_TAB, ZERO_COMBAT_STATS_IN_GEAR, QUEST_IN_FUNCTIONAL_TAB, NON_HERBLORE_FAMILY_IN_HERBLORE, VARIANT_FAMILY_SPLIT, NO_EQUIPABLE_EVIDENCE_IN_GEAR, then UNTRADEABLE_WITHOUT_RECORDED_FUNCTION. The specific workflow detector therefore beats a generic category-shape detector. All detector rows remain visible, but every row for the same ID receives the winner's route.")
$phase1.Add("")
$phase1.Add("Before resolution, **$($duplicateFindingGroups.Count)** IDs appeared under multiple detectors; **$($conflictingFindingGroups.Count)** had different proposed routes and were normalised, while **$($duplicateFindingGroups.Count - $conflictingFindingGroups.Count)** already agreed. For IDs 11876 and 11887, the NON_HERBLORE route changed from REVIEW_BY_FUNCTION to resources; the Wiki examines identify unfinished bolts that must receive feathers, or a pack containing those bolts, so the Fletching/Resources workflow wins.")
$phase1.Add("")
$phase1.Add("| Detector | Conflicting exact IDs normalised |")
$phase1.Add("|---|---:|")
foreach ($detectorName in @($findings.Detector | Sort-Object -Unique))
{
	$participatingIds = @($conflictingFindingGroups | Where-Object {
		@($_.Group.Detector | Sort-Object -Unique) -contains $detectorName
	}).Count
	$phase1.Add("| $detectorName | $participatingIds |")
}
$phase1.Add("")
$phase1.Add("## Detector totals")
$phase1.Add("")
$phase1.Add("| Detector | Families | Item IDs |")
$phase1.Add("|---|---:|---:|")
foreach ($detector in @($findings | Group-Object Detector | Sort-Object Name))
{
	$families = @($detector.Group.Family | Sort-Object -Unique).Count
	$ids = @($detector.Group.ItemId | Sort-Object -Unique).Count
	$phase1.Add("| $($detector.Name) | $families | $ids |")
}
$phase1.Add("")
$phase1.Add("## Family findings")
$phase1.Add("")
$phase1.Add("In every row, **Current tab(s)** is the placement being audited; **Proposed route** is the resolved destination or review action, never a confirmation that the current tab is correct. Detector-specific interpretation is listed after the table.")
$phase1.Add("")
$phase1.Add("| Detector | Family | Item IDs | Current tab(s) | Proposed route | Confidence | Sources |")
$phase1.Add("|---|---|---|---|---|---|---|")

foreach ($group in $groupedReviewFindings)
{
	$rows = @($group.Group)
	$detector = [string] $rows[0].Detector
	$family = [string] $rows[0].Family
	$ids = @($rows.ItemId | Sort-Object -Unique)
	$tabs = @($rows.CurrentTab | Sort-Object -Unique)
	$proposed = @($rows.ProposedTab | Sort-Object -Unique)
	$confidenceOrder = @{ HIGH = 3; MEDIUM = 2; LOW = 1; UNVERIFIED = 0 }
	$confidence = @($rows.Confidence | Sort-Object { -$confidenceOrder[$_] } | Select-Object -First 1)[0]
	$pageLinks = New-Object System.Collections.Generic.List[string]
	foreach ($page in @($rows | ForEach-Object { $_.Pages } | Sort-Object -Unique | Select-Object -First 3))
	{
		$revision = if ($revisionByTitle.ContainsKey((Normalize $page)))
			{ [long] $revisionByTitle[(Normalize $page)].Revision } else { 0 }
		$pageLinks.Add("[$page](" + (Wiki-Url $page $revision) + ")")
	}
	$sources = if ($pageLinks.Count -gt 0) { $pageLinks -join ", " } else { "UNVERIFIED (no exact Wiki item page)" }
	$phase1.Add("| $detector | $family | $($ids -join ', ') | $($tabs -join ', ') | $($proposed -join ', ') | **$confidence** | $sources |")
}

$phase1.Add("")
$phase1.Add("## Detector meanings")
$phase1.Add("")
foreach ($detector in @($findings | Group-Object Detector | Sort-Object Name))
{
	$phase1.Add("- **$($detector.Name):** $($detectorReading[[string] $detector.Name])")
}
$phase1.Add("")

$phase1.Add("## Review boundary")
$phase1.Add("")
$phase1.Add("No finding in this document changes production behavior. Phase 2 may only use reviewed exact IDs or reviewed family keys, with a source and regression test per corrected family. Deliberate splits such as filled versus empty tomes must be documented rather than flattened automatically.")
$phase1 | Set-Content -LiteralPath $Phase1Report -Encoding UTF8

$findings | Select-Object Detector, Family, ItemId, Name, CurrentTab, ProposedTab, Confidence, ResolutionDetector, Reason |
	Export-Csv -LiteralPath (Join-Path $CacheDir "contradictions.tsv") -Delimiter "`t" -NoTypeInformation -Encoding UTF8

Write-Output "Effective records: $($effective.Count)"
Write-Output "Wiki item variants: $($wikiItems.Count)"
Write-Output "Wiki bonus variants: $($wikiBonuses.Count)"
Write-Output "Finding families: $($groupedFindings.Count)"
Write-Output "Source-backed review families: $($groupedReviewFindings.Count)"
Write-Output "Finding item IDs: $(@($findings.ItemId | Sort-Object -Unique).Count)"
Write-Output "Multi-detector item IDs: $($duplicateFindingGroups.Count)"
Write-Output "Conflicting routes resolved: $($conflictingFindingGroups.Count)"
Write-Output "Phase 0 report: $Phase0Report"
Write-Output "Phase 1 report: $Phase1Report"
