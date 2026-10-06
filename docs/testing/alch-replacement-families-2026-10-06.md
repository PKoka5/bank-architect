# Alch replacement families and cannon grouping

Local development build, following the [earlier Alch feedback pass](alch-feedback-2026-10-06.md).
The development checks below preceded the authorized release 0.9.0.

## Shared Main and Ironman behavior

Automatic gathering still requires a positive local alch value and a real item,
with the Alch option enabled. Personal corrections and captured physical bank
destinations remain authoritative. Unknown equipment retains the earlier
conservative dominance/value/quantity policy.

The reviewed exceptions now compare the item's function before relying on
equipment-stat dominance:

- Rune pickaxe (1275), Rune axe (1359) and Adamant axe (1357) move to Alch when an actually banked
  higher functional tool tier is present. The existing tool progression table
  supplies the tiers. Gilded Rune tools are the same tier; placeholders, broken
  Dragon picks and inactive Crystal tools cannot prove an upgrade. Empty infernal
  pick variants 13244/25369/30346 and axe variants 13242/25371/30348 retain Dragon
  functionality and now belong to that functional tier.
- Each ordinary elemental staff compares only its own element: air
  1381/1397/1405, water 1383/1395/1403, earth 1385/1399/1407 and fire
  1387/1393/1401, in ascending staff/battlestaff/mystic order. A different element,
  an Ancient staff, a powered staff or an unrelated melee weapon cannot replace
  its rune supply through the generic dominance fallback.
- Ordinary blue/dark/light Mystic variants share five exact slot families:
  4089/4099/4109, 4091/4101/4111, 4093/4103/4113, 4095/4105/4115 and
  4097/4107/4117. Prefer a player-selected Combat variant, otherwise blue, then
  dark, then light. Other known personal destinations exclude a variant from
  being the automatic survivor. Saved captured tags also inform this preference.
  Bank slot order never breaks ties, avoiding repeated sorting after reopening.
  Dusk, ornaments and other similarly statted robes do not inherit this rule.
- Reviewed ordinary slash stock (Rune battleaxe/2h/longsword/scimitar/sword/dagger
  and Adamant battleaxe/2h/scimitar) can move when a Dragon scimitar (4587),
  Abyssal whip (4151), Abyssal tentacle (12006) or ornamented whip (26482) is
  actually banked. This is an explicit general-purpose weapon layout policy;
  a whip does **not** dominate every crush capability of a Rune battleaxe.
  Armour ownership alone does not replace a weapon. Dedicated maces, spears,
  warhammers and protected special weapons do not inherit this broader rule.
- Spiked boots (3107) retain their existing `CLEANUP/quest-item` classification
  even when equipment stats are available. They receive Quest Items, rather than
  being promoted back to Combat. Climbing boots remain separate functional gear.

Ownership-based replacement assumes the owned upgrade is usable; the plugin
does not read player inventory/equipment to establish this. A bank stack is one
layout item: these rules do not divide copies within a stack.

## Cannon layout

The set catalog now contains normal components 6/8/10/12 and ornamented
components 26520/26522/26524/26526, ordered base, stand, barrels, furnace.
They cannot be borrowed as equipment-row fillers, including the sparse-row
fallback. Semantic layouts reserve dense prefix positions for the owned parts;
the list layout places cannon sets first. Complete sets form adjacent horizontal
blocks in all three automatic gear layouts; partial sets keep only owned parts.
Manual captured order is applied afterward and can deliberately separate them.

These are blueprint placement constraints, not automated bank actions.

## Research

The checked-in RuneLite registry supplies exact canonical IDs. Mechanics were
checked against [Dragon pickaxe](https://oldschool.runescape.wiki/w/Dragon_pickaxe),
[infernal tool degradation](https://oldschool.runescape.wiki/w/%28uncharged%29),
[Staff of fire](https://oldschool.runescape.wiki/w/Staff_of_fire),
[Fire battlestaff](https://oldschool.runescape.wiki/w/Fire_battlestaff),
[Mystic robes](https://oldschool.runescape.wiki/w/Mystic_robes),
[Spiked boots](https://oldschool.runescape.wiki/w/Spike_boots) and
[Dwarf multicannon](https://oldschool.runescape.wiki/w/Dwarf_multicannon).
Some direct Wiki fetches were blocked; indexed Wiki content and canonical local
data were used for those facts, rather than treating blocked pages as fully read.

[Players discussing Mystic colours](https://www.reddit.com/r/ironscape/comments/1ovw1t1/)
give different preferences about keeping them for collection, fashion or clues.
[Players discussing alch tabs](https://www.reddit.com/r/ironscape/comments/1r970q2/post_your_alch_tab/)
include Rune picks and other surplus equipment. These discussions inform product
choices, rather than establishing a universal safe-to-alch list. Exact functional
replacement and personal overrides are more useful than treating all valuable
equipment as disposable.

## Verification and review size

- `gradlew check jar`: **1,384 tests**, no failures/errors/skips.
- 19 new replacement tests parameterized across both presets (38 cases), including
  Dragon axe replacing both Rune and Adamant axes and the option/ownership/manual guards.
  Seven cannon tests cover 108 preset/layout scenarios, asserting actual physical
  rectangle coordinates, item conservation, sparse/busy banks and capture priority.
- **1,950 fixed-seed simulations completed**; all four committed baseline hashes
  remain unchanged. No baseline regeneration.
- Two existing gear fixtures used real cannon IDs for fictitious armour/rings;
  their IDs moved to explicit synthetic ranges, preserving the alignment assertions.
- Small behavior-preserving cleanup shares the identical Main/Ironman category
  structure and metadata/source-table framing. Existing loader tests still pass;
  field counts, schema/BOM handling, source validation and error messages remain.
- `isCaptured()` is evaluated once per preview, avoiding a repeated scan of saved
  destinations for every owned item.
- Jar: `build/libs/ironman-bank-architect-0.8.1.jar`.
- Highest calibrated main-Java estimate: **197,758**, estimated headroom **2,242**.
  Other estimates: 197,015 / 197,630 / 197,127. Calibration remains the maintainer's
  **200,414** for `def1e856e101ff0e57dd96adccab2cf1d886b076` on 2026-09-30.
  The official Hub tokenizer/input scope are unknown; this is not Hub approval.
  New Java is counted; eight new set-resource rows, tests and docs were inspected
  separately because the estimator excludes them. Keep measuring before submission
  and consolidate further as features grow.
- No new runtime network, inventory/equipment reads, telemetry or game actions.
  The earlier Swing unchecked-renderer warning remains an existing build limitation;
  this incremental check emitted no new warnings.

Restart the development client and Analyze Bank with the same holdings under
Main and Ironman. Check Rune pick versus Dragon pick, Rune battleaxe versus an
approved general-purpose weapon, normal/dark Mystic bottoms, Staff of fire versus
Fire battlestaff, Spiked boots and all four cannon components. Reopen the bank to
check stable sorting, then confirm a manual item correction still wins.
