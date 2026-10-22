# Bank Architect 0.9.7

Prepared on **22 October 2026** above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`. The owner accepted the combined
in-game test round and explicitly authorized publication. The previous Hub
request is merged; this release uses a new submission.

## Player-facing changes

- **Move complete blueprint tabs:** Tab Layout has Up and Down controls for an
  entire destination, including Main and empty tabs. Assigned tags, current-order
  choices, saved/captured item positions and ammunition routing follow the moved
  destination. Editing a bundled preset creates a Custom layout.
- **Stable reviewed Alch stock:** quantities falling to one or a placeholder no
  longer bring reviewed stock back to Combat, including after restart. Reviewed
  tool, staff, Mystic-colour and slash-weapon replacement rules recognize bank
  placeholders. Unreviewed stock keeps conservative quantity/value/ownership
  requirements; no GE-price rule is added.
- **More careful replacements:** ordinary d'hide bodies and chaps need full-stat
  replacement proof. A real Scurrius' spine protects Rune mace from automatic
  Alch until a Bone mace or its placeholder is recorded. New crafting evidence
  also invalidates that item's old automatic session Alch choice.
- **Combat corrections:** Obsidian platebody has its missing progression tier.
  General Ranged head selection prioritizes Ranged damage and accuracy before
  defensive tie-breakers. Bowfa/Crystal context and personal placements retain
  priority; Slayer equipment remains available as secondary gear.

The first sidebar opening after upgrading shows **What's new: 0.9.7**. Dismissal
is remembered locally for this version. The Updates icon retains all thirteen
bundled release entries. Every real bank action remains manual.

## Validation

The final offline Java 11 release build passed **1,968 Java tests** with no
failures, errors or skips. **1,950 scenario banks** completed, with all four
reviewed simulation fingerprints unchanged. The development exporter also passed
its 12 local Node tests. The owner
reported the requested in-game check looked good; individual live steps were
not independently observed.

Detailed records: [blueprint tabs](testing/blueprint-tab-reorder-2026-10-22.md),
[selected stock/Obsidian fixes](testing/selected-alch-obsidian-2026-10-22.md),
[d'hide proof](testing/wiki-strategy-alch-2026-10-22.md),
[Ranged heads](testing/ranged-head-selection-2026-10-22.md),
[strategy contexts](testing/wiki-strategy-contexts-2026-10-22.md).

The Wiki [context corpus](research/wiki-strategy-corpus-2026-10-22.md) is a
development-only review queue, not an automatic strategy-ranking import. Twelve
set/specialist contexts and one crafting context were reviewed. No claim is made
that every strategy or possible bank has been validated. Wider bank grids remain
a separate feature; the plugin's supported layout remains eight columns.

## Final checks and artifact

- Final `build` passed on the 0.9.7 metadata and bundled release notes.
- Update notice and history rendered and visually checked at 204/225-pixel sidebar
  widths. Dismissal/restart and history preservation tests pass.
- Independent read-only review of the combined production diff found no blocker
  or newly forbidden runtime API. `git diff --check` passes.
- Existing sidebar unchecked-operation compiler note remains; no new dependency,
  runtime permission or compiler warning is introduced.

Calibration remains the maintainer's official **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,217 |
| cl100k, collapsed whitespace | 196,876 |
| o200k, preserved whitespace | 195,568 |
| o200k, collapsed whitespace | **197,237** |

The highest leaves **2,763 estimated tokens** below 200,000. Main Java is the
estimator's scope; the official Hub tokenizer, preprocessing and full input scope
remain unknown. Resources, tests, tools, documents and build changes were inspected
separately, including the new bundled history entry. This is not official token
approval. Logs are under `build/tmp/release-0.9.7-*-2026-10-22.log`.

- JAR: `build/libs/ironman-bank-architect-0.9.7.jar`.
- Size: **1,116,622 bytes**, **264 entries**.
- SHA-256: `7e4ccc2c4079f1ca7622f6ef035f0d138eb2f5fc0cfdfa1c45cb3b2aa4804488`.
- No developer tools, tests, Wiki snapshots, research, documentation or simulation
  reports are packaged. Only this exact versioned artifact is published.

## Publication

The release tag is `v0.9.7`. The GitHub release and Plugin Hub request record the
exact source revision. The Hub submission changes only `plugins/bank-architect`.
Players receive this update after RuneLite reviews and merges the Hub request.
