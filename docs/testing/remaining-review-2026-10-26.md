# Remaining Claude review findings

Local follow-up above released `b3583ca8e48bedb37514e89006cb677e5b7ea75a`.
The owner explicitly chose to finish the remaining review points before release.
Release preparation was not started; no version, update notice, commit, tag,
remote branch, GitHub release or Plugin Hub submission was changed.

## Analysis work and retained Alch evidence

Settings writes continue to invalidate the visible result and advance the request
generation immediately. An explicit analyze request made while the settings
refresh is already queued now joins that refresh instead of starting redundant
capture/work. The queued refresh captures the latest plan/options once. Existing
coalescing and stale-result guards remain in place.

`BankAnalysis.invalidate(false)` cancels pending work without throwing away the
last successful request and Alch evidence. The existing no-argument invalidation
still clears both. RuneLite profile changes, active plugin-profile changes and
preset switches retain hard invalidation. Shutdown still clears all history.

Retaining evidence remains conditional: preset, Alch mode/enabled state,
Frequently Used behavior, personal category choices, saved item-order records,
previous item facts, slot multiplicity and stock checks must remain compatible.
Geometry, tag positions and block arrangements alone no longer invalidate
disposal evidence. A plan change with explicit saved per-item destinations still
forces reevaluation because it can activate or deactivate those routes. Changing
real Alch rules, prices, stats, quantities, choices or ownership still reevaluates.
This changes retention of an established session decision, not Alch eligibility.

The controlled event test posts real synchronous ConfigChanged events through
the plugin handler. A plan save plus options save queues one refresh, produces
one capture and one background job, and captures the latest plan/options.
Already-captured older work cannot publish after a subsequent settings change.
Main/Ironman placeholder cases keep established Alch across a settings refresh
and a harmless Alch-tab move, but disabling Alch returns normal classification.
Existing profile, quantity, price, stats, closure and stale-generation cases pass.

## Dark bow policy

The reviewed general layout now explicitly recognizes ordinary Dark bow,
four paint colours and the normal-game Deadman cosmetic: 11235, 12765, 12766,
12767, 12768 and 29611. All are known two-handed Ranged specialists. They stay
behind the reviewed general training weapons, and serve as a primary fallback
when no such weapon is owned. They have a shared Ranged-special role for secondary
grouping. This is an explicit layout preference, not a universal DPS ordering.

The nine-tick base speed is recorded separately from Rapid's eight ticks. The
restricted Bounty Hunter version 27853 is retained as a specialist but excluded
from the general primary pool. The temporary LMS copy 20408 remains excluded from
the candidate table. No Dark bow variant is added to either Alch list.

The development candidate compiler/check reproduces **437 Ranged identities in
163 groups** and unchanged **443 Magic identities in 151 groups**. The Ranged
count includes reviewed excluded/restricted states. Current rule descriptions
advance to 10; kept older targets remain supported.

Primary source verification used indexed Wiki content and the existing local
canonical identity snapshot; direct Wiki requests were robots-blocked:

- [Dark bow](https://oldschool.runescape.wiki/w/Dark_bow): two hands, ordinary
  variants, base speed and special-attack role.
- [Restricted BH version](https://oldschool.runescape.wiki/w/Dark_bow_%28bh%29):
  exact identity and permitted combat locations, not generic equipment.
- [Armageddon rewards](https://oldschool.runescape.wiki/w/Deadman%3A_Armageddon):
  normal cosmetic theme source for the Deadman appearance.

## The reported Alch data inconsistency

The claim that the three Adamant slash-stock entries were absent from candidates
is not reproducible against the checked-in table. IDs 1371, 1317 and 1331 are all
in both sets, and every slash-stock entry is already a candidate. A regression
pins that invariant. The existing full candidate-membership compatibility test
also remains unchanged. `alch-candidates.tsv` has no production diff; no items were
added simply to match the review's claim.

## Whole-tab move decision

The owner subsequently chose to let a kept blueprint follow complete tab moves.
That separate implementation and verification are recorded in
[the accepted-tab follow-up](accepted-tab-move-2026-10-26.md). Other personal
changes still pause the snapshot.

## Verification

Focused analysis/context, placeholder, profile/event and Dark bow tests pass.
The first complete build passes **2,412 tests**, zero failures/errors/skips,
and **1,950 scenario banks**, with all four existing simulation references
unchanged. The final complete pass, including the specialist grouping resource,
also passes all 2,412 tests and all 1,950 scenarios with the same four references.
No compiler warning appears in the final log, and whitespace checks pass.

Highest local main-Java estimate: **197,326**, leaving **2,674 estimated tokens**.
Other estimates: 194,295 / 196,990 / 194,626. Calibration remains the maintainer's
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official
Hub count. Candidate/group resources, tests and docs are inspected separately.

Logs are under `build/tmp/`: `remaining-review-settings-focused.log`,
`remaining-review-focused-final.log`, `remaining-review-full-first.log`,
`remaining-review-full-final.log` and `remaining-review-size.log`. Prior simulation
reports are preserved in `build/tmp/remaining-review-before/`.

The final local development JAR still has the existing 0.9.9 filename. It has
**938,169 bytes**, SHA-256
`a3cabc9985b8de529fea6f88ea04546f1d3d999f6e98c4ea5acbbdc0b4a9c8bb`.
This is not the published 0.9.9 artifact and has not been uploaded. HEAD remains
`b3583ca8e48bedb37514e89006cb677e5b7ea75a`. A local in-game smoke check of
save/refresh behavior remains useful; no live account with all Dark bow variants
is claimed. This artifact and estimate precede the whole-tab move follow-up.
