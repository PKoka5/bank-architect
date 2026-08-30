param(
	[string] $OutputPath = "src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/wiki-item-categories.tsv",
	[string] $UserAgent = "BankArchitectResearch/1.0 (offline category snapshot)",
	[int] $PageSize = 5000,
	[double] $RequestIntervalSeconds = 1.0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Item category membership straight from the OSRS Wiki, baked into a resource at
# development time so the plugin itself never makes a network call. The wiki
# category names live here and their meaning for the bank lives in
# WikiItemCategories.java, so re-running this script refreshes facts without
# silently changing any classification decision.
$wikiApi = "https://oldschool.runescape.wiki/api.php"

$categories = @(
	"Runes",
	"Teleportation items",
	"Clue scrolls",
	"Potions",
	"Herbs",
	"Seeds",
	"Saplings",
	"Herblore",
	"Currency",
	"Tools",
	"Skilling equipment",
	"Ores",
	"Metal bars",
	"Logs",
	"Gems",
	"Bones",
	"Leather"
)

$lastRequest = [datetime]::MinValue

function Invoke-WikiRequest([string] $uri)
{
	$wait = $RequestIntervalSeconds - ([datetime]::UtcNow - $script:lastRequest).TotalSeconds
	if ($wait -gt 0)
	{
		Start-Sleep -Milliseconds ([int] ($wait * 1000))
	}

	$script:lastRequest = [datetime]::UtcNow
	return Invoke-RestMethod -Uri $uri -Headers @{ "User-Agent" = $UserAgent } -TimeoutSec 120
}

function Read-CategoryPage([int] $offset)
{
	$fields = @("'page_name'", "'item_id'", "'item_name'")
	foreach ($category in $categories)
	{
		$fields += "'Category:$category'"
	}

	$query = "bucket('infobox_item').select($($fields -join ',')).orderBy('page_name','asc')" +
		".limit($PageSize).offset($offset).run()"
	$uri = $wikiApi + "?action=bucket&format=json&formatversion=2&query=" +
		[uri]::EscapeDataString($query)
	$response = Invoke-WikiRequest $uri
	if ($null -ne $response.PSObject.Properties['error'])
	{
		throw "Wiki bucket query failed: $($response.error)"
	}

	return @($response.bucket)
}

$membership = @{}
$names = @{}
$offset = 0
while ($true)
{
	$page = @(Read-CategoryPage $offset)
	Write-Host "fetched $($page.Count) rows at offset $offset"
	foreach ($row in $page)
	{
		$present = @()
		foreach ($category in $categories)
		{
			$property = $row.PSObject.Properties["Category:$category"]
			if ($null -ne $property -and $property.Value -eq $true)
			{
				$present += $category
			}
		}

		if ($present.Count -eq 0)
		{
			continue
		}

		$rawIds = @()
		$idProperty = $row.PSObject.Properties['item_id']
		if ($null -ne $idProperty -and $null -ne $idProperty.Value)
		{
			$rawIds = @($idProperty.Value)
		}

		foreach ($rawId in $rawIds)
		{
			$itemId = 0
			if (-not [int]::TryParse([string] $rawId, [ref] $itemId) -or $itemId -le 0)
			{
				continue
			}

			# A page that lists several item IDs shares its categories with all of
			# them, and two pages can claim the same ID; keep the union either way.
			if (-not $membership.ContainsKey($itemId))
			{
				$membership[$itemId] = New-Object 'System.Collections.Generic.HashSet[string]'
				$names[$itemId] = [string] $row.item_name
			}

			foreach ($category in $present)
			{
				[void] $membership[$itemId].Add($category)
			}
		}
	}

	if ($page.Count -lt $PageSize)
	{
		break
	}

	$offset += $PageSize
}

$retrievedOn = [datetime]::UtcNow.ToString("yyyy-MM-dd")
$lines = New-Object 'System.Collections.Generic.List[string]'
[void] $lines.Add("# Item category membership from the Old School RuneScape Wiki")
[void] $lines.Add("# (oldschool.runescape.wiki, CC BY-NC-SA 3.0), retrieved $retrievedOn via")
[void] $lines.Add("# $wikiApi`?action=bucket on bucket infobox_item.")
[void] $lines.Add("# Regenerate with tools/fetch-wiki-item-categories.ps1. Columns: item id, item name,")
[void] $lines.Add("# comma-separated wiki categories.")
foreach ($itemId in ($membership.Keys | Sort-Object))
{
	$ordered = @($categories | Where-Object { $membership[$itemId].Contains($_) })
	[void] $lines.Add("$itemId`t$($names[$itemId])`t$($ordered -join ',')")
}

$directory = Split-Path -Parent $OutputPath
if ($directory -and -not (Test-Path -LiteralPath $directory))
{
	New-Item -ItemType Directory -Path $directory | Out-Null
}

Set-Content -LiteralPath $OutputPath -Value $lines -Encoding utf8
Write-Host "wrote $($membership.Count) items to $OutputPath"
