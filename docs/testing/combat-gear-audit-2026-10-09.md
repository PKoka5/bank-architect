# Combat gear and set audit — 2026-10-09

Local, unreleased work after 0.9.2 (`3b9012e360ff4a23c591f7eda722b81dc2d41a90`).
Includes the separately documented Avernic treads correction.

## Owner's requested behavior

Keep the best owned gear per combat style at the front, with remaining sets
grouped afterward. The reported friend's Oathplate legs were actually in the
bank. No exact bank snapshot, plugin version or layout setting was provided, so
the friend's precise result cannot be reconstructed.

On October 9 the live Hub manifest still references 0.9.1 source
`413355be10760da9d32de9632dbbb4e2f797784f`. That revision has no Oathplate exact
tiers or fallback score; its Rune plateskirt fallback can outrank Oathplate.
0.9.2 adds the missing Oathplate tier/slot/ranking correction. Its
[Hub submission](https://github.com/runelite/plugin-hub/pull/18098) is still open
with a successful build at the time of this audit. This is a plausible cause,
not a verified account-specific reproduction.

## Findings and implementation

- List layout read only the legacy item-set table, while grid layout used a
  merged family list. Both now use the same reviewed dictionary.
- 27 ordinary armour families were missing, including Oathplate/Radiant,
  Armadyl, Mystic variants, Splitbark/Swampbark, Snakeskin, Hueycoatl and Third Age.
  All are now exact-ID families. Six blessed dragonhide shields join their sets.
  Existing original family members retain their relative order.
- Functional wearables and weapons had incorrect categories, including bark
  gauntlets, blessed coifs, Enchanted head/legs, some Third Age pieces, Ghrazi
  rapier and Soulreaper axe. Exact overrides correct their categories and slots;
  personal choices and captured orders continue to take priority.
- Name-only fallback now recognizes reviewed magic/ranged families and unusual
  body/leg names. Identically named Yak-hide pieces use exact slot metadata.
- Complete BIS rows may borrow loose gear, but never secondary family members.
  Sparse best-owned pieces receive a compact front prefix. Only the actual best
  candidate is promoted, rather than a second choice after an existing primary.
- The secondary fallback order is grouped by the same family dictionary. A
  semantic result is accepted only if each remaining family retains an ordered
  dense run or an exact column-major rectangle (including vertical columns).
  Otherwise the grouped fallback is used. This prevents a five-piece Mystic
  set splitting around another after a compact prefix at physical column 4.
- Cannon components start after complete aligned rows, before compact wearable
  pieces can push them across opposite row edges. Both finishes and partial
  cannons remain compact. Full style columns stay at their original positions.

The gear family table grows from 43 to 70 rows. The independently reviewed
reference test covers 33 new/expanded families, **144 ordinary item IDs**, checked
against the local `TOP_LEVEL` research index with no exclusion flags. Its new
canonical table fingerprint is
`98290ad74fc1a597fe8d6e389e56aec9eb1cdf779c1a17635f4a5df1f608ad74`.
The six functional-coif expectations replace the former cosmetic-coif policy.
New family rows do not invent progression tiers.

## Sources and limits

Public game facts and local RuneLite item identities were used; no third-party
plugin implementation, UI or layout was copied.

- [Melee armour](https://oldschool.runescape.wiki/w/Armour/Melee_armour)
- [Ranged armour](https://oldschool.runescape.wiki/w/Armour/Ranged_armour)
- [Magic armour](https://oldschool.runescape.wiki/w/Armour/Magic_armour)
- [Blessed dragonhide](https://oldschool.runescape.wiki/w/Magic_prayer_armour)
- [Snakeskin bandana](https://oldschool.runescape.wiki/w/Snakeskin_bandana)
- [Armadyl armour](https://oldschool.runescape.wiki/w/Arma_set)
- [Third Age ranged equipment](https://oldschool.runescape.wiki/w/3rd_age_range_equipment)
- [Ghrazi rapier](https://oldschool.runescape.wiki/w/Ghrazier)

Some direct Wiki reads were blocked; indexed/encoded or redirected versions
provided the armour references. The supplied Item_set and Weapons/Categories
pages could not be read in full, so this is expanded ordinary-gear verification,
not a claim that every gear record or weapon role has been audited. Special-world
equipment, further appearance variants and utility/prayer exceptions need their
own verified policies. RuneLite bank equipment stats remain the live authority.

All game organization remains manual and bank-only. No branch, commit, push,
Hub update or publication was performed. In-game verification remains pending.

## Validation

### Manual simulation baseline review

The four previous reports were preserved before rerunning the simulations and
compared by scenario or item ID, rather than comparing shifted line numbers.
An independent read-only code review reached the same result.

- All 150 individual scenarios and 1,800 aggregate scenarios completed.
  No outcomes, statuses, item counts, tab counts or action-type counts changed.
- Ten individual scenarios changed only `totalMoves`, `swaps` and
  `minSwapsAtSortStart`, by the same amount. The total across 150 scenarios is
  12,163 → 12,171 moves; each changed swap count still equals its calculated
  minimum for the new target. The largest increase is five swaps. This does not
  establish faster organization; the target order changed to protect families.
  The existing `minimum=-1` sentinel case remains unchanged.
- Individual cleanup review removes only Dragon claws (13652), three sampled
  occurrences. Aggregate cleanup review removes the six IDs below, with no
  additions or changed counts for remaining IDs. All six now have explicit
  gear overrides.
- Aggregate metadata changes only the number of distinct cleanup IDs
  (9,283 → 9,277) and their occurrences (46,491 → 46,461). Seeds, registry,
  item universe, protocol and all completion/error counts stay unchanged.

| Removed cleanup item | ID | Aggregate occurrences |
| --- | --- | --- |
| Dragon claws | 13652 | 9 |
| Enchanted hat | 7400 | 6 |
| Mystic hat (or) | 26531 | 6 |
| 3rd age mage hat | 10342 | 3 |
| Tonalztics of ralos (uncharged) | 28919 | 3 |
| Tonalztics of ralos | 28922 | 3 |

The deliberately reviewed CRLF-canonical report fingerprints are now:

| Report | SHA-256 |
| --- | --- |
| `report.tsv` | `931427feb77c1734730ee7f36e735d07fbf2b8e85151e4f94766dccaf3e0dea1` |
| `cleanup-review.tsv` | `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091` |
| `aggregate/cleanup-review.tsv` | `e74bb1af3c5e06f045155a8086bfe77face3b2adfe48e8f847d0e0694f480140` |
| `aggregate/metadata.tsv` | `dcaf6463d72d7b903583a610e1d2abfcaa4b47905a2954cdac5f9d7f176cfec1` |

### Final verification

- Java 11 offline `gradlew.bat build --offline --console=plain`: successful.
  All four reviewed baseline guards passed.
- JUnit XML totals: **1,525 tests**, 151 suites, zero failures, errors or skipped
  tests. The final build reused the successful unchanged test task.
- **1,950 simulations completed**, with no unsupported plans, build errors,
  blocked advisors, stalled cases or non-termination.
- `git diff --check` passed. No compiler warnings appeared in the patch's
  compile/test/build logs. The estimator emitted Git LF/CRLF notices only.
- Regression coverage includes both presets; normal/Radiant Oathplate versus
  Rune legs; stats/name fallback; owned items/zero-quantity placeholders;
  compact best-owned fronts; whole and partial secondary families; degraded
  Barrows members; shared list/grid families; offset Mystic placement; existing
  cannon cases; and the separately documented Avernic progression correction.

Development-only review-size check against official calibration
`def1e856e101ff0e57dd96adccab2cf1d886b076` / 200,414 reported tokens:

| Tokenizer / whitespace | Estimated total |
| --- | --- |
| cl100k / preserved | 195,775 |
| cl100k / collapsed outside literals | 196,866 |
| o200k / preserved | 195,780 |
| o200k / collapsed outside literals | **196,891** |

Use the highest estimate: **3,109 estimated tokens of headroom** against 200,000.
This measures main Java changes only and does not certify Hub eligibility.
Resource tables, tests, README and the four build guard changes were separately
reviewed: exact item metadata and original local tests/documentation only; no
runtime code, dependency, automation, networking or telemetry was introduced
outside the measured Java scope. The official tokenizer and input scope remain
unknown. No new maintainer count was available for this uncommitted patch.

Logs are retained locally under `build/tmp/` as
`combat-gear-audit-test-recheck.log`, `combat-gear-audit-verified-build.log` and
`combat-gear-audit-final-review-size.log`.

### Remaining in-game checks

1. Re-analyze with the built-in Ironman and Main presets and the combat style
   layout. Confirm best owned pieces lead the tab and secondary sets remain
   grouped, including partial sets and banks without enough loose row fillers.
2. Confirm Oathplate/Radiant legs win over Rune legs with the better piece in
   the bank, then repeat with its bank placeholder. Check Avernic treads in the
   style determined by its live stats.
3. Check ordinary/ornamented and partial cannons alongside compact best gear.
   Recheck personal overrides and captured/custom orders, which retain priority.

The friend's exact bank was not available, so these automated results do not
replace verification of his bank in the current local build.
