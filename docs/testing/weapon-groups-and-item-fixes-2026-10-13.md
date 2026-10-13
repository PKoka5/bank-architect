# Large weapon groups and reported item classifications

## Scope and outcome

The user approved moving the large special-attack group closer to ordinary Melee weapons, then requested the two classification fixes from the open GitHub issues. Whole blueprint-tab reordering ([issue 43](https://github.com/PKoka5/bank-architect/issues/43)) was explicitly deferred by the user.

### Weapon groups

Pure reviewed weapon groups previously had only a contiguous-run shape. Seven specials could not fit in the five columns beside three long primary gear columns and consequently moved below the ring area.

`GearSetSemanticRuleSet` now offers one row-flow alternative for a weapon group longer than the free right-hand width. That width comes from the actual primary target columns; with three columns it is five. Seven items therefore occupy offsets `0,1,2,3,4,8,9`. Occupied-cell and row-boundary checks remain active. The original compact shape remains the fallback. No additional search pass or invented bank cell is added.

In both the 182- and 185-entry mixed fixtures, seven specials now occupy cells **27-31 and 35-36**, replacing cells 80-86 or 88-94. The eight alternative bows occupy **43-47 and 51-53**. These exact expectations were added to the independent mixed test. All primary targets, the eight/eleven-ring arrangements, vertical retained armour families, shuffled input, quantities and placeholders retain their existing gates. The small reported 85-item bank and the 238-entry all-Barrows-condition bank also pass.

The current mixed PNG/HTML reports under `build/reports/combat-quality/` were regenerated and the complete-ring Ironman PNG was visually inspected.

### Deadman Bowfa and Crystal: issue 42

[The report](https://github.com/PKoka5/bank-architect/issues/42) identified absent reviewed variant IDs. The ordinary registry and frozen RuneLite equipment snapshot already contained them. Active Deadman stats match the ordinary item counterparts; inactive armour has the correct equipment slot and zero bonuses.

- Added Bowfa **33021** to the reviewed Bowfa role, including the Crystal synergy check.
- Added one canonical Crystal family: **33031/33033** head, **33023/33025** body and **33027/33029** legs.
- Added exact `GEAR` slot classifications for those seven IDs, so the correct slots are available without runtime equipment stats. Existing controlled same-name tier fallback remains tier 4.

The original-family snapshot still verifies the same original 78 rows and their unchanged checksum; the new family is asserted independently before removing it from that historical comparison.

### Construction cape: issue 44

[The report](https://github.com/PKoka5/bank-architect/issues/44) asks for Construction cape beside the other teleports. Its ordinary cache names are abbreviated `Construct. cape` and `Construct. cape(t)`, which bypassed the skillcape name heuristic. IDs **9789/9790** now have exact `TELEPORT/teleport` classification. This routes them with the existing teleports tag: Main destination 0 for the default Ironman plan, destination 1 for the Main preset. The separate hood is unaffected. Explicit owner corrections remain authoritative.

`ReportedGithubItemRolesTest` contains **14 parameterized checks** across both presets: exact roles/slots/tier fallback, active and inactive adjacent Crystal columns in a populated region, missing runtime stats, positive cape equipment stats and item values, source shuffles, placeholders, saved physical orders and corrections followed by clearing those corrections. A fixture assumption was corrected by using an actual Combat necklace instead of a raw crafting-material vambrace record in the no-stats filled-region case; the strict geometry assertion remains intact.

## Verification

Java 11 offline `test simulateRandomBanks aggregateCleanupReview jar`: successful. **1799 tests, zero failures, errors or skips**, and all **1950 fixed-seed scenario banks** complete.

All four reports match the preserved `build/tmp/before-weapon-flow` reports after line-ending normalization. There are no reviewed benchmark behavior differences or fingerprint changes in this follow-up:

- `report.tsv`: `30d344d7a5ab2d06039996130a181fcf2001b5532f5a8331e684ad7fc361073f`
- `cleanup-review.tsv`: `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091`
- `aggregate/cleanup-review.tsv`: `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3`
- `aggregate/metadata.tsv`: `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe`

Final `build -x simulateRandomBanks -x aggregateCleanupReview` reused those freshly generated reports and the passing full test result. All four fingerprint checks pass. No source changed between generation and the build gate. Whitespace checks pass; Git continues to emit LF-to-CRLF notices.

## Review budget and artifact

The development estimator remains calibrated to the maintainer's **200414** tokens at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported 2026-09-30. Final estimates: cl100k preserved **196195**, collapsed **197570**; o200k preserved **196509**, collapsed **197901**. Highest estimate leaves **2099** tokens. This counts main Java only and is not the official Hub count. The three changed resource tables, test additions/expectations and documentation were inspected separately. No dependency, production asset, telemetry, networking, automation or equipment/inventory read is added.

The local development JAR contains **264 entries**, **1113565 bytes**, SHA256 `7f59e6553ceaa4fa65ad4145618070b2c7521c334fa7b610788d5c56fbe486fc`. Archive inspection excludes test fixtures/classes, quality reports, simulation helpers and the retired prototype helpers.

Ready for an ingame test. No commit, push, issue comment/closure, branch change or publication was performed. Issue 43 remains a future UI/model feature requiring safe remapping of saved and captured blueprint destinations.

## Crafting cape follow-up

The user subsequently requested the same teleport treatment for the Crafting cape. Exact IDs **9780/9781** now use `TELEPORT/teleport`. Both variants provide reusable Crafting Guild teleports ([OSRS Wiki](https://oldschool.runescape.wiki/w/Crafting_cape)); the previous skillcape name rule assigned them to Tools. The separate **9782** hood retains `CLUE/cosmetic`.

The existing parameterized regression now covers all four Construction/Crafting capes in both presets, including positive frozen equipment bonuses, item values, missing runtime stats, source shuffles, withdrawn placeholders, explicit Tools/Frequently Used corrections and clearing them, and saved physical Teleports/Combat orders. Only two production resource rows were added in this follow-up; no main Java source changed.

The 30 focused tests pass. Full Java 11 offline `build` also passes: **1799 tests, zero failures/errors/skips**, **1950 completed fixed-seed scenario banks**, and all four unchanged simulation fingerprints. Whitespace checks pass with the existing LF-to-CRLF Git notices. The main-Java estimator remains **197901** at its highest setting, calibrated to the same **200414 at def1e856e101ff0e57dd96adccab2cf1d886b076**; this is an estimate with only **2099** tokens of headroom, not an official Hub count or release approval. The added resource rows, test changes and this documentation were inspected separately.

The rebuilt local JAR supersedes the artifact above: **264 entries**, **1113604 bytes**, SHA256 `a6129ff89bf433d92bdbebd5bad73dbf2bb73b247a2e6bd8dae071fbc27f532a`. Archive inspection confirms both cape teleport rows and the unchanged hood role. No commit, push, issue comment/closure or publication was performed.

Subsequent Herblore layout changes, the current artifact and reviewed simulation fingerprint are documented in [Ironman Herblore verification](ironman-herblore-hybrid-2026-10-13.md).
