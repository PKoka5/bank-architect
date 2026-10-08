# Alch stability when depositing a new item - 2026-10-08

Pre-release verification snapshot. These changes are included in [0.9.2](../release-0.9.2.md); that record contains the final publication checks.

Follow-up to [Gear slot coverage](gear-slot-coverage-2026-10-08.md). This records the Alch-history correction and its combined local verification. No changes have been committed, pushed, or submitted to the Plugin Hub; published 0.9.1 remains at `413355be10760da9d32de9632dbbb4e2f797784f`.

Subsequent sorting guidance changes and the latest combined verification are recorded in [Guidance efficiency](guidance-efficiency-2026-10-08.md).

The later [fresh-placeholder armour correction](placeholder-armour-alch-2026-10-08.md) deliberately changes the fresh-start armour policy described below; untiered and other role replacements retain real-ownership/session safeguards.

## Report and reproduced cause

The player deposited a Barrelchest anchor and saw previously Alch-routed Rune full helm (1163), Rune med helm (1147), and Rune plateskirt (1093) return to Combat. Whether the player's upgrades were placeholders at that moment was not confirmed.

A reproducible cause is the analysis session's context comparison. It previously required identical bank IDs and identical complete gear-stat/Alch-value maps. Adding any new ID reset remembered Alch routing. When previously owned upgrades were now placeholders, a fresh evaluation no longer had real ownership evidence and returned older gear to Combat. Anchor's weapon stats do not directly compete with head or leg armour.

The regression establishes Alch decisions while Torva headgear and Bandos legs are owned, withdraws those upgrades into placeholders, then deposits Anchor. It reproduces the reported three Rune items under both Main and Ironman. Together with the updated new-chain regression, four of 28 parameterized cases failed before the production change and passed afterwards. This demonstrates the placeholder/history path; it does not establish that every possible cause of reclassification is covered.

## Focused correction

`BankAnalysisRequest.sameLayoutContext` now permits additions. Every ID from the preceding successful bank snapshot must still exist with the same physical occurrence count and the same captured stats/Alch-value facts. New item metadata does not invalidate old facts. Absence versus a newly available value still triggers reevaluation for an existing ID.

Preset, serialized plan, layout options, and the entire personal category-choice map must still match. Removing old IDs, changing occurrence counts, changing old metadata, or invalidating the analysis clears preservation. Existing per-candidate positive quantity changes still trigger reevaluation, including reducing bulk stock to one during a new deposit. The placeholder gate, latest-request generation checks, and session lifetime remain unchanged.

New IDs receive no historical Alch decision. They are classified independently from current real ownership. A fresh session with only placeholder upgrades cannot infer new Alch decisions from those placeholders. This correction does not change replacement rules, item categories, or automatic game actions.

## Verification

- `PlaceholderGearStabilityTest`: 28 parameterized cases, including the reported armour, adding a new chain without inheriting withdrawn upgrade evidence, adding Anchor and then coins, repeated analyses, fresh-session safety, old Alch-value changes, bulk quantity reduction during a deposit, removals, settings, and stale request protection.
- `BankAnalysisRequestContextTest`: 18 direct boundary cases for directional additions, absent/present metadata, old facts, occurrence counts, removals, quantities/placeholders, fallback preset, and explicit configuration changes.
- `gradlew.bat build --offline`: **1,464 tests**, zero failures/errors/skips; **1,950 banksimulations**, all completed. All four previously reviewed report hashes match; no simulation baseline was updated for this correction.
- The build emits no new warning. `git diff --check` passes. An independent read-only review found no blocking lifecycle or stale-proof issue.

No private bank export or real player quantities are included in these fixtures. Public item IDs and synthetic stat vectors/quantities are used.

## Review token estimate

Official calibration remains **200,414 tokens** at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported by a Hub maintainer on 2026-09-30. `npm run check --prefix tools/review-size` returns estimates 195,723; 196,632; 195,789; and **196,715**. Use the highest: **3,285 tokens** of estimated headroom, a 14-token increase over the preceding local slot audit.

The estimator counts main Java only. New/changed tests, documentation, and the preceding resource/Gradle changes were inspected separately. A Git LF-to-CRLF notice is not a build or estimator failure. These figures are estimates, not official Hub token-limit approval. Runtime behavior adds no network calls, telemetry, equipment/inventory reads, or game-state manipulation.
