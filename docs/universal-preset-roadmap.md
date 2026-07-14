# Universal Preset Roadmap

This note records how the multi-account bank-organisation research is adopted without changing the
active Ironman blueprint unexpectedly. It is a product and data contract, not a promise that every
listed preset is selectable today.

## Stable decisions

- The active Ironman preset keeps ten categories. Its reviewed Main combines quick-access items,
  runes, and teleports; the released slot separates Herblore recipe chains from Seeds & Farming.
- Main, PvM, PvP, and Skiller remain internal preset foundations until their routing is complete and
  preset selection has an explicit session-reset design.
- A category declares a reusable sort mode. Gear, supplies, teleports, Herblore, tools, resources,
  clues, loot, and review behaviour no longer depends on one preset using a magic category key.
- Every generated plan is dense and contains only entries already present in the scanned bank.
- Bank Fillers and invented empty cells are prohibited. Real owned OSRS placeholders remain valid.
- Unknown or weakly classified items fail closed into the preset's review destination.

## Rules that are already supported

- Equipment facts from RuneLite can drive gear slot and combat-style ordering, with name rules only
  as the current legacy fallback.
- In the active Ironman preset, curated dose-four potions remain in ready-to-use Supplies while
  owned dose-three, dose-two, and dose-one variants complete their ID-backed Herblore recipe line.
  Other presets may use a different split only through an explicit, tested routing policy. Special
  mixes retain stable fallback behaviour until their exact family semantics are curated.
- Herblore shared secondaries are allocated to the highest-tier owned recipe first.
- Resource families use production-flow ordering such as ore before bar and logs before planks.
- All moves remain manual; the plugin only reads the bank and shows guidance.

## Data-gated rules

These ideas are useful, but must not be implemented with broad name matching:

- Food sorting now has an initial local item-ID table for standard, combo, delayed, multi-bite,
  variable, and blighted food. Extend that curated table instead of adding name-based heal guesses.
- Main/PvM loot by GE value needs a permitted local RuneLite value source and a freshness policy.
- Sapling plus protection-payment pairs need a curated item-ID relationship table.
- PvP risk, replacement sets, combat requirements, STASH/Falo membership, and untradeable status
  need explicit facts before their tabs can be considered complete.
- Charged jewellery needs exact item-ID coverage for every supported family and state, including
  uncharged and eternal variants where they exist. Only after that coverage is complete may a
  family become an ordered charge run; numeric suffixes and broad display-name matching are not
  accepted. Potion doses likewise extend their canonical-ID catalog family by family.

## Explicit rejections

- No runtime OSRS Wiki, OSRSBox, or other network calls.
- No Bank Filler insertion, even as an opt-in layout feature.
- No assumption that a configurable bank width makes sparse vertical sets safe. The live bank
  compacts missing cells, so fixed columns are only retained when real entries can complete rows.
- No monolithic replacement `sortBank` function. Preset routing and reusable micro-sorters remain
  separate so each rule can be tested and improved independently.

## Verification sources

- The [OSRS Wiki bank page](https://oldschool.runescape.wiki/w/Bank) distinguishes placeholders
  from Bank Fillers and documents their bank behaviour.
- Healing values are reviewed from pinned Wiki revisions (Cooking, fast-food, hunter-meat,
  anglerfish, and blighted-food pages) and shipped locally with a source manifest. RuneLite performs
  no runtime Wiki request.
- Standard potion families and dose variants are reviewed against a pinned
  [OSRS Wiki Potions revision](https://oldschool.runescape.wiki/w/Potions?oldid=15243625), with
  canonical IDs cross-checked against the pinned RuneLite API data already used by this repository.
- Farming pair data should likewise be curated from Wiki recipe/payment pages such as
  [Yew sapling](https://oldschool.runescape.wiki/w/Yew_sapling).
