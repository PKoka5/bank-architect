# Reported midgame Combat and Slayer Tools

This continues the earlier Combat v2 work recorded in `endgame-combat-v2-2026-10-12.md`. It changes the local development tree; it does not publish or replace the released v0.9.5 artifact.

## Changes and exact fixtures

- Cannon parts form one compact group beside Slayer tools instead of preceding the outfit columns. Normal and ornamented finishes remain separate. Explicit saved item and cannon block orders remain authoritative.
- Primary rings use their relevant offensive power before the coarse progression tier. Lightbearer is additional support; Witchwood icon is conditional Slayer protection and cannot occupy the general Magic necklace position.
- The reported midgame weapons have reviewed functional roles, including single-owned role groups. Full armour families retain their weapons; remaining role groups keep canonical compact order. Single-role items cannot backfill ahead of preceding weapon groups. Armour retains its vertical/rectangle alternatives and bounded fallback.
- Role lookups are cached through `RequiredResource`, preserving recoverable catalog failure behavior. Slot and Alch-tier tables were consolidated without changing their values. Unused slot-grid, run-filter and width fixtures remain test-only.

The normalized reported fixtures contain **85 Combat items** and **161 Tools items**. Quantities are reduced to 0/1 and account identifiers are omitted. Combat equipment vectors are frozen from [RuneLite's public equipment snapshot](https://static.runelite.net/item/stats.ids.min.json), captured 2026-10-12; full snapshot SHA256 `99cdcb7514b2cf48655094d94e502d95d4ac708e328b25579d21511c4db3f1b8`. Downloading that development snapshot adds no plugin networking or dependency.

The Combat fixture deliberately freezes the reported catalog classifications and disables monetary Alch evidence, to isolate layout. It is not a replay of the player's complete live bank or custom settings. Independent assertions specify the primary items, three armour columns, relevant ring/support choices, weapon groups, shuffled/withdrawn-placeholder stability and saved-order precedence. Both presets are checked: Main retains its existing separate transport destination for Lunar staff, so it has 84 Combat items while preserving all 85 source entries across the preview.

The Tools fixture checks native outfit columns, Slayer adjacency, row boundaries, incomplete/ornamented cannon groups, placeholders and manual orders in both presets. At a bank-row edge the cannon may sit within the Slayer cluster rather than immediately before its first item.

## Verification

Java 11 offline `gradlew build`: **1759 tests, zero failures/errors/skips**. All **1950 fixed-seed scenario banks** complete. Four simulation fingerprints pass. PNG/HTML reports under `build/reports/combat-quality` were visually inspected for the reported midgame bank and the endgame bank. The shared renderer's midgame report checks conservation; the independently specified layout gates are in `ReportedMidgameCombatTest`, not inferred from production sorting helpers.

Compared with preserved reports in `build/tmp/before-midgame-roles`, **2/150 routing rows changed**, each by **+1 manual move** (RANDOM_TABS, seeds 20260731 and 20260756). Total manual moves: **12141 → 12143**. Every outcome remains COMPLETE. Cleanup review, aggregate cleanup review and aggregate metadata are unchanged. The routing fingerprint was reviewed before updating `build.gradle`:

- report.tsv: `22a34a0e139bf968fab5389fd12a651f86c3980bc232e19634a989611a51aeb1`
- cleanup-review.tsv: `309480b04ce63ce74996f4b15768e6c9a84e16611338e44c5abf689148bb3091`
- aggregate/cleanup-review.tsv: `fce40d023f6950a6813d97276f3836c73ab801e7c0c2c13235a4e7423f207ce3`
- aggregate/metadata.tsv: `76901b09b75f9d8219f3e1ba8b522940af373160718cd09cbf40439db088c3fe`

## Review scope and limits

Development-only estimator, calibration **200414** at `def1e856e101ff0e57dd96adccab2cf1d886b076`: cl100k preserved **195644**, collapsed **197046**; o200k preserved **195896**, collapsed **197311**. Highest estimate leaves **2689** tokens. These estimates count main Java only and do not certify the official Hub count. Resource changes were inspected separately: reviewed weapon-role IDs, one role-order name group and four existing-value Alch-tier rows. Test fixtures, helpers, renderers, documentation and development tooling are not bundled. No production image assets, dependencies, telemetry, input automation or new network calls were added.

Small style sections still use a compact family when their real cells cannot hold a vertical column. The reported secondary Ranged window occupies cells 53–67; its 15 cells cannot contain a three-high eight-column set without crossing into another style. Mixed hide therefore stays together as a compact group. Larger populated style sections retain vertical armour opportunities, including the 238-item all-Barrows-condition tests. This change does not redesign the front geometry or claim exact target-specific DPS rankings.

The local JAR remains version 0.9.5 pending a release: 264 entries, 1111571 bytes, SHA256 `959f3cf69d519f231e5d34fb27af4368d459b1e9d6dd580bfab3b51f844aeb56`. Archive inspection excludes the reported fixtures, test-only helpers and quality renderer. Git whitespace checks pass, with existing LF-to-CRLF notices. No commit, push, branch change or publication was performed. User validation should check a fresh automatic blueprint; saved custom physical orders intentionally preserve their chosen layout.
