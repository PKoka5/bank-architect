# Category Neighbour-Consensus Review Snapshot

Analysis closed: 2026-07-14 (Europe/Paris)

This note closes the urgent category-audit phase over the ten locally normalized community
templates. Individual layouts remain git-ignored and are not copied into the product. Only aggregate
signals and independently reviewed Bank Architect classifications are retained.

## Cohort and signal

- 10 templates, 95 tabs, 9,484 positive placements, and 2,908 unique item IDs.
- Every cohort ID is found by the production registry. This says nothing by itself about semantic
  classification confidence.
- For each occurrence, only the four direct neighbours in the same tab are considered. Horizontal
  neighbours never cross an eight-column row boundary.
- A template casts at most one neighbour-category vote for an item when the local winner has at
  least 50% support and a positive lead over the runner-up. Conflicting occurrences in one template
  are not counted as multiple independent votes.
- The aggregate result is a review signal, not a category oracle: players routinely place utility,
  currency, components, and cleanup material beside a different semantic family.
- `Votes` is the number of eligible template votes, `Share` is winner votes divided by all eligible
  votes, and `Lead` is winner votes minus runner-up votes.
- A strong candidate has at least 3 votes, 60% share, and lead 2. A very-strong candidate has at
  least 4 votes, 80% share, and lead 2.

## Recorded result after the reviewed correction batches

- Strong review candidates: **48** (down from 76 at the first pass).
- Very-strong review candidates: **5** (down from 25 at the first pass).

The five remaining very-strong signals are deliberate neighbour effects rather than catalog bugs:

| Item ID | Item | Bank Architect classification | Neighbour consensus | Votes | Share | Lead | Decision |
|---:|---|---|---|---:|---:|---:|---|
| 23962 | Crystal shard | `SKILLING/resource` | `CLEANUP` | 6 | 83% | 4 | Keep as a reusable skilling resource. |
| 22586 | Looting bag | `TOOL/utility-container` | `GEAR` | 5 | 100% | 5 | Keep as a utility container despite gear-tab adjacency. |
| 22947 | Rada's blessing 4 | `CURRENCY/currency` | `GEAR` | 5 | 80% | 3 | Keep with persistent account utilities in the current taxonomy. |
| 995 | Coins | `CURRENCY/currency` | `TOOL` | 4 | 100% | 4 | Currency remains currency wherever players place it. |
| 9381 | Runite bolts (unf) | `SKILLING/ammo-component` | `GEAR` | 4 | 100% | 4 | Unfinished bolts remain a production component, unlike ID 9144 finished ammo. |

The 48 strong candidates are lower-confidence review leads, not 48 known errors. They may be
processed in later exact-ID batches, but they no longer block the layout-engine milestone.

## Reproducibility boundary

This is a reviewed decision snapshot, not yet an automated CI artifact. The offline classifier path
used `CompositeItemCatalog.DEFAULT`, including the resource registry, refiner, and canonical
overrides. Live `GearStatsSource` promotion is not reproducible from the normalized template cache;
registry-only equipment signals such as Tome of water are therefore resolved separately at runtime
by `BankOrganizationPreviewBuilder.effectiveCatalogItem(...)` and were not treated as unresolved
semantic errors in the recorded result.

Input fingerprints for this snapshot:

- normalized cohort/analyzer fingerprint:
  `1175740e6770216bd4c5cf25092b8d0db0c69aeaf2fbbd4d548c037cd17de1cf`;
- `item-registry.tsv` SHA-256:
  `449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038`;
- `ItemClassificationRefiner.java` SHA-256:
  `e4013949021bcc3140bcf02548d0df6f5ede55cdb08847fa97a7170b6220b74f`;
- `CanonicalItemClassificationOverrides.java` SHA-256:
  `477ee90461dceeb6b151ae9428276c96f071fd33701ed195ede0c6ef51b93b9c`;
- `BankOrganizationPreviewBuilder.java` SHA-256:
  `796682eee2a1620f35396890ac99c0108cabb2f3f7a562df0149feb0679869a2`.

Before these counts become a regression gate, a tracked analyzer must export the aggregate result
and either consume a pinned equipment-fact source or explicitly separate registry-only and live
equipment-resolved candidates.

## Decision boundary

- Community adjacency can nominate an item for review but never overwrite exact item semantics.
- An override requires a canonical item ID plus a reviewed player-facing meaning.
- Certs, noted/generated records, placeholders, Battle Royale duplicates, and name collisions remain
  explicit negative controls for exact classification overrides. A central global eligibility gate
  is still future work.
- Within this neighbour heuristic, these five residual signals justify no further category changes;
  this is not a claim that their entire taxonomy can never evolve.
