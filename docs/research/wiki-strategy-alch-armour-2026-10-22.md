# Wiki strategy audit: reviewed Alch armour and jewellery

Date: 2026-10-22. Scope: a protection-only pilot for existing candidates, not an import of all Wiki equipment tables.

## Scope and method

`alch-candidates.tsv` contains 96 explicit candidate IDs. Four ordinary elemental staves enter through its ordered staff families, giving 100 unique IDs accepted by `IronmanAlchCandidateCatalog.contains`. This armour/utility audit accounts for 60 of the 96 explicit IDs: 55 armour pieces and five unenchanted jewellery items. The remaining 36 explicit IDs and four ordinary staves are weapon/tool audit scope.

Coverage below means that each candidate's current eligibility path, classification and existing guards were inspected. It does not mean every historical strategy mentioning each item was exhaustively researched. The deeper source comparison in this pilot concerns the seven ordinary dragonhide pieces and concrete replacement examples.

The developer-only recommended-equipment cache was generated on 2026-07-14: 453 equipment rows, 889 resolved item IDs. It is useful for locating relevant pages but is not current recommendation evidence or proof of item obsolescence. In particular, its aggregate `BestRank` should not be treated as a universal equipment tier; candidate entries include apparent rank-one results for budget items. Read the actual method, equipment slot, alternatives and conditions instead.

Current primary-source revisions were fetched from the Wiki's public MediaWiki API with a descriptive research user agent. The requests used `action=query`, `prop=revisions`, `rvprop=ids|content`, `rvslots=main`, `formatversion=2`, and explicit page titles. No Wiki requests are added to the plugin.

## Candidate coverage

| Group | Exact IDs and names | Current route and conclusion |
| --- | --- | --- |
| Rune armour (8) | 1127 Rune platebody; 1079 Rune platelegs; 1093 Rune plateskirt; 1113 Rune chainbody; 1201 Rune kiteshield; 1185 Rune sq shield; 1163 Rune full helm; 1147 Rune med helm | Existing same-style/slot tier or full-stat replacement rules. Preserve the owner's explicit policy that obsolete ordinary Rune gear can go to Alch in both presets. Budget, Wilderness and clue use do not automatically retain it forever. |
| Lower metal med helms (6) | 1139 Bronze; 1137 Iron; 1141 Steel; 1151 Black; 1143 Mithril; 1145 Adamant med helm | Existing reviewed progression rules. The current override table deliberately handles these as ordinary stock; no additional protection in this pilot. |
| Lower metal chainbodies (6) | 1103 Bronze; 1101 Iron; 1105 Steel; 1107 Black; 1109 Mithril; 1111 Adamant chainbody | Existing reviewed progression rules. No automatic retention merely because a low-level guide lists them. |
| Dragon armour (5) | 3140 Dragon chainbody; 1149 Dragon med helm; 1187 Dragon sq shield; 4087 Dragon platelegs; 4585 Dragon plateskirt | Already require relevant slot/style replacement proof; some use explicit progression stages, others full stats. This audit adds no general Dragon-armour exception. |
| Adamant armour (6) | 1123 Adamant platebody; 1073 Adamant platelegs; 1091 Adamant plateskirt; 1199 Adamant kiteshield; 1183 Adamant sq shield; 1161 Adamant full helm | Preserve reviewed progression rules and the existing manual-placement precedence. |
| Mystic tops (3) | 4091 normal; 4101 dark; 4111 light Mystic robe top | Exact colour family already elects one keeper. Equivalent colour alone does not require retaining every variant. |
| Mystic bottoms (3) | 4093 normal; 4103 dark; 4113 light Mystic robe bottom | Same exact-family keeper rule; owner explicitly requested duplicate colours to be eligible. |
| Mystic hats (3) | 4089 normal; 4099 dark; 4109 light Mystic hat | Same existing colour-family rule; no new blanket protection. |
| Mystic boots (3) | 4097 normal; 4107 dark; 4117 light Mystic boots | Same existing colour-family rule. |
| Mystic gloves (3) | 4095 normal; 4105 dark; 4115 light Mystic gloves | Same existing colour-family rule. |
| Ordinary dragonhide (7) | 1135 Green d'hide body; 2499 Blue d'hide body; 2493 Blue d'hide chaps; 2501 Red d'hide body; 2495 Red d'hide chaps; 2503 Black d'hide body; 2497 Black d'hide chaps | Pilot correction: a higher scalar armour stage is insufficient. Require a recorded replacement that fully dominates the candidate's known stats. A qualifying higher-stage replacement may be a placeholder. These items are not always protected. |
| Granite armour (2) | 3122 Granite shield; 6809 Granite legs | Existing full-stat proof remains important because defence can matter separately from offence. No new positive routing or retention rule. |
| Unenchanted jewellery (5) | 11092 Diamond bracelet; 1645 Dragonstone ring; 11115 Dragonstone bracelet; 11069 Gold bracelet; 21123 Topaz bracelet | `ItemClassificationRefiner` classifies these as `SKILLING/crafting-jewellery`. The Alch gear rule returns false for their non-Combat category. Being present in the candidate table does not currently move them to Alch. Their enchanted utility versions are different IDs and are not these entries. |

The runtime also rejects missing/nonpositive Alch values, unsupported presets and unknown classifications. Existing utility tags and special-attack weapon guards run before replacement proof. The separate owner-approved Dragon halberd exception remains unchanged. Explicit item choices and captured/custom placement retain their existing precedence.

## Concrete finding: Ranged progression is not every equipment role

An armour stage describes progression for sorting; it cannot prove that every secondary function is replaced. Ordinary dragonhide can supply Magic defence without a Melee attack penalty. Strategies such as Corporeal Beast and Kraken make Magic defence relevant even when the player is not using Ranged damage.

- [Corporeal Beast/Strategies, revision 15373688](https://oldschool.runescape.wiki/w/Corporeal_Beast/Strategies?oldid=15373688) distinguishes several methods and values Magic defence in quick-pool and other setups. It recommends fortified Masori and Karil's in relevant equipment rows. This is evidence for the defensive role, not a claim that the page explicitly recommends every ordinary dragonhide piece.
- [Kraken/Strategies, revision 15371400](https://oldschool.runescape.wiki/w/Kraken/Strategies?oldid=15371400) explains that incoming accuracy uses the player's Magic defence, and discusses the tradeoff between defensive Ranged armour and stronger damage-boosting Magic robes.
- [Black d'hide body, revision 15352980](https://oldschool.runescape.wiki/w/Black_d%27hide_body?oldid=15352980) has +45 Magic defence. [Crystal body, revision 15369568](https://oldschool.runescape.wiki/w/Crystal_body?oldid=15369568) has +44 while active, despite being assigned a higher general progression stage. Crystal's damage effect is also conditional on the appropriate bow; it is not a universal body replacement.
- [Masori body, revision 15190527](https://oldschool.runescape.wiki/w/Masori_body?oldid=15190527) has +25 Magic defence. [Masori chaps, revision 15190528](https://oldschool.runescape.wiki/w/Masori_chaps?oldid=15190528) have +19, below the +23 of [Black d'hide chaps, revision 15352979](https://oldschool.runescape.wiki/w/Black_d%27hide_chaps?oldid=15352979). Their higher offensive stage does not replace that defensive role.

Consequently, a bank containing only Black d'hide plus Crystal/unfortified Masori must not use stage alone to declare the dragonhide obsolete. This conclusion is an inference from the strategy role and item stats. It is not a copied Wiki disposal recommendation.

A complete stat replacement still qualifies. For example, the blessed Saradomin pieces and fortified Masori below improve all relevant stats of their ordinary Black counterparts. This avoids preserving budget items solely because they appear somewhere in the strategy category.

## Verified regression vectors

All nine items below have zero Stab, Slash and Crush attack; zero Melee strength; zero Magic damage. Armour attack speed is represented as zero in test fixtures. Column order matches the individual fields of `GearStats`, rather than the placement score. Magic attack penalties must be preserved.

| ID / item | Slot | Magic attack | Ranged attack | Ranged strength | Prayer | Stab defence | Slash defence | Crush defence | Magic defence | Ranged defence | Wiki revision |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 2503 Black d'hide body | BODY | -15 | 30 | 0 | 0 | 30 | 38 | 45 | 45 | 50 | [15352980](https://oldschool.runescape.wiki/w/Black_d%27hide_body?oldid=15352980) |
| 2497 Black d'hide chaps | LEGS | -10 | 17 | 0 | 0 | 18 | 20 | 26 | 23 | 26 | [15352979](https://oldschool.runescape.wiki/w/Black_d%27hide_chaps?oldid=15352979) |
| 23975 Crystal body, active | BODY | -18 | 31 | 0 | 3 | 46 | 38 | 48 | 44 | 68 | [15369568](https://oldschool.runescape.wiki/w/Crystal_body?oldid=15369568) |
| 27229 Masori body | BODY | -4 | 43 | 4 | 0 | 37 | 35 | 38 | 25 | 33 | [15190527](https://oldschool.runescape.wiki/w/Masori_body?oldid=15190527) |
| 27232 Masori chaps | LEGS | -2 | 27 | 2 | 0 | 26 | 24 | 29 | 19 | 22 | [15190528](https://oldschool.runescape.wiki/w/Masori_chaps?oldid=15190528) |
| 10386 Saradomin d'hide body | BODY | -15 | 30 | 0 | 1 | 55 | 47 | 60 | 50 | 55 | [15351668](https://oldschool.runescape.wiki/w/Saradomin_d%27hide_body?oldid=15351668) |
| 10388 Saradomin chaps | LEGS | -10 | 17 | 0 | 1 | 31 | 25 | 33 | 28 | 31 | [15351669](https://oldschool.runescape.wiki/w/Saradomin_chaps?oldid=15351669) |
| 27238 Masori body (f) | BODY | -4 | 43 | 4 | 1 | 59 | 52 | 64 | 74 | 60 | [15190976](https://oldschool.runescape.wiki/w/Masori_body_%28f%29?oldid=15190976) |
| 27241 Masori chaps (f) | LEGS | -2 | 27 | 2 | 1 | 35 | 30 | 39 | 46 | 37 | [15190977](https://oldschool.runescape.wiki/w/Masori_chaps_%28f%29?oldid=15190977) |

Crystal body inactive ID 23977 has zero offensive/defensive bonuses and does not supply the active vector above. The source distinguishes these variants explicitly.

Additional inspected item revisions: [Dragon chainbody 15363006](https://oldschool.runescape.wiki/w/Dragon_chainbody?oldid=15363006), [Granite shield 15322326](https://oldschool.runescape.wiki/w/Granite_shield?oldid=15322326), [Karil's leathertop 15314560](https://oldschool.runescape.wiki/w/Karil%27s_leathertop?oldid=15314560). These do not establish a new blanket policy for their families.

## Boundaries and future audits

- Strategy presence is a research lead, not automatic protection. Budget, low-level, Wilderness-risk and clue alternatives can remain valid Alch stock under the owner's selected policy.
- Strategy-table ordering is contextual. Separate methods, complete-set conditions, specialist weapons, attack styles and side effects need independent review before adding replacement evidence.
- This pilot only removes insufficient tier-only evidence from seven existing candidates. It adds no new candidate and no GE-price rule. Existing actual-ownership/full-stat dominance remains usable; known higher-stage full-stat replacements retain placeholder support.
- No inventory/equipment read, account-level inference, automatic bank action, runtime Wiki/GE call or telemetry is introduced.
- A bank placeholder records an item identity, not its equipped presence or current usable charge state. Canonical item variants and the supplied stat source still need to be correct.
- Tests must cover both presets, real and placeholder upgrades, returning stock, missing/full stats, manual overrides and ordinary obsolete Rune/tool behaviour. A passing build does not establish complete coverage of all Wiki strategies.
