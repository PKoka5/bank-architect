# Combat v2 verification - 12 October 2026

## Approved behavior

- Three primary styles: Melee, Ranged, Magic. No fixed Prayer column.
- Actual selected rings/weapons/accessories near the front. Head/body/legs use vertical columns only when all targets fit in the real primary pool; sparse pools use compact style runs.
- Remaining equipment occupies contiguous style sections. Armour families are allocated before reviewed functional weapon groups. Vertical columns are preferred, with complete rectangles or canonical runs where dense geometry requires them.
- Condition variants retain exact IDs and canonical slot grouping. Large matrices have a bounded compact fallback that preserves the highest-priority feasible small column before using fully dense fallback.
- Normal and ornamented cannon components are grouped in Tools; the boxed cannon set stays standalone. Ammunition remains in Drops.
- Reviewed functional weapons cannot disappear through generic Alch dominance. Exact reviewed weapon identities can restore Combat classification without runtime equipment stats.
- Saved user corrections, legacy editor routes and captured physical order retain priority. Repeated physical entries use compact grouping in the affected section; subsequent sections use actual physical offsets.

## Results

Java 11, offline Gradle build: successful. **1741 tests, zero failures/errors/skips.** All **1950 fixed-seed scenario banks** complete. All four simulation fingerprints pass.

Independent test-only reports cover Main and Ironman, base102, conditions112, populated110, sparse7/8/9/15/16/17, unequal/incomplete styles, missing stats, shuffled inputs, withdrawals leaving placeholders, unrelated deposits, manual overrides, repeated physical entries and a **238-item bank containing all144 Barrows condition IDs**. Actual PNG/HTML output was visually inspected in build/reports/combat-quality. Every input stack is preserved; no real blank cells are fabricated. Missing equipment vectors are explicitly metadata fallback, not invented live bonuses.

The final238-item stress run built three previews per preset in **74/60/58 ms (Main)** and **51/50/49 ms (Ironman)** on this machine. These are local measurements, not guaranteed client timings. Search is bounded to two passes of10,000 candidate checks per section.

Simulation comparison with the preserved prior reports in build/tmp/before-combat-v2: **26/150 routing rows changed**, total manual moves12182 ->12141, individual deltas-17 to+10; all outcomes still COMPLETE. The cleanup review, aggregate cleanup review and aggregate metadata reports are byte-identical. The changed route fingerprint was inspected before updating build.gradle.

Fingerprints:

- report.tsv: fbf362a1dbbece8d3ca9bc6c73c4725c9cf955a70e7e660cd35c953683ce5a92
- cleanup-review.tsv: 309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091
- aggregate/cleanup-review.tsv: fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3
- aggregate/metadata.tsv: 76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe

## Review size and artifact

Development-only estimator, official calibration pair200414 at def1e856e101ff0e57dd96adccab2cf1d886b076 (maintainer report30 September2026): cl100k preserved195337/collapsed196708; o200k preserved195553/collapsed**196939**. Highest estimate leaves**3061** tokens. This counts main Java only; it does not certify official Hub eligibility. Resources, tests, documentation and development tooling require separate scope review.

Changed resources include exact cannon categories/domain changes and the earlier reviewed family/classification/tablet metadata. Tests, fixtures and quality renderers remain under test/development scope. No new production image assets or external dependencies are introduced. Retired global set-request logic, its geometry-only validator and constant-zero alignment field are absent from production. Necessary historical geometry checks remain test-only.

Local development JAR retains version0.9.5 pending an explicit release:265 entries,1110456 bytes, SHA256636da0744fc63676b7e95b7366126282a3c45a5b2ef4ede8a20e00fe90ab9ca2. Archive audit finds no test reports, fixtures, renderers, simulations, research files or retired prototype classes. This is a local development artifact; the published v0.9.5 asset is immutable.

Git diff whitespace check passes. Git emits existing LF-to-CRLF notices; Gradle build has no compilation warnings. No commit, push, branch change or release publication was performed.

## Remaining user validation

Check fresh automatic Main and Ironman blueprints ingame: three front styles, remaining vertical/compact set geometry, weapon roles, cannon Tools group and the existing native-bank preview across tabs. Explicit custom physical orders intentionally retain their chosen arrangement. These representative fixtures do not exhaust every possible bank or prove target-specific DPS rankings.
