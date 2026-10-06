function Get-SelectedCommunityTemplateFiles([string] $InputDir, [string] $RepoIds)
{
	if ([string]::IsNullOrWhiteSpace($RepoIds))
	{
		return Get-ChildItem -LiteralPath $InputDir -File -Filter "*.normalized.json" | Sort-Object Name
	}

	$ids = @($RepoIds -split '[,\s]+' | Where-Object { $_ } | ForEach-Object {
		if ($_ -notmatch '^[1-9][0-9]*$') { throw "Invalid public template ID: $_" }
		[int] $_
	} | Sort-Object -Unique)
	if ($ids.Count -eq 0) { throw "No public template IDs selected." }
	$selected = @($ids | ForEach-Object {
		$path = Join-Path $InputDir "$_.normalized.json"
		if (!(Test-Path -LiteralPath $path -PathType Leaf))
		{
			throw "Selected public template has no normalized input: $_"
		}
		Get-Item -LiteralPath $path
	})
	return $selected | Sort-Object Name
}
