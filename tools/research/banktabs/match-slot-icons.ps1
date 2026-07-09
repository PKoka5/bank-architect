param(
	[Parameter(Mandatory = $true)]
	[string] $CropsDir,

	[Parameter(Mandatory = $true)]
	[string] $ReferenceDir,

	[Parameter(Mandatory = $true)]
	[string] $OutputTsv,

	[int] $TopN = 5,
	[int] $SampleSize = 16
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $CropsDir))
{
	throw "Crops directory not found: $CropsDir"
}
if (!(Test-Path -LiteralPath $ReferenceDir))
{
	throw "Reference directory not found: $ReferenceDir"
}
if ($TopN -lt 1)
{
	throw "TopN must be at least 1"
}
if ($SampleSize -lt 4)
{
	throw "SampleSize must be at least 4"
}

$cropFiles = @(Get-ChildItem -LiteralPath $CropsDir -Filter "slot-*.png" | Sort-Object Name)
if ($cropFiles.Count -eq 0)
{
	throw "No slot crops found in $CropsDir"
}

$referenceFiles = @(Get-ChildItem -LiteralPath $ReferenceDir -Filter "*.png" | Sort-Object Name)
if ($referenceFiles.Count -eq 0)
{
	throw "No reference icon PNGs found in $ReferenceDir"
}

Add-Type -AssemblyName System.Drawing

function Parse-Reference-Name([System.IO.FileInfo] $file)
{
	$name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
	if ($name -match '^(\d+)[-_ ]?(.*)$')
	{
		return [pscustomobject] @{
			ItemId = [int] $Matches[1]
			Name = (($Matches[2] -replace '[-_]+', ' ').Trim())
		}
	}

	return [pscustomobject] @{
		ItemId = -1
		Name = $name
	}
}

function Get-Sample([string] $path, [int] $sampleSize)
{
	$source = [System.Drawing.Bitmap]::FromFile($path)
	$scaled = [System.Drawing.Bitmap]::new($sampleSize, $sampleSize)
	$graphics = [System.Drawing.Graphics]::FromImage($scaled)
	try
	{
		$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
		$graphics.DrawImage($source, 0, 0, $sampleSize, $sampleSize)

		$values = New-Object 'double[]' ($sampleSize * $sampleSize)
		$index = 0
		for ($y = 0; $y -lt $sampleSize; $y++)
		{
			for ($x = 0; $x -lt $sampleSize; $x++)
			{
				$pixel = $scaled.GetPixel($x, $y)
				$values[$index] = (($pixel.R * 0.299) + ($pixel.G * 0.587) + ($pixel.B * 0.114)) / 255.0
				$index++
			}
		}

		return $values
	}
	finally
	{
		$graphics.Dispose()
		$scaled.Dispose()
		$source.Dispose()
	}
}

function Compare-Samples([double[]] $left, [double[]] $right)
{
	$total = 0.0
	for ($i = 0; $i -lt $left.Length; $i++)
	{
		$delta = $left[$i] - $right[$i]
		$total += $delta * $delta
	}

	return $total / [double] $left.Length
}

function Escape-Tsv([string] $value)
{
	if ($null -eq $value)
	{
		return ""
	}

	return (($value -replace "`t", " ") -replace "`r?`n", " ")
}

$references = New-Object System.Collections.Generic.List[object]
foreach ($file in $referenceFiles)
{
	$parsed = Parse-Reference-Name $file
	$references.Add([pscustomobject] @{
		File = $file
		ItemId = $parsed.ItemId
		Name = $parsed.Name
		Sample = Get-Sample $file.FullName $SampleSize
	})
}

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("screenshot_id	slot	candidate_rank	candidate_item_id	candidate_name	score	confidence")
$screenshotId = Split-Path -Leaf $CropsDir

foreach ($cropFile in $cropFiles)
{
	$slotText = [System.IO.Path]::GetFileNameWithoutExtension($cropFile.Name) -replace '^slot-', ''
	$cropSample = Get-Sample $cropFile.FullName $SampleSize
	$matches = foreach ($reference in $references)
	{
		$score = Compare-Samples $cropSample $reference.Sample
		[pscustomobject] @{
			ItemId = $reference.ItemId
			Name = $reference.Name
			Score = $score
			Confidence = [Math]::Max(0.0, 1.0 - [Math]::Min(1.0, $score * 4.0))
		}
	}

	$rank = 1
	foreach ($match in ($matches | Sort-Object Score | Select-Object -First $TopN))
	{
		$lines.Add([string]::Join("`t", @(
			(Escape-Tsv $screenshotId),
			(Escape-Tsv $slotText),
			$rank,
			$match.ItemId,
			(Escape-Tsv $match.Name),
			("{0:N6}" -f $match.Score),
			("{0:N4}" -f $match.Confidence)
		)))
		$rank++
	}
}

$outputParent = Split-Path -Parent $OutputTsv
if (![string]::IsNullOrWhiteSpace($outputParent))
{
	New-Item -ItemType Directory -Force -Path $outputParent | Out-Null
}
Set-Content -LiteralPath $OutputTsv -Value $lines -Encoding UTF8
Write-Host "Wrote match candidates to $OutputTsv"
