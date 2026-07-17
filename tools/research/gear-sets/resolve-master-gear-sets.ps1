param(
	[string] $WorkbookPath = (Join-Path $HOME "Downloads/OSRS_Master_Gear_Sets_v7.xlsx"),
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $AuditPath = "tmp/research/master-gear-sets-audit.tsv",
	[string] $CatalogPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/organize/item-set-catalog.tsv"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $WorkbookPath))
{
	throw "Workbook not found: $WorkbookPath"
}
if (!(Test-Path -LiteralPath $RegistryPath))
{
	throw "Item registry not found: $RegistryPath"
}

$utf8 = New-Object System.Text.UTF8Encoding($false)
$slotRanks = @{
	Head = 0; Cape = 1; Neck = 2; Ammo = 3; Weapon = 4; Body = 5;
	Shield = 6; Legs = 7; Hands = 8; Boots = 9; Ring = 10
}

function Normalize-Key([string] $value)
{
	return (($value.ToLowerInvariant() -replace "[^a-z0-9]+", "-").Trim("-"))
}

function Read-WorkbookRows([string] $path)
{
	Add-Type -AssemblyName System.IO.Compression.FileSystem
	$zip = [System.IO.Compression.ZipFile]::OpenRead($path)
	try
	{
		$entry = $zip.GetEntry("xl/worksheets/sheet2.xml")
		if ($null -eq $entry)
		{
			throw "Master Set Database worksheet was not found"
		}
		$reader = New-Object System.IO.StreamReader($entry.Open())
		try
		{
			[xml] $sheet = $reader.ReadToEnd()
		}
		finally
		{
			$reader.Dispose()
		}
	}
	finally
	{
		$zip.Dispose()
	}

	$ns = New-Object System.Xml.XmlNamespaceManager($sheet.NameTable)
	$ns.AddNamespace("x", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")
	$result = New-Object System.Collections.Generic.List[object]
	$sourceIndex = 0
	foreach ($row in ($sheet.SelectNodes("//x:sheetData/x:row", $ns) | Select-Object -Skip 1))
	{
		$values = @()
		foreach ($cell in $row.SelectNodes("x:c", $ns))
		{
			$values += (($cell.SelectNodes(".//x:t", $ns) |
				ForEach-Object { $_.InnerText }) -join "")
		}
		if ($values.Count -lt 5)
		{
			continue
		}
		$result.Add([pscustomobject] @{
			SourceIndex = $sourceIndex++
			SetName = [string] $values[0]
			MainCategory = [string] $values[1]
			Subcategory = [string] $values[2]
			Slot = [string] $values[3]
			WorkbookItemName = [string] $values[4]
		})
	}
	return $result.ToArray()
}

$registry = @(Get-Content -LiteralPath $RegistryPath |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, DisplayName, Category, ConstantName)
$byName = @{}
$byConstant = @{}
foreach ($item in $registry)
{
	$nameKey = $item.DisplayName.ToLowerInvariant()
	if (!$byName.ContainsKey($nameKey)) { $byName[$nameKey] = @() }
	$byName[$nameKey] += $item
	if (!$byConstant.ContainsKey($item.ConstantName)) { $byConstant[$item.ConstantName] = @() }
	$byConstant[$item.ConstantName] += $item
}

function Is-CanonicalCandidate([object] $item)
{
	$constant = [string] $item.ConstantName
	return $constant -notmatch "^(BR_|PVPA_|LMS_|DEADMAN_|CERT_|POH_|SET_|PLACEHOLDER_|MAGICTRAINING_)" -and
		$constant -notmatch "(_LEGACY|_WORN|_DEGRADED)$"
}

function Resolve-Constant([string] $constant)
{
	if (!$byConstant.ContainsKey($constant)) { return $null }
	return @($byConstant[$constant] | Where-Object { Is-CanonicalCandidate $_ } |
		Sort-Object { [int] $_.ItemId })[0]
}

function Graceful-Constant([string] $setName, [string] $slot)
{
	$piece = switch ($slot)
	{
		"Head" { "HOOD" }; "Cape" { "CAPE" }; "Body" { "TOP" };
		"Legs" { "LEGS" }; "Hands" { "GLOVES" }; "Boots" { "BOOTS" }
		default { return $null }
	}
	$variant = switch -Regex ($setName)
	{
		"\(Base\)" { "BASE"; break }
		"\(Arceuus\)" { "ZEAH|ARCEUUS"; break }
		"\(Piscarilius\)" { "ZEAH|PISCARILIUS"; break }
		"\(Lovakengj\)" { "ZEAH|LOVAKENGJ"; break }
		"\(Shayzien\)" { "ZEAH|SHAYZIEN"; break }
		"\(Hosidius\)" { "ZEAH|HOSIDIUS"; break }
		"\(Kourend\)" { "ZEAH|KOUREND"; break }
		"\(Dark\)" { "GRACEFUL|HALLOWED"; break }
		"\(Trailblazer\)" { "GRACEFUL|TRAILBLAZER"; break }
		"\(Speedrunning\)" { "GRACEFUL|ADVENTURER"; break }
		"\(Brimhaven\)" { "GRACEFUL|WYRM"; break }
		default { return $null }
	}
	if ($variant -eq "BASE") { return "GRACEFUL_$piece" }
	$parts = $variant -split "\|"
	if ($parts[0] -eq "ZEAH") { return "ZEAH_GRACEFUL_${piece}_$($parts[1])" }
	return "GRACEFUL_${piece}_$($parts[1])"
}

function League-Constant([string] $setName, [string] $slot)
{
	if ($setName -notmatch "\(T([123])\)") { return $null }
	$tier = $Matches[1]
	$piece = switch ($slot)
	{
		"Head" { "HOOD" }; "Body" { "TOP" }; "Legs" { "LEGS" };
		"Boots" { "BOOTS" }; default { return $null }
	}
	if ($setName -like "Twisted*")
	{
		if ($piece -eq "HOOD") { $piece = "HAT" }
		return "TWISTED_RELIC_HUNTER_${piece}_T$tier"
	}
	if ($setName -like "Trailblazer Relic*")
	{
		return "TRAILBLAZER_RELIC_HUNTER_${piece}_T$tier"
	}
	if ($setName -like "Shattered*")
	{
		return "LEAGUE_3_RELIC_HUNTER_${piece}_T$tier"
	}
	if ($setName -like "Trailblazer Reloaded*")
	{
		if ($piece -eq "HOOD") { $piece = "HAT" }
		return "LEAGUE_4_RELIC_HUNTER_${piece}_T$tier"
	}
	if ($setName -like "Raging Echoes*")
	{
		if ($piece -eq "HOOD") { $piece = "HAT" }
		return "LEAGUE5_RELIC_HUNTER_${piece}_T$tier"
	}
	if ($setName -like "Demonic Pacts*")
	{
		return "LEAGUE_6_RELIC_HUNTER_${piece}_T$tier"
	}
	return $null
}

function Alias-Constant([object] $row)
{
	if ($row.SetName -like "Graceful*")
	{
		return Graceful-Constant $row.SetName $row.Slot
	}
	if ($row.MainCategory -eq "Leagues Cosmetics")
	{
		return League-Constant $row.SetName $row.Slot
	}
	if ($row.SetName -like "*Naval Set" -and $row.SetName -match "^(Black|Blue|Brown|Green|Grey|Purple|Red)")
	{
		$colour = $Matches[1].ToUpperInvariant()
		if ($row.Slot -eq "Head") { return "BREW_TRICORN_$colour" }
		if ($row.Slot -eq "Body") { return "BREW_UNIFORM_$colour" }
	}
	if ($row.SetName -eq "Black Wizard")
	{
		$result = switch ($row.Slot)
		{
			"Head" { "BLACKWIZHAT" }; "Body" { "BLACK_ROBE" };
			"Legs" { "BLACK_SKIRT" }; default { $null }
		}
		return $result
	}
	if ($row.SetName -eq "Blood Moon Set" -and $row.Slot -eq "Weapon")
	{
		return "DUAL_MACUAHUITL"
	}
	if ($row.SetName -eq "Zombie Outfit" -and $row.Slot -eq "Head")
	{
		return "ZOMBIE_HEAD"
	}
	return $null
}

function Domain([string] $mainCategory)
{
	$result = switch ($mainCategory)
	{
		"Combat" { "gear" }
		"GE Unpacked Sets" { "gear" }
		"Skilling" { "tools" }
		"Clothing Sets" { "cosmetics" }
		"Leagues Cosmetics" { "cosmetics" }
		default { throw "Unsupported main category: $mainCategory" }
	}
	return $result
}

function Set-Key([object] $row)
{
	if ($row.MainCategory -eq "GE Unpacked Sets" -and
		$row.SetName -match "^(Bronze|Iron|Steel|Black|Mithril|Adamant|Rune|Dragon) Armour Set")
	{
		return "gear.$($Matches[1].ToLowerInvariant())-armour"
	}
	return (Domain $row.MainCategory) + "." + (Normalize-Key $row.SetName)
}

function Catalog-SetName([object] $row)
{
	if ($row.MainCategory -eq "GE Unpacked Sets" -and
		$row.SetName -match "^(Bronze|Iron|Steel|Black|Mithril|Adamant|Rune|Dragon) Armour Set")
	{
		return "$($Matches[1]) Armour"
	}
	return $row.SetName
}

$audit = New-Object System.Collections.Generic.List[object]
foreach ($row in (Read-WorkbookRows $WorkbookPath))
{
	$alias = Alias-Constant $row
	$resolved = $null
	$resolution = ""
	$note = ""
	if (![string]::IsNullOrEmpty($alias))
	{
		$resolved = Resolve-Constant $alias
		$resolution = if ($null -eq $resolved) { "unresolved-alias" } else { "reviewed-alias" }
		$note = "Workbook alias resolved through RuneLite constant $alias."
	}
	else
	{
		$nameKey = $row.WorkbookItemName.ToLowerInvariant()
		$candidates = if ($byName.ContainsKey($nameKey))
			{ @($byName[$nameKey] | Where-Object { Is-CanonicalCandidate $_ }) } else { @() }
		$candidates = @($candidates)
		if ($candidates.Count -gt 0)
		{
			$resolved = @($candidates | Sort-Object { [int] $_.ItemId })[0]
			$resolution = if ($candidates.Count -eq 1) { "exact-name" } else { "canonical-exact-name" }
			$note = if ($candidates.Count -gt 1)
				{ "Selected lowest canonical bank item; excluded noted/internal/restricted variants." } else { "" }
		}
		else
		{
			$resolution = "unresolved"
			$note = "No reviewed alias or canonical exact-name match."
		}
	}

	$audit.Add([pscustomobject] @{
		SourceIndex = $row.SourceIndex
		Domain = Domain $row.MainCategory
		SetKey = Set-Key $row
		CatalogSetName = Catalog-SetName $row
		SetName = $row.SetName
		MainCategory = $row.MainCategory
		Subcategory = $row.Subcategory
		Slot = $row.Slot
		SlotRank = if ($slotRanks.ContainsKey($row.Slot)) { $slotRanks[$row.Slot] } else { 99 }
		WorkbookItemName = $row.WorkbookItemName
		ItemId = if ($null -eq $resolved) { "" } else { [int] $resolved.ItemId }
		RegistryDisplayName = if ($null -eq $resolved) { "" } else { $resolved.DisplayName }
		ConstantName = if ($null -eq $resolved) { "" } else { $resolved.ConstantName }
		Resolution = $resolution
		Note = $note
	})
}

$unresolved = @($audit | Where-Object { [string]::IsNullOrEmpty([string] $_.ItemId) })
if ($unresolved.Count -gt 0)
{
	$details = ($unresolved | ForEach-Object { "$($_.SetName)/$($_.WorkbookItemName)" }) -join "; "
	throw "Unresolved workbook rows: $details"
}

$auditDirectory = Split-Path -Parent $AuditPath
$catalogDirectory = Split-Path -Parent $CatalogPath
New-Item -ItemType Directory -Force -Path $auditDirectory | Out-Null
New-Item -ItemType Directory -Force -Path $catalogDirectory | Out-Null
$auditText = ($audit | ConvertTo-Csv -Delimiter "`t" -NoTypeInformation) -join [Environment]::NewLine
[System.IO.File]::WriteAllText($AuditPath, $auditText + [Environment]::NewLine, $utf8)

$catalogRows = @($audit |
	Where-Object { ![string]::IsNullOrEmpty([string] $_.ItemId) } |
	Sort-Object Domain, SetKey, SlotRank, SourceIndex |
	Group-Object { "$($_.Domain)|$($_.SetKey)|$($_.ItemId)" } |
	ForEach-Object { $_.Group[0] })

# Reviewed exact-ID supplements missing from the source workbook.
# H.A.M. uniform: https://oldschool.runescape.wiki/w/Ham_robe?oldid=14804087
# Silly jester costume: https://oldschool.runescape.wiki/w/Chest_(Jester_costume)
# Lunar equipment: https://oldschool.runescape.wiki/w/Lunar_equipment?oldid=14805285
$supplementalRows = @(
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 0; ItemId = 4302 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 1; ItemId = 4304 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 2; ItemId = 4306 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 5; ItemId = 4298 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 7; ItemId = 4300 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 8; ItemId = 4308 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ham-robes"; CatalogSetName = "H.A.M. Robes"; SlotRank = 9; ItemId = 4310 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.silly-jester"; CatalogSetName = "Silly Jester Costume"; SlotRank = 0; ItemId = 10836 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.silly-jester"; CatalogSetName = "Silly Jester Costume"; SlotRank = 5; ItemId = 10837 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.silly-jester"; CatalogSetName = "Silly Jester Costume"; SlotRank = 7; ItemId = 10838 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.silly-jester"; CatalogSetName = "Silly Jester Costume"; SlotRank = 9; ItemId = 10839 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 0; ItemId = 9096 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 1; ItemId = 9101 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 2; ItemId = 9102 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 4; ItemId = 9084 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 5; ItemId = 9097 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 7; ItemId = 9098 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 8; ItemId = 9099 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 9; ItemId = 9100 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.lunar-equipment"; CatalogSetName = "Lunar Equipment"; SlotRank = 10; ItemId = 9104 },

	# Additional complete families found during the 757-item blueprint review.
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 0; ItemId = 19687 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 1; ItemId = 19697 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 5; ItemId = 19689 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 7; ItemId = 19693 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 8; ItemId = 19691 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.clue-hunter"; CatalogSetName = "Clue Hunter Outfit"; SlotRank = 9; ItemId = 19695 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 0; ItemId = 6109 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 1; ItemId = 6111 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 5; ItemId = 6107 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 7; ItemId = 6108 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 8; ItemId = 6110 },
	[pscustomobject] @{ Domain = "gear"; SetKey = "gear.ghostly-robes"; CatalogSetName = "Ghostly Robes"; SlotRank = 9; ItemId = 6106 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ironman-armour"; CatalogSetName = "Ironman Armour"; SlotRank = 0; ItemId = 12810 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ironman-armour"; CatalogSetName = "Ironman Armour"; SlotRank = 5; ItemId = 12811 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.ironman-armour"; CatalogSetName = "Ironman Armour"; SlotRank = 7; ItemId = 12812 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 0; ItemId = 1506 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 1; ItemId = 6070 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 5; ItemId = 6065 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 7; ItemId = 6066 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 7; ItemId = 6067 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 8; ItemId = 6068 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.mourner-outfit"; CatalogSetName = "Mourner Outfit"; SlotRank = 9; ItemId = 6069 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.bomber-jacket"; CatalogSetName = "Bomber Jacket Costume"; SlotRank = 0; ItemId = 9945 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.bomber-jacket"; CatalogSetName = "Bomber Jacket Costume"; SlotRank = 0; ItemId = 9946 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.bomber-jacket"; CatalogSetName = "Bomber Jacket Costume"; SlotRank = 5; ItemId = 9944 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.black-desert"; CatalogSetName = "Black Desert Outfit"; SlotRank = 5; ItemId = 6750 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.black-desert"; CatalogSetName = "Black Desert Outfit"; SlotRank = 7; ItemId = 6752 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.plague-outfit"; CatalogSetName = "Plague Outfit"; SlotRank = 5; ItemId = 284 },
	[pscustomobject] @{ Domain = "cosmetics"; SetKey = "cosmetics.plague-outfit"; CatalogSetName = "Plague Outfit"; SlotRank = 7; ItemId = 285 }
)
$catalogRows = @($catalogRows + $supplementalRows |
	Sort-Object Domain, SetKey, SlotRank, ItemId |
	Group-Object { "$($_.Domain)|$($_.SetKey)|$($_.ItemId)" } |
	ForEach-Object { $_.Group[0] })
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("# schema=1")
$lines.Add("# Generated from the reviewed workbook plus exact-ID supplements by tools/research/gear-sets/resolve-master-gear-sets.ps1")
$lines.Add("# domain`tsetKey`tsetName`tslotRank`titemId")
foreach ($item in $catalogRows)
{
	$lines.Add("$($item.Domain)`t$($item.SetKey)`t$($item.CatalogSetName)`t$($item.SlotRank)`t$($item.ItemId)")
}
[System.IO.File]::WriteAllText($CatalogPath, ($lines -join [Environment]::NewLine) +
	[Environment]::NewLine, $utf8)

Write-Output "Workbook rows: $($audit.Count)"
Write-Output "Resolved rows: $(@($audit | Where-Object { ![string]::IsNullOrEmpty([string] $_.ItemId) }).Count)"
Write-Output "Catalog rows: $($catalogRows.Count)"
Write-Output "Catalog sets: $(@($catalogRows.SetKey | Sort-Object -Unique).Count)"
Write-Output "Audit: $AuditPath"
Write-Output "Catalog: $CatalogPath"
