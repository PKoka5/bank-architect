# Bank Architect 0.9.2

Prepared on **8 October 2026** above published source
`413355be10760da9d32de9632dbbb4e2f797784f` (0.9.1).
The owner explicitly authorized publication of the combined changes.

## Player-facing changes

- Reviewed armour stock uses higher exact-tier upgrades in the same combat style
  and equipment slot even when the upgrade is a bank placeholder. This also works
  after restarting. Rune plateskirt now has the same stage as Rune platelegs.
  Dominance, unknown stock and tool/staff/weapon-role rules still require real bank
  items. Manual corrections and captured destinations retain priority.
- Depositing a new item keeps established session Alch choices when existing
  item facts, physical occurrence counts and layout choices remain compatible.
  Removed items, changed existing facts/preferences and changed candidate
  quantities still trigger reassessment. New IDs inherit no previous decision.
- Combat scoring aligns fallback gear with the curated progression stages.
  Equipment-slot coverage, prayer naming and reviewed recent melee gear data
  improve placement across the supported layouts and both presets.
- Manual guidance recognizes suitable neighboring category tabs and highlights
  their headers for a whole-tab drag. Internal order and Main remain preserved.
  Only adjacent header moves are advised; this is not arbitrary tab permutation
  optimization. Insert-mode distribution follows target order to reduce later
  inserts. Unique-ID insert matching avoids the quadratic LCS allocation;
  duplicates retain their existing matching rules.
- The sidebar displays bundled **What's new** notes for 0.9.2 once after upgrading.
  Acknowledgement persists after reopening and restarting.

All bank moves remain manual. No runtime network, telemetry, game actions,
inventory/equipment reads or dependency changes were added.

## Token headroom and code consolidation

Seventeen identical model text guards now share `TextValidation.requireText`.
They retain the same exceptions and return the original text without trimming it.
Advised and accepted manual tab creation/transfers now share existing transition
validators. Exact count deltas, ID multiplicities, other tabs, Main and a remaining
source-tab item stay checked; section bounds precede manual anchor access.
Reorder, swap, insert, collapse and saved-order checks remain unchanged. Read-only
reviews found no blocking equivalence issue.

## Validation and limits

- Final `gradlew.bat build --offline`: **1,511 tests**, zero failures/errors/skips,
  and **1,950 completed bank simulations**. All four reviewed hash guards pass.
- The guidance report's deliberate schema/two-row improvement relative to 0.9.1
  is recorded in [guidance efficiency](testing/guidance-efficiency-2026-10-08.md).
  The three cleanup/aggregate hashes remain unchanged. Subsequent Alch and
  consolidation changes do not alter those reviewed results.
- Regression records cover [progression](testing/combat-progression-consistency-2026-10-08.md),
  [equipment slots](testing/gear-slot-coverage-2026-10-08.md),
  [addition stability](testing/alch-addition-stability-2026-10-08.md), and
  [fresh armour placeholders](testing/placeholder-armour-alch-2026-10-08.md).
  The fresh-placeholder cases use original synthetic bank snapshots; no private
  exports or real player quantities are included.
- Update-notice tests cover dismissed 0.9.1 -> 0.9.2, repeated opening, dismissal,
  restart and a future release. The rendered 204px sidebar was visually inspected.
- No new build warning. `git diff --check` passes. Git LF/CRLF notices concern
  line endings rather than runtime behavior.
- Live adjacent-tab smoke testing in both modes with equal/unequal tabs remains
  outstanding, as does the final fresh-client Rune Alch smoke check. Unit tests
  and simulated transforms do not replace these live checks. Publication was
  explicitly requested; no owner-confirmed final live pass is claimed.

## Review-token budget

The development estimator was run after the final source edits and is checked
again on the committed release. Calibration remains the maintainer's **200,414**
count at `def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,374 |
| cl100k, collapsed whitespace | 196,484 |
| o200k, preserved whitespace | 195,377 |
| o200k, collapsed whitespace | **196,507** |

The highest leaves **3,493 estimated tokens of headroom** below 200,000. This is
not the official Hub tokenizer/scope or a guarantee of eligibility. Keep the
strict-under-200,000 requirement and recalibrate any new official count against
its exact source revision.

Outside main Java, three bundled TSVs, synthetic tests, reviewed Gradle baseline
and version changes, README and developer documentation were inspected
separately. No new runtime resources or raw caches are bundled by this release.

## Artifact and submission

- Jar: `build/libs/ironman-bank-architect-0.9.2.jar`
- Size: **1,097,916 bytes**; **269 entries**.
- SHA-256: `7a76398833a7384eca17fa5e8bc2a64ae05da3bcfdb4003d74c4a8b3ff4361a3`.
- Jar contents: production classes/properties/bundled catalogs only; no tests,
  simulators, research tools, documentation or private data.

Publish tag `v0.9.2` with this verified jar. The Hub submission changes only
`plugins/bank-architect` to the tag's actual source commit. GitHub's release and
Hub pull request record the resulting revision. Client distribution follows Hub
maintainer review and merging. The previous 0.9.1 Hub PR 18075 is already merged.

The current [Hub update instructions](https://github.com/runelite/plugin-hub/blob/master/README.md)
and [rejected-feature guidance](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features)
were refreshed before submission.
