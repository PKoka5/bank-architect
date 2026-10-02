# Item role audit — first batch, 2 October 2026

## Scope

First focused implementation batch from the [placement research](plugin-hub-placement-and-cleanup-october-2.md). The companion [ledger](item-role-audit-october-2.tsv) records exact IDs, previous placement, proposed usage roles and source confidence. Proposed roles are research metadata, not yet loaded by the plugin. No cleanup eligibility or quest-progress feature was added.

Current classification comes from `build/classifications-after-style.tsv`; post-change export is `build/classifications-role-audit.tsv`. Independent exact-ID Wiki facts come from the existing local July 15, 2026 bulk snapshot and `effective-with-source-facts.tsv`. This is a dated snapshot, not an October refresh. The nine cosmetic corrections use named outfit membership and recorded equipment facts, not a blanket rule that zero stats implies cosmetic. The live search corroborated the Clown outfit as an event reward; direct outfit pages were blocked. No third-party plugin dataset was used.

## Implemented corrections

- Lederhosen hat/top/shorts: IDs 6182, 6180, 6181. Previously two Cleanup and one Gear; now one cosmetic outfit.
- Clown mask/bow tie/gown/trousers/shoes: IDs 22689, 22692, 22695, 22698, 22701. Previously three Cleanup and two Gear; now one cosmetic outfit.
- Zombie mask: ID 7594. Previously Cleanup; now joins the existing Zombie outfit. ID 19912, the already catalogued Zombie head, remains in the same outfit.

Both the resource and its developer generator supplements were updated so regeneration preserves these additions. Cosmetic coverage moves from 41 sets / 164 IDs to 43 sets / 173 IDs. No new main-Java logic was needed. Existing personal assignment precedence is preserved.

Independent item references: [Lederhosen top](https://oldschool.runescape.wiki/w/Lederhosen_top), [Lederhosen shorts](https://oldschool.runescape.wiki/w/Lederhosen_shorts), [Lederhosen hat](https://oldschool.runescape.wiki/w/Lederhosen_hat), [Clown outfit](https://oldschool.runescape.wiki/w/Clown_outfit), [Zombie mask](https://oldschool.runescape.wiki/w/Zombie_mask), [event rewards](https://oldschool.runescape.wiki/w/Event_reward).

## Retained uses and next review

The ledger also captures retained placements for functional gauntlets, staffs, monk robes and HAM clothing. Its roles are explicit audit proposals, not verified disposal rules. In particular HAM clothing already routes to Cosmetics but also has Thieving, quest and clue uses; its cosmetic destination must never establish disposal eligibility. Monk robes' combat/prayer role also prevents a blanket cosmetic reclassification. Dramen staff already uses transport access; Lunar staff retains its existing gear-set membership and can still be personally assigned to Frequently Used.

Evidence for the next role review: [HAM clothing](https://oldschool.runescape.wiki/w/Ham_robe), [HAM Hideout](https://oldschool.runescape.wiki/w/HAM_Fanatics%27_Camp), [clothing requirements](https://oldschool.runescape.wiki/w/Costumes), [Smithing training](https://oldschool.runescape.wiki/w/Smithing_guide_osrs), [bar dispenser](https://oldschool.runescape.wiki/w/Bar_dispenser), [Ancient mace](https://oldschool.runescape.wiki/w/Ancient_mace), [Dramen staff](https://oldschool.runescape.wiki/w/Dramen_staff). Consulted search content was cached; exact current requirements and conditional cleanup remain pending verification.

## Verification

- Final full build passed: 1,108 tests, zero failures/errors; all 1,950 simulation scenarios completed and all four reviewed baseline hashes passed. Git diff whitespace check passed; Git reported only line-ending conversion warnings.
- Targeted community regression tests and effective classification export passed.
- Full export comparison found exactly the nine intended classification changes; 32,586 effective and 606 excluded records retained.
- Regression checks exercise contiguous outfit grouping, personal Frequently Used assignment, and unchanged functional gauntlet placement.
- The coverage test initially failed because it expected the old set count; its expectation was updated to the reviewed 43 sets / 173 IDs while retaining duplicate checks.
- Reviewed simulation baseline changes by temporarily removing only the nine supplemental IDs, running the same seeds to a separate output directory and restoring the current resource in a finally block. All four reconstructed old hashes exactly matched the prior baselines. The new aggregate removes only IDs 6182, 7594, 22689 and 22695 from Cleanup, six occurrences each: 9,325 / 46,689 becomes 9,321 / 46,665. The short cleanup report removes ID 7594 (three occurrences). One short simulation scenario changes its move accounting; all outcomes remain complete. The four expected hashes were then explicitly updated.
- Highest local review estimate remains 196,165, with 3,835 headroom. The estimator excludes resource/test/tool changes; the nine resource rows and generator supplements were reviewed separately. This is not an official Hub count.

This batch does not complete the full catalog audit. Continue with independently sourced roles and variant review; do not infer current account storage or disposal eligibility from this ledger.
