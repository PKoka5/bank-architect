# V1 Product Contract

## Product
Ironman Bank Architect is a standalone RuneLite sidebar plugin.

## First usable version
- Read InventoryID.BANK only.
- Generate one saved blueprint named:
  Ironman Combat - Core Progression
- Show a fixed 8-column combat grid in the plugin's own sidebar UI.
- Owned curated item: show the real item.
- Missing curated item: show a clearly empty planned slot.
- Show a short manual organization checklist.
- Store blueprints locally.
- Never alter the player's native bank layout.
- Never automate moving items.

## First curated grid
Row 1:
Dragon scimitar | Abyssal whip | Dragon dagger (p++) | EMPTY |
Rune crossbow | Magic shortbow (i) | Trident of the swamp | EMPTY

Row 2:
Dragon defender | Helm of neitiznot | Fighter torso | Fire cape |
Barrows gloves | Dragon boots | EMPTY | EMPTY

## Explicitly out of scope for first build
- Native bank rendering or bank-tab manipulation
- Input automation
- Overflow classification
- Variants, charged-state matching, and poison-tier matching
- Network calls or external services
