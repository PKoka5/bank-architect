# Fixed Alch mode — 23 October 2026

Release 0.9.8 prepared above published 0.9.7 source
`efc0a61256c36a8415d8e4045ddb05d612e7c931`. The owner's final choice is literal
Fixed routing, with protections reserved for Flexible. Flexible remains the
default for new users and existing preferences. The owner authorized publication
on 23 October after the raid audit. The verified release checkpoint is recorded
below.

## Final contract

- `Gather Alch items` remains the independent on/off switch. `Alch sorting`
  selects Flexible or Fixed list in RuneLite settings and Tab Layout.
- Fixed sends every bank item on the exact 105-ID list to the Alch tag, including
  placeholders. BIS, special attacks, rune supply, crafting roles, stat tradeoffs,
  ownership of replacements, quantity and supported Alch-value metadata are not
  conditions. A listed exact ID also works without catalogue classification.
- Fixed does not infer membership from names, categories or prices. Items outside
  the list cannot enter through Flexible's surplus rules. Normal ammo routing can
  still place other ammunition in the same physical loot tab under `boss-loot`.
- Rune crossbows, Dragon daggers, Magic longbows, elemental battlestaves and Onyx
  bolts (e) therefore go to Alch in Fixed. Rune maces do so even when a Scurrius'
  spine is present. These are placement decisions; no bank action is automated.
- Flexible's pre-feature replacement implementation is unchanged, including
  source-category, placeholder, Scurrius, special-role and value guards.
- Personal category/tag corrections keep priority. Active saved per-occurrence
  destinations survive Fixed's automatic tag change. Dormant routes remain
  dormant; new explicit corrections retain the established priority over older
  saved routes. Captured physical positions remain authoritative.

## Integration and scope

Missing/invalid mode means Flexible. An Alch-only change keeps Ironman or Main
selected and stores its mode independently under the base's `builtinAlchMode`
key. This is separate from RuneLite's raw global `alchMode` input: an event while
Main is active cannot overwrite Ironman's saved choice. Custom profiles retain
their existing profile-local `alchMode` storage. Real layout edits still create a
Custom copy seeded with the current effective mode. An Alch-only legacy setting
does not trigger custom-layout migration. Existing Custom records are retained.

Mode travels through save, copy, restart and complete blueprint-tab moves.
Options copies and equality include mode, invalidating retained automatic Alch
decisions when switching modes. Existing plan-only share codes do not claim to
contain sorting options.

The exact list contains 57 finished Mithril/Adamant/Rune armour/melee products,
six ordinary metal tools, six additional Rune/Adamant halberds/crossbows/boots,
eight Dragon items, six elemental battlestaves, four d'hide bodies, two strung
longbows, fifteen normal/dark/light Mystic pieces and Onyx bolts (e). The IDs were
checked against the bundled registry and developer-only Wiki infobox snapshot.
Poisoned/ornament/clue variants, Dusk Mystic, unstrung bows, raw materials and
smithing components are outside this list. The ordinary Dragon scimitar (4587)
and Dragon halberd (3204) are included by explicit owner choice after the ingame
export check. Flexible retains its original special-attack/scimitar and
always-Alch/halberd behavior.

Reviewed recipe sources: [Mithril 15151075](https://oldschool.runescape.wiki/w/Template:Smithing/Mithril_bar?oldid=15151075),
[Adamant 15229092](https://oldschool.runescape.wiki/w/Template:Smithing/Adamantite_bar?oldid=15229092),
[Rune 15151078](https://oldschool.runescape.wiki/w/Template:Smithing/Runite_bar?oldid=15151078).
Their component/ammunition rows were not bulk-imported. Runtime code makes no
network request. No private full-bank export is committed.

## Implementation

Fixed's exact-ID branch runs before Flexible's guards and skips building the
replacement-comparison index. The former Fixed-only combination-staff and
crossbow protection code was removed. Flexible's replacement helper matches
published HEAD. UI descriptions state that Fixed includes best gear and special
weapons and that personal choices win.

`BlueprintItemOrders` has a package-private predicate for retaining active saved
destinations across automatic tag changes. The builder enables it only for exact
Fixed-list IDs with gathering enabled in Main/Ironman and without an explicit
override. Existing public apply overloads retain their previous matching behavior.
Physical occurrence identity and dormant destination validation are unchanged.

The sidebar scroll content follows viewport width. Minimum heights preserve
the editor and destination tiles at 204/225 pixels. Existing UI regression tests
cover the selector, accessibility, bounds and independent on/off state.
Two unused review-label formatting methods were removed after confirming they
had no production callers; their tests assert review lanes and actual sort order.
The generic sorter's legacy subgroup branches were also removed: all branch
keys use dedicated sort modes, while every supported generic category had the
same constant fallback rank. Normalized-name/ID order is preserved, with a
regression across all bundled preset definitions.

## Verification and budget

The final local 0.9.8 Java 11 build passed **2,064 tests** with zero failures,
errors or skips, including the later raid audit. All **1,950 expanded-universe
scenario banks** completed against the reviewed protocol-2 baselines. A separate
old-universe replay preserved all four previous fingerprints; see the
[raid verification record](fractured-archive-items-2026-10-23.md).
`FixedAlchRoutingTest` has 52 parameterized cases across
Main/Ironman. Its corpus checks route all 105 list members individually at
quantities 0/1/25 without gear stats, value metadata or upgrades. Other cases
cover functional tags, missing classification, Flexible protection, on/off,
explicit/saved/captured choices and conflicts, dormant routes, moved Alch/ammo
tags, physical-copy conservation and warm mode/evidence changes.

`CustomPresetsTest` covers Alch-only sidebar saves and RuneLite config events
without Custom creation, per-base persistence/restart, missing/legacy preference
migration, profile-capacity limits and preserving the mode when a real layout
change later forks. The event fixtures include RuneLite's prior write of the raw
global config key, proving that Main/Ironman builtin preferences remain isolated.

The Flexible replacement helper was compared verbatim with published HEAD and
matches. The raid audit adds one evidence-index guard for dedicated-ammunition
weapons, leaving that helper itself unchanged. Independent read-only review
found no remaining blocker. `git diff --check` is clean. An earlier compile
reported the existing sidebar unchecked-operation note; no new warning or
failed test remains.

Highest local main-Java estimate: **197,154**, with **2,846 estimated tokens of
headroom**. Other estimates are 195,029 / 196,771 (cl100k) and 195,402 (o200k
preserved). Recheck before publishing; this is limited headroom and not an
official Hub count.

Logs:

- `build/tmp/release-0.9.8-final-build-2026-10-23.log`
- `build/tmp/release-0.9.8-review-size-2026-10-23.log`

Final local JAR is `build/libs/ironman-bank-architect-0.9.8.jar`:
**1,118,811 bytes**, **265 entries**, SHA-256
`c4f568706a997561633a7b37aad2f824195b9d309346962e08a8caec8090b379`.
No test, tool, raw research, documentation or simulation entries are packaged.
The owner checked literal Fixed behavior ingame before pausing publication for
the raid audit. The new raid rewards still require an ingame check when owned.

The standing Hub limit remains strictly below 200,000 review tokens. Estimates
are main-Java-only and not the official Hub count. Resources, tests and documents
are reviewed separately. Calibration remains the maintainer's 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`. Publication is authorized; see the
[0.9.8 release record](../release-0.9.8.md).
