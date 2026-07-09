# Combat Gear Columns

Status: early human-reviewed pattern

## Pattern

Strong gear tabs often keep combat gear in one large tab, then arrange recognizable sets as vertical columns. A column usually reads top-to-bottom like a wearable setup: head, body, legs, cape, neck, gloves, boots, weapon, shield/offhand, ammo or utility.

For ironman accounts, melee, ranged, and magic gear often stay near each other instead of being split into separate top-level tabs, because progression gear is uneven and players keep many sidegrades.

## Evidence

User-reviewed BankTabs examples show players placing their best melee setup vertically, with ranged and magic columns nearby. The repeated insight is the column pattern, not the exact item order from any one screenshot.

## Bank Architect Interpretation

The combat category should eventually support gear-set lanes:

- melee setup lane;
- ranged setup lane;
- magic setup lane;
- spec and switch lane;
- jewellery and utility lane;
- ammo and consumable combat support lane;
- remaining gear review lane.

For the Ironman preset, all combat styles should remain in one `Combat Gear` bank tab by default.

## Avoid

- Do not copy exact third-party bank layouts.
- Do not require the player to own a complete endgame setup.
- Do not make missing-item checking the main mode.
- Do not split ironman gear into separate top-level melee/ranged/magic tabs by default.

## Production Candidate

Add a layout planner that places owned best-guess gear lanes as visual columns inside the combat tab, while gracefully leaving blank slots where the player does not own a matching item. Use conservative rules until screenshot-derived matches have been manually reviewed.
