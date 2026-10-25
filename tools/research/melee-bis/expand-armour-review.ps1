param()
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$registryFile = Join-Path $repo 'src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv'
$wikiFile = Join-Path $repo 'tools/research/wiki-recommended-equipment/cache/infobox-item.bucket.json'
$registry = @{}
Get-Content -LiteralPath $registryFile | Where-Object { $_ -and !$_.StartsWith('#') } | ForEach-Object {
    $parts = $_ -split "`t"
    $registry[[int]$parts[0]] = $parts[1]
}
$wiki = Get-Content -LiteralPath $wikiFile -Raw | ConvertFrom-Json
$normal = @($wiki | Where-Object { $_.page_name -notmatch 'Last Man Standing|unobtainable|beta|Deadman Mode' })
$identities = @{}
foreach ($entry in $normal) {
    foreach ($id in $entry.item_id) {
        if ($id -match '^\d+$') { $identities[[int]$id] = $entry }
    }
}
$result = @{}
function Add-Decision($ids, $slot, $kind, $band, $rank, $group, $condition, $source, $evidence) {
    foreach ($value in ($ids -split ',')) {
        $id = [int]$value
        if (!$registry.ContainsKey($id)) { throw "Registry identity missing: $id" }
        if (!$identities.ContainsKey($id)) { throw "Canonical Wiki identity missing: $id" }
        if ($result.ContainsKey($id)) { throw "Duplicate exact item ID: $id" }
        $result[$id] = [pscustomobject][ordered]@{
            item_id = $id; name = $registry[$id]; slot = $slot; kind = $kind; band = $band
            fallback_rank = $rank; group = $group; condition = $condition
            wiki_version = $identities[$id].page_name_sub; source = $source
            evidence = $evidence; checked = '2026-10-24'
        }
    }
}
$slots = @('body','legs','cape','neck','hands','feet','ring')
$groups = Import-Csv (Join-Path $PSScriptRoot 'candidate-review.tsv') -Delimiter "`t"
$rankOverrides = @{
    'granite-gloves' = 650; 'regen-bracelet' = 640; 'adamant-gloves' = 630
    'brimstone' = 800; 'berserker' = 700
}
$idRankOverrides = @{ 13121=700; 13122=710; 13123=720; 13124=730 }
foreach ($row in $groups) {
    if ($row.slot -notin $slots -or $row.group -in @('metal-body','metal-legs','dragon-body','warrior')) { continue }
    $kind = if ($row.decision.StartsWith('general')) { 2 } else { 1 }
    $band = 0
    if ($row.slot -eq 'neck' -and $kind -eq 2) {
        $band = if ($row.group -in @('rancour','torture','fury')) { 20 } else { 10 }
    }
    if ($row.group -eq 'sunlit-bracers') { $kind = -1 }
    $rank = if ($rankOverrides.ContainsKey($row.group)) { $rankOverrides[$row.group] } else { [int]$row.fallback_rank }
    Add-Decision $row.canonical_ids $row.slot $kind $band $rank $row.group $row.policy_or_condition $row.source $row.evidence
}

# Additional reviewed base states and explicit non-equivalent variants.
$extra = @'
ids|slot|kind|rank|group|condition|page
21892|body|1|244|dragon-platebody|No Strength; separate defence fallback from chainbody; keep personal/Alch priority|Dragon_platebody
3140|body|1|240|dragon-chainbody|No Strength; separate defence fallback from platebody|Dragon_chainbody
10564|body|1|230|granite-body|Tank fallback; no offensive bonus|Granite_body
6129|body|1|200|rock-shell-body|Rune-like raw defence; no Strength|Rock-shell_plate
12414|body|1|240|dragon-chain-gold|Cosmetic same raw stats as Dragon chainbody3140|Dragon_chainbody_(g)
28065|body|1|240|dragon-chain-cr|Bounty ornament cosmetic same raw stats as Dragon chainbody3140; distinct from cutscene2513|Dragon_chainbody_(cr)
22242|body|1|244|dragon-plate-gold|Cosmetic same raw stats as Dragon platebody21892|Dragon_platebody_(g)
12415,12416|legs|1|240|dragon-legs-gold|Cosmetic same raw stats as Dragon legs4087/skirt4585|Dragon_platelegs_(g)
28061,28063|legs|1|240|dragon-legs-cr|Bounty ornament cosmetic same raw stats as Dragon legs4087/skirt4585; distinct from cutscene4180|Dragon_platelegs_(cr)
6809|legs|1|230|granite-legs|Tank fallback; no offensive bonus|Granite_legs
6130|legs|1|200|rock-shell-legs|Rune-like raw defence; no Strength|Rock-shell_legs
544,20199,23303|body|1|310|monk-body|Prayer gear; trim cosmetic stats equivalent; no damage bonus|Monk%27s_robe_top
542,20202,23306|legs|1|310|monk-legs|Prayer gear; trim cosmetic stats equivalent; no damage bonus|Monk%27s_robe
5575|body|1|320|initiate-body|Prayer fallback; no raw offensive bonus|Initiate_hauberk
5576|legs|1|320|initiate-legs|Prayer fallback; no raw offensive bonus|Initiate_cuisse
9674|body|1|330|proselyte-body|Prayer fallback; no raw offensive bonus|Proselyte_hauberk
9676,9678|legs|1|330|proselyte-legs|Cuisse/tasset equal raw bonuses; no raw offensive bonus|Proselyte_cuisse
28936|body|1|340|sunfire-body|Prayer fallback; no raw offensive bonus|Sunfire_fanatic_cuirass
28939|legs|1|340|sunfire-legs|Prayer fallback; no raw offensive bonus|Sunfire_fanatic_chausses
29283|legs|2|500|mixed-hide-legs|STR0 stab3; conditional stab-useful raw stats; allocation pool remains unchanged|Mixed_hide_legs
29013,29037|body|2|590|blue-moon-body|STR2; new/used raw stats equal; no Frostweaver condition credited; allocation pool unchanged|Blue_Moon_chestplate
29016,29039|legs|2|650|blue-moon-legs|STR1; new/used raw stats equal; no Frostweaver condition credited; allocation pool unchanged|Blue_Moon_tassets
29004,29031|body|2|690|eclipse-moon-body|STR3; new/used raw stats equal; no atlatl condition credited; allocation pool unchanged|Eclipse_Moon_chestplate
29007,29033|legs|2|660|eclipse-moon-legs|STR1; new/used raw stats equal; no atlatl condition credited; allocation pool unchanged|Eclipse_Moon_tassets
29289|cape|2|790|mixed-hide-cape|STR1 stab2; actual raw offence, not a set effect; allocation pool remains unchanged|Mixed_hide_cape
29286|feet|2|390|mixed-hide-boots|STR2; keep current cross-style allocation pool unchanged|Mixed_hide_boots
1052|cape|1|290|legends-cape|Defensive fallback; no raw offensive bonus|Cape_of_legends
20050|cape|1|300|obsidian-cape-r|Cosmetic equal defensive bonuses to Obsidian cape6568; master-clue item not default offensive gear|Obsidian_cape_(r)
7458|hands|2|490|mithril-gloves|STR6 attack6; separate from bracelet whose attack7 is higher|Mithril_gloves
7457|hands|2|400|black-gloves|STR5 attack5; ordinary RFD progression|Black_gloves
7456|hands|2|300|steel-gloves|STR4 attack4; ordinary RFD progression|Steel_gloves
7455|hands|2|200|iron-gloves|STR3 attack3; ordinary RFD progression|Iron_gloves
7454|hands|2|100|bronze-gloves|STR2 attack2; ordinary RFD progression|Bronze_gloves
1059|hands|1|50|leather-gloves|No Strength or attack; early defensive fallback|Leather_gloves
24046|hands|2|290|dragonstone-gauntlets|STR4; exact cache identity; complete raw-stat tie check against Steel gloves still pending|Dragonstone_gauntlets
12391|feet|2|400|gilded-boots|Rune-boots rawstats equivalent; cosmetic identity not an extra offensive tier|Gilded_boots
24043|feet|2|400|dragonstone-boots|STR2; exact cache identity; complete raw-stat tie check against Rune boots still pending|Dragonstone_boots
28055|feet|2|700|dragon-boots-cr|Bounty ornament same raw combat stats as Dragon boots11840|Dragon_boots_(cr)
4129|feet|2|250|adamant-boots|STR1; metal boot progression, separate from RuneSTR2|Adamant_boots
4127|feet|1|190|mithril-boots|STR0; metal boot fallback|Mithril_boots
4125|feet|1|180|black-boots|STR0; metal boot fallback|Black_boots
4123|feet|1|170|steel-boots|STR0; metal boot fallback|Steel_boots
4121|feet|1|160|iron-boots|STR0; metal boot fallback|Iron_boots
4119|feet|1|150|bronze-boots|STR0; metal boot fallback|Bronze_boots
6619|feet|1|200|white-boots|STR0 Prayer1; distinct from steel boots|White_boots
11836|feet|1|240|bandos-boots|STR0 Prayer1; tank/god item; not Dragonboots equivalent|Bandos_boots
22951|feet|2|290|brimstone-boots|STR0 stab3; raw accuracy general usable; Karuulm protection not scored|Boots_of_Brimstone
9005,28672|feet|1|100|fancy-boots|STR0; fancy/fancier actual usable alternatives to fighting boots|Fancy_boots
6735|ring|2|300|warrior-base|STR0 slash4; distinguish imbued slash8|Warrior_ring
11772,25262,26769|ring|2|400|warrior-imbued|STR0 slash8; all three permanent imbues|Warrior_ring_(i)
12603|ring|2|300|tyrannical-base|STR0 crush4; distinguish imbued crush8|Tyrannical_ring
12691,25254,26765|ring|2|400|tyrannical-imbued|STR0 crush8; all three permanent imbues|Tyrannical_ring_(i)
12605|ring|2|300|treasonous-base|STR0 stab4; distinguish imbued stab8|Treasonous_ring
12692,25256,26766|ring|2|400|treasonous-imbued|STR0 stab8; all three permanent imbues|Treasonous_ring_(i)
28327|ring|2|600|ring-shadows|STR2 plus accuracy4 and Prayer2; generic raw stats usable independently of teleports|Ring_of_shadows
28329|ring|2|600|ring-shadows-uncharged|Same raw combat stats as charged28327; only teleports need charges, not Serp-style inactivity|Ring_of_shadows
12601|ring|1|340|gods-base|STR0 Prayer4; specialist prayer fallback, distinguish imbuedPrayer8|Ring_of_the_gods
13202,25252,26764|ring|1|350|gods-imbued|STR0 Prayer8; Holy-wrench condition does not add raw DPS|Ring_of_the_gods_(i)
19550,20655|ring|1|320|suffering-base|STR0 Prayer2; charged recoil has same raw stats as uncharged; recoil effect not scored|Ring_of_suffering
19710,20657,25246,25248,26761,26762|ring|1|330|suffering-imbued|STR0 Prayer4; charged recoil/raw-stats equivalent; all three permanent imbues|Ring_of_suffering_(i)
21739|ring|1|210|granite-ring-base|No raw offence; distinct from imbued defence; preserve utility|Granite_ring
21752,25193,26685|ring|1|220|granite-ring-imbued|No raw offence; double defence vs base; all three permanent imbues|Granite_ring_(i)
'@ | ConvertFrom-Csv -Delimiter '|'
foreach ($row in $extra) {
    Add-Decision $row.ids $row.slot ([int]$row.kind) 0 ([int]$row.rank) $row.group $row.condition ("https://oldschool.runescape.wiki/w/" + $row.page) 'cached-canonical-version+reviewed-raw-function'
}

# Neck raw bonuses are usable without activating Barrows/Obsidian effects.
Add-Decision '12851,12853' 'neck' 2 10 600 'amulet-damned' 'Same raw stats as Glory: STR6 attack10 Prayer3; full/used; extra Barrows effect requires full set and is not scored' 'https://oldschool.runescape.wiki/w/Amulet_of_the_Damned' 'live-item-table-2026-10-24+cached-canonical-versions'
Add-Decision '23240' 'neck' 1 0 0 'berserker-necklace-ornament' 'Same function as Berserker necklace11128; Obsidian weapon requirement not activated by ownership' 'https://oldschool.runescape.wiki/w/Berserker_necklace_(or)' 'cached-canonical-version+conditional-role'

# God rune armour has Prayer; gilded/dragonstone/3rdage are independent reviewed identities.
foreach ($entry in $normal) {
    if ($entry.page_name -notmatch '^(Gilded|Dragonstone|3rd age|Ancient|Bandos|Armadyl|Saradomin|Zamorak|Guthix) (platebody|platelegs|plateskirt)$') { continue }
    $family = $Matches[1]; $piece = $Matches[2]
    $slot = if ($piece -eq 'platebody') { 'body' } else { 'legs' }
    $rank = if ($family -eq '3rd age') { 250 } elseif ($family -in @('Gilded','Dragonstone')) { 161 } else { 170 }
    foreach ($id in $entry.item_id) {
        if ($id -notmatch '^\d+$') { throw "Non-numeric armour identity: $id" }
        Add-Decision $id $slot 1 0 $rank ($family.ToLower().Replace(' ','-')+'-'+$piece) 'No raw Strength; cosmetic/god protection is not extra damage; Prayer differs on god rune pieces; respect category/Alch priority' ("https://oldschool.runescape.wiki/w/" + $entry.page_name.Replace(' ','_')) 'cached-canonical-version+reviewed-armour-family'
    }
}

# Only ordinary, verified-name metal variants. This does not infer adjacent IDs.
$metalRanks = @{ Bronze=100; Iron=110; Steel=120; Black=130; Mithril=140; Adamant=150; Rune=160; White=165 }
foreach ($entry in $normal) {
    if ($entry.page_name -notmatch '^(Bronze|Iron|Steel|Black|Mithril|Adamant|Rune|White) (platebody|chainbody|platelegs|plateskirt)( \((g|t|h[1-5])\))?$') { continue }
    $metal = $Matches[1]; $piece = $Matches[2]
    $slot = if ($piece -in @('platebody','chainbody')) { 'body' } else { 'legs' }
    $rank = $metalRanks[$metal] + $(if ($piece -eq 'chainbody') { 0 } else { 1 })
    foreach ($id in $entry.item_id) {
        if ($id -notmatch '^\d+$') { throw "Non-numeric metal identity: $id" }
        Add-Decision $id $slot 1 0 $rank ($metal.ToLower()+'-'+$piece) 'No raw offensive bonus; actual tier/piece rank, cosmetic variants equal; existing categorization and Alch keep priority' ("https://oldschool.runescape.wiki/w/" + $entry.page_name.Replace(' ','_')) 'cached-canonical-version+metal-progression'
    }
}

$inactive = @'
ids|slot|group|condition|page
26378|body|torva-damaged-body|Requires restoration; not wearable restored-equivalent|Torva_platebody
26380|legs|torva-damaged-legs|Requires restoration; not wearable restored-equivalent|Torva_platelegs
20513,33520,33522|body|torso-broken|Broken/locked broken/locked mangled need repair; not normal torso|Fighter_torso
29067|body|blood-moon-broken-body|Broken; needs repair|Blood_Moon_chestplate
29070|legs|blood-moon-broken-legs|Broken; needs repair|Blood_Moon_tassets
29058|body|blue-moon-broken-body|Broken; needs repair; no live raw-stat equivalence|Blue_Moon_chestplate
29061|legs|blue-moon-broken-legs|Broken; needs repair; no live raw-stat equivalence|Blue_Moon_tassets
29049|body|eclipse-moon-broken-body|Broken; needs repair; no live raw-stat equivalence|Eclipse_Moon_chestplate
29052|legs|eclipse-moon-broken-legs|Broken; needs repair; no live raw-stat equivalence|Eclipse_Moon_tassets
4896,4920,4968,4992|body|barrows-broken-body|Durability0; needs repair, not positive-durability equivalent|Barrows_equipment
4902,4926,4974,4998|legs|barrows-broken-legs|Durability0; needs repair, not positive-durability equivalent|Barrows_equipment
20445,20447|cape|fire-cape-broken|Broken Fire/Firemax; needs repair|Fire_cape
21287,21289,33496,33498,33500,33502|cape|infernal-cape-broken|Broken Infernal/Infernalmax and locked broken/mangled variants; need repair|Infernal_cape
'@ | ConvertFrom-Csv -Delimiter '|'
foreach ($row in $inactive) {
    Add-Decision $row.ids $row.slot 0 0 0 $row.group $row.condition ("https://oldschool.runescape.wiki/w/" + $row.page) 'cached-canonical-version+broken-state-review'
}

# One rank per actual item, not one rank for a non-equivalent progression group.
foreach ($id in $idRankOverrides.Keys) { $result[$id].fallback_rank = $idRankOverrides[$id] }
foreach ($record in $result.Values) {
    $ready = if ($record.item_id -in @(24037,24040,24043,24046)) { 'needs-full-stats-tie-review' }
        elseif ($record.item_id -in @(11128,23240)) { 'needs-neck-weapon-band-decision' }
        else { 'ready-for-implementation-fixtures' }
    $record | Add-Member -NotePropertyName readiness -NotePropertyValue $ready
}

$columns = @('item_id','name','slot','kind','band','fallback_rank','group','readiness','condition','wiki_version','source','evidence','checked')
$lines = @($columns -join "`t")
$lines += @($result.Values | Sort-Object slot,item_id | ForEach-Object {
    $record = $_
    ($columns | ForEach-Object { [string]$record.$_ }) -join "`t"
})
$target = Join-Path $PSScriptRoot 'armour-decisions.tsv'
[IO.File]::WriteAllLines($target,$lines,(New-Object Text.UTF8Encoding($false)))
"Wrote $($result.Count) explicit item decisions: $target"
$result.Values | Group-Object slot | Sort-Object Name | Select-Object Name,Count | Format-Table -AutoSize
