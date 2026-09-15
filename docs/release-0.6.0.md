# Bank Architect 0.6.0 — release preparation

Status: release 0.6.0, publication authorized by the maintainer on 2026-09-15.
The maintainer confirmed the click editor works in game and authorized release
after the What's new screen was added. Other live scenarios have not been
individually confirmed; the smoke checklist preserves that distinction.

## Release notes

### Arrange your blueprint

- A dismissible What's new screen appears when you first open the sidebar after
  an update. Dismissal is saved locally per RuneLite settings profile.

- Click an item to select it with a green border, choose Swap or Insert, then
  click its target slot. Put coins first and platinum tokens second, for example.
- Move an item between blueprint tabs by dropping it on a tab header. Choose its
  destination tag when that tab contains several tags.
- Undo, Cancel and Save let you review changes before applying them to your local
  blueprint. Orders are saved per profile and used by the preview, export and guide.
- Reset a tab's order and incoming item moves to return to automatic placement.
- Missing items retain their saved place for when they return; new items append.

### Fixes

- Bank deposits and withdrawals trigger fresh analysis with auto-guide enabled.
  Pure rearrangements do not restart analysis. Physical copies, quantities and
  placeholders are included when detecting changes.
- Quick-tool selection respects placeholders, so withdrawing a better tool does
  not promote a lower-tier tool into its place.
- Assigning a main-tab item to Frequently Used now changes its sorting role.
- The black Wizard hat no longer outranks the mystic hat through a missing tier.
- Duplicate-item recovery no longer generically asks players to remove legitimate
  charged items as placeholders.
- A rejected editor Save stays disabled until the stale draft is cancelled;
  a rejected reset refreshes the displayed preview.

Thanks to kevmuko for the deposit/withdrawal recovery contribution in
[PR #38](https://github.com/PKoka5/bank-architect/pull/38), reused and extended with
physical-quantity comparison. Thanks to StylianosGakis for the placeholder report
in [#37](https://github.com/PKoka5/bank-architect/issues/37), and to community testers
for placement and blueprint-editing feedback. Existing repeated-entry work by
feldrok and grouping/layout work by kevmuko are preserved and regression-tested.

### Behaviour and limitations

All real bank moves remain manual. Editing affects the local blueprint only.
Manual ordering can deliberately break automatic set shapes. Resetting a destination
tab releases its incoming item moves; it does not clear unrelated category corrections.
Moving a destination tag or changing an item's category correction makes incompatible
saved item destinations inactive. Empty tabs need a tag before accepting an item.
Bank fillers remain unsupported by guidance. Guiding only one bank tab is separate
future work. Synthetic tests do not establish that every reported gear layout is fixed.

## Verification record

- `gradlew.bat test build`: 1,092 tests, zero failures/errors (including click selection, Swap/Insert and release notices).
- 150/150 random-bank and 1,800/1,800 cleanup scenarios completed.
- Four fixed simulation baselines unchanged; no baseline updates.
- Review tests reproduced two asynchronous editor failures before the fixes.
- Added full 1,410-entry persistence/order and changed-category destination tests.
- Inspected the pre-notice candidate jar: 235 production classes and 16 non-class resource/metadata files;
  no test, simulator, research, documentation or screenshot payload.
- Production forbidden-API scan: the sole network-package match is existing URI
  parsing for bundled source metadata, not network I/O. No new runtime dependencies.
- Existing `IronmanBankArchitectPanel` unchecked-operations compiler note remains.
- The maintainer confirmed the click-selection and Swap/Insert editor interaction
  works in game. Remaining live checks are tracked in [the smoke checklist](live-smoke-test-checklist.md).

## Hub update handoff

The [official Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md#updating-a-plugin)
were read on 2026-09-15: updates change the existing plugin marker's commit hash
and go through a pull request and Hub checks. Publication must use the final
reviewed commit hash, not the previous candidate's hash.

Publication procedure (authorized by the maintainer):

For each future release, update `ReleaseNoticePanel.RELEASE_ID` and its bundled
notes together. The ID is independent of the Gradle SNAPSHOT suffix; rebuilding
the same release does not repeat the notice. No runtime update check is performed.

1. Record Swap/Insert guidance, deposits/withdrawals, placeholders, duplicate Moon
   items, fillers, platinum assignment, editor drags, restart and profile results.
2. Change the development version to `0.6.0` and rebuild the final candidate.
3. With explicit publication authorization, commit/push the reviewed changes and
   use that actual full commit hash in `plugins/bank-architect`.
4. Open the Hub update PR, review current feature rules and require Hub CI/review.

Prepared PR title: `bank-architect: blueprint editor and bank-change recovery`

Prepared PR body:

> Adds local blueprint item reordering and cross-tab moves with Undo/Cancel/Save
> and per-profile persistence. Improves deposit/withdrawal recovery, placeholder
> tool selection and manual main-tab placement. All game bank movements remain
> manual. Local tests and fixed simulation baselines pass; attach the completed
> live smoke results and final candidate commit before submission.
