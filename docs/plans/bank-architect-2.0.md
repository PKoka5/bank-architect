# Bank Architect 2.0 direction

Development starts above 0.9.8 (`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`).
The owner authorized this work on 23 October 2026. This is an incremental
development plan, not a published 2.0 release. Existing release notices keep
their published version until a new release is deliberately prepared.

## 1. Understand proposed changes

The first working slice compares destination and category choices with an
explicitly remembered reference for the selected Main/Ironman/custom profile.
It distinguishes changed destinations, newly present physical entries and
remembered entries absent from the current bank. Stack quantities, placeholders,
occurrence ordinals and absolute slot offsets are not layout changes. Identical
physical copies match their existing destination first.

Blueprint **Review changes** shows this report and offers **Remember reference
only**. Remembering establishes a comparison reference; it does not pin
positions, create a Custom profile, accept game moves or start another analysis.
The bank must remain open and match the analyzed contents. A changed preview,
profile or settings revision prevents a queued save. References are stored
locally under canonical profile names, isolated by base preset, and cleared when
a custom profile is deleted. Unsupported formats are preserved and not replaced.

Preview items also carry reasons recorded at the decision branch. The first
slice distinguishes preset classification, quick tools, potion-dose grouping,
Fixed/Flexible Alch, category choices, preserved automatic Alch and saved or
captured destinations. Physical copies retain independent saved-route reasons.
These reasons appear in blueprint tooltips and in changed-destination rows.
Within-tab comparison now uses relative order of the common item multiset;
insertions and removals do not count as reorders, including duplicate copies.

## 2. Explain Alch decisions

Flexible Alch now returns its explanation from the actual successful policy
branch, including the recorded tool, role or armour replacement. Placeholders
are identified explicitly. Higher-tier armour progression is distinct from
full-stat coverage, and equivalent Mystic colours are not described as upgrades.
Unreviewed surplus names its full-stat witness separately from its count of
higher-scoring bank entries. Fixed, personal, saved and retained decisions keep
their own provenance. Historical retained categories do not fabricate evidence.

Witnesses are stable across bank-order permutations. Routing, protection gates,
quantities and physical positions retain the existing policy. This part passes
2,183 tests and all 1,950 scenario banks with unchanged simulation fingerprints.
See the [explanation checkpoint](../testing/alch-explanations-2026-10-24.md).

## 3. Keep an accepted blueprint

The owner chose to append new items at the end of their matching tab. An explicit
**Keep this proposal** saves a versioned snapshot of real-item order, categories
and destinations. Main/Ironman acceptance creates a Custom profile. Missing
entries remain dormant until they return. Quantity and placeholder metadata stay
current. Empty planner cells are not part of the saved physical sequence.

Analysis publishes both the effective target and a separate latest proposal.
Review compares the snapshot against that proposal, and a read-only viewer does
not replace active guidance or publish a bank overlay. Explicit adoption replaces
the entire snapshot, including currently absent entries. Personal settings pause
the snapshot using persisted raw input context; bundled rules changing cannot do
that. Accepted Alch destinations do not feed classification retention.

The accepted format is strict and whole-record validated. Unknown or damaged
records remain stored and stop guidance instead of silently rebuilding a target.
Existing comparison references remain comparison-only. Deleting a custom profile
clears its accepted record as well as its comparison reference.

When classification/layout rules change, advance `AcceptedBlueprint.RULES`.
Older known-format snapshots stay usable; their rules revision is displayed
beside the current revision in Review. Do not automatically migrate or overwrite
an accepted target. Raw personal context must not include resolved defaults,
session counters, or another profile's item orders.

## Remaining stages

The 25 October Reddit-feedback slice adds automatic skill activity groups in
the packed Tools layout: outfits with their equipment and skill containers,
including fishing barrels and Woodcutting baskets. The owner chose to retain
both Frequently Used and skill grouping through the existing Gather frequently
used items checkbox. Personal/saved/accepted layout priorities remain intact.
This passes 2,342 tests and 1,950 scenario banks; its highest local token estimate
is 194,457. Owner in-game testing remains outstanding. See
[the activity-group checkpoint](../testing/skill-activity-groups-2026-10-25.md).
The next local slice adds optional Melee Gear, Ranged Gear and Magic Gear tags
through the existing Tab layout chooser. Default plans and old category expansion
keep Combat Gear together. Shared accessories remain in Combat Gear; players
can put that parent tag beside any style. Disabling a style merges its automatic
items back into the parent. Saved item moves retain source-tag compatibility
across a parent/style split or merge. This passes 2,365 tests and 1,950 scenario
banks, with a highest local token estimate of 195,521. The owner subsequently
confirmed the in-game combat-group test passed. User-defined named groups were
considered and explicitly deferred for UI simplicity. See the
[optional combat-group checkpoint](../testing/optional-combat-tags-2026-10-25.md).

The next slice extends the reviewed primary selection to Ranged and Magic using
the shared `PrimaryGearSelector`, with 427 and 442 exact identities respectively.
Weapon charge states, handedness, Crystal bow/Bowfa pairing and raw style stats
have dedicated expectations. The owner supplied both Wiki progression guides
and OSRSBestInSlot references; source limits and the readable slot progression
are recorded in [the Ranged/Magic review](ranged-magic-slot-order-review.md).
Melee preferences and Alch policy retain their previous priority. The completed
offline build passes 2,376 tests and 1,950 scenario banks, with a highest local
estimate of 196,237 review tokens. Owner in-game testing of this new selection
remains pending. Full build evidence is recorded in
[the verification checkpoint](../testing/ranged-magic-equipment-2026-10-25.md).

The owner's subsequent bank test looked good apart from separated Void gloves.
The focused follow-up keeps multi-piece Void families intact, including Elite,
ornament and locked variants, and preserves explicit personal/saved placements.
It passes 2,388 tests and all unchanged simulation fingerprints, with a highest
local token estimate of 196,430. See the
[Void checkpoint](../testing/void-set-coherence-2026-10-25.md).
The owner subsequently confirmed the Void fix improved the in-game layout.

The accepted-blueprint checkpoint passed 2,250 tests and all 1,950 scenario banks
but had only 43 estimated review tokens left. Static classification compilation
subsequently preserved all 33,766 records and restored several thousand tokens
of estimated headroom. The all-ten-slot Melee implementation is being verified
with a highest local estimate around 193,100. These are main-Java estimates,
not an official Hub count. See the [Melee checkpoint](../testing/melee-equipment-2026-10-24.md)
and [compiled registry workflow](../../tools/research/melee-bis/COMPILED-REGISTRY.md).

4. First establish the reviewed general-Melee candidate/selection policy in the
   [Melee BIS plan](melee-bis-policy.md). Ranged and Magic follow only after Melee
   passes its data review, regression cases and in-game acceptance. Then make
   Combat focus selectable while retaining complete style columns and
   vertical sets. Functional compatibility includes ammunition, special attacks,
   charged states and shared equipment.
5. Improve manual guidance using visibility, scroll distance and safe recovery
   after other valid player moves. Whole-tab moves remain part of the cost model.
6. Consolidate reviewed item facts into one development source that generates
   compact runtime tables. Unknown game items still require explicit review.
7. Add fixed midgame/endgame quality cases for group coherence, stability and
   special functions alongside completion simulations.

## Release constraints

Every real bank action remains manual. Runtime data stays local. No automatic
classification from new names, external service or telemetry is introduced.
Keep the Plugin Hub submission strictly below 200,000 review tokens and preserve
several thousand estimated tokens of margin. Main-Java estimates do not prove
the official count or cover resource changes. Recheck the complete submission
before publication; no release is authorized by this plan.

The first code cleanup shares identical high-confidence semantic-rule setup and
uses the existing exact-name membership helper for repeated OR clauses. Rule
keys, atoms, widths, evidence, priority and matching order must remain equivalent.
Required checks include reference/provenance/UI tests, all established simulation
fingerprints, a complete build and the development-only review-size estimator.

The follow-up cleanup also shares strict bundled-table framing and layout atom
construction/owned-ID filtering, preserving each format's validation policy and
all four simulation fingerprints. The 24 October pass shares candidate generation,
target application, metadata checks and preview wrapping. The subsequent Alch
explanations bring the highest local estimate to 197,390, with 2,610 estimated
tokens remaining. These are development estimates, not official Hub approval.
Continue the feature work locally; no publication is planned. Larger stages
still need a headroom plan and a fresh estimate. The
[budget checkpoint](../testing/blueprint-budget-2026-10-24.md) records the earlier
196,998 checkpoint before the explanation feature.
