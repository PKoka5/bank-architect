param(
	[string] $BaseUrl = "https://exchange-insights.gg",
	[string] $CacheDir = "tools/research/community-templates/cache",
	[int] $DelayMilliseconds = 1000,
	[switch] $RefreshPages
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if ($DelayMilliseconds -lt 500)
{
	throw "DelayMilliseconds must be at least 500ms"
}

Add-Type -AssemblyName System.Net.Http

$utf8 = New-Object System.Text.UTF8Encoding($false)
$client = [System.Net.Http.HttpClient]::new()
$client.Timeout = [TimeSpan]::FromSeconds(30)
$client.DefaultRequestHeaders.UserAgent.ParseAdd("IronmanBankArchitect-Research/0.1 (public-page coverage audit)")
$client.DefaultRequestHeaders.Accept.ParseAdd("text/html")

$pagesDir = Join-Path $CacheDir "pages"
$normalizedDir = Join-Path $CacheDir "normalized"
New-Item -ItemType Directory -Force -Path $pagesDir, $normalizedDir | Out-Null

$script:lastRequestAt = $null

function Get-PublicText([string] $uri, [string] $cachePath, [bool] $refresh)
{
	if (!$refresh -and (Test-Path -LiteralPath $cachePath))
	{
		return [System.IO.File]::ReadAllText((Resolve-Path -LiteralPath $cachePath), $utf8)
	}

	if ($null -ne $script:lastRequestAt)
	{
		$elapsed = ([DateTimeOffset]::UtcNow - $script:lastRequestAt).TotalMilliseconds
		$remaining = $DelayMilliseconds - [int] $elapsed
		if ($remaining -gt 0)
		{
			Start-Sleep -Milliseconds $remaining
		}
	}

	$response = $client.GetAsync($uri).GetAwaiter().GetResult()
	$script:lastRequestAt = [DateTimeOffset]::UtcNow
	if (!$response.IsSuccessStatusCode)
	{
		throw "GET $uri returned HTTP $([int] $response.StatusCode)"
	}

	$text = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
	[System.IO.File]::WriteAllText($cachePath, $text, $utf8)
	return $text
}

function Decode-Html([string] $value)
{
	if ($null -eq $value)
	{
		return ""
	}

	$withoutTags = [regex]::Replace($value, "<[^>]+>", "")
	return [System.Net.WebUtility]::HtmlDecode($withoutTags).Trim()
}

function Match-Required([string] $value, [string] $pattern, [string] $field)
{
	$match = [regex]::Match($value, $pattern, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
	if (!$match.Success)
	{
		throw "Required field '$field' was not present in the public SSR page"
	}

	return $match
}

try
{
	$robotsPath = Join-Path $CacheDir "robots.txt"
	$robots = Get-PublicText "$BaseUrl/robots.txt" $robotsPath $true
	if ($robots -notmatch "(?m)^Allow:\s*/\s*$" -or
		$robots -notmatch "(?m)^Disallow:\s*/api/\s*$" -or
		$robots -notmatch "(?m)^Disallow:\s*/tools/partials/\s*$")
	{
		throw "robots.txt no longer matches the reviewed access boundary; inspect it manually before continuing"
	}

	$sitemapPath = Join-Path $CacheDir "sitemap.xml"
	$sitemap = Get-PublicText "$BaseUrl/sitemap.xml" $sitemapPath $true
	$urlPattern = [regex]::Escape("$BaseUrl/tools/osrs-bank-templates/") + "(?<id>\d+)/(?<slug>[^<]+)"
	$urlMatches = [regex]::Matches($sitemap, $urlPattern)
	$templateRefs = @($urlMatches | ForEach-Object {
		[pscustomobject] @{
			Id = [int] $_.Groups["id"].Value
			Slug = [System.Net.WebUtility]::HtmlDecode($_.Groups["slug"].Value)
			Url = $_.Value
		}
	} | Sort-Object Id -Unique)

	$templates = New-Object System.Collections.Generic.List[object]
	$failures = New-Object System.Collections.Generic.List[object]
	$processed = 0

	foreach ($reference in $templateRefs)
	{
		$processed++
		$pagePath = Join-Path $pagesDir ("{0}-{1}.html" -f $reference.Id, $reference.Slug)
		try
		{
			$html = Get-PublicText $reference.Url $pagePath ([bool] $RefreshPages)
			$nameMatch = Match-Required $html '<article class="bt-ssr">\s*<h1>(?<value>.*?)</h1>' "name"
			$metaMatch = Match-Required $html '<p class="bt-ssr-meta">.*?by\s*<strong>(?<author>.*?)</strong>\s*\u00B7\s*(?<items>\d+) items\s*\u00B7\s*(?<tabs>\d+) tab(?:s)?\s*\u00B7\s*imported (?<imports>[\d,]+) time(?:s)?\s*\u00B7\s*shared (?<shared>\d{4}-\d{2}-\d{2})</p>' "metadata"

			$visibleTabs = @()
			$tabPattern = '<section class="bt-ssr-tab"><h3>Tab (?<index>\d+) \((?<count>\d+) items\)</h3><p>(?<summary>.*?)</p></section>'
			foreach ($tabMatch in [regex]::Matches($html, $tabPattern, [System.Text.RegularExpressions.RegexOptions]::IgnoreCase))
			{
				$summary = Decode-Html $tabMatch.Groups["summary"].Value
				$visibleTabs += [pscustomobject] @{
					TabIndex = [int] $tabMatch.Groups["index"].Value
					DeclaredItemCount = [int] $tabMatch.Groups["count"].Value
					PublicSummary = $summary
					IsTruncated = $summary -match "\band \d+ more\.$"
				}
			}

			$templates.Add([pscustomobject] @{
				TemplateId = $reference.Id
				TemplateUrl = $reference.Url
				Slug = $reference.Slug
				Name = Decode-Html $nameMatch.Groups["value"].Value
				Author = Decode-Html $metaMatch.Groups["author"].Value
				TotalItemCount = [int] $metaMatch.Groups["items"].Value
				TabCount = [int] $metaMatch.Groups["tabs"].Value
				TotalImports = [int] ($metaMatch.Groups["imports"].Value -replace ",", "")
				SharedAt = $metaMatch.Groups["shared"].Value
				VisibleTabSummaries = $visibleTabs
				ExactPositionsAvailable = $false
				ParseStatus = "partial-public-ssr"
			})
		}
		catch
		{
			$failures.Add([pscustomobject] @{
				TemplateId = $reference.Id
				TemplateUrl = $reference.Url
				Error = $_.Exception.Message
			})
		}

		if (($processed % 20) -eq 0 -or $processed -eq $templateRefs.Count)
		{
			Write-Host ("Processed {0}/{1} public template pages" -f $processed, $templateRefs.Count)
		}
	}

	$uniqueTemplateIds = @($templateRefs | Select-Object -ExpandProperty Id -Unique)
	$declaredTabs = 0
	$declaredItemPlacements = 0
	$publicTabSummaries = 0
	$templatesWithoutTabSummaries = 0
	$maxPublicTabSummaries = 0
	foreach ($template in $templates)
	{
		$declaredTabs += $template.TabCount
		$declaredItemPlacements += $template.TotalItemCount
		$summaryCount = @($template.VisibleTabSummaries).Count
		$publicTabSummaries += $summaryCount
		if ($summaryCount -eq 0)
		{
			$templatesWithoutTabSummaries++
		}
		$maxPublicTabSummaries = [Math]::Max($maxPublicTabSummaries, $summaryCount)
	}

	$coverage = [ordered] @{
		ExtractionTimestampUtc = [DateTimeOffset]::UtcNow.ToString("o")
		Source = "$BaseUrl/tools/osrs-bank-templates"
		RobotsPolicy = "Public pages and sitemap allowed; /api/ and /tools/partials/ excluded"
		DiscoveredTemplates = $templateRefs.Count
		SuccessfullyDownloaded = $templates.Count
		PartiallyParsed = @($templates | Where-Object { $_.ParseStatus -eq "partial-public-ssr" }).Count
		Failed = $failures.Count
		DuplicateTemplateIds = $templateRefs.Count - $uniqueTemplateIds.Count
		DeclaredTabs = $declaredTabs
		DeclaredItemPlacements = $declaredItemPlacements
		PublicTabSummaries = $publicTabSummaries
		TemplatesWithoutTabSummaries = $templatesWithoutTabSummaries
		MaxPublicTabSummariesPerTemplate = $maxPublicTabSummaries
		TemplatesWithExactPositions = 0
		TemplatesMissingExactPositions = $templates.Count
		Limitation = "Allowed SSR pages expose metadata and a variable, incomplete set of truncated tab summaries, not complete item IDs or positions. No excluded endpoint was accessed."
	}

	[System.IO.File]::WriteAllText((Join-Path $normalizedDir "templates-summary.json"),
		($templates | ConvertTo-Json -Depth 8), $utf8)
	$failuresJson = if ($failures.Count -eq 0) { "[]" } else { $failures | ConvertTo-Json -Depth 5 }
	[System.IO.File]::WriteAllText((Join-Path $normalizedDir "failures.json"),
		$failuresJson, $utf8)
	[System.IO.File]::WriteAllText((Join-Path $normalizedDir "coverage.json"),
		($coverage | ConvertTo-Json -Depth 5), $utf8)

	$coverage | Format-List
}
finally
{
	$client.Dispose()
}
