# Bank Architect 0.7.0

Publication authorized by the owner on 2026-10-02 after the owner reported that the
ingame test round looked good. This is not an assertion that every checklist row
was individually reported or that the entire item catalog is verified.

## Player-facing changes

- A simpler RuneLite-style sidebar with clearer actions and better use of space.
- Assign an item's destination directly in the blueprint editor; existing saved
  item placements and category overrides remain authoritative.
- Improved hunter supplies, tools/containers, cosmetics, quest utilities and
  functional combat equipment. Better Guild hunter and Golden prospector grouping.
- Independently curated usage roles in item tooltips, including exact easy-clue
  requirements and skilling outfits.
- Conditional Tool Leprechaun, seed-vault and fancy-dress-box storage hints.
  Quest and clue guidance explains what to check before removing an item.
- Items with known additional uses stay out of automatic outclassed-gear selection.
  Explicit player assignments still win; all real bank movements remain manual.

## Update notice

`ReleaseNoticePanel.RELEASE_ID` and the Gradle version both change from 0.6.0 to
0.7.0. The bundled What's new text describes this release. The first sidebar
opening after installing this version shows the notice, including for players
who already acknowledged 0.6.0. Dismissal is saved locally per settings profile;
reopening and restarting do not repeat the acknowledged release. A later release
ID shows its notice again. No runtime update service or network request is used.

## Validation

- Full offline build: 1,122 tests, zero failures/errors/skips; all 1,950 bank
  simulations complete and four explicitly reviewed baseline hashes pass.
- Notice regression exercises acknowledgement of 0.6.0 followed by upgrading,
  dismissal, reopening, restart and a subsequent release. Notice rendered and
  visually inspected at 204px sidebar width.
- Owner reported the ingame test round looked good. The detailed scope and
  longer-term limits are in [the audit](research/item-role-audit-final-october-2.md)
  and [test checklist](testing/ingame-october-2.md).
- Jar inspection: 238 production classes, 19 non-class resource/metadata entries;
  no tests, simulator, research, documentation or screenshot payload.
- Forbidden-API scan found only existing `java.net.URI` metadata parsing;
  no network I/O, reflection, automation or external-process execution was added.
- Existing panel unchecked/unsafe-operations compiler note remains.
- Final highest local review estimate: **196,922**, headroom **3,078**, calibrated
  to `def1e856e101ff0e57dd96adccab2cf1d886b076` / maintainer count **200,414**.
  Resource, test and tooling changes were inspected separately. This is not an
  official Hub count or eligibility certification.

## Publication

The official Hub marker currently points to `33f3a6a3978c7b30c2211a35babb8a76afef124a`.
The previous 0.6.0 and Main-ordering submissions have merged. Publish this reviewed
source as v0.7.0, then open a new Hub update PR changing only `plugins/bank-architect`
to the actual new source commit. Hub availability follows the maintainer's merge
and distribution; publishing a GitHub release alone does not update Hub clients.
