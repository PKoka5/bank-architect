# Combat layout visual simulation — 2026-10-09

The owner asked to inspect the Combat tab through simulation. This developer
run uses the current unreleased combat patch and the real preview implementation.
It makes no additional production changes and publishes nothing.

## Data and completeness

- Friend's Main export: 323 complete available rows, 48 placeholders. Its
  Combat header declares 256 items, but only 200 complete Combat rows survive
  the attachment's truncation. The final incomplete row is skipped. This is
  **a partial bank**, so missing gear may change the result in the full bank.
- Owner's Ironman export: 821 rows, matching the sum of its ten tab headers;
  18 placeholders. Complete as supplied.
- Four additional constructed scenarios use real item IDs: sparse Oathplate
  and Rune, Oathplate placeholders, partial degraded Barrows and a busy bank
  with multiple armour families/cannon. No synthetic equipment bonuses are used.

Fresh preset defaults intentionally ignore historical exported tags and manual
orders. Quantity and placeholder states are retained. These are layouts of the
supplied items under current defaults, not captures of a running game client.

Official [RuneLite equipment data](https://static.runelite.net/item/stats.ids.min.json)
and [Wiki high-alch values](https://prices.runescape.wiki/api/v1/osrs/mapping)
were cached on October 9, 2026. Production networking was not introduced.
The normal metadata fallback handles IDs without direct equipment records;
the simulator does not reconstruct worn/charged variant canonicalization.
Wiki mapping absence yields zero Alch value. Cache fingerprints are retained
in `build/reports/combat-layout-visual/simulation.json`.

## Outcomes

All **12 full previews** completed and passed ID/quantity/placeholder preservation,
secondary-family geometry and cannon checks. Both sparse cases additionally
verify that Oathplate leads and the Rune skirt routes to Alch.

| Bank | Main Combat / Alch | Ironman Combat / Alch |
| --- | --- | --- |
| Partial friend's Main | 198 / 3 | 195 / 3 |
| Complete owner's Ironman | 115 / 20 | 107 / 20 |
| Sparse Oathplate + Rune | 3 / 1 | 3 / 1 |
| Oathplate placeholders + Rune | 3 / 1 | 3 / 1 |
| Partial degraded Barrows | 6 / 0 | 6 / 0 |
| Busy combat + cannon | 59 / 6 | 59 / 6 |

### Confirmed placement improvements

- Friend's Main Oathplate helm/chest/legs occupy zero-based positions 0, 8 and
  16, the first melee column. The Rune plateskirt (1093) moves to Alch.
  In the supplied older export Oathplate legs occupied position 20, while the
  Rune skirt occupied position 16.
- Dark Squall (29566/29568/29570) occupies 78/86/94, one vertical column.
  The owned Lunar remainder occupies 80/88/96/104/112/120/128/136, one column.
- Busy bank's Radiant Oathplate, Dharok, Guthan and Armadyl remainders form
  contiguous runs at 39–41, 42–45, 46–49 and 50–52 respectively.
- Owner's Ironman Bandos body/legs occupy 8/16; Mystic body/legs occupy 10/18.
  Cannon components occupy 64–67, a contiguous row block.

### Remaining visible weaknesses

These are findings for follow-up, not changes implemented in this simulation:

- Loose fillers can still make the front visually busy. Friend's front has
  Corrupted/Ornate legs, cape/gloves/boots and Cape pouch. Complete aligned rows
  require real fillers; the current policy prioritizes protecting reviewed sets.
- Several utility/cosmetic items remain in Combat, including Cooking/Crafting
  hoods and Lovakengj/Shayzien banners in the partial friend's bank.
- Colored Crystal appearance IDs (27705/27697/27701) have no family metadata
  in this result. The helm enters the best front; remaining body/legs occupy
  122/129, so they are not one vertical column.
- Raw attack-stat style selection classifies Iban's staff (12658) and ordinary
  elemental staves as Melee. The Ironman result places Iban's staff in the
  front at 61 and Earth/Air/Water staves at 74–76. This is the current production
  behavior with actual stats, not a display error.

The best front and reviewed family grouping are demonstrably improved; this
does not establish perfect classification or tidy placement for every item.

## Visual verification

The eight-column conversation raster uses 352 successfully cached official
item sprites. It exposes both presets, current Combat/Alch and supplied Combat
orders; selecting an item highlights its family. Placeholders are dimmed.
The fragment is below 1 MB, with no runtime network requests.

Browser checks verify preset/scenario switching, original/current comparison,
placeholder/Alch views and family selection. Checks at 736 px and 320 px show
matching grid/client widths, no horizontal overflow and zero unloaded sprites.
The incomplete export button is removed for constructed cases. The full
Ironman view renders exactly 107 Combat slots; the placeholder case renders
three Oathplate slots and one Rune skirt in its Alch view.
The front highlight explicitly includes row fillers. Local generated artifacts
are ignored and do not alter the plugin JAR or the main-Java token estimate.
