param(
	[Parameter(Mandatory = $true)]
	[string] $InputImage,

	[Parameter(Mandatory = $true)]
	[string] $OutputDir,

	[int] $OriginX = 0,
	[int] $OriginY = 0,
	[int] $Columns = 8,
	[int] $Rows = 14,
	[int] $SlotWidth = 36,
	[int] $SlotHeight = 32,
	[int] $GapX = 2,
	[int] $GapY = 2,
	[switch] $WriteManifest
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $InputImage))
{
	throw "Input image not found: $InputImage"
}

$extension = [System.IO.Path]::GetExtension($InputImage).ToLowerInvariant()
if ($extension -eq ".webp")
{
	throw "WebP is not supported by this cropper. Convert the screenshot to PNG first, then run the cropper again."
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Add-Type -AssemblyName System.Drawing

$bitmap = [System.Drawing.Bitmap]::FromFile((Resolve-Path -LiteralPath $InputImage))
try
{
	$slotIndex = 0
	$manifest = New-Object System.Collections.Generic.List[string]
	$manifest.Add("slot,row,column,x,y,width,height")
	for ($row = 0; $row -lt $Rows; $row++)
	{
		for ($column = 0; $column -lt $Columns; $column++)
		{
			$x = $OriginX + ($column * ($SlotWidth + $GapX))
			$y = $OriginY + ($row * ($SlotHeight + $GapY))
			if ($x + $SlotWidth -gt $bitmap.Width -or $y + $SlotHeight -gt $bitmap.Height)
			{
				continue
			}

			$manifest.Add([string]::Join(",", @($slotIndex, $row, $column, $x, $y, $SlotWidth, $SlotHeight)))
			$rect = [System.Drawing.Rectangle]::new($x, $y, $SlotWidth, $SlotHeight)
			$crop = $bitmap.Clone($rect, $bitmap.PixelFormat)
			try
			{
				$outputPath = Join-Path $OutputDir ("slot-{0:D3}.png" -f $slotIndex)
				$crop.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
			}
			finally
			{
				$crop.Dispose()
			}

			$slotIndex++
		}
	}

	if ($WriteManifest)
	{
		$manifestPath = Join-Path $OutputDir "slots.csv"
		Set-Content -LiteralPath $manifestPath -Value $manifest -Encoding UTF8
		Write-Host "Wrote manifest to $manifestPath"
	}

	Write-Host "Wrote $slotIndex slot crops to $OutputDir"
}
finally
{
	$bitmap.Dispose()
}
