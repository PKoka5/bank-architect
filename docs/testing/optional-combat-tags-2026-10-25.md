# Optional combat groups — 25 October 2026

Local development only; no commit, release or Plugin Hub submission.

## Behavior

Tab layout's existing **Add a category or tag...** chooser now offers **Melee
Gear**, **Ranged Gear** and **Magic Gear**, labelled optional until placed. Each
can share a tab with other tags or use a separate destination. Its remove control
merges the automatic group back into Combat Gear. The existing destination
controls, profile save/load and sharing formats support these tags.

The three tags are inactive in both bundled presets. Default serialization,
legacy category expansion, subcategory lookups and the existing item order stay
unchanged. There is no extra checkbox or configuration field. Enabling a group
is a personal layout edit and uses the existing custom-profile behavior.

Automatic routing reuses the local equipment stats, reviewed weapon roles and
known armour families. Only Combat gear is eligible. Ammunition, Alch and other
functional categories retain their priority. Unknown equipment without sufficient
facts stays in Combat Gear. Hybrid accessories stay there too, as a single
physical item; players can place Combat Gear beside Melee or any other group.
This does not replace any BIS selector or disposal comparison.

A compact, validated 100-ID table records style-specific Void helms, shared
usable Slayer helmets/Void bodies/robes/gloves and usable Fire/Infernal capes.
The capes retain their Melee role despite small bonuses for other styles.
Crystal armour retains Ranged membership despite lacking ordinary offensive
bonuses. Identities come from the existing canonical Wiki snapshot and reviewed
Melee candidates; stat vectors come from the existing RuneLite test snapshot.
Supporting primary references are [Void Knight equipment](https://oldschool.runescape.wiki/w/Void_Knight_equipment)
and [Slayer helmet (i)](https://oldschool.runescape.wiki/w/Slayer_helmet_%28i%29).
All data is bundled; runtime makes no network request.

Personal tag assignments take priority. An assignment to an inactive style
falls back to Combat Gear without deleting the stored choice. Item assignment
menus offer only placed styles. Older editor moves whose original tag was
Combat Gear still apply after splitting; moves from a style can survive merging
back into Combat Gear. Source compatibility does not equate two different
styles, or gear with Alch. Captured destinations remain authoritative under the
existing rules. Accepted targets still require their matching personal context;
changing a plan intentionally pauses a prior accepted target as before.
`AcceptedBlueprint.RULES` advances from 4 to 5, without rewriting older records.

Split tabs use the existing gear layout and dense fallback. A short tab cannot
hold a full ten-item vertical column without adding empty bank slots; no new
blanks or duplicate item representations are introduced.

## Verification

The new parameterized tests cover both presets: inactive defaults and legacy
plans, profile/share round trips, whole-tab moves, merging, exact reviewed
identities, core armour and weapons, Crystal and Void exceptions, hybrid gear,
Fixed/Flexible Alch invariance, old/new saved transfers, accepted positions,
placeholders, repeated physical copies and shuffled inputs. A Swing event-thread
test selects an optional group through the real chooser and removes it through
the actual merge control.

Compatibility tests continue comparing every standard tag and original lookup
against the pre-migration fixture. Optional tags are asserted separately; the
old fixture and all simulation fingerprints are unchanged.

The full offline build passes in **45 seconds**: **2,365 tests**, zero failures,
errors or skips, and **1,950 completed scenario banks** (150 standard plus 1,800
multi-seed). All four normalized simulation fingerprints pass unchanged. There
is no compiler warning in the build log, and `git diff --check` passes.
The principal log is `build/tmp/combat-style-full-2026-10-25.log`. Developer
blueprint exports, HTML and rendered style PNGs are in
`build/reports/combat-style-groups/`. Visual review of the Melee, Ranged and Magic
outputs confirms separation of the tested style gear and the retained dense
layout. Other tags deliberately left on a destination still appear beside its
style group. These outputs are simulated banks. The owner subsequently completed
the in-game test of the optional combat groups and confirmed that it worked
well ("was goed"). No further issue was reported in that test; this acceptance
does not establish coverage of every account or gear combination.

The highest local main-Java token estimate is **195,521**, with **4,479** estimated
tokens of margin. Other estimates are 192,342 / 195,208 / 192,649. Calibration is
the maintainer's 200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`.
The estimator is not the official Hub count and does not count resources/tests.
The three tag declarations and compact exception table are reviewed separately;
no dependency, registry recompilation or build configuration change is needed
for this slice. Publication still requires the complete submission checks.

The local development JAR retains the v0.9.8 filename and contains **927,719
bytes / 264 entries**, including the new class and style table. The four
development classification helpers/compiler and five raw derivation inputs
remain excluded. SHA-256:
`a0889ebc4da8fe1f55f50d8418da54b4d1a3a8a66422311f29091fc8448406ee`.
HEAD remains `675ccbd`; nothing has been committed or published.
