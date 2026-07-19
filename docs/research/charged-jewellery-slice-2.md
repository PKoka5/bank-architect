# Charged jewellery slice 2

Date: 2026-07-19

## Scope and evidence

This Phase 3A slice migrates the four families formerly held in
`IronmanMainItemSorter.LEGACY_CHARGED_JEWELLERY` to the shared item-sort
metadata catalog. Membership was reviewed against the checked-in
`docs/research/item-id-research-index.tsv`, generated from the locally cached
RuneLite 1.12.32 `net/runelite/api/gameval/ItemID.java`, and cross-checked
against the production registry. No display-name, suffix, or adjacent-ID rule
supplies membership or charge facts.

All 33 shipped states have an exact classification override to `TELEPORT` and
map to the IRONMAN `currency-utilities` tab. Metadata controls membership and
order only. The legacy map and fallback path were deleted, so all charged
jewellery sorting now reads the shared catalog.

## Ordering policy

Slice 1's policy is unchanged: never-degrading states first, imbued states
before non-imbued states, finite charges high to low, and uncharged states
last. `CHARGE` value `2147483647` is the never-degrading sentinel and value
`0` denotes an uncharged state.

The ring of wealth uses two explicit catalog groups under the stable
`jewellery.ring_of_wealth` prefix. The complete `.imbued` run sorts before the
complete `.standard` run; both preserve literal charge values 5 through 0.
This avoids inventing encoded charge values or inferring imbued status from
display names.

## Shipped manifests

### Combat bracelet — 7 states

| State | Exact ID |
|---|---:|
| 6 charges | 11972 |
| 5 charges | 11974 |
| 4 charges | 11118 |
| 3 charges | 11120 |
| 2 charges | 11122 |
| 1 charge | 11124 |
| Uncharged | 11126 |

Excluded: CERT IDs 11119, 11121, 11123, 11125, 11127, 11973, and 11975;
PLACEHOLDER IDs 16266, 21451, and 21452.

### Digsite pendant — 5 states

| State | Exact ID |
|---|---:|
| 5 charges | 11194 |
| 4 charges | 11193 |
| 3 charges | 11192 |
| 2 charges | 11191 |
| 1 charge | 11190 |

The local index declares no uncharged or depleted digsite pendant. ID 11195
is the quest item `NECKLACE_OF_DIGSITE` (Clean necklace), not a depleted
pendant state. Excluded: quest item 11195; PLACEHOLDER IDs 18818, 18819, and
21468; POH-mounted object 22709.

### Ring of wealth — 12 states

| Charge/state | Imbued exact ID | Standard exact ID |
|---|---:|---:|
| 5 charges | 20786 | 11980 |
| 4 charges | 20787 | 11982 |
| 3 charges | 20788 | 11984 |
| 2 charges | 20789 | 11986 |
| 1 charge | 20790 | 11988 |
| Uncharged | 12785 | 2572 |

The imbued family is complete in the local index and is therefore shipped,
not deferred. Excluded: CERT IDs 2573, 11981, 11983, 11985, 11987, and 11989;
PLACEHOLDER IDs 15089, 16361, 21456, 21457, 21458, and 21459; ring-of-wealth
imbue scroll 12783, which is an input item rather than a jewellery state.

### Slayer ring — 9 states

| State | Exact ID |
|---|---:|
| Eternal / never degrading | 21268 |
| 8 charges | 11866 |
| 7 charges | 11867 |
| 6 charges | 11868 |
| 5 charges | 11869 |
| 4 charges | 11870 |
| 3 charges | 11871 |
| 2 charges | 11872 |
| 1 charge | 11873 |

Excluded: PLACEHOLDER IDs 14008, 21269, and 21441. The local index declares
no player-facing uncharged state.

## Slayer-ring routing decision

Slayer rings remain `TELEPORT → currency-utilities`. Although their use is
slayer-oriented, their bank function is reusable teleport jewellery. This
preserves the existing IRONMAN destination, keeps the charge run in the Main
quick-access sorter, and matches the teleport-jewellery routing established
by slice 1. The destination is supplied by exact classification overrides,
not inferred by metadata.

## Deferred candidates

None. The ring-of-wealth imbued states were fully verifiable locally, and all
four requested families have complete normal-game player-facing coverage.
The records listed as exclusions are deliberate non-members, not deferred
states.

## Verification and benchmark

Protocol v1 before this slice contained 9,407 cleanup IDs / 47,091
occurrences, with aggregate SHA-256
`5B355BBD9593387AFCA7E3571AEB59ABC50CB660584475B1143A8C6031E8A37C`.
After this slice the aggregate remains 9,407 cleanup IDs / 47,091
occurrences, with the same SHA-256
`5B355BBD9593387AFCA7E3571AEB59ABC50CB660584475B1143A8C6031E8A37C`.
All 33 states already routed outside cleanup, so replacing inferred/legacy
handling with exact overrides and shared ordering metadata correctly produces
no benchmark delta. The fixed simulation completed 150/150 scenario banks and
the aggregate completed 1,800/1,800 scenario banks.
