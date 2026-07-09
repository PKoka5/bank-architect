param(
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $OutputDir = "docs/research"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Parse-Registry-Category([string] $value)
{
	if ([string]::IsNullOrWhiteSpace($value) -or $value -eq "UNKNOWN")
	{
		return "CLEANUP"
	}

	$known = @("HERBLORE", "FARMING", "POTION", "GEAR", "RUNE", "TELEPORT", "SKILLING", "CURRENCY", "CLEANUP")
	if ($known -contains $value)
	{
		return $value
	}

	return "CLEANUP"
}

function Has-Any([string] $value, [string[]] $needles)
{
	foreach ($needle in $needles)
	{
		if ($value.Contains($needle))
		{
			return $true
		}
	}

	return $false
}

function Find-Rule-Category([string] $displayName, [string] $constantName)
{
	$name = ($displayName + " " + ($constantName -replace "_", " ")).ToLowerInvariant()

	if (Has-Any $name @("clue scroll", "reward casket", "ornament kit", "graceful", "pyromancer",
		"prospector", "angler", "rogue", "lumberjack", "farmer's", "carpenter", "cosmetic",
		"costume", "mask", "hat", "robe top", "robe bottom", "platebody ornament", "platelegs ornament",
		"beer glass", "nulodion's notes", "instruction manual", "holy table napkin", "grail bell",
		"holy grail", "cog", "rat poison", "red vine worm", "insect repellent", "candle",
		"khazard cell", "lever", "shiny key", "child's blanket"))
	{
		return "CLEANUP"
	}
	if (Has-Any $name @("ring of dueling", "games necklace", "amulet of glory", "skills necklace",
		"combat bracelet", "burning amulet", "necklace of passage", "digsite pendant", "ring of wealth",
		"bracelet of ethereum", "teleport", "tablet", "teletab", "jewellery", "ectophial",
		"xeric's talisman", "xeric talisman", "drakan's medallion", "drakans medallion",
		"book of the dead", "magic whistle"))
	{
		return "TELEPORT"
	}
	if (Has-Any $name @("shark", "monkfish", "karambwan", "manta ray", "anglerfish", "lobster",
		"swordfish", "tuna", "salmon", "trout", "saradomin brew", "restore", "stamina potion",
		"prayer potion", "super combat", "ranging potion", "magic potion", "cooked", "pizza",
		"potato", "cake", "pie", "wine", "summer pie", "karambwanji", "shrimp", "anchovies",
		"sardine", "herring", "mackerel", "cod", "pike", "bass", "mantaray", "sea turtle",
		"chilli potato", "egg potato", "tuna potato", "stew", "curry", "kebab"))
	{
		return "POTION"
	}
	if (Has-Any $name @("pickaxe", " axe", "harpoon", "lobster pot", "small fishing net",
		"big fishing net", "chisel", "hammer", "saw", "rake", "seed dibber", "secateurs",
		"watering can", "tinderbox", "knife", "pestle and mortar", "glassblowing pipe",
		"spade", "needle", "thread", "mould", "hammerstone", "fishing rod", "fly fishing rod",
		"small pouch", "medium pouch", "large pouch", "giant pouch", "colossal pouch"))
	{
		return "SKILLING"
	}
	if (Has-Any $name @("arrow", "bolt", "dart", "knife", "javelin", "cannonball", "chinchompa",
		"bolt rack", "toktz", "tzhaar", "helmet", "helm", "coif", "body", "chaps", "vambraces",
		"boots", "gloves", "shield", "defender", "sword", "scimitar", "mace", "dagger", "spear",
		"halberd", "whip", "bow", "staff", "wand", "crossbow", "maul", "warhammer", "battleaxe",
		"excalibur", "pendant of lucien", "armadyl pendant", "cannon base", "cannon stand",
		"cannon barrels", "cannon furnace", "twpart", "mcannon", "railing"))
	{
		return "GEAR"
	}
	if (Has-Any $name @("logs", "log", "ore", "bar", "plank", "hide", "leather", "gem",
		"uncut", "essence", "fish", "raw ", "scale", "dust", "ash", "bone", "bones",
		"limestone", "clay", "sand", "molten glass", "flax", "bow string", "bowstring",
		"feather", "nail", "nails", "coconut", "seaweed", "bucket of", "vial", "orb",
		"battlestaff", "dragonhide"))
	{
		return "SKILLING"
	}
	if (Has-Any $name @("seed", "sapling", "compost", "ultracompost", "plant cure", "watering can"))
	{
		return "FARMING"
	}
	if (Has-Any $name @("grimy", "clean", "herb", "secondary", "unf", "potion unfinished",
		"eye of newt", "snape grass", "red spiders", "white berries", "limpwurt", "mort myre fungus",
		"unidentified guam", "unidentified marentill", "unidentified marrentill",
		"unidentified tarromin", "unidentified harralander", "unidentified ranarr",
		"unidentified irit", "unidentified avantoe", "unidentified kwuarm",
		"unidentified cadantine", "unidentified dwarf weed", "unidentified torstol",
		"guam", "marentill", "marrentill", "tarromin", "harralander", "ranarr weed",
		"irit leaf", "avantoe", "kwuarm", "cadantine", "dwarf weed", "torstol",
		"unicorn horn", "jangerberries", "weapon poison"))
	{
		return "HERBLORE"
	}
	if (Has-Any $name @("coins", "tokkul", "numulite", "mark of grace", "nugget", "stardust",
		"trading sticks", "ecto-token", "castle wars ticket", "pieces of eight", "pearl",
		"western banner", "rada's blessing", "rada blessing", "ghommal's hilt", "ghommals hilt"))
	{
		return "CURRENCY"
	}

	return $null
}

function Escape-Tsv([string] $value)
{
	if ($null -eq $value)
	{
		return ""
	}

	return (($value -replace "`t", " ") -replace "`r?`n", " ")
}

if (!(Test-Path -LiteralPath $RegistryPath))
{
	throw "Registry not found: $RegistryPath"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$rows = New-Object System.Collections.Generic.List[object]
foreach ($line in Get-Content -LiteralPath $RegistryPath)
{
	if ([string]::IsNullOrWhiteSpace($line))
	{
		continue
	}

	$fields = $line -split "`t", -1
	if ($fields.Count -lt 2)
	{
		throw "Invalid registry line: $line"
	}

	$itemIdText = $fields[0].TrimStart([char] 0xFEFF)
	$displayName = $fields[1]
	$sourceCategory = if ($fields.Count -ge 3) { $fields[2] } else { "" }
	$constantName = if ($fields.Count -ge 4) { $fields[3] } else { "" }
	$baseCategory = Parse-Registry-Category $sourceCategory
	$ruleCategory = Find-Rule-Category $displayName $constantName
	$matchedRule = $null -ne $ruleCategory
	$finalCategory = if ($matchedRule) { $ruleCategory } else { $baseCategory }
	$changed = $matchedRule -and $baseCategory -ne $finalCategory
	$fallback = $sourceCategory -eq "UNKNOWN" -and -not $matchedRule

	$rows.Add([pscustomobject] @{
		ItemId = [int] $itemIdText
		DisplayName = $displayName
		ConstantName = $constantName
		SourceCategory = $sourceCategory
		FinalCategory = $finalCategory
		MatchedRule = $matchedRule
		ChangedByRules = $changed
		FallbackReview = $fallback
	})
}

$reportPath = Join-Path $OutputDir "category-classifier-report.md"
$detailPath = Join-Path $OutputDir "category-classifier-detail.tsv"

$detailLines = New-Object System.Collections.Generic.List[string]
$detailLines.Add("item_id	display_name	constant_name	source_category	final_category	matched_rule	changed_by_rules	fallback_review")
foreach ($row in ($rows | Sort-Object FinalCategory, ItemId))
{
	$detailLines.Add([string]::Join("`t", @(
		$row.ItemId,
		(Escape-Tsv $row.DisplayName),
		(Escape-Tsv $row.ConstantName),
		(Escape-Tsv $row.SourceCategory),
		(Escape-Tsv $row.FinalCategory),
		$row.MatchedRule,
		$row.ChangedByRules,
		$row.FallbackReview
	)))
}
Set-Content -LiteralPath $detailPath -Value $detailLines -Encoding UTF8

$summary = New-Object System.Collections.Generic.List[string]
$summary.Add("# Category Classifier Report")
$summary.Add("")
$summary.Add("Generated from the production item registry and classifier rules.")
$summary.Add("")
$summary.Add("- Registry: ``$RegistryPath``")
$summary.Add("- Total positive item IDs: $($rows.Count)")
$summary.Add("- Matched by classifier rules: $(($rows | Where-Object { $_.MatchedRule }).Count)")
$summary.Add("- Changed by rules: $(($rows | Where-Object { $_.ChangedByRules }).Count)")
$summary.Add("- Items still using source/fallback category: $(($rows | Where-Object { -not $_.MatchedRule }).Count)")
$summary.Add("- Unknown-source items still needing rules: $(($rows | Where-Object { $_.FallbackReview }).Count)")
$summary.Add("")
$summary.Add("## Final Category Counts")
$summary.Add("")
foreach ($group in ($rows | Group-Object FinalCategory | Sort-Object Name))
{
	$summary.Add("- $($group.Name): $($group.Count)")
}
$summary.Add("")
$summary.Add("## Fallback Review Samples")
$summary.Add("")
foreach ($row in ($rows | Where-Object { $_.FallbackReview } | Select-Object -First 50))
{
	$summary.Add("- $($row.DisplayName) (#$($row.ItemId), $($row.ConstantName))")
}
Set-Content -LiteralPath $reportPath -Value $summary -Encoding UTF8

Write-Output "Wrote $reportPath"
Write-Output "Wrote $detailPath"
Write-Output "Rows: $($rows.Count)"
