# Cleanup Review Benchmark Protocol

This note defines the proposed reproducible multi-seed benchmark for Workstream A in
`remaining-work-roadmap.md`. It is a maintainer decision pending approval. Until approved, changing
the seed list or scale requires updating this note and the test-scope
`CleanupReviewBenchmarkProtocol` together.

## Protocol version 1

| Field | Value |
|---|---|
| Protocol seeds | `20260718`, `314159265`, `271828182` |
| Banks per seed | 200 |
| Item-count range | 6-120 inclusive |
| Scenarios per sampled bank | 3 (`SHUFFLED_NO_TABS`, `RANDOM_TABS`, `NEARLY_SORTED`) |
| Sampled banks | 600 |
| Simulated scenario banks | 1,800 |
| Seed derivation within each protocol seed | `protocol seed + zero-based run index` |
| Registry resource | `src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/item-registry.tsv` |
| Registry SHA-256 | `97331c2f6826461713807b576e6b17a0dc4fd8ffdcc5e6b7ec94f79191ff96bf` (line endings normalised to LF) |

The three seeds are deliberately far enough apart that their 200 derived simulation-seed ranges do
not overlap. `20260718` preserves continuity with the original fixed-seed verification. The other
two values are stable, recognizable constants and do not encode an item or category preference.

The checked fingerprint makes the registry revision explicit. A changed fingerprint stops the
benchmark and requires a reviewed protocol revision; it must not be silently accepted as the same
baseline.

The fingerprint is taken over the registry with CRLF and lone CR normalised to LF. The file is
committed with normalised line endings, so a Windows checkout holds CRLF and every other checkout
holds LF for the same commit; hashing the raw bytes would make the same revision fingerprint
differently per contributor.

## Occurrence semantics

One occurrence means one sampled item in one simulated scenario bank. Each sampled bank is exercised
under all three scenarios, so an item sampled once normally contributes three occurrences. Counts
are summed across all protocol seeds.

The aggregate contains exactly sampled item IDs whose effective
`CompositeItemCatalog.DEFAULT` classification maps through the IRONMAN preset to
`storage-cleanup`. Canonical name and source constant are report labels loaded from the local
registry. They are never used to decide production classification, family membership, or routing.

CERT, PLACEHOLDER, cache-only, activity-specific, and non-bankable markers may inform manual review
of aggregate rows. They do not become broad production rules or simulator sampling filters through
this protocol.

## Invocation and outputs

Run the complete benchmark in one offline Gradle invocation:

```text
./gradlew aggregateCleanupReview
```

The task runs only test-scope Java and performs no network call or external-process merge. It writes:

- `build/reports/bank-simulation/aggregate/cleanup-review.tsv` - one row per cleanup item ID,
  ordered by occurrence count descending and then item ID ascending;
- `build/reports/bank-simulation/aggregate/metadata.tsv` - protocol inputs, registry fingerprint,
  item-universe size, aggregate totals, and every simulator outcome count.

Neither output includes a generation timestamp. With identical source, registry, Java/Gradle
environment, and protocol inputs, rerunning the task produces byte-identical files.

`-PaggregateOutput=<directory>` may redirect the generated files without changing protocol inputs.
The fixed inputs are intentionally not exposed as Gradle properties: a different seed, scale, or
item range is a different protocol revision, not the same benchmark.

## Single-run compatibility

The quick local command remains supported and unchanged:

```text
./gradlew simulateRandomBanks -PsimBanks=50 -PsimSeed=20260718
```

It continues to write `report.tsv` and sibling `cleanup-review.tsv` under
`build/reports/bank-simulation/`. The aggregate task does not read, overwrite, or merge those
single-run files.

