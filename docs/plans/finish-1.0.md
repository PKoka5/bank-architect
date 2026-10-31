# Bank Architect: finish the product, then maintain it

**Published:** [1.0.0 publication checkpoint](../releases/1.0.0-publication.md).
GitHub release and issue cleanup are complete; Hub #19838 has a successful build
and awaits maintainer review. #50 closed completed; the unreproduced part of #48
closed not planned pending concrete reproduction. Earlier open-status entries
below describe the development history, not the current tracker.

**31 October publication decision:** the owner authorized releasing 1.0.0 and
replying to/closing the remaining issues. [The release record](../release-1.0.0.md)
supersedes the development-only pending status below: it records the 3,604-token
estimated reserve instead of the 5,000-token target, no additional combined
owner live-play run, and the honest unreproduced disposition of #48. The exact
release commit still receives a clean-clone build before publication. The
original plan/checkpoints remain an audit trail, not a new feature backlog.

Agreed direction and tracker review: **26 October 2026**. This plan supersedes
the open-ended feature stages in [the 2.0 exploration](bank-architect-2.0.md).
It authorizes no release, merge or automatic bank action. The owner subsequently
authorized starting implementation. Round 1 still has exit criteria open. The
finite automated matrix from round 2 is now implemented and passing; see
[the latest lifecycle checkpoint](../testing/finish-lifecycle-matrix-2026-10-26.md),
[runtime cleanup](../testing/finish-runtime-cleanup-2026-10-26.md), and
[the initial shield/defender checkpoint](../testing/finish-round-1-2026-10-26.md).
The later owner-requested Main diary grouping is verified in
[the diary checkpoint](../testing/main-diary-teleports-2026-10-26.md).
The maintenance entry point and source-build rehearsal are now recorded in
[the handover checkpoint](../testing/maintenance-rehearsal-2026-10-26.md), with a
[reviewable 1.0 draft](../release-1.0.0-draft.md). Completion gates remain open.
The 31 October owner-requested #50 fix is verified in
[the visage checkpoint](../testing/visage-components-2026-10-31.md).

## Finished product

A local, read-only bank planner for Main and Ironman accounts using the standard
eight-column bank. Players review a recommendation, adjust it, keep a preferred
layout and perform every real bank move themselves. A kept layout preserves
existing choices while new items append to the matching tab. Automatic planning
can produce a new arrangement when its inputs change; this is not a promise that
every automatic plan stays physically identical forever.

Completion does not require every possible personal preference, all game items
or every future feature request to become a bundled rule.

## Tracker disposition

The complete open listing had three issues and two PRs. No additional open
report or contributor reply had arrived since 23 October before this cleanup.
The latest published GitHub release was v0.9.9; local review work is unpublished.

| Item | Decision | Evidence or remaining work |
| --- | --- | --- |
| [#46](https://github.com/PKoka5/bank-architect/issues/46), Obsidian body | Closed, completed | Exact ID 21301 tier-3 fix shipped in 0.9.7, retained in 0.9.9; actual placement tests cover both presets. |
| [#47](https://github.com/PKoka5/bank-architect/pull/47), matching PR | Closed without merging | Targeted correction integrated separately; author explicitly welcomed closure. |
| [#49](https://github.com/PKoka5/bank-architect/pull/49), withdrawn Alch stock | Closed without merging | Reviewed-stock/placeholder fix integrated separately in 0.9.7. Price-based eligibility and removal of session retention were not adopted; author accepted that decision. |
| [#45](https://github.com/PKoka5/bank-architect/issues/45), wider bank grids | Closed, not planned | Final scope is standard eight columns; wider planner/editor/preview/guide support is not promised. |
| [#48](https://github.com/PKoka5/bank-architect/issues/48), bank reordering | **Remains open** | Alch-stock portion addressed; exact shield/defender withdrawal/return case remains unconfirmed. Related coverage is not proof of the reporter's exact case. |
| [#50](https://github.com/PKoka5/bank-architect/issues/50), Draconic visage | **Locally fixed, pending release** (31 October) | Three ordinary visage components now route to Combat in both presets, without wearable/BIS or automatic Alch eligibility. No issue closure/publication yet. |

Each thread received a specific explanation and thanks. Closing #49 explicitly
does not resolve the remaining shield/defender part of #48. The reporter was
asked for exact item names, before/after exports, retained-placeholder state and
automatic/saved-layout mode if the behavior persists on 0.9.9. Exports should be
checked for information the reporter does not want public.

## Three bounded rounds

### 1. Finish the existing fixes and regain review margin

- Reproduce #48 using the actual shield/defender identities and withdrawal/return
  sequence when supplied. Exercise the closest supported cases meanwhile. Only
  close the bug as fixed when the claim is supported; otherwise record exactly
  what is still needed. Do not invent a reproduction from a related test.
- Preserve the existing local Claude-review, ammo-label and kept-tab-move work.
- Audit behavior-neutral simplifications to regain token headroom. Hidden preset
  code and repeated setup are candidates, not approved deletions: first check
  references and stored-data compatibility. Do not discard old readers or move
  executable complexity into resources merely to improve a local estimate.
- Target a highest local estimate at or below **195,000**, strictly below the
  official 200,000 limit. The current checkpoint is 196,396; 3,604 estimated
  headroom is not official approval. Inspect non-main-Java changes separately.

Exit: all required tests pass; cleanup-only simulation reports remain identical;
functional changes have specific regressions; the owner checks the combined work
in game; #48 has an honest supported disposition; release preparation is ready.
Publication requires a separate explicit owner instruction. No intermediate
0.9.10 release is required merely to satisfy a version-number plan.

### 2. Prove a fixed set of completion promises

Reuse four existing fixtures: `real-bank-fixture-2026-07-19.tsv`,
`reported-midgame-combat-85.tsv`, `reported-void-combat-94.tsv` and
`main-bank-combat-2026-10-12.tsv`. Add at most one small reviewed beginner bank.
Reuse existing endgame placement tests and frozen gear facts; identify genuine
gaps instead of inventing full-account stats or claiming subset fixtures are
complete endgame banks.

Bound the main matrix to five banks, two presets, three modes (automatic,
accepted proposal, captured current bank) and six event sequences: **180 cases**.
Each sequence starts from a defined baseline; do not accidentally test an
arbitrary cumulative mutation chain.

1. Initial analysis and repeated analysis of identical inputs.
2. Add one reviewed item, with a separate unknown-ID control.
3. Withdraw to a placeholder and return the item.
4. Fully remove an item and later return it, without a placeholder.
5. Move a complete planner tab, including saved item routes.
6. Restart/reload persisted state and switch profiles away and back.

Use focused Fixed/Flexible Alch cases on two banks and existing specialist tests
for manual corrections, missing stats, set coherence and damaged data. Reuse
existing coverage when it already proves a criterion; do not multiply every
setting through the matrix or write tests that merely mirror implementation.

Required assertions, applied where relevant:

- No item/quantity/placeholder is invented or lost; no crash or unsafe guide
  action; every real action remains manual.
- Kept layouts retain existing relative item order and destinations through new
  items, placeholders and restart. Shifted absolute slot indices are not by
  themselves a reorder. Explicit whole-tab moves remap the kept records.
- Deliberate corrections and saved routes follow their documented precedence;
  operations that pause a kept snapshot do so visibly, without deleting it.
- Supported 0.9.9 data remains readable: profile, plan, correction, item order,
  block order and accepted snapshot. Future/damaged records remain preserved;
  unsafe guidance stops and explicit recovery is available.
- A valid unknown item uses the review destination, never automatic Alch.
  Invalid IDs are handled safely at the relevant boundary, not fabricated as
  bank entries. Explicit personal assignments are tested separately.
- Guide completion holds in supported Swap/Insert states. Unsupported filters or
  views give a clear preparation/recovery instruction instead of false progress.

Exit: the finite matrix and existing required checks pass, findings are resolved
or honestly scoped, and one full supported in-game workflow succeeds. There is
no five-fix quota: data loss, broken personal-choice guarantees and wrong guide
actions remain blockers. Optional requests do not expand this round.

### 3. Make maintenance transferable and declare completion

Write one short `MAINTAINING.md` as the entry point to existing documentation:
build/test commands, source-data review and generation, precedence rules,
compatibility/recovery, simulation baselines, token calibration, release checks
and issue triage. Link the existing detailed instructions rather than duplicating
them. Mark earlier roadmaps as historical directions, not extra obligations.

Implemented: [MAINTAINING.md](../../MAINTAINING.md), README entry point and a
passing source-only clean-build rehearsal. The rehearsal used warm dependency
caches and the uncommitted current source; a clean clone of the final release
commit remains a publication prerequisite. See the handover checkpoint above.

Perform a clean-checkout rehearsal using that document: review one item-data
change and prepare a release locally, without publishing. It must be possible
without private chat history. A final owner in-game check and explicit release
authorization precede publication. Version 1.0 is the proposed completion marker,
not a reason to skip unresolved blockers.

Exit: the maintenance rehearsal and completion criteria pass, the final release
is accepted for distribution, and the README states the supported scope and
maintenance policy. Feature development then stops.

## Explicitly outside the finish line

No new presets, wider grids, user-defined named groups, equipment/inventory reads,
boss-specific DPS/BIS engine, GE-price sorter, cloud service, usage telemetry,
upgrade journal or wholesale storage/UI redesign. Existing preview, explanations,
editor and kept-layout controls are sufficient unless the finite validation shows
a concrete failure of the stated product promise.

## Maintenance after completion

- Address reproducible crashes, data loss, broken guidance, unintended Alch and
  OSRS/RuneLite/Hub incompatibilities. Fixed Alch deliberately following the
  owner's literal list is not the same as an unintended Flexible Alch decision.
- Batch reviewed new-item identities, gear variants and classification mistakes
  when needed. Unknown-item fallback plus a manual correction can bridge a gap;
  each game update does not automatically require a plugin release.
- Personal tab/order/BIS preferences use existing controls. Thank users and close
  requests outside the supported scope with a clear explanation.
- No monthly release commitment or support SLA. Keep compatibility readers until
  their removal can be justified without silently abandoning stored user data.
- Keep every Hub submission below 200,000 official review tokens; aim for at
  least 5,000 estimated margin. If a necessary fix uses that reserve, recover
  headroom or reduce scope rather than treating the local estimate as approval.

A new request becomes work only if it restores the supported product promise or
compatibility, cannot be handled acceptably with existing controls, and has a
bounded testable change. A good optional idea is allowed to remain unbuilt.

## Current verification checkpoint

The last complete local build passed **2,618 tests and 1,950 scenario banks**,
including the **180-case domain lifecycle matrix**. The #50 fix intentionally
removes one ID/twelve occurrences from aggregate Cleanup; two reference hashes
were reviewed and updated, while standard reports remain unchanged. See
[the visage checkpoint](../testing/visage-components-2026-10-31.md),
[the Main diary checkpoint](../testing/main-diary-teleports-2026-10-26.md)
and [the lifecycle checkpoint](../testing/finish-lifecycle-matrix-2026-10-26.md)
for coverage boundaries and reused plugin-integration evidence. This is not a
completed release gate: round 1 still needs its headroom target and owner check,
and round 3 maintenance handover/rehearsal remains. No source branch, commit,
release or Hub submission was changed.
