# Alch feedback: med helms, chainbodies and heraldic Rune helms

Local development build; no publishing requested.

Later on the same date, the [replacement-family and cannon pass](alch-replacement-families-2026-10-06.md)
added functional tool/staff replacements, equivalent Mystic colours and grouping.
Its verification section contains the latest test count and token estimate.

## Placement policy

- The reviewed candidate list now includes exact ordinary metal med-helm and
  chainbody IDs, from Bronze through Dragon. Noted, ornamented, clue-reward,
  quest-specific and minigame namesakes do not inherit membership by name.
- Both presets use local replacement stages for the previously missing armour IDs:
  ordinary metal med helms/chainbodies are stage 2; Dragon med helm/chainbody are
  stage 3. A matching owned slot/style must still prove a replacement. The shared
  gear tier catalog and Ironman gear sorting remain unchanged.
- Main and Ironman use the same candidate selection. Reviewed gear, including
  large duplicate stacks, needs an actual owned replacement; quantity alone no
  longer moves reviewed Ironman gear. Ordinary reviewed clue-required equipment
  follows this shared rule; decorative heraldic clue helms remain Clues.
- Unreviewed duplicates retain the conservative fallback in both presets:
  full stat dominance, two better owned alternatives and alch value at least
  5,000, or bulk armour stock (quantity at least 8, value at least 1,000) with
  dominance and one better alternative. Known roles still protect unreviewed
  items. Missing equipment stats cannot prove replacement.
- The old Ironman-only Rune kiteshield (1201) forced-loot mapping was removed:
  its best-owned vs replaced decision now uses the shared Alch rules.
- The owner explicitly chose **always Alch** for the ordinary Dragon halberd
  (3204), despite its special attack. Both Main and Ironman therefore allow this
  exact item without an owned upgrade or equipment-stat requirement, provided
  automatic gathering is enabled, its local alch value is positive and it is
  not a placeholder. Other special-attack weapons retain their protection.
- Rune helm (h1-h5), IDs 10286/10288/10290/10292/10294, now have exact
  `CLUE/treasure-trail` classifications, producing the **Clues** layout tag.
  Ordinary Rune full helms and POH-painted heraldic helms retain their own rules.
- Dragon battleaxe (1377) now has an exact `GEAR/weapon` correction: the registry
  previously put it in Resources. Its special-attack protection keeps it Combat.
- Personal item corrections and captured bank destinations still take priority.
  Legacy category corrections to `slayer-boss-loot` keep the Boss Loot tag even
  when Alch has been moved elsewhere, just like modern `boss-loot` corrections.

## Evidence

The checked-in RuneLite gameval item registry supplies the canonical IDs. The
[Wiki heraldic armour page](https://oldschool.runescape.wiki/w/Rune_heraldic_armour)
confirms h1-h5 are Treasure Trails rewards, distinct from POH-painted armour,
and can satisfy heraldic helm clue requirements. The
[Dragon halberd page](https://oldschool.runescape.wiki/w/Dragon_halberd)
confirms ID 3204 and its special attack; its always-Alch placement is the owner's
explicit product policy, not a claim that it lacks combat utility.

Only six exact classification rows were added to production resources. No
runtime network or inventory/equipment reading was introduced.

## Verification

The regression suite checks actual Composite catalog classifications and exact
IDs, replacement vs best-owned armour, placeholders, switch-off, protected roles,
special-weapon boundaries and personal overrides. Synthetic equipment vectors
exercise the comparison rules; this is not a capture of the player's bank.

The new test file includes the real Dragon battleaxe classification and legacy
Boss Loot correction boundaries. Production resources, tests and development
documents are outside the main-Java token estimator and were inspected separately.

- `gradlew check jar`: **1,339 tests passed**, no failures/errors/skips.
- **20 feedback regression methods** exercise both presets; **23 existing Alch
  regression methods** are now parameterized for both (46 cases).
- **1,950 fixed-seed simulation scenarios completed**. All four committed
  baseline hashes remain unchanged; no baselines were regenerated.
- The existing Swing unchecked-renderer compiler warning remains.
- Highest calibrated main-Java estimate: **197,191**, headroom **2,809**;
  calibration remains the maintainer's **200,414** at
  `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official Hub count.

Restart the local development client to load the new build. Compare Main and
Ironman with the same bank holdings, inspect upgraded vs best-owned med helms
and chainbodies, check the Dragon halberd and heraldic Rune helms, then move Alch
away from Boss Loot and confirm personal assignments remain authoritative.
