# Compiled item registry

## Purpose

The plugin now loads reviewed item facts directly by item ID. It no longer reruns
the static name rules, Wiki-category precedence, canonical overrides and usage-tag
joins when loading its catalogue. Those derivations remain in development code
and are checked against the shipped table.

This changes when static facts are computed. It does not change bank-dependent
gear selection, Alch replacement checks, profile routing, personal corrections,
saved blueprints or manual guidance.

## Source and runtime formats

The original four-column `item-registry.tsv` stays at its existing source path so
research scripts and the simulator continue to read the same input. It contains
**33,766 records** in this checkpoint. Every record is compiled in its original
insertion order, including filler, cert, placeholder and restricted cache records.
The simulator's smaller usable-item universe is not the catalogue record count.

The shipped file is:

`src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/compiled-item-registry.tsv`

It uses an exact `# schema=1` header followed by five tab-separated columns:

| Column | Meaning |
| --- | --- |
| Item ID | Positive ID from the original registry |
| Display name | Final catalogue name |
| Category | Exact `ItemCategory` enum name |
| Subcategory | Final nonempty classification |
| Usage tags | Ordered comma-separated tags; an empty final field is valid |

The compiler derives the resource catalogue, not `CompositeItemCatalog.DEFAULT`.
The separate `StaticItemCatalog` retains precedence for its seed workflows and
supplemental items. Resource records continue to have no workflow key. The tests
check every record's ID, name, category, subcategory, tag order and workflow value
against the original derivation.

The generated table uses UTF-8 and LF line endings. Its `.gitattributes` entry
preserves LF and permits the intentionally empty final TSV fields.

## Development-only code and inputs

`RegistryCompiler` lives in the test source set alongside these four retained
derivation helpers:

- `ItemClassificationRefiner`
- `WikiItemCategories`
- `CanonicalItemClassificationOverrides`
- `ItemUsageTags`

Their original rule order and validations are retained. Existing parser tests now
call the compiler where they are testing the original derivation format.

These five source resources are excluded from `processResources` and copied into
the test resources by `processTestResources`:

- `item-registry.tsv`
- `canonical-item-classification-overrides.tsv`
- `wiki-item-categories.tsv`
- `item-usage-tags.tsv`
- `registry-name-rules.tsv`

They remain checked-in, reviewable development inputs. The source files retain
their evidence and attribution; moving the derivation does not replace those
sources. Other resources needed by runtime sorters remain shipped.

## Regeneration and verification

After an intentional change to a classification input or derivation rule, run:

```powershell
.\gradlew.bat compileItemRegistry --offline --console=plain
```

This explicit task writes the checked-in compiled table. Review its diff with the
source change, then run the tests and the normal build. A future addition to the
raw registry also requires deliberately updating the expected complete record
count in `GeneratedRegistryTest`.

The compiler CLI accepts:

```text
RegistryCompiler <output-path> [--check]
```

Without `--check`, it writes deterministic output and creates missing parent
directories. With `--check`, it compares existing output with fresh derivation;
missing or stale output fails, and the check never writes or repairs the file.
The CLI runs with the development/test classpath, not inside the plugin.

Normal tests also detect stale output: `GeneratedRegistryTest` compares the
bundled compiled artifact with fresh compiler output byte for byte. A regular
build does not silently regenerate source files. That test additionally checks
all 33,766 records independently against the original parser, preserving the full
lookup set and insertion order. A focused verification command is:

```powershell
.\gradlew.bat test --tests '*GeneratedRegistryTest' --offline --console=plain
```

The existing simulation baseline checks remain an additional check of resulting
bank guidance. They must not be regenerated automatically to accommodate a
classification mismatch.

## Runtime validation and failures

`ResourceItemRegistry` defers opening its bundled stream until its first use.
Its loader requires the exact supported header and five columns; rejects
nonpositive or duplicate IDs, missing names/subcategories, unknown category
values, and malformed or duplicate tags; and returns an immutable completed map.
It does not expose a partially parsed catalogue after an error. The table stream
is closed on successful parsing and on a parsing failure.

Catalogue availability still validates the other resources required by the
runtime path:

- Gear tiers;
- Wiki item lists, including special-attack weapon protection;
- Item sets and cosmetic-family facts;
- Classification name groups used by runtime sorters;
- Item-sort metadata and its source keys.

`RequiredResource` preserves a loading failure for repeated calls.
`CompositeItemCatalog` checks all its required catalogues before returning an
item, including when that item already exists in the static catalogue. Analysis
therefore fails with the compiled-registry error for both an empty bank and a
bank containing a known static item when the required compiled table is broken.
Unknown IDs remain unknown; there is no runtime name-rule or network fallback.

## Review size

The prior derivation pipeline occupied roughly 10,200–10,400 measured main-Java
source tokens before replacement. Its removal from runtime source provides room
for the planned Melee policy, while retaining the derivation and its tests as
development code. This is also a runtime simplification: loading facts no longer
requires per-item name matching and precedence resolution.

The review-size estimator counts main Java source only. It does not establish
the Hub's treatment of the generated table, test code, build changes or other
resources. Inspect those changes separately and report the highest fresh local
estimate as an estimate. The standing publication requirement remains strictly
below 200,000 official review tokens with several thousand tokens of headroom.
No build result or historical estimate certifies that requirement.
