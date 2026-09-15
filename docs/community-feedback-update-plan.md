# Community feedback update plan — 2026-09-15

## Objective and ownership

Make bank guidance reliable and give players control over the proposed blueprint.
The plugin proposes; the player can revise the proposal and moves every real bank
item manually. The maintainer explicitly authorized Codex to implement and test as
well as research/design/review on September 15, superseding that role split in
AGENTS.md for this work. This document is the implementation plan, not a claim that the
features or fixes are implemented. Do not commit, push, merge, switch branches or
publish without a separate explicit instruction.

Implement in small slices: reliability, placement correctness, then blueprint editing.
Release version: 0.6.0. Publication authorized by the maintainer on September 15.

## Feedback ledger

Sources checked during this conversation:

- [Reddit update thread](https://www.reddit.com/r/BankTabs/comments/1w35knq/bank_architect_update/).
  The fetched page was cached; private and collapsed replies were not fully available.
- [Repository issues and PRs](https://github.com/PKoka5/bank-architect/issues).
- SkypeFriends' private message supplied by the maintainer: assigning platinum
  tokens to Frequently Used does not produce the expected placement. Desired result:
  coins first, platinum tokens second. Keep private message text out of public fixtures.

| Feedback | Evidence/status | Planned action |
| --- | --- | --- |
| Deposit is treated as an incorrect drag; guide gets stuck | kevmuko's [PR #38](https://github.com/PKoka5/bank-architect/pull/38), open at review | Review and reuse the contribution where correct; do not independently duplicate it or merge without instruction. |
| Rune pickaxe promoted when dragon pickaxe is a placeholder | StylianosGakis' [#37](https://github.com/PKoka5/bank-architect/issues/37), open | Make quick-tool choice stable using bank placeholder evidence. |
| Platinum tokens do not follow the intended manual placement | Maintainer-supplied PM; not reproduced yet | Trace assignment, routing and final layout; fix at the failing layer. |
| Blood Moon/Eclipse and crystal sets split or displaced | Reddit reports; later PM examples mentioned but not provided | Reproduce against current code before changing gear rules. |
| Mystic hat misplaced and black wizard hat preferred | Reddit report | Separate classification from layout ranking; test both. |
| Two Blue Moon sets with different charges rejected | Reddit; feldrok's [#21](https://github.com/PKoka5/bank-architect/pull/21) already merged | Regression-check multiplicities and physical occurrences, not another blanket duplicate fix. |
| Fillers leave SYNCING BANK indefinitely | Reddit report | Reproduce; provide a supported outcome or actionable recovery instead of indefinite progress. |
| Clue/cosmetic equipment should go to clue tab | Reddit preference | Validate exact examples; respect overrides, do not route every clue reward away from useful gear. |
| Edit blueprint before moving bank items | Reddit and maintainer request | Implement the staged editor below. |
| Organize only an individual tab | Reddit feature request | Separate follow-up; editing one preview tab does not mean guidance is limited to that bank tab. |
| Graceful recolours misclassified | [#36](https://github.com/PKoka5/bank-architect/issues/36) closed, fix on main | Retain regression coverage; do not rebuild an existing fix. |
| Positive feedback: preview, personalised layout, category corrections | Reddit and PM | Preserve preview-first workflow and automatic defaults. |

Existing work to preserve: kevmuko's merged set grouping (#24), sort options (#26),
Frequently Used gathering switch (#29), block arrangement (#33), tag weaving (#34),
and feldrok's repeated-entry support (#21). PR #35 was declined: retain both settings
entry points. Hub [0.5.0 submission](https://github.com/runelite/plugin-hub/pull/16026)
was merged on September 5; recheck live status before preparing another release.

## 1. Reliability first

### Deposit/withdrawal recovery

Review PR #38 against current `IronmanBankArchitectPlugin`, bank snapshot and guide
session code. Inspect the actual diff and tests rather than relying on its description.
Distinguish quantity-only changes, new/removed IDs, duplicate-count changes and actual
rearrangements. A normal deposit must never instruct the player to undo a drag that
did not happen. With auto-guide enabled, coalesce changes into a fresh analysis;
with it disabled, clearly request analysis. Stale analysis must not overwrite a newer
snapshot, profile choice or blueprint edit. Include placeholder release, bursts of
events and a real wrong drag in verification.

### Placeholder-aware tools

`organize/IronmanQuickToolSelector.java` currently excludes all placeholders before
selecting the highest tier. Use canonical bank placeholder evidence when choosing a
tool; do not read inventory or equipment or assume where an absent item is located.
Check downstream main-tab layout so the placeholder keeps its intended position.
Respect manual overrides and the gathering switch. Test dragon placeholder + rune
item, dragon deposit, placeholder release, axes, and gathering disabled.

### Fillers and repeated items

Use small synthetic bank fixtures for fillers at the start/middle/end and duplicate
charged entries. Preserve every physical item occurrence. Do not instruct players to
remove legitimate charged items as duplicates. Do not invent filler items or empty
cells. If fillers are unsupported by guidance, terminate preparation with a specific
explanation and manual recovery; supporting arbitrary filler geometry is separate work.

## 2. Manual destination and gear placement

Relevant seams: `override/UserCategoryOverrides`, `organize/BankTags`,
`BankOrganizationPreviewBuilder`, `PresetItemSorter`, main-tab semantic rules, the
panel and the plugin's assignment menu/config persistence.

The current builder already applies a pinned tag after automatic category decisions
and carries routed tags downstream. Therefore, the PM does not establish that the
currency classifier overrides the assignment. Reproduce the full path on the current
version, including loading a saved override and the shaped main-tab layout.

Acceptance:

- Assign platinum tokens to Frequently Used: effective destination, preview label,
  count and layout must agree, including after restart and reanalysis.
- Offer a clear per-item return to automatic assignment, using existing removal
  semantics where possible. This clears the player's override; it does not delete
  built-in item metadata or require an item to have no destination.
- Explicit placement in the editor can put coins first and tokens second, independent
  of their automatic currency grouping.
- Test gear examples using exact locally verified IDs and actual set/slot metadata.
  Keep sets whole where the chosen layout promises this. Manual choices still win.
- A report without a bank export/version remains unverified; do not claim it fixed
  solely because an adjacent grouping PR was merged.

## 3. Blueprint editing

### First complete slice: reorder within one tab

Add an explicit edit mode to the existing Bank Blueprint window, hosted in
`IronmanBankArchitectPanel`. Prefer a small dedicated editor component/model to
expanding the panel's existing responsibilities. Review `BlockArrangements` and
`BankLayoutProfiles` before introducing persistence; block order is not equivalent
to arbitrary item positions in a shaped grid.

Interaction contract:

1. Enter edit mode on a selected tab, starting from its displayed proposal.
2. Drag an item to an insertion marker; occupied targets insert and shift subsequent
   items, rather than silently swapping. Explain this in a short UI hint.
3. Show the resulting dense layout immediately. The edit affects one physical
   occurrence, with equivalent duplicate occurrences interchangeable.
4. Provide Undo, Cancel and Save. Cancel discards the draft; Save records the layout
   for the active saved profile and regenerates guidance from the current bank.
5. Provide a per-tab reset to the automatic proposal. Resetting positions must not
   silently clear unrelated manual category choices.

Persistence and planning contract:

- For an edited tab, explicit item order takes priority over curated geometry and
  block order. Unedited tabs keep existing automatic output exactly.
- Store a versioned ordered sequence based on canonical identity and multiplicity,
  not raw live slot indexes or item names. Resolve duplicate occurrences against
  the current snapshot without dropping or creating entries.
- Keep absent entries dormant. Existing present entries retain their chosen relative
  order; genuinely new items append in curated order. Returning items recover their
  saved relative position. Do not reserve empty cells for truly absent items.
- Coins and tokens stay at the front while present, after reanalysis, restart and
  profile round-trip. A bank placeholder is an existing entry, not an invented gap.
- Curated set shapes may be deliberately broken in a manually edited tab; make the
  change of mode visible and resettable. Do not silently drag an entire set when
  the player selected one item.
- Preview, export and guidance consume the same resolved final plan. Editing or
  changing profiles invalidates any previously pinned move and stale analysis result.
- Saving cannot exceed supported bank capacity or produce invalid destinations.

### Second slice: move between tabs

Build only after the first slice passes. A cross-tab edit records an explicit local
destination and insertion order atomically. If the target tab contains several tags,
offer a tag choice instead of guessing from the target cell. A dedicated item-tab
override may be necessary: do not move an entire tag to move one item. Define how
tab removal and later tag reassignment affect saved item destinations before shipping.
Preserve the source tab's remaining order and provide the same Undo/Cancel/Save flow.

### Separate follow-up: guide only one bank tab

Design a true scope-limited plan. Other bank tabs must remain outside the required
moves, and cross-tab dependencies need an explicit resolution. Do not advertise this
as part of the initial editor unless routing, guidance and recovery all enforce it.

## 4. Verification and release gate

Local review and release preparation completed on September 15. Two additional
editor defects were reproduced and fixed: a rejected Save could re-enable while
both source/latest previews were null, and a rejected reset could keep old tab
contents visible. Four new tests cover asynchronous saves/resets, full capacity
and category changes after a cross-tab move. Full validation passes with 1,088
tests and 1,950 simulations; baseline hashes are unchanged.

Candidate version, contributor credits, jar/API review and the Hub update handoff
are recorded in [release-0.6.0.md](release-0.6.0.md). Live RuneLite acceptance and
publication remain pending. This is not a claim of Hub approval or a released build.

Implement one slice at a time, explain files before editing and report tests.
Review behavior and evidence before progressing to the next slice.

- Targeted regression tests for every reproduced defect and editor acceptance case.
- Editor checks: cancel/undo/reset, restart/profile round-trip, duplicate identities,
  disappearing/returning items, new items, concurrent scan completion and capacity.
- Run `./gradlew test` and `./gradlew build`; build includes the repository's fixed-seed
  simulation baseline check. Use `simulateRandomBanks` and `aggregateCleanupReview`
  reports to inspect any behavior changes. Never automatically replace baseline hashes.
- Review expected changed layouts explicitly; preserve unchanged defaults where the
  slice promises compatibility. Report every warning/failure and unavailable live check.
- Live RuneLite smoke test: deposit/withdrawal recovery, placeholder tools, Blue Moon
  duplicates, fillers, platinum assignment and saved coins/tokens ordering. All real
  bank movements remain manual. Test both Swap and Insert guidance.
- Update `docs/bank-tab-mechanics.md`, `docs/live-smoke-test-checklist.md` and README
  for shipped behavior; prepare release notes with contributor credit and limitations.
- Prepare the version change and Hub submission only after review. Publishing/merging
  remains a separate explicitly authorized action.

No runtime network, telemetry, reflection, native code, external processes, game
state mutation, or automated mouse/keyboard/bank actions are permitted. Blueprint
mouse handlers only edit local planner state. Do not copy third-party plugin designs.

## Immediate implementation handoff

Start with PR #38 review and the placeholder regression in #37. Next reproduce the
platinum-token assignment path and remaining Reddit cases. Then implement and verify
the within-tab editor as a complete usable slice before cross-tab editing. Keep this
ledger updated with reproduced/fixed/tested/deferred status; do not mark reported
issues resolved until the relevant acceptance checks pass.

## Step 1 implementation record — September 15

Implemented locally by Codex under the maintainer's explicit authorization:

- Reused kevmuko's PR #38 recovery/session change and bank-event integration without
  merging the PR. Review found that its unique-ID comparison misses extra physical
  copies and stack/placeholder changes; replaced it with an order-independent map
  of all physical quantities per canonical ID. Pure rearrangements compare equal.
- Quick-tool selection now includes bank placeholders. Tests cover withdrawal and
  return, placeholder release, axes, manual assignment, and gathering disabled.
- Confirmed existing filler detection runs before view synchronization. Added tests
  for fillers at different positions and for real charged copies not being fillers.
  Filler layouts remain unsupported; the existing message explains manual recovery.
- Removed the generic instruction to release duplicate placeholders when extra
  copies have no proven placeholder recovery target. Ask for analysis instead.
- Added/extended regressions for Blue Moon physical copies, Swap/Insert completion,
  changed-content recovery, restored advice and queued analysis bursts.

Validation: `gradlew.bat test build` passed: 1,063 tests, 150/150 random-bank
scenarios, 1,800/1,800 cleanup scenarios, all four simulation baselines unchanged.
The initial full run caught an incorrect new assertion counting placeholders in
the tag counter; corrected to check the actual blueprint destination as well.
Compilation reported the existing unchecked/unsafe-operations note in
`IronmanBankArchitectPanel`; this slice does not modify that class.

Live bank-event wiring, overlay rendering and actual player actions remain untested
in RuneLite. The September 15 section of `live-smoke-test-checklist.md` records the
remaining release checks. No version bump, commit, branch change, PR merge or
publication was performed. Subsequent steps are recorded below.

## Step 2 implementation record — September 15

- Reproduced platinum's incorrect same-tab ordering and corrected main sorting
  to use the effective tag. Old automatic geometry no longer pulls a retagged
  item back to its catalogue role. Tag metadata survives physical expansion and
  appears in preview tooltips and exports.
- Existing override persistence and per-item clear action verified with a
  save/load/reset case. No duplicate UI control added.
- Reproduced black Wizard hat 1017 outranking mystic hat 4089. Added the missing
  starter tier for 1017, with equipment evidence and scope documented in
  `research/community-feedback-placement-review.md`.
- Verified existing Crystal tie-breaking and Moon tier/column fixes; added a
  three-Moon-set List regression and exact-ID clue override checks.
- Exact coins/tokens slot editing remains step 3. Original full-bank reports and
  the separate mystic-to-boss-loot symptom are not declared resolved by synthetic
  cases. The maintainer will run live checks after the implementation sequence.

Validation: `gradlew.bat test build` passed with 1,071 tests, zero failures/errors,
150/150 random-bank scenarios, 1,800/1,800 cleanup scenarios and all four unchanged
simulation baselines. Cleanup coverage remained 9,335 distinct IDs and 46,731
occurrences. The two new reproduction tests failed before the fixes and passed
afterward. Compilation still reports the panel's existing unchecked-operations
warning. No version bump, commit, merge or publication.

## Step 3 implementation record — September 15

- Added a dedicated Swing blueprint editor and local draft model: insertion drags,
  Undo, Cancel, Save and per-tab reset. Coins and tokens can occupy the first two slots.
- Cross-tab header drops move one physical occurrence; multiple tags prompt for a
  destination choice. Empty unassigned tabs require assigning a tag in Layout first.
- Versioned per-profile persistence preserves absent entries and physical duplicates.
  Saved destinations precede manual order; preview, export and guidance share the result.
- Source and target edits save together after current-preview, profile/config, live
  bank-content and multiplicity checks. Stale drafts cannot overwrite newer analysis.
- Explicit order overrides automatic geometry and block arrangements. Reset clears
  the selected tab's order and incoming individual destinations, preserving unrelated
  category corrections. Changed item/target tag assignments make routes dormant.
- Added persistence, duplicate, reset, missing/new item, future-format, stale draft,
  Undo/Cancel and real Swing mouse-event regressions, including cross-tab header drops.

Validation: `gradlew.bat test build` passed: 1,084 tests, zero failures/errors,
150/150 random-bank scenarios, 1,800/1,800 cleanup scenarios, four unchanged baselines.
Reviewed the rendered Swing test image. One test initially used an invalid tag name;
another run was stopped because its multi-tag destination opened the expected modal
choice. The automated header-drop test now uses a single-tag destination.
The existing panel unchecked-operations compiler note remains. Live RuneLite checks,
including the multi-tag modal and actual game event wiring, are pending in the smoke
checklist. No version bump, commit, branch change, merge or publication.
