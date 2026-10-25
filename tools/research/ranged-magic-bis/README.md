# Reviewed Ranged and Magic primary candidates

Local development research, 25 October 2026. Item facts and original selection
policy only; no third-party plugin code, UI or layout is used.

`candidate-groups.tsv` is the reviewed page-family inventory. The explicit
development compiler expands canonical identities from the already-local Wiki
infobox snapshot. It writes separate compact runtime tables and the complete
`ranged-candidate-review.tsv` / `magic-candidate-review.tsv` audit. Every audit
record contains its exact ID, name, slot, preference, canonical version and
source. The plugin never runs this compiler or contacts a network service.

Run locally from the repository root:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools/research/ranged-magic-bis/compile-candidates.ps1
powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools/research/ranged-magic-bis/compile-candidates.ps1 -Check
```

The process-only execution-policy option permits this development script without
changing the machine's policy. The ignored canonical snapshot is required;
generation fails on an unmatched reviewed family, duplicate or unregistered ID.
There is no speculative adjacent-ID expansion. Temporary LMS/beta/unobtainable
pages are excluded. Ordinary cosmetic Deadman appearances remain ordinary items.

## Sources and freshness

The owner's sources were checked through the web tool on 25 October:

- [Ranged progression](https://oldschool.runescape.wiki/w/Guide:Ranged_Gear_Progression)
  and [Magic progression](https://oldschool.runescape.wiki/w/Guide:Magic_Gear_Progression);
- [OSRSBestInSlot Ranged](https://www.osrsbestinslot.com/ranged-gear/),
  [Magic](https://www.osrsbestinslot.com/magic-gear/),
  [Melee](https://www.osrsbestinslot.com/melee-gear/) and
  [general progression](https://www.osrsbestinslot.com/osrs-gear-progression/).

Direct Wiki fetches were blocked, but indexed Wiki progression and item-page
content was available through search. No source revision is fabricated where
one was not returned. Existing revision-pinned identity research, the canonical
Wiki snapshot and the independently frozen RuneLite stat vectors supplement
that observation. The secondary site is a checklist, not an authoritative stat
table: its Magic introduction describes powered staffs as rune-free, whereas
[Tumeken's shadow](https://oldschool.runescape.wiki/w/Tumeken%27s_shadow) consumes
charges made from runes. Its blowpipe discussion also needs the distinction
between base and Rapid attack speeds; the [Wiki's blowpipe table](https://oldschool.runescape.wiki/w/Blowpipe)
states both. No secondary-site numbers are copied into runtime stats.

Recent rewards were checked against their current item and release descriptions:
[Ascension crossbows](https://oldschool.runescape.wiki/w/Ascension_crossbows),
[Zorya's tome](https://oldschool.runescape.wiki/w/Zorya%27s_tome),
[Eye of Ayak](https://oldschool.runescape.wiki/w/Eye_of_Ayak), and
[Necklace of Fangs](https://oldschool.runescape.wiki/w/Necklace_of_Fangs).
The new raids' exact IDs already have pinned release evidence in this repository.
Necklace of Fangs 34401 was absent from the bundled registry; the existing
supplemental StaticItemCatalog now supplies its exact neck/gear identity.
The compiler records that explicit exception rather than inventing a cached page.

Canonical charge-state identity takes precedence over a search result showing
one infobox tab beside another version's description: Eye of Ayak 31113 is the
charged ID, 31115 uncharged; Shadow 27275 charged, 27277 uncharged. Crystal bow
23983 is active and 24123 new; 23985 is inactive. Historical imbued Crystal bows
are excluded from these new primary lists. Broken and uncharged powered-weapon
IDs remain represented but excluded from primary selection.

## Selection policy

- Tables use `slot/kind/band/fallback-rank/base-ticks/hands/group`, matching the
  established Melee schema. Kind 2 is general, 1 a reviewed fallback, 0 excluded
  from primary selection (unusable state or support function).
- General armour/accessories compare actual Ranged Strength or Magic damage
  tenths first, then that style's attack bonus, Prayer and defence. Existing
  family/placement tie-breaks resolve equal Ranged/Magic vectors, ending in a
  stable ID. Melee retains its previous exact-ID tie-break unchanged.
  Missing stats switch the entire comparable slot/band to reviewed ranks.
- Shared accessories are the previously reviewed exact identities. They have
  no imported Melee amulet preference band. Non-offensive support rings remain
  outside Ranged/Magic primary selection, without an Alch implication.
- Weapon bands are a visible all-round navigation policy, not target DPS.
  Ranged normally leads with Twisted bow, then Ascension crossbows, Bowfa and
  Zaryte crossbow; Magic with usable Shadow, Eye of Ayak, Sanguinesti and Trident
  families. Casting/specialist alternatives remain available. Base speed is
  validated only for comparable stat groups, not used as a fictitious universal
  DPS formula or confused with the selected attack style.
- A chosen two-handed weapon releases the primary offhand. One-handed crossbows
  and staffs retain a compatible shield/book. Unknown weapon handedness is
  conservative. Released offhands stay in the ordinary secondary equipment.
- Active Crystal armour pieces accompany a selected active Bowfa or Crystal
  bow. This is their verified [per-piece interaction](https://oldschool.runescape.wiki/w/Crystal_body),
  not a fabricated complete-set or target condition. Inactive versions do not
  acquire charged effects. A placeholder of an active ID remains owned gear.
- Slayer helmets use raw stats, not a task bonus. Void is fallback gear without
  assigning a full-set effect. Virtus's Ancient-only extra damage and spell,
  target, ammo, special-attack or charge assumptions are not added to raw stats.
- Reviewed candidates precede the existing unreviewed style fallback. Unknown
  gear is retained; it does not receive a new reviewed preference or disposal
  proof. Updated game items still require identity/function review.
- Melee keeps its previous numeric policy and priority for single shared
  physical items. The helper is shared, not three copied ranking algorithms.
  General disposal scores, dominance and Alch policy are untouched.

The expanded tests also exposed an existing missing support-family definition:
Elysian/Spectral spirit shields were only accidentally adjacent in the older
layout. They now have one explicit support-role family, so releasing primary
offhands does not scatter that pair in the fully populated quality fixture.

See [the readable slot review](../../../docs/plans/ranged-magic-slot-order-review.md)
and the verification checkpoint for the eventual complete build results.
