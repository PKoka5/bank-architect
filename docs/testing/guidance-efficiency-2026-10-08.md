# Manual sorting guidance efficiency - 2026-10-08

Pre-release verification snapshot. These changes are included in [0.9.2](../release-0.9.2.md); that record contains the final publication checks.

Follow-up to [Alch addition stability](alch-addition-stability-2026-10-08.md). All changes remain local and unpublished. Submitted 0.9.1 still points to `413355be10760da9d32de9632dbbb4e2f797784f`.

Subsequent fresh-placeholder Alch changes and the latest combined verification are recorded in [placeholder armour Alch routing](placeholder-armour-alch-2026-10-08.md).

## Changes and limits

The route recognizes reordered category tabs before suggesting individual recovery moves or collapse. An intact Combat tab at bank position 2 that belongs at position 3 can therefore be moved with one adjacent tab-header drag. The new action has no item/grid slots. Both live header widgets must exist, be visible, have the expected child index/action, and have valid geometry. The overlay highlights headers with MOVE/DROP and draws the connector outside the item-grid clip. Instructions use bank positions including Main, rather than potentially sparse blueprint category numbers.

For uniquely assigned pure bucket permutations, the route uses the minimum number of **adjacent** exchanges: each removes one inversion. Partial buckets qualify only when they uniquely map to the existing leading targets. A mixed-bank exchange requires an unambiguous neighboring bucket destination and at least two fewer misplaced physical entries. Every proposed exchange preserves internal order and Main. An expected complete transformation advances immediately; incomplete count/container samples wait. Stable unadvised adjacent exchanges can be recognized without destructive recovery.

Guidance requires All-items view for whole-tab instructions, consistently with existing bucket routing. Only neighboring headers are advised. [Bank mechanics and sources](../bank-tab-mechanics.md) explain the supported vanilla drag callbacks and the unverified nonadjacent swap/insert semantics. Insertion and swapping have the same result for neighbors, but actual live acknowledgement/overlay behavior still needs the documented smoke test. The new feature adds no game input, packet, state manipulation, network call, inventory/equipment read, or telemetry.

Interior sorting already uses minimal swaps for unique IDs and minimal inserts via increasing/common subsequences. Duplicate-ID swap estimates can exceed the true minimum; this change does not claim a globally optimal bank route. Unique-ID insert matching now takes the linear occurrence-mapping path instead of allocating a quadratic LCS matrix. Duplicate matching retains its LCS reconstruction and deterministic tie behavior. Shared remainder matching and membership counters reduce repeated code without dropping safeguards. No measured FPS or millisecond improvement is claimed.

Insert-mode distribution now appends missing items in target order. For target `1,2,3,4,5,6` and existing prefix `1,2,3,6`, the prior swap strategy appended `5,4`, needing two later inserts. The corrected Insert strategy appends `4,5`, needing one. Swap retains its cycle-closing optimization. The known append mechanic improves efficiency; transition checks still accept membership and subsequent sorting handles different landing order.

## Tests

- `WholeTabGuidanceTest`: nine cases, including all 24 four-tab permutations in both item modes, unequal/equal sizes, preserved internal/Main order, partial buckets, mixed-bank restrictions, expected/safe alternative transitions, separate count/container samples, and changed internal/Main order rejected as a single tab action.
- `WholeTabOverlayTest`: nine cases for header-only movement, compressed blueprint numbering, source/destination bounds/actions/visibility, invalid endpoints, Main exclusion, and disabled highlights.
- `GuideSortingEfficiencyTest`: five cases. Independent LCS oracle checks all 1,236 unique six-item prefixes against all possible missing tails. All 720 unique-ID permutations check offsets, section bounds, and a one-move insert-bound decrease. A 1,000-item rotation retains its exact 137-insert minimum. No timing threshold or private player fixture is used.
- Existing session, duplicate-item, customized-order, filtering, sorting, and simulation tests remain enabled. Read-only reviews found no blocking planner, matcher, or overlay issue. A long MOVE TAB badge was shortened to MOVE to fit tabheaders.

## Manually reviewed simulation baseline

The first broad mixed-tab heuristic reduced aggregate moves but made six random rows longer. The final stricter strategy only reorders a mixed pair when a known neighboring bucket has a clear membership saving. It produces the following reviewed changes against the preceding local snapshot:

| Scenario / seed | Moves before | Moves after | New header drags |
|---|---:|---:|---:|
| RANDOM_TABS / 20260731 | 95 | 89 | 1 |
| RANDOM_TABS / 20260744 | 48 | 47 | 1 |

The other 148 rows retain all old fields. Item counts, target-tab counts, final status, outcome and error fields remain unchanged for every row. Total moves fall **12,170 to 12,163**; no final row gets longer. For seed 20260731, local swaps/minimum drop 32 to 27 and transfers 29 to 27; for seed 20260744 transfers drop 20 to 18. Collapse/create/distribution/return counts remain unchanged. Random banks rarely contain complete category permutations; the deterministic permutation tests establish that targeted case separately.

The report adds a `tabReorders` column, with two nonzero rows. Its reviewed hash changes from `331d5ea825c55d91756cd473cf0d1471647c820050a689019d8e88249e4671c2` to `0eaf605942c45776532344818621adc45369dd62c8f642bec856bdbe42dff941`. All three cleanup/aggregate hashes remain unchanged. The first build intentionally failed the report hash guard; the expected hash was changed only after inspecting this delta, not regenerated automatically.

## Combined verification and token budget

`gradlew.bat build --offline` passes all four reviewed hash guards, with **1,487 tests** and **1,950 completed banksimulations**, zero failed/error/skipped tests, and no new build warning. `git diff --check` passes. The live whole-tab smoke test remains outstanding; nothing is published.

`npm run check --prefix tools/review-size` retains the official calibration **200,414 tokens** at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 2026-09-30. Current estimates are 196,949; 197,750; 197,013; and **197,834**, leaving **2,166** estimated tokens below 200,000. Use the highest, not the most favorable estimate. Future code additions need renewed review and more consolidation as this headroom shrinks.

Main-Java estimates exclude tests, resources, Gradle and documentation; those changes were inspected separately. Git LF/CRLF notices are unrelated to the build result. This is not the official Hub tokenizer/scope or token-limit approval. The standing strict-under-200,000 requirement remains active.
