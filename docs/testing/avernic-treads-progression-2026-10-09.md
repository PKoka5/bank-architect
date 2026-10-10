# Avernic treads progression — 2026-10-09

Local, unreleased correction after 0.9.2 (`3b9012e360ff4a23c591f7eda722b81dc2d41a90`).

## Finding and correction

All eight ordinary canonical Avernic treads IDs already classify as `GEAR / feet`.
RuneLite equipment stats participate in their slot, style and score, including
canonical bank placeholders. However, none had an exact progression stage and
the name heuristic did not recognize this family. Their progression base was
therefore zero, while Primordial, Pegasian and Eternal boots receive 1,000 before
adding equipment stats. This could put treads behind older boots in their assigned
style column.

Add stage 5 to canonical IDs 31088 and 31091–31097. Stage 5 is this plugin's
endgame progression bucket, shared with the three Cerberus boots, not a claim
that every treads variant is best for every combat style. Existing equipment
stats still decide ordering within the stage. No sorting implementation changes.

ID provenance is recorded in `docs/research/item-id-research-index.tsv` and the
existing exact feet overrides. Do not add certificate/placeholder IDs 31089,
31090 or 31098, or Battle Royale copies 33172/33173, to the exact tier table.
The normal snapshot canonicalization handles real bank placeholders.

Jagex identifies these as boots and an upgradeable family in its
[Grand Exchange entry](https://secure.runescape.com/m=itemdb_oldschool/Avernic+treads/viewitem?obj=31088)
and [Delve Boss rewards poll](https://oldschool.runescape.com/polls/2025/1702).
These sources support item identity; the progression stage is our layout policy.

## Scope and verification

Regression tests cover all eight exact stages, exclusion of noncanonical IDs,
the progression base plus runtime stats, and stronger feet candidates in each
assigned combat style. Complete default layout rows are checked in both Main
and Ironman presets, with real items and zero-quantity placeholders. Test stat
vectors are synthetic and explicitly do not assert current game bonuses.

The existing layout assigns each physical hybrid item to one strongest style
from its equipment stats. This correction does not duplicate treads into all
three setup columns or alter that policy.

## Final validation

- Java 11 `gradlew.bat build --offline --console=plain`: passed.
- 1,515 tests; zero failures, errors or skipped tests. Four new tests cover the
  catalog and progression/integration regression.
- All 1,950 simulated bank scenarios completed. All four existing baseline hash
  guards passed unchanged. No compiler warnings were reported.
- Review-size estimator passed: highest main-Java estimate **196,507**, estimated
  headroom **3,493**, calibrated to maintainer count 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
  No main Java changed. The eight new resource rows, test additions and this note
  were inspected separately and are outside the estimator's scope; this is not
  an official Hub token count.
- Logs: `build/tmp/avernic-treads-build.log` and
  `build/tmp/avernic-treads-review-size.log` (development output, not bundled).
- No in-game test or publication of this patch has been performed.
