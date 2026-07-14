# V1 Product Contract

> Historical milestone fixture: the fixed two-block examples below are retained for V1 regression
> tests and do not define the active ten-category organizer. Current routing and sorting rules live
> in `bank-organisation-rules.md` and `universal-preset-roadmap.md`; finished potion doses no longer
> belong to the active Herblore prep category.

## Long-Term Product Goal
Bank Architect is a standalone RuneLite sidebar plugin for analysing a player's bank and generating a manual organisation blueprint. Its V1 preset remains Ironman-focused.

The long-term product may:
- read `InventoryID.BANK` through supported RuneLite APIs;
- match bank items to stable blueprint slots;
- generate and save local layout blueprints;
- show sidebar planning, checklists, and manual organisation guidance.

The product must never:
- alter the player's native bank layout;
- automate mouse, keyboard, clicks, drags, packets, or bank actions;
- manipulate game state;
- use reflection, `Runtime.exec`, native code, external processes, network calls, or telemetry;
- read inventory or equipment unless a later documented feature explicitly allows it.

The player always moves bank items manually.

## Sidebar And Bank Guide Overlay

The sidebar is a compact command center, not a full blueprint canvas. It shows plugin identity, the active profile name, a guide block selector, a show/hide toggle, and a short status line.

The Bank Guide overlay — a translucent, input-transparent guide rendered directly over the native bank item grid when it is open — is the plugin's intended future main execution view. The sidebar drives it; it does not attempt to replace it with an in-panel grid.

## Current Milestone: Phase A — Static Bank Guide Overlay Proof

This milestone is a static selected-block alignment proof for the Bank Guide overlay. It is not yet linked to a specific native bank tab or a final target row.

It must:
- provide a pure Java blueprint model with stable, machine-readable keys at every hierarchy level (profile, tab, section, block);
- provide one preset named `Ironman — All-Round Bank`;
- show compact sidebar controls: profile name, a guide block dropdown (`Melee Setup`, `Irit → Super attack`), a show/hide toggle, and status text;
- render the selected block as a single 8-cell translucent guide strip over the top visible row of the native bank item grid, only while the bank is open and the guide is enabled;
- read only native bank widget visibility and geometry to decide whether/where to draw the guide;
- recompute guide geometry from current widget bounds on every render, never from cached pixel coordinates;
- render nothing if safe grid geometry cannot be determined;
- support a distinct reserved-cell style for `SlotKind.EMPTY`, even where the current preset blocks do not use it yet.

It must not:
- read `InventoryID.BANK`, `ItemContainer`, item IDs, quantities, `WidgetItem` data, `ItemManager`, icons, or item names;
- use item icons or category scanning;
- persist blueprints;
- create, mutate, or otherwise alter native bank widgets, tabs, or native bank rendering;
- automate any input or bank action, or add overlay menu entries, mouse listeners, drag targets, or keyboard handling.

## Current Preset Preview
Tab: `Combat & Loadouts`

Section: `Core setup blocks`

Block: `Melee Setup`

Block type: `Gear set`

Slots:
Helmet | Cape | Amulet | Body | Legs | Gloves | Boots | Weapon

Tab: `Herblore & Farming`

Section: `Potion workflows`

Block: `Irit → Super attack`

Block type: `Recipe`

Slots:
Grimy irit | Clean irit | Irit potion (unf) | Eye of newt |
Super attack (4) | Super attack (3) | Super attack (2) | Super attack (1)

## Explicitly Out Of Scope For This Milestone
- Native bank rendering or bank-tab manipulation
- Input automation
- Bank reads
- Persistence
- Item IDs or icons
- Overflow classification
- Variants, charged-state matching, and poison-tier matching
- Network calls or external services
