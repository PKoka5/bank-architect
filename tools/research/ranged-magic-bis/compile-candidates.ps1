param([switch]$Check)
$ErrorActionPreference='Stop'
$repo=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$wiki=Get-Content (Join-Path $repo 'tools/research/wiki-recommended-equipment/cache/infobox-item.bucket.json') -Raw | ConvertFrom-Json
$normal=@($wiki | Where-Object {$_.page_name -notmatch 'Last Man Standing|unobtainable|beta|Deadman Mode'})
$registry=@{}
Get-Content (Join-Path $repo 'src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/compiled-item-registry.tsv') | ForEach-Object {
    if($_ -and !$_.StartsWith('#')) { $p=$_ -split "`t"; $registry[[int]$p[0]]=$p[1] }
}
$facts=@{ranged=@{};magic=@{}}
$registry[34401]='Necklace of Fangs' # Exact live Wiki identity, supplied by StaticItemCatalog until the next registry refresh.
function Add-Fact($style,$id,$slot,$kind,$band,$rank,$ticks,$hands,$group,$version,$source) {
    $id=[int]$id
    if(!$registry.ContainsKey($id)){throw "Unregistered candidate $id ($version)"}
    if($facts[$style].ContainsKey($id)){throw "Repeated candidate $style/$id ($version)"}
    $facts[$style][$id]=[pscustomobject][ordered]@{item_id=$id;name=$registry[$id];slot=$slot;kind=$kind;band=$band;fallback_rank=$rank;ticks=$ticks;hands=$hands;group=$group;wiki_version=$version;source=$source}
}
foreach($row in (Import-Csv (Join-Path $PSScriptRoot 'candidate-groups.tsv') -Delimiter "`t")) {
    $matches=@($normal | Where-Object {$_.page_name -match $row.pages_regex})
    if(!$matches.Count){throw "No canonical page matched $($row.style)/$($row.group): $($row.pages_regex)"}
    foreach($entry in $matches) {
        foreach($id in $entry.item_id) {
            if($id -notmatch '^\d+$'){throw "Noncanonical identity $id"}
            $inactive=$entry.page_name_sub -match 'broken|mangled|inactive|#0$|#Uncharged|^Uncharged|#Empty'
            if($row.group -in @('quiver','tomes','broodoo','dragonfire-ward','ancient-wyvern')) { $inactive=$entry.page_name_sub -match 'broken|mangled' }
            $kind=if($inactive){0}else{[int]$row.kind}
            $band=if($inactive){0}else{[int]$row.band}
            $rank=if($inactive){0}else{[int]$row.fallback_rank}
            $ticks=if($inactive){0}else{[int]$row.ticks}
            $hands=if($inactive){0}else{[int]$row.hands}
            Add-Fact $row.style $id ([int]$row.slot) $kind $band $rank $ticks $hands $row.group $entry.page_name_sub ('https://oldschool.runescape.wiki/w/'+$entry.page_name.Replace(' ','_'))
        }
    }
}
# Previously reviewed shared accessories retain raw bonuses; no task or set condition is enabled.
$shared='fury|glory|power|accuracy|amulet-damned|barrows|rune-gloves|dragon-gloves|adamant-gloves|mithril-gloves|black-gloves|steel-gloves|iron-gloves|bronze-gloves|combat-bracelet|regen|brimstone-ring|ring-shadows|suffering|recoil|gods-'
foreach($line in (Get-Content (Join-Path $repo 'src/main/resources/com/pkoka5/ironmanbankarchitect/organize/melee-primary-candidates.tsv'))) {
    if(!$line -or $line.StartsWith('#')){continue}
    $parts=$line -split "`t"; $key=$parts[0] -split '/'
    if($key[6] -notmatch "^($shared)" -or [int]$key[1] -eq 0){continue}
    foreach($style in @('ranged','magic')) { foreach($id in ($parts[1] -split ',')) {
        $supportRing=[int]$key[0] -eq 12 -and [int]$key[1] -eq 1
        Add-Fact $style $id ([int]$key[0]) $(if($supportRing){0}else{[int]$key[1]}) 0 $(if($supportRing){0}else{[int]$key[3]}) 0 0 $key[6] 'existing-reviewed-shared-accessory' 'tools/research/melee-bis/'
    } }
}
foreach($style in @('ranged','magic')) {
    $void=if($style -eq 'ranged'){'Void ranger helm'}else{'Void mage helm'}
    foreach($entry in ($normal | Where-Object {$_.page_name -match 'slayer helmet.*\(i\)' -or $_.page_name -match ('^'+[regex]::Escape($void)+'( \(or\))?$') -or $_.page_name -match '^(Elite )?Void knight (top|robe|gloves)( \(or\))?$'})) {
        $slot=if($entry.page_name -match '(helm|helmet)'){0}elseif($entry.page_name -match 'top'){1}elseif($entry.page_name -match 'robe'){2}else{6}
        $slayer=$entry.page_name -match 'slayer helmet'
        foreach($id in $entry.item_id) {
            if($id -notmatch '^\d+$'){throw "Noncanonical special identity $id"}
            $inactive=$entry.page_name_sub -match 'broken|mangled'
            $kind=if($inactive){0}elseif($slayer){2}else{1}
            $rank=if($slayer -and !$inactive){150}else{0}
            Add-Fact $style $id $slot $kind 0 $rank 0 0 $(if($slayer){'slayer-imbued'}else{'void-fallback'}) $entry.page_name_sub ('https://oldschool.runescape.wiki/w/'+$entry.page_name.Replace(' ','_'))
        }
    }
}
# Released 20 October; canonical raid IDs already verified against the pinned release snapshot.
Add-Fact 'ranged' 34681 8 2 65 950 3 2 'ascension-crossbows' 'Ascension crossbows' 'https://oldschool.runescape.wiki/w/Ascension_crossbows'
Add-Fact 'ranged' 34401 4 2 0 700 0 0 'fangs' 'Necklace of Fangs' 'https://oldschool.runescape.wiki/w/Necklace_of_Fangs'
Add-Fact 'magic' 34725 8 1 24 540 3 2 'zorya-tome' "Zorya's tome#Charged" "https://oldschool.runescape.wiki/w/Zorya%27s_tome"
Add-Fact 'magic' 34727 8 0 0 0 0 0 'zorya-tome' "Zorya's tome#Uncharged" "https://oldschool.runescape.wiki/w/Zorya%27s_tome"
foreach($style in @('ranged','magic')) {
    $lines=[Collections.Generic.List[string]]::new()
    $lines.Add('# schema=1');$lines.Add('# Reviewed layout candidates: slot/kind/band/rank/base-ticks/hands/group. No target-specific DPS claim. See tools/research/ranged-magic-bis/.')
    foreach($group in ($facts[$style].Values | Group-Object slot,kind,band,fallback_rank,ticks,hands,group | Sort-Object Name)) {
        $f=$group.Group[0];$ids=($group.Group | Sort-Object item_id | ForEach-Object item_id) -join ','
        $lines.Add("$($f.slot)/$($f.kind)/$($f.band)/$($f.fallback_rank)/$($f.ticks)/$($f.hands)/$($f.group)`t$ids")
    }
    $target=Join-Path $repo "src/main/resources/com/pkoka5/ironmanbankarchitect/organize/$style-primary-candidates.tsv"
    $content=($lines -join "`n")+"`n"
    if($Check){if(!(Test-Path $target) -or [IO.File]::ReadAllText($target) -cne $content){throw "Stale $style artifact"}}
    else{[IO.File]::WriteAllText($target,$content,[Text.UTF8Encoding]::new($false))}
    $audit=Join-Path $PSScriptRoot "$style-candidate-review.tsv"
    if(!$Check){$facts[$style].Values | Sort-Object slot,item_id | Export-Csv $audit -Delimiter "`t" -NoTypeInformation -Encoding UTF8}
    "$style candidates: $($facts[$style].Count) IDs, $($lines.Count-2) groups"
}
