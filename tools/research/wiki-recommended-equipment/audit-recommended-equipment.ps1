param(
	[string] $CacheDir = "tools/research/wiki-recommended-equipment/cache",
	[string] $RegistryPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv",
	[string] $UserAgent = "BankArchitectResearch/1.0 (offline classification audit)",
	[switch] $Refresh
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$apiUrl = "https://oldschool.runescape.wiki/api.php"
$recommendedPath = Join-Path $CacheDir "recommended-equipment.bucket.json"
$infoboxPath = Join-Path $CacheDir "infobox-item.bucket.json"
$outputPath = Join-Path $CacheDir "recommended-equipment-audit.json"

if (!(Test-Path -LiteralPath $RegistryPath))
{
	throw "Registry not found: $RegistryPath"
}
New-Item -ItemType Directory -Force -Path $CacheDir | Out-Null

function Invoke-BucketQuery([string] $query)
{
	$uri = $apiUrl + "?action=bucket&format=json&formatversion=2&query=" +
		[uri]::EscapeDataString($query)
	$response = Invoke-RestMethod -Uri $uri -Headers @{ "User-Agent" = $UserAgent } -TimeoutSec 90
	if ($response.PSObject.Properties.Name -contains "error")
	{
		throw "Bucket query failed: $($response.error)"
	}
	return @($response.bucket)
}

function Read-Bucket([string] $bucketName, [string[]] $fields, [string] $path)
{
	if (!$Refresh -and (Test-Path -LiteralPath $path))
	{
		return @(Get-Content -LiteralPath $path -Raw | ConvertFrom-Json)
	}

	$rows = New-Object System.Collections.Generic.List[object]
	$offset = 0
	$pageSize = 5000
	$selection = ($fields | ForEach-Object { "'$_'" }) -join ","
	do
	{
		$query = "bucket('$bucketName').select($selection).orderBy('page_name','asc').limit($pageSize).offset($offset).run()"
		$page = @(Invoke-BucketQuery $query)
		foreach ($row in $page)
		{
			$rows.Add($row)
		}
		$offset += $page.Count
	}
	while ($page.Count -eq $pageSize)

	$rows | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $path -Encoding UTF8
	return $rows.ToArray()
}

function Normalize([string] $value)
{
	if ($null -eq $value)
	{
		return ""
	}
	return $value.Trim().ToLowerInvariant()
}

function Context-Kind([string] $pageName, [string] $style, [string[]] $slots)
{
	$text = (Normalize ($pageName + " " + $style))
	$nonCombat = "agility|mining|woodcut|fishing|runecraft|thiev|hunter|farming|wintertodt|tempoross|guardians of the rift|blast mine|motherlode|sepulchre|sorceress|trawler|zalcano|graceful"
	$combat = "melee|ranged|range |magic|mage|tank|crush|slash|stab|strength|combat|dps|hybrid|tribrid|phase [0-9]|slayer|boss|inferno|fight caves|colosseum|chambers of xeric|theatre of blood|tombs of amascut"
	if ($text -match $nonCombat)
	{
		return "noncombat-like"
	}
	if (($slots -contains "special") -or $text -match $combat)
	{
		return "combat-like"
	}
	return "ambiguous"
}

$recommendedRows = @(Read-Bucket "recommended_equipment" @("page_name", "json") $recommendedPath |
	ForEach-Object { $_ })
$infoboxRows = @(Read-Bucket "infobox_item" @(
	"page_name", "page_name_sub", "item_name", "item_id", "default_version") $infoboxPath |
	ForEach-Object { $_ })

$registryById = @{}
Get-Content -LiteralPath $RegistryPath |
	ConvertFrom-Csv -Delimiter "`t" -Header ItemId, Name, Category, ConstantName |
	ForEach-Object { $registryById[[int] $_.ItemId] = $_ }

$infoboxByPage = @{}
$infoboxByName = @{}
foreach ($row in $infoboxRows)
{
	$rawIds = if ($row.PSObject.Properties.Name -contains "item_id") { @($row.item_id) } else { @() }
	$ids = @($rawIds | ForEach-Object {
		$value = 0
		if ([int]::TryParse([string] $_, [ref] $value) -and $value -gt 0) { $value }
	})
	if ($ids.Count -eq 0)
	{
		continue
	}
	$fact = [pscustomobject] @{
		PageName = [string] $row.page_name
		ItemName = if ($row.PSObject.Properties.Name -contains "item_name")
			{ [string] $row.item_name } else { [string] $row.page_name }
		ItemIds = $ids
		DefaultVersion = ($row.PSObject.Properties.Name -contains "default_version") -and
			[bool] $row.default_version
	}
	$pageKey = Normalize $fact.PageName
	$nameKey = Normalize $fact.ItemName
	if (!$infoboxByPage.ContainsKey($pageKey)) { $infoboxByPage[$pageKey] = @() }
	if (!$infoboxByName.ContainsKey($nameKey)) { $infoboxByName[$nameKey] = @() }
	$infoboxByPage[$pageKey] += $fact
	$infoboxByName[$nameKey] += $fact
}

function Resolve-Link([string] $link)
{
	$title = ($link -split "#", 2)[0].Trim()
	$key = Normalize $title
	$facts = @()
	if ($infoboxByPage.ContainsKey($key))
	{
		$facts = @($infoboxByPage[$key])
	}
	elseif ($infoboxByName.ContainsKey($key))
	{
		$facts = @($infoboxByName[$key])
	}
	if ($facts.Count -eq 0)
	{
		return @()
	}
	$defaults = @($facts | Where-Object { $_.DefaultVersion })
	if ($defaults.Count -gt 0)
	{
		$facts = $defaults
	}
	return @($facts | ForEach-Object { $_.ItemIds } | Sort-Object -Unique)
}

$occurrences = New-Object System.Collections.Generic.List[object]
$unresolved = @{}
$linkPattern = [regex] '\|link=([^\]]+)\]\]'
foreach ($row in $recommendedRows)
{
	$data = ConvertFrom-Json -InputObject ([string] $row.json)
	$equipment = $data.'Recommended Equipment'
	if ($null -eq $equipment)
	{
		continue
	}
	$slots = @($equipment.PSObject.Properties.Name)
	$style = if ($data.PSObject.Properties.Name -contains "style") { [string] $data.style } else { "" }
	$contextKind = Context-Kind ([string] $row.page_name) $style $slots
	foreach ($slotProperty in $equipment.PSObject.Properties)
	{
		$cells = @($slotProperty.Value)
		for ($rankIndex = 0; $rankIndex -lt $cells.Count; $rankIndex++)
		{
			$links = @($linkPattern.Matches([string] $cells[$rankIndex]) |
				ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
			foreach ($link in $links)
			{
				$itemIds = @(Resolve-Link $link)
				if ($itemIds.Count -eq 0)
				{
					$key = Normalize $link
					if (!$unresolved.ContainsKey($key))
					{
						$unresolved[$key] = [ordered] @{ Link = $link; Occurrences = 0 }
					}
					$unresolved[$key].Occurrences++
					continue
				}
				foreach ($itemId in $itemIds)
				{
					$occurrences.Add([pscustomobject] @{
						ItemId = [int] $itemId
						PageName = [string] $row.page_name
						Style = $style
						Slot = [string] $slotProperty.Name
						Rank = $rankIndex + 1
						ContextKind = $contextKind
					})
				}
			}
		}
	}
}

$items = @($occurrences | Group-Object ItemId | ForEach-Object {
	$itemId = [int] $_.Name
	$group = @($_.Group)
	$registry = if ($registryById.ContainsKey($itemId)) { $registryById[$itemId] } else { $null }
	[pscustomobject] @{
		ItemId = $itemId
		Name = if ($null -ne $registry) { [string] $registry.Name } else { "" }
		RegistryCategory = if ($null -ne $registry) { [string] $registry.Category } else { "MISSING" }
		RecommendationOccurrences = $group.Count
		DistinctPages = @($group.PageName | Sort-Object -Unique).Count
		BestRank = ($group.Rank | Measure-Object -Minimum).Minimum
		CombatLikePages = @($group | Where-Object ContextKind -eq "combat-like" |
			Select-Object -ExpandProperty PageName -Unique).Count
		NonCombatLikePages = @($group | Where-Object ContextKind -eq "noncombat-like" |
			Select-Object -ExpandProperty PageName -Unique).Count
		AmbiguousPages = @($group | Where-Object ContextKind -eq "ambiguous" |
			Select-Object -ExpandProperty PageName -Unique).Count
		Slots = @($group.Slot | Sort-Object -Unique)
	}
} | Sort-Object @{Expression="CombatLikePages"; Descending=$true},
	@{Expression="DistinctPages"; Descending=$true}, ItemId)

$result = [ordered] @{
	SchemaVersion = 1
	GeneratedAtUtc = [DateTime]::UtcNow.ToString("o")
	Source = "OSRS Wiki public Bucket API"
	SourceApi = $apiUrl
	RecommendedEquipmentRows = $recommendedRows.Count
	InfoboxItemRows = $infoboxRows.Count
	ResolvedOccurrences = $occurrences.Count
	RecommendedItemIds = $items.Count
	RegistryMatchedItemIds = @($items | Where-Object RegistryCategory -ne "MISSING").Count
	UnresolvedLinkCount = $unresolved.Count
	Items = $items
	UnresolvedLinks = @($unresolved.Values | Sort-Object @{Expression="Occurrences"; Descending=$true}, Link)
}
$result | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath $outputPath -Encoding UTF8

Write-Output "Recommended-equipment rows: $($recommendedRows.Count)"
Write-Output "Infobox-item rows: $($infoboxRows.Count)"
Write-Output "Resolved occurrences: $($occurrences.Count)"
Write-Output "Unique recommended item IDs: $($items.Count)"
Write-Output "Registry matches: $($result.RegistryMatchedItemIds)/$($items.Count)"
Write-Output "Unresolved link targets: $($unresolved.Count)"
Write-Output "Audit output: $outputPath"
