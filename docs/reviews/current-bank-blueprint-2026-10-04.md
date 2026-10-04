# Current bank as the active blueprint

Implemented on 2026-10-04. The owner subsequently confirmed the in-game test round passed and explicitly authorized publication. Release preparation and publication details are recorded in [release-0.8.0.md](../release-0.8.0.md).

## Behavior

- Open Blueprint > Save current bank... creates a new active named layout. Existing names receive a free numbered suffix; previous plans, item orders and block orders remain available.
- Read the full supported bank container and all nine tab varbits on the client thread. Numbered tabs precede Main in the physical container. Reject fillers, gaps, inconsistent tab counts, empty banks and stale quantities or placeholder states.
- Capture every canonical item occurrence, preserving mixed-category tabs and dense physical order. Rebase occurrence identities to the fresh physical order when recapturing an edited blueprint. Ignore automatic planned blank slots.
- Recorded positions remain authoritative across automatic category or tag-plan changes. Updated classification metadata remains visible. New items use automatic destinations; absent items retain dormant saved orders. Explicit item assignments and per-tab reset remain available.
- Persist captured destinations as v2 item orders; ordinary v1 orders remain compatible. Unknown future versions cannot be overwritten. Older plugin versions preserve v2 as unsupported data. Layout share codes continue to share category assignments, not per-item positions.
- Reuse the guarded save transaction for editor saves, reset and capture. Refuse stale preview/profile/order state or full profile storage before configuration changes.

## Validation

- Full offline Gradle build: **1,191 tests**, no failures or errors.
- **1,950 bank simulations completed**, with all four existing baseline reports unchanged.
- New tests cover UI pending/cancellation and retry, profile limits and preservation, fresh bank reads, fillers and invalid boundaries, duplicate and placeholder recapture, reset, metadata changes, explicit assignments and missing/new items.
- Headless editor images checked at narrow and normal widths; the capture button fits the existing controls. The owner confirmed the live test round passed; individual steps were not separately recorded.
- Read-only production review found no remaining blocking issues. Existing unchecked-operation warning in the sidebar remains; Git warns about LF/CRLF conversion.

## Review-token budget

The development-only estimator's highest local estimate is **197,391**, with **2,609 tokens of headroom**. This is not the official Hub count. Calibration remains maintainer-reported **200,414** for source revision `def1e856e101ff0e57dd96adccab2cf1d886b076` (2026-09-30).

This feature changes main Java, tests, README and this report; it adds no runtime resource data or dependencies. The estimator excludes tests and documentation. Earlier pending review fixes include resource changes, audited in their existing review reports. Shared validation/canonicalization, shorter constructor forwarding and precise config tooltips preserve functionality while limiting growth.

## In-game check

1. Restart the local test client, open the bank and Analyze Bank.
2. Manually reorder items and move an item into a mixed-category tab.
3. Open Blueprint > Save current bank..., enter a name and verify the new active layout matches the real bank.
4. Close/reopen the bank and analyze again: existing positions should remain the target.
5. Switch to the previous saved layout and back. Verify both remain independent. Optionally test a placeholder, a new item and Reset tab order.
