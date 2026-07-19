# Bank Organisation Rules

This document captures product rules for how Bank Architect understands and groups bank
items. It describes intent and design rules, not implementation details.

These rules are product inspiration and plugin design rules. They are not hard claims about any
one specific YouTube video unless independently verified.

## HERB_001 — Herblore prep workflows

A Herblore prep workflow is a left-to-right preparation and progression chain. In the active
Ironman preset its reviewed order is:

`grimy herb → clean herb → seed → unfinished potion → secondary → (3) → (2) → (1)`

The lower finished doses deliberately complete the potion-making line, while the ready-to-use
four-dose potion remains in Supplies. Partial owned recipes compact without phantom cells. A real
unmatched potion may occupy dense spillover space, but is never presented as part of a recipe that
has not been proven by canonical item-ID metadata.

The standard grimy, clean, and unfinished-potion stages are identified by canonical item ID and a
curated family key. Display-name recognition remains only as a deterministic fallback for herb
families that have not yet been migrated to metadata.

## POT_001 — Potion / Consumables dose grids

Known standard potions are grouped by canonical item ID and ordered `(4) → (3) → (2) → (1)` within
their destination and family. In the active Ironman preset, dose `(4)` belongs in ready-to-use
Supplies while owned `(3)`, `(2)`, and `(1)` doses join the corresponding Herblore recipe line when
that relationship is curated. A future preset may keep a full dose family together only through an
explicit preset policy.
Barbarian mixes, minigame copies, divine variants, and area-restricted variants are separate
families and must never be merged by display-name or item-ID arithmetic.

Missing doses compact in reviewed order without phantom cells. A singleton remains in the exact
deterministic fallback for its routed destination. Widths 1–8 are considered, but the current
aggregate evidence does not justify forcing one universal potion-block width.

## RESOURCE_001 — ID-backed processing columns

Canonical metal and gem families use curated family keys and exact workflow stages. Metals form
separate tier rows: ores stay together and processed bars stay together. They have no requirement
that an ore sit directly above a corresponding bar, because coal is shared and metal recipes are
not symmetrical. Uncut/cut gems remain eligible for a stage matrix with raw members above their
processed members in aligned family columns. The semantic engine considers widths 1–8, and gem
evidence prefers five family columns.

Missing stages never create phantom cells. A singleton family remains in the deterministic resource
micro-sort, while compatible real owned resources may fill otherwise free dense targets. The plugin
never invents Bank Fillers or empty targets. When no semantic shape fits the real category size, the
result is exactly the compact deterministic micro-sort order.

The metal relation expresses primary source/processed affinity, not a complete smelting recipe.
Coal remains an independent resource because steel, mithril, adamantite, and runite bars have
additional ingredient requirements.

Standard construction nails form their own horizontal material-tier run in the reviewed order
`Bronze → Iron → Steel → Mithril → Adamantite → Rune`. Black and Dragon nails are not silently
inserted into that standard run without separate reviewed semantics.

## BLOCK_001 — Semantic multi-row blocks

Some families form a two-dimensional semantic block rather than one horizontal run or one vertical
chain. A block declares ordered semantic groups and the meaning of each group. For example, one group
may contain basic members while later groups contain advanced or combined members. The candidate
generator considers every width from 1 through the current bank grid width. A family rule may reject
a width only when it would split an atomic subgroup or violate its reviewed row semantics; there is
no universal preferred width.

Width 1 is a vertical column, width 8 consumes a complete bank row, and widths 2–7 are partial-row
blocks. Rows may be shorter than their selected width when the family is incomplete or naturally
ragged. A partial block may share its remaining columns with spillover or another compatible semantic
block.

A candidate is valid only when it preserves the exact multiset of real input entries, every player
lock, all atomic family groups, their semantic order, and every bank-row boundary. It may not create
an empty target for a missing family member. Spillover is permitted only after the meaningful members
of a row, never inside an atomic group.

Valid candidates are compared by a deterministic lexicographic tuple of integer values: rule
confidence and family completeness; evidence-backed width preference; semantic row integrity and
start-column consistency; spillover contamination and fragmentation; density and unused footprint;
then required movement. Exact ties use the canonical family/rule key, width-preference rank, and
row-major item-ID order. Iteration or map order must never affect the result. Spatial compactness must
not win by destroying a clearer semantic grouping.

Each block occupies a contiguous column interval from its assigned start column; only the first block
on a physical row may begin at the left edge. Only real remaining bank items or another compatible
block may occupy columns outside the meaningful members. If the available real entries cannot form a
valid block, that family falls back to a dense deterministic order; the plugin never creates Bank
Fillers or synthetic empty targets.

Diagrams and spreadsheets supplied during design are shape examples only. Their literal items,
subgroups, row order, and coordinates do not become production rules without independent item-ID
metadata and reviewed evidence.

## GEAR_001 — Complete gearset blocks

A gearset block lists one full loadout in a fixed role order:

`Helmet → Cape → Amulet → Body → Legs → Gloves → Boots → Weapon`

This role order may later expand to include offhand, ring, ammo, and spec weapon slots as
additional gearset blocks are added.

## TELE_001 — Compact rune and teleport utility grouping

Runes and teleport utilities (teleport tabs, tablets, staves, jewellery charges) are grouped
compactly by destination or spell-book utility, favouring density over per-item detail.

## SKILL_001 — Tool → raw resource → processed resource workflow

Gathering and processing skills follow a tool-to-output chain, for example:

- `pickaxe → ore → bar`
- `axe → logs → planks/fletching outputs`

## CLEAN_001 — External storage recommendations

Some item groups are better suited to dedicated external storage than a bank slot: Seed Vault,
POH Costume Room, STASH units, Potion Storage, and similar. These are advisory-only suggestions
in the plugin — no automation moves items into external storage.

## Deliberate routing policies

### Currency split

The Ironman preset keeps actively spent progression currencies such as Hallowed marks and Marks of
grace in Main. Collection and minigame currencies — Tokkul, Stardust, Numulite, Golden nuggets,
Frog tokens, and Trading sticks — live in Clues & Cosmetics. This is a maintainer-approved product
split, not an inference from item names or value.

### Elemental tomes

A charged elemental tome is functional combat equipment and belongs in Combat Gear. Its empty tome
belongs with its charge pages in Slayer & Boss Loot. The current exact-ID routing implements this
for the charged and empty fire, water, and earth tomes. Burnt/searing, soaked, and soiled pages all
reach Slayer & Boss Loot. The Soiled page deviation recorded on 2026-07-19 was resolved the same
day with an exact-ID override for 30068; its duplicate record 30069 stays fail-closed.

### Alch and production-stock gear

Alch placement is an explicit real-bank preview decision, not a static category assigned by the
item classifier. It applies only in the Ironman preset to non-placeholder items that first route to
Combat Gear, and never moves a listed special-attack weapon. Reviewed exact-ID alch stock with more
than one copy moves when it has a positive high-alch value. A reviewed singleton needs a positive
value and at least one strictly better owned item in the same combat-style/slot tier.

For other gear, the item must have more than one copy and local gear stats. Bulk non-weapon,
non-ammo stock moves at eight or more copies when its high-alch value is at least 1,000 coins and
one strictly better same-tier item is owned. The general fallback requires a high-alch value of at
least 5,000 coins and two strictly better same-tier items. This is why smithed platebody stacks can
join Slayer & Boss Loot while useful best/backup gear, cheap clothing, weapon/ammo stacks, unknown-
value items, and placeholders stay out of the alch workflow.

## DENSE_001 — Filler-free blueprints

A generated blueprint is a dense order of actual entries from the player's bank. The plugin never
adds a Bank Filler, dummy item, or invented empty target to make a visual grid line up. A real OSRS
placeholder already present in the player's bank is preserved because it represents that player's
item slot; it is not the same thing as a Bank Filler.

Vertical set alignment is best-effort. It may use other real owned items as row spillover, but when
a complete row cannot be built without inventing a cell, the sorter must fall back to a dense order.

## FOOD_001 — ID-based food roles and direct healing

Known food is classified and sorted from a local, pinned OSRS Wiki fact table keyed by canonical
item ID. Food is ordered by immediate healing from high to low; its standard, combo, delayed, or
multi-bite role provides an explicit tie-break. Total healing is not substituted for healing per
manual consume action: a two-slice pizza that heals 11 per slice is not treated as a 22-point shark.

Variable food such as anglerfish uses its level-independent Wiki range (3–22) and sorts on the
maximum after fixed food at the same tier. The blueprint never reads live Hitpoints, inventory, or
equipment to change the bank order. Blighted restrictions and remaining servings are explicit facts.
Unknown IDs receive no invented heal value and fall back to stable name/ID ordering after curated
food. Real placeholders use their canonical item ID; Bank Fillers are excluded entirely.

Non-healing effects such as Prayer restoration, run energy, poison curing, or overheal behaviour do
not silently change this universal healing order. A future preset-specific rule may use those effects
only after they have their own explicit item-ID metadata and tests.

## TAG_001 — Future Bank Tag Layout export

A future Bank Tag Layout export may preserve the blueprint's dense item order and real owned
placeholders. It must not require Bank Fillers, dummy items, or synthetic blank cells.

## Future catalog work (not yet implemented)

The initial potion catalog covers common combat, restore, run-energy, protection, and brew
families. Newer, divine, blighted, barbarian-mix, minigame, and raid families require their own
explicit canonical IDs and semantics before they can join a preset; broad suffix matching is not
accepted as proof.
