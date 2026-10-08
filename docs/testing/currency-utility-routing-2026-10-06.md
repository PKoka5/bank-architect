# Currency and achievement utility routing

Date: 2026-10-06

The maintainer requested currency classification to describe what an item is,
independently of the tab where an earlier blueprint placed it. This supersedes
the July policy that placed several activity currencies in Clues & Cosmetics
and classified achievement utilities as Currency to keep them on Main.

## Current defaults

| Items | Classification / layout tag | Main and Ironman behavior |
| --- | --- | --- |
| Golden nugget (12012), Numulite (21555), Stardust (25527), Tokkul (6529), Trading sticks (6306) | CURRENCY / currency | Follow the configured Currency destination. |
| Vale offerings (31054), from the earlier Reddit fix | CURRENCY / currency | Follow the configured Currency destination. |
| Rada's blessing 1-4 (22941, 22943, 22945, 22947) | TELEPORT / teleports | Follow the configured Teleports destination. |
| Ghommal's hilt 1-6 (25926, 25928, 25930, 25932, 25934, 25936) | TELEPORT / teleports | Follow the configured Teleports destination. |
| Western banner 3-4 (13143, 13144) | TELEPORT / teleports | Follow the configured Teleports destination. |
| Western banner 1-2 (13141, 13142) | TOOL / achievement-utility | Tools in Main; Frequently Used in Ironman when gathering is enabled, Tools otherwise. |
| Frog token (6183) | Cosmetic reward destination retained | Clues & Cosmetics. |

Sources for the utility roles: [Western Provinces Diary](https://oldschool.runescape.wiki/w/Western_Provinces_Diary),
[Rada's blessing](https://oldschool.runescape.wiki/w/Rada%27s_blessing),
[Ghommal's hilt](https://oldschool.runescape.wiki/w/Ghommal%27s_hilt).
Only Western banner 3/4 has a teleport. Exact canonical item IDs are corrected;
same-name cert, dummy and placeholder records do not inherit new role overrides.
The plugin continues to normalize bank placeholders through its existing reader.
Ghommal's avernic defenders remain equipment.

## Implementation and saved choices

- Remove the achievement-family names from the legacy Currency name rule.
- Apply canonical TELEPORT/TOOL overrides rather than broadly changing all
  diary rewards or equippable teleport items.
- Remove the five spendable currencies from the special activity-reward route;
  keep the Frog token exception without retaining a one-entry lookup set.
- Explicit personal itemtag/category corrections still win. Clearing a personal
  correction restores the new automatic role.
- Captured banks keep their physical tab and order when an automatic role changes.
  Older ordinary editor routes still follow the existing original-tag guard and
  may become dormant when their original Currency tag changes to Teleports.
- Teleport rewards now use the Teleports layout rather than the Frequently Used
  diary matrix. Built-in preset definitions and profile persistence are unchanged.

The five corresponding expectations in the 770-ID real-bank fixture are deliberately
updated. Its remaining destinations are retained. The separate Graceful stability
test retains its 53-item Main shape using a Frog token outside Main instead of
Tokkul; the new currency tests cover Tokkul's corrected destination directly.

## Reviewed simulation reference change

The fixed-seed report was compared with the pre-change copy, and the simulator's
Java Random sampling was independently reproduced. The item universe remains
32,585 IDs. Only two of the 50 sampled banks contain items affected by this fix:

| Seed | Affected item | Scenario | Before / after total moves |
| --- | --- | --- | --- |
| 20260746 | Ghommal's hilt 6 (25936) | SHUFFLED_NO_TABS | 207 / 208 |
| 20260746 | Ghommal's hilt 6 (25936) | RANDOM_TABS | 200 / 199 |
| 20260761 | Western banner 3 (13143) | RANDOM_TABS | 209 / 207 |

Only total moves, swaps and minimum swaps at sort start differ. These items stay
in the same main category in this simulation, but their Currency-to-Teleport
correction changes the final order within it. Item counts, tab counts, outcomes,
final status and errors remain unchanged. All 150 random scenarios and all 1,800
aggregate scenarios complete; the three cleanup/metadata report hashes are
unchanged. After this review, only the report.tsv reference in build.gradle is
updated from `2e2b19244b4457e802da63f4ecfecfbeba870a06e1b664fdbacaf51794a77ca3`
to `02ecac28ab673cd0ec7b9ed890c45ab7975d1cc17c505ddb694af1e8244569bf`.

## Validation

- Java 11 `gradlew check jar` passes with all four reviewed report references.
- **1,399 unit tests**, zero failures/errors/skips; **1,950 bank scenarios** completed.
- The new parameterized routing test covers both presets with 12 test cases,
  including separated tag destinations, positive gear stats, personal corrections,
  placeholders, and captured-bank migration across the old Currency/Clues roles.
- The earlier Vale offerings regression remains green. The Graceful rescan test
  still exercises its original 53-item Main shape.
- Final verification introduces no warnings; the local jar was rebuilt. No
  version, release notice, GitHub release or Plugin Hub submission was changed.
- Development logs: `build/tmp/currency-utility-check-final.log` (complete unit
  run and simulation comparison), `build/tmp/currency-utility-verified-check.log`
  (successful final reference check), `build/tmp/currency-utility-tokens.log`.

## Review size

The highest local main-Java estimate is **197,812 tokens**, with **2,188** estimated
headroom. This uses the maintainer's **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076` (2026-09-30). Resources, tests and docs
are outside that estimate and were reviewed separately: a narrow name-rule removal,
14 exact utility rows plus the earlier Vale row, regression tests and policy notes.
This is not the official Hub count and does not certify a submission.
