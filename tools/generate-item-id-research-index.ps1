param(
	[string] $SourceJar = "",
	[string] $OutputDir = "docs/research"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Find-LatestRuneLiteApiSourcesJar
{
	$root = Join-Path $env:USERPROFILE ".gradle\caches\modules-2\files-2.1\net.runelite\runelite-api"
	if (!(Test-Path -LiteralPath $root))
	{
		throw "RuneLite API Gradle cache not found: $root"
	}

	$candidates = Get-ChildItem -LiteralPath $root -Recurse -Filter "runelite-api-*-sources.jar" |
		Sort-Object LastWriteTimeUtc -Descending
	if ($candidates.Count -eq 0)
	{
		throw "No runelite-api sources jar found under: $root"
	}

	return $candidates[0].FullName
}

function Read-ZipEntryText([string] $jarPath, [string] $entryName)
{
	Add-Type -AssemblyName System.IO.Compression.FileSystem
	$zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
	try
	{
		$entry = $zip.GetEntry($entryName)
		if ($null -eq $entry)
		{
			throw "Entry not found in jar: $entryName"
		}

		$reader = New-Object IO.StreamReader($entry.Open())
		try
		{
			return $reader.ReadToEnd()
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
}

function Normalize-Text([string] $value)
{
	if ([string]::IsNullOrWhiteSpace($value))
	{
		return ""
	}

	return (($value -replace "\s+", " ").Trim())
}

function Name-To-Label([string] $constantName)
{
	$text = $constantName.ToLowerInvariant() -replace "_", " "
	return (Get-Culture).TextInfo.ToTitleCase($text)
}

function Has-Pattern([string] $text, [string] $pattern)
{
	return $text -match $pattern
}

function Classify-Entry([int] $itemId, [string] $namespace, [string] $constantName, [string] $displayName)
{
	$name = $constantName.ToLowerInvariant()
	$label = $displayName.ToLowerInvariant()
	$combined = "$name $label"
	$tags = New-Object System.Collections.Generic.List[string]
	$flags = New-Object System.Collections.Generic.List[string]

	if ($namespace -ne "TOP_LEVEL")
	{
		$flags.Add("exclude-main-catalog")
		$flags.Add($namespace.ToLowerInvariant())
	}
	if ($itemId -le 0)
	{
		$flags.Add("exclude-main-catalog")
		$flags.Add("non-positive-id")
	}
	if (Has-Pattern $constantName "(^|_)CERT($|_)|(^|_)NOTED($|_)|(^|_)NOTE($|_)")
	{
		$flags.Add("exclude-main-catalog")
		$flags.Add("cert-or-noted-name")
	}
	if (Has-Pattern $constantName "PLACEHOLDER|PLACE_HOLDER")
	{
		$flags.Add("exclude-main-catalog")
		$flags.Add("placeholder-name")
	}
	if (Has-Pattern $constantName "^NULL|_NULL|DUMMY|UNUSED|TEMPLATE|_OBJ($|_)|OBJ_")
	{
		$flags.Add("review-before-main-catalog")
		$flags.Add("internal-looking-name")
	}
	if ([string]::IsNullOrWhiteSpace($displayName))
	{
		$flags.Add("missing-direct-javadoc")
	}

	$category = "UNKNOWN"
	$confidence = "LOW"

	if (Has-Pattern $combined "\brune\b|rune$|_rune|rune_")
	{
		$category = "RUNE"
		$confidence = "HIGH"
		$tags.Add("rune")
	}
	elseif (Has-Pattern $combined "teleport|teletab|tablet|portal|fairy ring|jewellery box")
	{
		$category = "TELEPORT"
		$confidence = "MEDIUM"
		$tags.Add("teleport")
	}
	elseif (Has-Pattern $combined "potion|\([1-4]\)|dose|flask|brew|restore|antipoison|antifire|stamina|super attack|super strength|super defence|ranging potion|magic potion")
	{
		$category = "POTION"
		$confidence = "MEDIUM"
		$tags.Add("potion")
		if (Has-Pattern $combined "\([1-4]\)|dose")
		{
			$tags.Add("dose")
		}
	}
	elseif (Has-Pattern $combined "grimy|clean herb| herb\b|herb |leaf|unf|unfinished|secondary|eye of newt|snape grass|limpwurt|red spiders")
	{
		$category = "HERBLORE"
		$confidence = "MEDIUM"
		$tags.Add("herblore")
	}
	elseif (Has-Pattern $combined "seed|sapling|compost|allotment|tree patch|fruit tree|herb seed")
	{
		$category = "FARMING"
		$confidence = "MEDIUM"
		$tags.Add("farming")
	}
	elseif (Has-Pattern $combined "coins|tokkul|nugget|mark of grace|stardust|numulite|trading sticks|ticket|points|token")
	{
		$category = "CURRENCY"
		$confidence = "MEDIUM"
		$tags.Add("currency")
	}
	elseif (Has-Pattern $combined "helm|helmet|body|platebody|legs|platelegs|skirt|boots|gloves|shield|defender|sword|scimitar|mace|dagger|bow|crossbow|staff|wand|cape|amulet|ring|bracelet|chaps|coif|robe|armour|armor")
	{
		$category = "GEAR"
		$confidence = "LOW"
		$tags.Add("gear")
	}
	elseif (Has-Pattern $combined "ore|bar|logs|plank|fish|raw |hide|leather|gem|uncut|essence|clay|bone|ashes|feather|wool|flax|bow string|pickaxe|axe|harpoon|net|chisel|hammer")
	{
		$category = "SKILLING"
		$confidence = "LOW"
		$tags.Add("skilling")
	}
	elseif (Has-Pattern $combined "burnt|broken|empty|vial|jug|bucket|bowl|pot|junk|ashes")
	{
		$category = "CLEANUP"
		$confidence = "LOW"
		$tags.Add("cleanup")
	}

	if ($flags.Contains("exclude-main-catalog") -and $confidence -eq "HIGH")
	{
		$confidence = "MEDIUM"
	}

	return @{
		Category = $category
		Confidence = $confidence
		Flags = @($flags | Select-Object -Unique)
		Tags = @($tags | Select-Object -Unique)
	}
}

function Escape-Tsv([string] $value)
{
	if ($null -eq $value)
	{
		return ""
	}

	return (($value -replace "`t", " ") -replace "`r?`n", " ")
}

function Parse-SegmentConstants([string] $namespace, [string] $text)
{
	$parsed = New-Object System.Collections.Generic.List[object]
	$inDoc = $false
	$docLines = New-Object System.Collections.Generic.List[string]
	$lines = $text -split "`r?`n"

	foreach ($line in $lines)
	{
		if ($line -match "^\s*/\*\*")
		{
			$inDoc = $true
			$docLines.Clear()
			continue
		}

		if ($inDoc)
		{
			if ($line -match "^\s*\*/")
			{
				$inDoc = $false
				continue
			}

			if ($line -match "^\s*\*\s?(?<doc>.*)$")
			{
				$part = $Matches["doc"]
				if (![string]::IsNullOrWhiteSpace($part) -and $part -notmatch "^@")
				{
					$docLines.Add($part.Trim())
				}
			}
			continue
		}

		if ($line -match "^\s*public static final int\s+(?<name>[A-Z0-9_]+)\s*=\s*(?<id>-?\d+)\s*;")
		{
			$parsed.Add([pscustomobject] @{
				Namespace = $namespace
				Name = $Matches["name"]
				Id = [int] $Matches["id"]
				Doc = Normalize-Text ([string]::Join(" ", $docLines))
			})
			$docLines.Clear()
		}
	}

	return $parsed
}

if ([string]::IsNullOrWhiteSpace($SourceJar))
{
	$SourceJar = Find-LatestRuneLiteApiSourcesJar
}

$entryName = "net/runelite/api/gameval/ItemID.java"
$sourceText = Read-ZipEntryText $SourceJar $entryName

$certIndex = $sourceText.IndexOf("public static final class Cert")
$placeholderIndex = $sourceText.IndexOf("public static final class Placeholder")
if ($certIndex -lt 0 -or $placeholderIndex -lt 0)
{
	throw "Could not find Cert and Placeholder namespaces in $entryName"
}

$segments = @(
	@{ Namespace = "TOP_LEVEL"; Text = $sourceText.Substring(0, $certIndex) },
	@{ Namespace = "CERT"; Text = $sourceText.Substring($certIndex, $placeholderIndex - $certIndex) },
	@{ Namespace = "PLACEHOLDER"; Text = $sourceText.Substring($placeholderIndex) }
)

$rows = New-Object System.Collections.Generic.List[object]
foreach ($segment in $segments)
{
	$namespace = $segment.Namespace
	$text = $segment.Text
	$matches = Parse-SegmentConstants $namespace $text

	foreach ($match in $matches)
	{
		$constantName = $match.Name
		$itemId = $match.Id
		$displayName = $match.Doc
		$inferredLabel = Name-To-Label $constantName
		$classification = Classify-Entry $itemId $namespace $constantName $displayName

		$rows.Add([pscustomobject] @{
			ItemId = $itemId
			Namespace = $namespace
			ConstantName = $constantName
			DisplayName = $displayName
			InferredLabel = $inferredLabel
			Category = $classification.Category
			Confidence = $classification.Confidence
			Flags = [string]::Join(",", $classification.Flags)
			Tags = [string]::Join(",", $classification.Tags)
		})
	}
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$indexPath = Join-Path $OutputDir "item-id-research-index.tsv"
$summaryPath = Join-Path $OutputDir "item-id-research-summary.md"

$header = "item_id	namespace	constant_name	display_name	inferred_label	category	confidence	flags	tags"
$lines = New-Object System.Collections.Generic.List[string]
$lines.Add($header)
foreach ($row in ($rows | Sort-Object ItemId, Namespace, ConstantName))
{
	$lines.Add([string]::Join("`t", @(
		$row.ItemId,
		(Escape-Tsv $row.Namespace),
		(Escape-Tsv $row.ConstantName),
		(Escape-Tsv $row.DisplayName),
		(Escape-Tsv $row.InferredLabel),
		(Escape-Tsv $row.Category),
		(Escape-Tsv $row.Confidence),
		(Escape-Tsv $row.Flags),
		(Escape-Tsv $row.Tags)
	)))
}

Set-Content -LiteralPath $indexPath -Value $lines -Encoding UTF8

$namespaceCounts = $rows | Group-Object Namespace | Sort-Object Name
$categoryCounts = $rows | Group-Object Category | Sort-Object Name
$confidenceCounts = $rows | Group-Object Confidence | Sort-Object Name
$excludedCount = ($rows | Where-Object { $_.Flags -like "*exclude-main-catalog*" }).Count
$directDocCount = ($rows | Where-Object { ![string]::IsNullOrWhiteSpace($_.DisplayName) }).Count

$summary = New-Object System.Collections.Generic.List[string]
$summary.Add("# Item ID Research Index")
$summary.Add("")
$summary.Add("Generated from local RuneLite source cache only.")
$summary.Add("")
$summary.Add("- Source jar: ``$SourceJar``")
$summary.Add("- Source entry: ``$entryName``")
$summary.Add("- Total rows: $($rows.Count)")
$summary.Add("- Rows with direct Javadoc/display names: $directDocCount")
$summary.Add("- Rows flagged `exclude-main-catalog`: $excludedCount")
$summary.Add("")
$summary.Add("## Namespace Counts")
$summary.Add("")
foreach ($group in $namespaceCounts)
{
	$summary.Add("- $($group.Name): $($group.Count)")
}
$summary.Add("")
$summary.Add("## Category Counts")
$summary.Add("")
foreach ($group in $categoryCounts)
{
	$summary.Add("- $($group.Name): $($group.Count)")
}
$summary.Add("")
$summary.Add("## Confidence Counts")
$summary.Add("")
foreach ($group in $confidenceCounts)
{
	$summary.Add("- $($group.Name): $($group.Count)")
}
$summary.Add("")
$summary.Add("## Production Catalog Rule")
$summary.Add("")
$summary.Add("This file is a research index, not the production catalog. Production `StaticItemCatalog` should stay curated. Items flagged `exclude-main-catalog` should not be added to the main organizer catalog unless explicitly reviewed.")

Set-Content -LiteralPath $summaryPath -Value $summary -Encoding UTF8

Write-Output "Wrote $indexPath"
Write-Output "Wrote $summaryPath"
Write-Output "Rows: $($rows.Count)"
