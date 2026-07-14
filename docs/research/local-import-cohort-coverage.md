# Local Import Cohort Coverage

Extraction date: 2026-07-13 (Europe/Paris)

Ten public Ironman community templates were manually imported through the normal Bank Templates user
workflow and normalized locally. Exact per-template layouts remain under the git-ignored research
cache and must not be committed, published, bundled, or used as production presets.

## Coverage

- Requested unique templates: 10
- Successfully normalized unique templates: 10
- Missing templates: 0
- Failed files: 0
- Identical duplicate import files: 1 (template ID 104)
- Total tabs: 95
- Total encoded slots: 9,983
- Positive item placements: 9,484
- Non-positive sentinels: 499
- Unique item IDs across the complete cohort: 2,908
- Grid widths observed: 8 columns in all 10 templates

## Per-template validation summary

| Template ID | Tabs | Encoded slots | Positive placements | Unique item IDs | Duplicate placements | Sentinels |
|---:|---:|---:|---:|---:|---:|---:|
| 70 | 9 | 1,147 | 1,147 | 1,138 | 9 | 0 |
| 53 | 10 | 1,060 | 1,060 | 1,059 | 1 | 0 |
| 32 | 10 | 862 | 862 | 861 | 1 | 0 |
| 127 | 10 | 803 | 803 | 803 | 0 | 0 |
| 82 | 9 | 1,033 | 1,033 | 1,029 | 4 | 0 |
| 104 | 10 | 1,410 | 946 | 945 | 1 | 464 |
| 169 | 10 | 808 | 777 | 764 | 13 | 31 |
| 39 | 10 | 1,289 | 1,289 | 1,286 | 3 | 0 |
| 61 | 9 | 937 | 937 | 935 | 2 | 0 |
| 209 | 8 | 634 | 630 | 623 | 7 | 4 |

The public gallery's item total corresponds to unique positive item IDs, not encoded slot count. A
template may place the same item ID more than once. Some layouts also contain non-positive sentinel
values that reserve or describe non-item cells. The normalizer preserves these as unexplained
`sentinel` records; their exact semantic meaning is not inferred without independent evidence.

## Validated invariants

- Every encoded source slot becomes exactly one normalized placement.
- Every placement has a unique `(templateId, tabIndex, absolutePosition)` coordinate.
- `row = floor(absolutePosition / columns)`.
- `column = absolutePosition % columns` and remains within the declared grid width.
- Positive-placement, unique-item, duplicate-placement, tab, and sentinel totals reconcile with the
  source files.
- Each source file and layout has a SHA-256 provenance hash.
- The identical duplicate of template ID 104 is reported but not counted as another template.
- `owned-banks.json` is excluded from the workflow.
