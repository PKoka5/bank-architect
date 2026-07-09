param(
	[Parameter(Mandatory = $true)]
	[string] $InputDir,

	[Parameter(Mandatory = $true)]
	[string] $OutputImage,

	[int] $Columns = 8,
	[int] $CellWidth = 36,
	[int] $CellHeight = 32,
	[int] $Gap = 2
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (!(Test-Path -LiteralPath $InputDir))
{
	throw "Input directory not found: $InputDir"
}

$files = @(Get-ChildItem -LiteralPath $InputDir -Filter "slot-*.png" | Sort-Object Name)
if ($files.Count -eq 0)
{
	throw "No slot crops found in $InputDir"
}

Add-Type -AssemblyName System.Drawing

$rows = [Math]::Ceiling($files.Count / [double] $Columns)
$width = ($Columns * $CellWidth) + (($Columns - 1) * $Gap)
$height = ([int] $rows * $CellHeight) + (([int] $rows - 1) * $Gap)
$sheet = [System.Drawing.Bitmap]::new($width, $height)
$graphics = [System.Drawing.Graphics]::FromImage($sheet)
try
{
	$graphics.Clear([System.Drawing.Color]::FromArgb(38, 38, 38))
	for ($i = 0; $i -lt $files.Count; $i++)
	{
		$image = [System.Drawing.Bitmap]::FromFile($files[$i].FullName)
		try
		{
			$x = ($i % $Columns) * ($CellWidth + $Gap)
			$y = [Math]::Floor($i / $Columns) * ($CellHeight + $Gap)
			$graphics.DrawImage($image, $x, $y, $CellWidth, $CellHeight)
		}
		finally
		{
			$image.Dispose()
		}
	}

	$outputParent = Split-Path -Parent $OutputImage
	if (![string]::IsNullOrWhiteSpace($outputParent))
	{
		New-Item -ItemType Directory -Force -Path $outputParent | Out-Null
	}
	$sheet.Save($OutputImage, [System.Drawing.Imaging.ImageFormat]::Png)
	Write-Host "Wrote contact sheet to $OutputImage"
}
finally
{
	$graphics.Dispose()
	$sheet.Dispose()
}
