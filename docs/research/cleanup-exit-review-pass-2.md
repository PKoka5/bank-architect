# Cleanup exit review, pass 2

Date: 2026-07-19

## Protocol and aggregate result

This confirming Workstream A review used approved cleanup benchmark protocol
v1: seeds `20260718`, `314159265`, and `271828182`; 200 generated banks per
seed; three scenarios per generated bank; 1,800 scenario banks total; and
registry SHA-256
`449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038`.
All 1,800 scenario banks completed before and after the deferred resolutions.

| Measurement | Before | After | Moved from cleanup |
|---|---:|---:|---:|
| Distinct item IDs | 9,434 | 9,408 | 26 |
| Occurrences | 47,241 | 47,100 | 141 |

The input aggregate SHA-256 was
`8D697A27E3C0212DCB86476F7487FB446F4C4EE9FCAE2A65E46EC8B911930B65`.
The final aggregate SHA-256 is
`64DE0F23D622919EA7FBF3FFFF5FAF5BE5871DC1315795077F580F4F9B79A850`.

## Deferred-row resolutions

### Live cats

The local registry contains 35 unflagged, top-level, normal-game lifecycle
states. All are now routed to `CLUE` / `clues-cosmetics`:

| State group | Exact IDs | IDs |
|---|---|---:|
| Kittens | 1555-1560 | 6 |
| Grown cats | 1561-1566 | 6 |
| Overgrown cats | 1567-1572 | 6 |
| Lazy cats | 6549-6554 | 6 |
| Wily cats | 6555-6560 | 6 |
| Hellcat lifecycle | 7581-7585 | 5 |

Quest-specific `Witch's cat` (1491) and `Fluffs' kitten` (1554), plus dummy
states 24965-24970, are not members of the normal player cat family and stay
fail-closed. Twenty-five cat IDs were sampled by protocol v1; they account for
129 occurrences moved from cleanup.

### Legacy clue states

Each deferred clue declaration is top-level and has a display name, but the
local registry classifies it as `UNKNOWN` with `LOW` confidence and provides
no current-item or bankability fact. That is insufficient proof under the
repository's fail-closed rule. None is promoted; each receives the final
disposition **legacy/cache clue record, fail-closed**.

| itemId | Local gameval constant | Per-ID evidence and final disposition |
|---:|---|---|
| 3557 | `TRAIL_CLUE_HARD_SEXTANT029_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 3583 | `TRAIL_CLUE_MEDIUM_SEXTANT014_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 7246 | `TRAIL_CLUE_HARD_RIDDLE030_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 7318 | `TRAIL_CLUE_MEDIUM_SEXTANT027_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 12551 | `TRAIL_HARD_EMOTE_EXP5_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 19765 | `TRAIL_CLUE_MEDIUM_CIPHER002_CHALLENGE` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 19861 | `TRAIL_HARD_RIDDLE_EXP15_CASKET` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |
| 19897 | `TRAIL_HARD_ANAGRAM_EXP14_PUZZLEBOX` | Top-level but `UNKNOWN`/`LOW`; no local current-bankable evidence. Legacy/cache clue record, fail-closed. |

### Mystery box

ID 6199 is the unflagged, top-level `MACRO_QUIZ_MYSTERY_BOX` declaration and
is now routed to `UNIQUE` / `slayer-boss-loot`. The only same-family sibling
found locally, ID 18086, is explicitly `PLACEHOLDER` and remains excluded.
ID 20703 (`WINT_REWARD_BOX`) is a separately declared supply/reward-crate
family, not a mystery-box state. ID 6199 contributed 12 sampled occurrences.

## Fresh top-250 confirmation

The confirmation passed on the first attempt; no restart was required. It
found no new clearly bankable player-facing family or classification bug.
The final top 250 reconciles exactly as follows:

- 239 rows are unchanged from the complete table in
  [pass 1](cleanup-exit-review-pass-1.md) and retain the exact disposition and
  reason recorded there.
- The eight legacy clue rows listed above remain at their aggregate positions,
  but their deferred disposition is replaced by the final non-player/cache
  fail-closed disposition documented per ID above.
- The three resolved rows 1560, 6199, and 7581 leave the top 250 after routing.
- Three rows newly enter the top 250 and are dispositioned below.

| itemId | Canonical name | Occurrences | Disposition | Evidence |
|---:|---|---:|---|---|
| 21001 | Twisted Buckler | 12 | Non-player/cache | Local registry declaration is `CERT`; excluded copy. |
| 21017 | Dinhs Bulwark | 12 | Non-player/cache | Local registry declaration is `PLACEHOLDER`; excluded copy. |
| 21210 | Birthday Balloons | 12 | Non-player/cache | Local registry declaration is `PLACEHOLDER`; excluded copy. |

This compact reconciliation covers all 250 final rows: 239 unchanged pass-1
rows, eight finalized clue rows, and three new rows.

| Final disposition | Rows |
|---|---:|
| Legitimate cleanup | 67 |
| Non-player-facing or cache record | 183 |
| Intentionally deferred borderline | 0 |
| Classification bug / clearly bankable item | 0 |
| **Total** | **250** |

## Workstream A closing totals

| Round or pass | IDs moved from cleanup | Occurrences moved from cleanup |
|---|---:|---:|
| Round 1 (single-seed era) | 29 | 102 |
| Round 2 | 104 | 747 |
| Round 3 | 91 | 495 |
| Exit review pass 1 (gross curation) | 54 | 348 |
| Exit review pass 2 | 26 | 141 |
| **Cumulative** | **304** | **1,833** |

The final protocol-v1 cleanup remainder is 9,408 distinct IDs and 47,100
occurrences. Pass 1's gross moved count is used for cumulative curation
accounting; its explicit cleanup corrections explain the smaller net aggregate
change recorded in that pass.

## Definition-of-done checklist

1. **Satisfied:** protocol v1 uses three fixed seeds, 200 generated banks per
   seed, all three scenarios, and 1,800/1,800 completed scenario banks.
2. **Satisfied:** every final top-250 row has an explicit disposition through
   the pass-1 table and the compact reconciliation above.
3. **Satisfied:** zero classification bugs or clearly bankable families remain
   in the final top 250.
4. **Satisfied:** all 11 formerly deferred rows have final maintainer
   resolutions; no deferred row remains.
5. **Satisfied:** this fresh second review used the same protocol and registry
   revision and found no new bankable family; it passed on the first attempt.
6. **Satisfied:** final totals, cumulative moved totals, and residual
   disposition counts are recorded above.
7. **Satisfied:** negative controls 0, -1, and an unknown high ID remain in
   cleanup; the full tests, fixed 50-bank simulation (150/150 completed),
   aggregate (1,800/1,800 completed), and build gates pass.

All seven Workstream A cleanup-loop exit conditions are satisfied.
