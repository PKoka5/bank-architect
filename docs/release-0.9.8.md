# Bank Architect 0.9.8

Release **23 October 2026**, above published 0.9.7 source
`efc0a61256c36a8415d8e4045ddb05d612e7c931`. The release combines the verified
Fixed-mode and preset changes with reviewed Fractured Archive item support.
The owner authorized publication after the raid audit. The previous Plugin Hub
request is merged; this release requires a new submission.

## Player-facing changes

- **Fixed or Flexible Alch:** Flexible remains the default and retains the
  existing replacement and functional protections. Fixed always assigns the
  105 exact listed items to Alch, including best gear, special weapons and
  placeholders, without requiring upgrades or equipment/value metadata.
- The Fixed list includes selected Mithril/Adamant/Rune and Dragon equipment,
  elemental battlestaves, d'hide bodies, normal/dark/light Mystic, Yew/Magic
  longbows and Onyx bolts (e). Ordinary Dragon scimitar and Dragon halberd are
  included. Names alone do not add ornaments or other variants.
- **Keep the selected preset:** changing only the Alch mode keeps Ironman or
  Main selected. Each remembers its own mode after restart. Saved Customs keep
  their own mode. Real layout edits still create a Custom copy with that mode.
- **Personal choices retain priority:** active saved per-item destinations and
  captured layouts survive Fixed's automatic tag change. A later explicit item
  correction keeps its existing priority; dormant routes stay dormant.
- **Sidebar fit:** the scroll content follows narrow viewport widths while
  preserving editor and destination tile heights.
- **Fractured Archive rewards:** 24 canonical states now have reviewed roles:
  new weapons and Rondache in Combat, Ascension ammo and charge/upgrade drops in
  Loot, Archivist's folios in Teleports, and Living record capes in Cosmetics.
  The new ammunition stays separate from the 105-item Fixed Alch list.

The first sidebar opening after upgrading shows **What's new: 0.9.8**. Dismissal
is remembered for this version. The Updates icon retains all fourteen release
entries. Every real bank move remains manual.

## Scope and validation

The Flexible replacement helper matches published 0.9.7. No GE-price policy,
runtime network call, new dependency or game automation is introduced.

Unused review-label formatting methods and an unreachable generic grouping
fallback were removed to preserve review-token headroom. All actual generic
categories already sorted by normalized name and item ID. A corpus regression
covers the generic categories across all five bundled preset definitions; no
supported placement rule or necessary test was removed.

Final build results, artifact identity and release-note visual checks are
recorded below. The detailed policy record is
[Fixed Alch verification](testing/fixed-alch-mode-2026-10-23.md).
The [Fractured Archive record](testing/fractured-archive-items-2026-10-23.md)
pins source IDs/revisions and documents old-input replay before the expanded
simulation baselines were accepted. Raid data lives in resources, with one
comparison-index guard for dedicated-ammunition weapons. Ascension crossbows
cannot become extra generic evidence to alch an ordinary crossbow; the existing
Flexible replacement helper itself is unchanged.

## Plugin Hub review budget

Calibration remains the maintainer's **200,414** count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`, reported on 30 September 2026.
The previous Hub request contains no newer numeric count.

| Tokenizer / normalization | Local estimate |
| --- | ---: |
| cl100k, preserved whitespace | 195,029 |
| cl100k, collapsed whitespace | 196,771 |
| o200k, preserved whitespace | 195,402 |
| o200k, collapsed whitespace | **197,154** |

The highest leaves **2,846 estimated tokens** below 200,000. Main Java is the
estimator's scope; the official tokenizer, preprocessing and full input scope
remain unknown. Resources, tests, documents and build metadata were inspected
separately. This is not official token-limit approval.

## Final local verification

- Offline Java 11 `gradlew build` passed **2,064 tests**, with zero failures,
  errors or skips. This includes 22 Main/Ironman raid cases and 52 literal-Fixed
  routing cases. The prior failed capture fixture, catalog-size expectation and
  release-note expectation were corrected; the final test run is clean.
- All **1,950 expanded-universe scenario banks** completed and all four reviewed
  protocol-2 baselines matched. A separate replay of all 1,950 old-universe
  scenarios with the final code retained all four previous fingerprints.
- The combined Bowfa/Ascension/Rune crossbow case failed before the restricted
  ammunition evidence guard and passed afterward. Final fixtures use Rune's
  real 9,720 high-alch value and keep Fixed unconditional.
- Update notice and newest history were rendered and visually inspected at
  **204 and 225 pixels**. The notice fits, buttons remain visible, and long
  history uses vertical scrolling. The first open after 0.9.7 displays 0.9.8;
  dismissal and restart behavior pass.
- Independent resource review checked all 24 source IDs/constants, Wiki stat
  fixtures and exact Onyx-ammunition separation. No third-party code or private
  bank export was added. `git diff --check` is clean.
- The final build reports no new compiler warning. An earlier full compilation
  retained the existing sidebar unchecked-operation note; Git also prints
  checkout line-ending notices during the estimator's inventory.

Local artifact: `build/libs/ironman-bank-architect-0.9.8.jar`, **1,118,811 bytes**,
**265 entries**, SHA-256
`c4f568706a997561633a7b37aad2f824195b9d309346962e08a8caec8090b379`.
It contains the new mode, raid facts and update history. Removed generic helper
classes are absent; tests, tools, research caches, docs and simulator outputs are
not bundled. These offline checks do not replace an ingame check with new raid
rewards owned by the player.

Verification logs:

- `build/tmp/release-0.9.8-final-build-2026-10-23.log`
- `build/tmp/release-0.9.8-review-size-2026-10-23.log`
- `build/tmp/release-0.9.8-publication-review-size-2026-10-23.log`
- `build/tmp/fractured-archive-crossbow-regression-before-2026-10-23.log`
- `build/tmp/fractured-archive-crossbow-regression-after-2026-10-23.log`

## Publication

The release tag is `v0.9.8`. Publication is authorized. The release uses the
verified artifact above, and the new Plugin Hub request must point to the exact
source revision tagged `v0.9.8`. Plugin Hub availability follows upstream review
and merge; publishing a GitHub release does not itself update Hub installations.
