param([switch]$Check)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$slots = @{head=0; body=1; legs=2; cape=3; neck=4; shield=5; hands=6; feet=7; weapon=8; ring=12}
$lines = [Collections.Generic.List[string]]::new()
$lines.Add('# schema=1')
$lines.Add('# Reviewed all-round Melee preferences. slot/kind/band/rank/ticks/hands/group -> exact usable or inactive IDs.')
$lines.Add('# Sources, decisions and exclusions: tools/research/melee-bis/. Generated explicitly; no bank/user settings.')
foreach ($row in (Import-Csv (Join-Path $PSScriptRoot 'helm-review.tsv') -Delimiter "`t")) {
    $kind = if ($row.decision.StartsWith('general')) { 2 } elseif ($row.decision -eq 'inactive') { 0 } else { 1 }
    $lines.Add("0/$kind/0/$($row.fallback_rank)/0/0/$($row.group)`t$($row.canonical_ids)")
}
$armour = Import-Csv (Join-Path $PSScriptRoot 'armour-decisions.tsv') -Delimiter "`t" |
    Where-Object { [int]$_.kind -ge 0 -and $_.readiness -eq 'ready-for-implementation-fixtures' }
foreach ($group in ($armour | Group-Object slot,kind,band,fallback_rank,group)) {
    $row = $group.Group[0]
    $ids = ($group.Group | Sort-Object {[int]$_.item_id} | ForEach-Object { $_.item_id }) -join ','
    $lines.Add("$($slots[$row.slot])/$($row.kind)/$($row.band)/$($row.fallback_rank)/0/0/$($row.group)`t$ids")
}
foreach ($line in (Get-Content (Join-Path $PSScriptRoot 'weapon-offhand-decisions.tsv'))) {
    if ($line -and !$line.StartsWith('#')) { $lines.Add($line) }
}
$seen = [Collections.Generic.HashSet[int]]::new()
foreach ($line in $lines) {
    if ($line.StartsWith('#')) { continue }
    foreach ($id in (($line -split "`t")[1] -split ',')) {
        if ([int]$id -le 0 -or !$seen.Add([int]$id)) { throw "Invalid or repeated item: $id" }
    }
}
$target = Join-Path $repo 'src/main/resources/com/pkoka5/ironmanbankarchitect/organize/melee-primary-candidates.tsv'
$content = ($lines -join "`n") + "`n"
if ($Check) {
    if (!(Test-Path -LiteralPath $target) -or [IO.File]::ReadAllText($target) -cne $content) { throw 'Melee candidate artifact is stale' }
} else {
    [IO.File]::WriteAllText($target,$content,[Text.UTF8Encoding]::new($false))
}
"Melee candidates: $($seen.Count) exact IDs, $($lines.Count - 3) groups; check=$Check"
