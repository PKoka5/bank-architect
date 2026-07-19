# Charged jewellery slice 1

Date: 2026-07-19

## Scope and evidence

This first Phase 3A slice ships six complete jewellery families using exact
item IDs. Membership was reviewed against the checked-in
`docs/research/item-id-research-index.tsv`, generated from the locally cached
RuneLite 1.12.32 `net/runelite/api/gameval/ItemID.java`, and cross-checked
against the production item registry. No display-name, suffix, or adjacent-ID
rule supplies membership or charge facts.

All 48 shipped states have a separate exact classification override to
`TELEPORT` and map explicitly to the IRONMAN `currency-utilities` tab. The
shared item-sort metadata catalog supplies only family membership and order;
it cannot change routing.

## Ordering policy

The maintainer-proxy policy applied here is:

1. never-degrading states first;
2. imbued states before equivalent non-imbued states;
3. finite charges high to low;
4. uncharged states last.

None of these six families has a current player-facing imbued state in the
local index. `CHARGE` value `2147483647` is the explicit never-degrading
sentinel for eternal glory, while `CHARGE` value `0` denotes an uncharged
state. Equal-charge standard and trimmed glories retain a deterministic
name/item-ID fallback; trimmed appearance does not imply a different charge.
This proxy ordering policy, including the equal-charge trimmed tie-break,
should be confirmed by the maintainer before later slices treat it as settled.

## Shipped manifests

### Ring of dueling — 8 states

| State | Exact ID |
|---|---:|
| 8 charges | 2552 |
| 7 charges | 2554 |
| 6 charges | 2556 |
| 5 charges | 2558 |
| 4 charges | 2560 |
| 3 charges | 2562 |
| 2 charges | 2564 |
| 1 charge | 2566 |

The final charge destroys the ring, so no uncharged object exists. Excluded:
CERT IDs 2553, 2555, 2557, 2559, 2561, 2563, 2565, and 2567; PLACEHOLDER IDs
16358 and 21455.

### Games necklace — 8 states

| State | Exact ID |
|---|---:|
| 8 charges | 3853 |
| 7 charges | 3855 |
| 6 charges | 3857 |
| 5 charges | 3859 |
| 4 charges | 3861 |
| 3 charges | 3863 |
| 2 charges | 3865 |
| 1 charge | 3867 |

The final charge destroys the necklace, so no uncharged object exists.
Excluded: CERT IDs 3854, 3856, 3858, 3860, 3862, 3864, 3866, and 3868;
PLACEHOLDER IDs 16362 and 21460.

### Burning amulet — 5 states

| State | Exact ID |
|---|---:|
| 5 charges | 21166 |
| 4 charges | 21169 |
| 3 charges | 21171 |
| 2 charges | 21173 |
| 1 charge | 21175 |

The final charge destroys the amulet, so no uncharged object exists. Excluded:
CERT IDs 21167, 21170, 21172, 21174, and 21176; PLACEHOLDER IDs 21168 and
21463.

### Necklace of passage — 5 states

| State | Exact ID |
|---|---:|
| 5 charges | 21146 |
| 4 charges | 21149 |
| 3 charges | 21151 |
| 2 charges | 21153 |
| 1 charge | 21155 |

The final charge destroys the necklace, so no uncharged object exists.
Excluded: CERT IDs 21147, 21150, 21152, 21154, and 21156; PLACEHOLDER IDs
21148 and 21462.

### Skills necklace — 7 states

| State | Exact ID |
|---|---:|
| 6 charges | 11968 |
| 5 charges | 11970 |
| 4 charges | 11105 |
| 3 charges | 11107 |
| 2 charges | 11109 |
| 1 charge | 11111 |
| Uncharged | 11113 |

Excluded: CERT IDs 11106, 11108, 11110, 11112, 11114, 11969, and 11971;
PLACEHOLDER IDs 16264, 21449, and 21450.

### Amulet of glory — 15 states

| Charge/state | Standard exact ID | Trimmed exact ID |
|---|---:|---:|
| Eternal / never degrading | 19707 | — |
| 6 charges | 11978 | 11964 |
| 5 charges | 11976 | 11966 |
| 4 charges | 1712 | 10354 |
| 3 charges | 1710 | 10356 |
| 2 charges | 1708 | 10358 |
| 1 charge | 1706 | 10360 |
| Uncharged | 1704 | 10362 |

The resulting deterministic run is eternal first, then each charge tier from
6 to 1, then the two uncharged states. Excluded:

- CERT IDs 1705, 1707, 1709, 1711, 1713, 10355, 10357, 10359, 10361,
  10363, 11965, 11967, 11977, 11979, and 19708;
- PLACEHOLDER IDs 16091, 16346, 19709, 21443, 21444, 21453, and 21454;
- internal dummy ID 10719;
- POH trophy/build object ID 8283;
- Last Man Standing-only ID 20586.

## Deferred candidates

None. All six proposed families had complete normal-game player-facing state
coverage in the local gameval index. Activity-only, cache, and construction
records listed above are deliberate exclusions, not partial family members.

## Verification and benchmark

Protocol v1 before this slice contained 9,408 cleanup IDs / 47,100
occurrences, with aggregate SHA-256
`64DE0F23D622919EA7FBF3FFFF5FAF5BE5871DC1315795077F580F4F9B79A850`.
After the slice it remains 9,408 cleanup IDs / 47,100 occurrences with the
same SHA-256. All states already had curated teleport destinations, so adding
exact routing and ordering metadata correctly made no cleanup change. The
fixed simulation completed 150/150 scenario banks and the aggregate completed
1,800/1,800 scenario banks.
