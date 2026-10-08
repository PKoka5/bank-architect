# Fresh placeholder armour and Rune Alch routing - 2026-10-08

Pre-release verification snapshot. These changes are included in [0.9.2](../release-0.9.2.md); that record contains the final publication checks.

Follow-up to [guidance efficiency](guidance-efficiency-2026-10-08.md) and [Alch addition stability](alch-addition-stability-2026-10-08.md). This records the latest combined local verification. Nothing is committed, pushed or published; submitted 0.9.1 still references `413355be10760da9d32de9632dbbb4e2f797784f`.

## Confirmed cause

The private Ironman blueprint export places Rune full helm (1163), Rune med helm (1147) and Rune plateskirt (1093) in Combat. Helm of neitiznot (10828) and Bandos tassets (11834) are genuine bank placeholders. The prior session-history fix cannot establish a replacement decision after a fresh start: the builder excluded all placeholders from its gear comparison buckets.

Rune plateskirt also lacked an exact curated tier. Equivalent Rune platelegs (1079) have stage 2. The missing skirt tier prevented the reviewed armour progression rule from using owned Obsidian platelegs (21304, stage 3) when their stats do not fully dominate Rune defence.

## Bounded policy change

Both Main and Ironman now accept a genuine bank armour placeholder as planned-kit evidence only when the candidate is reviewed stock and the replacement has a higher exact curated tier in the same combat-style/equipment-slot bucket. This works without session history. A placeholder is not proof of present ownership; removing it restores classification from the remaining bank evidence.

Real bank items remain mandatory for stat-dominance comparisons, unreviewed bulk stock and tool/staff/weapon-role helpers. Candidate placeholders never automatically become Alch stock. Known-role protections, Alch value, enabled gathering, explicit corrections and saved/captured physical destinations retain their existing precedence. No saved layout is cleared or migrated.

`BankOrganizationPreviewBuilder` reuses its gear buckets, renaming their entries to `BankGear`. The real-owned ID set is unchanged. The scoring preview passes the actual placeholder flag so a zero-quantity snapshot respects `BankPreviewItem`'s constructor invariant. Runtime capture already collects canonical equipment facts for placeholders; no inventory/equipment read or new runtime API is needed.

The gear-tier resource adds only Rune plateskirt's exact stage-2 row for this follow-up. Its catalog count is now 336 including the preceding local gear additions. No broad display-name Alch inference is introduced.

## Regression coverage

- Before the policy correction, five focused cases failed because fresh known armour placeholders still routed reviewed Rune stock to Gear.
- `PlaceholderArmourAlchTest`: 22 cases across both presets. Fresh Neitiznot/Bandos placeholders replace the three reported items; owned Obsidian replaces Rune skirt despite a deliberate defensive tradeoff; unrelated Anchor deposits do not change routing. Negative cases cover incompatible style/slot, equal/unknown tiers, unreviewed bulk, unchanged tool/staff/weapon ownership rules, placeholder stock, missing stats, disabled gathering, manual Gear pins and saved/captured destinations.
- `PlaceholderGearStabilityTest`: 30 cases. Fresh curated armour expectations now accept Alch. Synthetic untiered replacements keep session-memory tests meaningful: their real copies establish dominance, their placeholders cannot establish fresh decisions. Initial Alch decisions are asserted before invalidation tests. Removal, new IDs, old fact changes, quantity/occurrence changes, preset/options/personal choices and stale success handling remain covered.
- Existing role-family, Alch and metadata tests remain enabled. Fixtures use public IDs and synthetic vectors/quantities. The private export and its real quantities are not copied into the repository.

## Combined verification

`gradlew.bat build --offline` passes: **1,511 tests**, zero failures/errors/skips, and **1,950 completed bank simulations**. All four previously reviewed simulation hashes remain unchanged; no baseline was updated for this correction. The build emits no new warning. `git diff --check` passes. Independent read-only review found no blocking policy or fixture issue.

The live whole-tab smoke test from the previous change remains outstanding. This Alch follow-up also needs the local game check: restart with the current build, analyze the bank with the preferred armour withdrawn, and check the three Rune items' Alch destinations. Explicit manual/saved placements remain authoritative.

## Review-token budget

The development estimator still calibrates to the maintainer's **200,414** count at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 2026-09-30. Estimates are 196,957; 197,763; 197,022; and **197,849**, leaving **2,151** estimated tokens below 200,000. Use the highest estimate. More consolidation is needed as future additions use this remaining headroom.

Main-Java estimates exclude tests, resources, Gradle and documentation; those changes were inspected separately. This is not the official Hub tokenizer/scope or submission approval. The standing strict-under-200,000 requirement remains in force.
