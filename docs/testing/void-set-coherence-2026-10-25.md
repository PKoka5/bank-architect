# Void set coherence — 25 October 2026

The owner reported that Void knight gloves 8842 were isolated from the remaining
Void equipment in the new Ironman blueprint. The supplied export's 94-item
Combat tab placed the gloves in the Ranged primary hands cell while its three
helmets, top and robe remained secondary. All six Void entries were placeholders.

## Focused fix

When at least two distinct usable Void identities are present in a Combat
bucket, the primary selector leaves those pieces to the existing atomic family
layout. It no longer consumes the gloves, or a different set piece, as a loose
primary fallback. Partial kits are included. Repeated physical copies of one
ID alone do not constitute another set piece.

The Void family now includes **32 reviewed normal, Elite, ornament and locked
identities**, grouped by helmet role, top, robe and gloves. Exact identities
were checked against the existing canonical Wiki infobox snapshot; no adjacent
IDs or set effects are inferred. The original usable members retain their
relative order. The test pins this expanded row separately; the other 77 legacy
gear rows retain their independently checked order fingerprint
`dd5476807c7e7ede8e685663ffcb6530e821c343572e55dbc9a7577b1adccba7`.

Automatic optional combat styles keep a multi-piece Void family in Combat Gear
instead of splitting the helmets away from the shared body/gloves. Explicit
personal assignments still win. Existing saved physical order and matching
accepted targets also retain priority. `AcceptedBlueprint.RULES` advances 6 → 7;
no stored target is silently rewritten.

A compact Void family of at most eight pieces is tried within one physical row
when the available cells permit it. Existing bounded dense fallback remains for
banks where a row or rectangle cannot fit without inventing empty cells.

## Evidence

`reported-void-combat-94.tsv` retains only the supplied Combat IDs and placeholder
flags, with positive quantities normalized to one. The regression reproduces the
glove separation and now checks the complete six-item family. In the resulting
Ironman export, all six entries occupy **row 11, columns 1–6**, including gloves
at column 6. The ten chosen Melee items remain unchanged. Rendered output was
visually inspected in `build/reports/void-coherence/IRONMAN-reported-void-94.png`.

Twelve new parameterized test instances cover both presets, including:

- the supplied 94-item bank with every Void part withdrawn;
- normal, Elite, ornament, locked, mixed and partial kits across all three
  combat layouts and owned/placeholder states (72 layout combinations);
- optional style destinations and Void-only banks;
- input permutations, duplicate stacks/placeholders and exact conservation;
- explicit personal tags, saved physical order and accepted targets;
- registration of every reviewed Void ID.

The prior optional-tag test consciously expects owned Void helmets to stay in
Combat Gear together, while its ordinary ranged weapon still uses the selected
optional style. No other compatibility expectation or simulation fingerprint
was updated.

The full offline build passes in **42 seconds**: **2,388 tests**, zero failures,
errors or skips. All **1,950** scenario banks complete and all four existing
normalized simulation fingerprints pass unchanged. No compiler warning appears
in the log; `git diff --check` also passes.

Logs: `build/tmp/void-coherence-full-2026-10-25.log` and
`build/tmp/void-coherence-size-2026-10-25.log`. Pre-change simulation outputs are
preserved under `build/tmp/void-before-2026-10-25/`.

## Budget and local artifact

Highest main-Java estimate: **196,430**, leaving **3,570** estimated review tokens.
Other estimates: 193,279 / 196,090 / 193,615. Calibration remains the maintainer's
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official
Hub count; the resource membership and test fixture were inspected separately.

The local v0.9.8 development JAR is **934,919 bytes**, SHA-256
`e459387721843e09efa8d48cdfaa9f5057e339edd3ea2048936309994650b5c2`.
HEAD remains `675ccbd`. No release, commit, push or Hub submission was made.
The owner subsequently tested the fix in game and confirmed the layout improved
("ja beter"). This completes the reported Void-glove regression's owner check;
it does not claim coverage of every bank or personal layout.
