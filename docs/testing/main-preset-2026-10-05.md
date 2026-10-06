# Main preset: local test build

## Behavior

The sidebar has one preset chooser, in its header: **Ironman**, **Main** and named
customs such as **Ironman: My bank** or **Main: My bank**. Tab Layout no longer has
a second saved-layout dropdown. Unsupported base preset names fall back to
Ironman; the other account scaffolds are not exposed.

Both bundled presets always use their original default plans and preferences.
Editing their tag placement, layout options, corrections, block order or item
order creates a **Custom layout** automatically. Subsequent edits save that
custom. The header shows its name; choosing Ironman or Main restores the bundled
defaults. Save as, Import, Export and Delete remain in Tab Layout.

Main starts with these destinations:

| Destination | Contents |
| --- | --- |
| Main | Frequently Used and currency |
| 1 | Runes and teleports, including Lunar staff |
| 2 | All combat styles and Saturated heart |
| 3 | Finished potion families in dose order 4, 3, 2, 1; food |
| 4 | Grimy herbs, clean herbs, Farming seeds, unfinished potions, other ingredients and produce |
| 5 | Skilling tools, outfits and containers |
| 6 | Resources, gems and components |
| 7 | Slayer and boss loot, plus a separately movable Alch tag |
| 8 | Clues, cosmetics and collection items |
| 9 | Cleanup and quest items |

Main's herb stages are dense runs, without invented empty slots or Ironman recipe
rows. Gear defaults to the existing Best in slot matrix; the other gear layouts
remain available in customs. Main does not automatically gather Ironman's
frequently used items. Main now gathers reviewed alch candidates only when an
actual owned upgrade replaces the corresponding combat slot/style; see
`main-alch-2026-10-06.md`. Ironman uses the same Alch candidate rules and
separately movable tag. Best-owned reviewed stacks stay Combat in both presets.
Explicit
item corrections, captured blueprints, hand-edited orders and Keep current order
remain authoritative. The known Ghrazi and Holy Ghrazi rapier catalog error is
overridden for Main Combat; Ironman behavior is unchanged.

## Saved state and compatibility

Ironman's original configuration keys are retained. On first use of the new
storage, the previous working layout and preferences are preserved as a custom
if they differ from bundled defaults. Existing named layouts remain; legacy
unnamed item orders are copied into the migrated custom without deleting their
original entry. A saved custom's plan also survives when its legacy working-plan
key is blank.

Main's keys remain separate under `main.`. Each named custom has its own local
preferences, corrections and block orders in an encoded profile namespace.
Item orders remain indexed by profile name. Identical custom names under Main
and Ironman are independent, and the header lists both. Switching presets
invalidates older analysis and pending blueprint saves. Assignment-menu callbacks
check the base preset and custom they were opened under. Guidance reads the
active preset's plan.

Legacy Ironman sharing remains `BAv1`. Main exports use `BAv2` with preset identity;
the import screen asks the player to select the matching preset before importing.

## Verification

- `gradlew check jar`: **1,339 tests passed**, no failures or skipped tests.
- **1,950 Ironman simulation scenarios completed**; all four committed baseline
  hashes are unchanged.
- Main tests include the existing 770-item fixture and an 817-item extended fixture
  with 47 synthetic canonical additions. All three gear layouts, row filling
  on/off, stable rescans and completed Swap/Insert guidance are checked. These are
  offline fixtures, not a new capture of the player's current bank.
- Sidebar screenshots and bounds checks cover widths of 225 and 230 pixels.
- Regression checks cover auto-created customs, options/corrections/block/item
  isolation, restart persistence, legacy migration, deletion, a full custom list,
  selecting customs across both parents and bundled-default restoration.
- Changing one sidebar option preserves the other preferences, including the
  frequently-used setting. RuneLite settings events save their actual new value
  to the active custom, even when Main is selected.
- The mechanical import consolidation was reviewed separately: all changes to
  code bodies outside the preset feature are absent.
- The local RuneLite development client initialized successfully.

The existing unchecked Swing renderer compiler warning remains. RuneLite itself
also reports its existing Java 11 reflective-access warning on startup; no
reflection was introduced into this plugin.

## Review size

`npm.cmd run check --prefix tools/review-size` reports **197,191** as the highest
calibrated estimate, leaving **2,809** estimated tokens below 200,000. Calibration
remains the maintainer's 200,414 count for
`def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not an official Hub count.
Imports were consolidated to recover headroom while retaining functionality.
The later Alch feedback adds six exact classification resource rows, inspected
separately; dependencies are unchanged. Tests and
development research documents/tools are outside the estimator's main-Java scope
and require separate consideration before a future Hub submission.

## In-game check

1. Open the local development client and the bank.
2. Choose **Preset: Main**, then **Analyze Bank**.
3. Inspect the teleport tab, potion-dose runs, herb-stage runs and Combat placement.
4. Edit a bundled preset. Confirm the header switches to a named custom, and no
   preset dropdown appears inside Tab Layout.
5. Choose Ironman and Main to check their defaults; choose the custom by name to
   restore its saved edits. Close/reopen the bank and restart the test client to
   check persistence.

All bank moves stay manual. This is a local test build; publishing is a separate
step after the in-game review.
