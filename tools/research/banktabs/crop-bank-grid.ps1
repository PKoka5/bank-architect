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
	[int] $GapY = 2
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $InputImage))
{
	throw "Input image not found: $InputImage"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Add-Type -AssemblyName System.Drawing

$bitmap = [System.Drawing.Bitmap]::FromFile((Resolve-Path -LiteralPath $InputImage))
try
{
	$slotIndex = 0
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

	Write-Host "Wrote $slotIndex slot crops to $OutputDir"
}
finally
{
	$bitmap.Dispose()
}
