# Blueprint destination review — development checkpoint

23 October 2026, above published 0.9.8 source
`675ccbd0e708ed6ea3396f0f35ff54ad96dfea21`. This implements the first slice of
the [2.0 direction](../plans/bank-architect-2.0.md), not a published release.

Follow-up: the [24 October checkpoint](blueprint-budget-2026-10-24.md) records
the next cleanup pass, 2,138 passing tests and a highest estimate of 196,998.
The figures below describe this earlier checkpoint.

## Behavior

**Review changes** compares physical item destinations/tags with an explicitly
remembered reference for the active base preset and canonical profile name.
Matching identical destinations first prevents exchanged duplicate copies from
creating false moves. New and absent copies are separate counts. Quantities,
placeholder status, occurrence numbers and within-tab slot order are ignored.
Absent reference entries remain available to compare when those items return.
Merely analyzing, opening or editing a preview never replaces the reference.

**Remember this blueprint** stores a local comparison reference through the
existing expected-preview/live-bank/context guard. It does not fork a bundled
preset or request another analysis. Config events for this metadata are ignored
by the analysis refresh path. Settings changes advance a revision included in
the edit context, so an already queued save cannot accept stale settings.
Deleting a custom profile clears its reference. Unsupported/corrupt stored
formats are preserved, not partially accepted or silently overwritten.

Reasons travel on immutable preview items through physical expansion, saved
routes and drafts. They identify the branch that chose the category/destination.
Retained automatic Alch is distinct from personal overrides, including in a
real warm analysis. Captured physical positions retain the independent reason
for the effective category when a newer personal correction wins. The current
coarse Flexible reason does not claim a specific replacement witness or BIS.

## Checks

Offline Java 11 `gradlew build` passed **2,126 tests**, zero failures/errors/skips.
All **1,950 scenario banks completed** and all four existing simulation
fingerprints matched. Shared semantic-rule construction preserves rule keys,
atoms, confidence, shape, allowed widths and optional width evidence. Sixteen
exact-name OR chains use the existing `equalsAny` helper with the same names and
matching position; no item data or routing policy changes.

Follow-up cleanup shares schema-1 TSV framing across seven bundled loaders.
Each loader retains its own row/domain checks; header whitespace and ignored-row
policies remain explicit. Eight parser boundary tests cover UTF-8, trailing empty
fields, strict/BOM headers, independent formatting policies, stream closure and
the original checked/unchecked row exception. No resource data changed.

Layout construction also shares ordered present/owned-ID collection, indexed
atom construction and eight-cell chunks. Atom/member keys, empty/singleton
validation, existing locks and rule evidence remain intact. The two sidebar
layout chooser types share their construction and event handling. Independent
read-only reviews found no issues; all simulation fingerprints remain unchanged.

New checks include:

- 20 destination comparison cases: duplicate multisets, additions/absence,
  quantities/placeholders, order/ordinal changes, bounded serialization,
  malformed/future formats, report reasons and immutability.
- 12 real-plugin reference lifecycle cases: built-in/custom/preset isolation,
  restart, stale analysis/settings/profile, closed/changed bank, future formats,
  profile deletion, config events and a full custom-profile capacity.
- 20 Main/Ironman placement reason cases, including all 105 Fixed IDs without
  equipment/value data, invalid assignments, dormant routes, captured copies,
  physical conservation and a real retained-Alch analysis.
- Two editor acceptance cases and extended narrow-editor control bounds.

The lifecycle tests exposed fresh Ironman using an empty legacy profile key.
Reference storage now uses the canonical saved-profile name without changing
other legacy preference keys. The corrected roundtrip/restart tests pass.

The existing sidebar unchecked-operation compiler note remains. `git diff
--check` is clean. The 560-pixel editor render was visually inspected; controls
fit and reference review is disabled during a draft. The owner accepted the
visible result; no additional ingame test of the internal cleanup was requested.

## Review budget

Highest local estimate: **198,081**, leaving **1,919 estimated tokens** below
200,000. Other estimates: cl100k 195,983 / 197,754 and o200k preserved 196,297.
The follow-up cleanup reclaimed 597 estimated tokens beyond the earlier shared
rule/name cleanup (approximately 400).
This remaining margin is insufficient for the standing several-thousand-token
release target. Further simplification is a release gate before submitting this
development slice or expanding it. These estimates are main-Java-only and not
an official Hub count. Calibration remains 200,414 at
`def1e856e101ff0e57dd96adccab2cf1d886b076`; tests/docs are reviewed separately.

Logs: `build/tmp/blueprint-review-build-2026-10-23.log`,
`build/tmp/blueprint-reference-lifecycle-2026-10-23.log`, and
`build/tmp/blueprint-review-size-2026-10-23.log`. Latest full build and estimate:
`build/tmp/blueprint-headroom-build-2026-10-23.log` and
`build/tmp/blueprint-headroom-size-2026-10-23.log`.

Local development artifact retains the base version filename
`build/libs/ironman-bank-architect-0.9.8.jar`, but differs from published 0.9.8:
**1,126,584 bytes**, SHA-256
`004f74230d5321b47dc63050f6b1c31075b48a347ace05f4dc1d7092384a0328`.
Tests, tools, docs and research are not packaged. No commit, push, tag, release
or Hub update was made for this development checkpoint.
