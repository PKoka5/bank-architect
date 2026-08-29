package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Optional;

/**
 * Exact item-ID overrides for canonical player-facing classification
 * exceptions (equipment, teleports, Herblore secondaries, and other
 * resources), not exclusively equipment.
 *
 * Used only where a broad display-name rule would also sweep in cert,
 * placeholder, or Battle Royale duplicate records that share a display name
 * or constant family with the real item (for example "Avernic treads (max)"
 * at ID 31097 versus the unrelated Battle Royale duplicate at ID 33172, or
 * the three identically-named "Amethyst" records where only ID 21347 is the
 * real drop). Unknown item IDs receive no override.
 */
final class CanonicalItemClassificationOverrides
{
	private CanonicalItemClassificationOverrides()
	{
	}

	static Optional<ItemClassificationRefiner.Classification> find(int itemId)
	{
			switch (itemId)
		{
			case 10: // Cannon barrels
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_barrels_(or)?oldid=15190733
			case 26524: // Reversible ornamented cannon barrels
			// Only the barrels of the ornamented cannon were listed here, so its
			// other three parts scattered across generic gear and storage cleanup.
			case 26520: // Cannon base (or)
			case 26522: // Cannon stand (or)
			case 26526: // Cannon furnace (or)
				return cannonPart();

			case 805: // Rune thrownaxe
				return thrownWeapon();

			// Wiki: https://oldschool.runescape.wiki/w/Morrigan's_throwing_axe_(bh)?oldid=15191344
			case 27912:
			case 27914: // Active and inactive Bounty Hunter states remain functional ranged weapons
				return thrownWeapon();

			case 20714: // Tome of fire
			case 25574: // Tome of water
			case 30064: // Tome of earth
				return magicOffhand();

			// The registry files both Burning claws and its drop piece under
			// SKILLING, which sent a melee weapon to the resources tab. The
			// Last Man Standing copy at 33200 stays junk; it is handled below.
			case 29577: // Burning claws
				return weapon();
			case 29574: // Burning claw: drop piece that combines into the weapon
				return gear();

			case 28997: // Dual macuahuitl
			case 4158: // Leaf-bladed spear
			case 11902: // Leaf-bladed sword
			case 20727: // Leaf-bladed battleaxe
			case 28583: // Warped sceptre (uncharged)
			case 28585: // Warped sceptre
			case 9084: // Lunar staff
				return weapon();

			// Wiki: https://oldschool.runescape.wiki/w/Dragon_battleaxe_(cr)?oldid=15191318
			case 28037: // Normal-game cosmetic dragon battleaxe
				return weapon();

			case 10498: // Ava's attractor
			case 10499: // Ava's accumulator
			case 22109: // Ava's assembler
			case 24222: // Ava's assembler (l)
			case 27374: // Masori assembler
			case 27376: // Masori assembler (l)
				return gear();

			// Wiki: https://oldschool.runescape.wiki/w/Broken_bark_snelm?oldid=15182921
			case 3335: // Functional snail-protection helmet
			// Wiki: https://oldschool.runescape.wiki/w/Fighter_hat?oldid=15260298
			case 20507:
			// Wiki: https://oldschool.runescape.wiki/w/Ranger_hat?oldid=15260300
			case 20509:
			// Wiki: https://oldschool.runescape.wiki/w/Healer_hat?oldid=15260302
			case 20511:
			// Wiki: https://oldschool.runescape.wiki/w/Runner_hat?oldid=15260301
			case 24531: // Broken Penance hats are repairable by Perdu
				return head();

			case 4740: // Bolt rack
			case 20220: // Holy blessing
			case 20223: // Unholy blessing
			case 20226: // Peaceful blessing
			case 20229: // Honourable blessing
			case 20232: // War blessing
			case 20235: // Ancient blessing
				return ammo();

			case 31088: // Avernic treads
			case 31091: // Avernic treads (pr)
			case 31092: // Avernic treads (pe)
			case 31093: // Avernic treads (et)
			case 31094: // Avernic treads (pr)(pe)
			case 31095: // Avernic treads (pr)(et)
			case 31096: // Avernic treads (pe)(et)
			case 31097: // Avernic treads (max)
			case 23037: // Boots of stone
				return feet();

			case 1: // Toolkit
			case 14: // Railing
			case 16: // Magic whistle
			case 74: // Khazard helmet
			case 75: // Khazard armour
			case 286: // Orange goblin mail
			case 287: // Blue goblin mail
			case 288: // Goblin mail
			case 295: // Glarial's amulet
			case 762: // CERT intelligence-report collision
			case 9054: // Red goblin mail
			case 9055: // Black goblin mail
			case 9056: // Yellow goblin mail
			case 9057: // Green goblin mail
			case 9058: // Purple goblin mail
			case 9059: // Pink goblin mail
			case 26567: // White goblin mail
				return cleanupQuestItem();

			case 686: // Rusty sword
				return cleanupJunk();

			// Wiki: https://oldschool.runescape.wiki/w/Broken_axe_(bronze)?oldid=15184560
			// Family cross-checks:
			// https://oldschool.runescape.wiki/w/Broken_axe_(adamant)?oldid=15184562
			// https://oldschool.runescape.wiki/w/Broken_axe_(black)?oldid=15182617
			case 494:
			case 496:
			case 498:
			case 500:
			case 502:
			case 504:
			case 6741: // Historical broken axes; automatically repaired in 2014
			// Wiki: https://oldschool.runescape.wiki/w/Logs_(Tutorial_Island)?oldid=15190473
			case 2511:
			case 24650: // Tutorial Island-only logs
			// Wiki: https://oldschool.runescape.wiki/w/Raw_shrimps_(Tutorial_Island)?oldid=15190474
			case 2514:
			case 24652: // Tutorial Island-only raw shrimps
			// Wiki: https://oldschool.runescape.wiki/w/Bones_(Tutorial_Island)?oldid=15190476
			// Wiki: https://oldschool.runescape.wiki/w/Bones_(Soul_Wars)?oldid=15209204
			case 2530:
			case 24655:
			case 25199: // Tutorial Island/Soul Wars gameplay copies
			// Wiki: https://oldschool.runescape.wiki/w/Climbing_rope?oldid=15187162
			case 4047: // Castle Wars gameplay item
			// Wiki: https://oldschool.runescape.wiki/w/Barricade?oldid=15187164
			// Wiki: https://oldschool.runescape.wiki/w/Barricade_(Soul_Wars)?oldid=15209192
			case 4053:
			case 25209:
			case 25210: // Castle Wars/Soul Wars gameplay items
			// Wiki: https://oldschool.runescape.wiki/w/Queen_help_book?oldid=15186957
			case 10562: // Reclaimable Barbarian Assault reference book
			// Wiki: https://oldschool.runescape.wiki/w/Rope_(Last_Man_Standing)?oldid=15209259
			case 20587: // LMS-only rope
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_dust?oldid=15189349
			case 23830:
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_orb?oldid=15189352
			case 23833:
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_ore?oldid=15189391
			case 23837:
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_paddlefish?oldid=15190372
			case 25958: // Corrupted Gauntlet-only resources
			// Wiki: https://oldschool.runescape.wiki/w/Burning_claws_(Last_Man_Standing)?oldid=15208943
			case 33200: // LMS-only weapon copy
			// Wiki: https://oldschool.runescape.wiki/w/Barbarian_arm?oldid=15194403
			case 33221: // Hidden Demonic Pacts League cache item
				return cleanupJunk();

			// Wiki: https://oldschool.runescape.wiki/w/Broken_pickaxe_(bronze)?oldid=15184083
			// Family cross-checks:
			// https://oldschool.runescape.wiki/w/Broken_pickaxe_(adamant)?oldid=15184086
			// https://oldschool.runescape.wiki/w/Broken_pickaxe_(black)?oldid=15189068
			case 468:
			case 470:
			case 472:
			case 474:
			case 476:
			case 478:
			case 11923:
			case 12594: // Historical broken pickaxes; automatically repaired in 2014
			// Wiki: https://oldschool.runescape.wiki/w/Broken_fishing_rod?oldid=15184527
			case 6662: // No use and cannot be repaired
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_axe?oldid=15189345
			case 23821: // Corrupted Gauntlet-only axe
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_pickaxe?oldid=15189346
			case 23822: // Corrupted Gauntlet-only pickaxe
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_harpoon?oldid=15189347
			case 23823: // Corrupted Gauntlet-only harpoon
			// Wiki: https://oldschool.runescape.wiki/w/Echo_axe?oldid=15239873
			case 25110: // Removed Raging Echoes League relic tool
			// Wiki: https://oldschool.runescape.wiki/w/Echo_pickaxe?oldid=15262483
			case 25112: // Removed Raging Echoes League relic tool
			// Wiki: https://oldschool.runescape.wiki/w/Echo_harpoon?oldid=15239875
			case 25114: // Removed Raging Echoes League relic tool
			// Wiki: https://oldschool.runescape.wiki/w/Sage's_axe?oldid=15224281
			case 28773: // Removed Trailblazer Reloaded League relic weapon
				return cleanupJunk();

			case 626: // Pink boots
			case 628: // Green boots
			case 630: // Blue boots
			case 632: // Cream boots
			case 634: // Turquoise boots
			case 636: // Pink robe top
			case 638: // Green robe top
			case 640: // Blue robe top
			case 642: // Cream robe top
			case 644: // Turquoise robe top
			case 646: // Pink robe bottoms
			case 648: // Green robe bottoms
			case 650: // Blue robe bottoms
			case 652: // Cream robe bottoms
			case 654: // Turquoise robe bottoms
			case 656: // Pink hat
			case 658: // Green hat
			case 660: // Blue hat
			case 662: // Cream hat
			case 664: // Turquoise hat
			case 19958: // Dark tuxedo jacket
			case 19961: // Dark tuxedo cuffs
			case 19964: // Dark trousers
			case 19967: // Dark tuxedo shoes
			case 19970: // Dark bow tie
			case 19973: // Light tuxedo jacket
			case 19976: // Light tuxedo cuffs
			case 19979: // Light trousers
			case 19982: // Light tuxedo shoes
			case 19985: // Light bow tie
				return clueCosmetic();

			case 88: // Boots of lightness
				return skillingUtility();

			case 1005: // White apron
				return cookingTool();

			case 20713: // Pyromancer gloves
			case 25434: // Zealot's robe top
			case 25436: // Zealot's robe bottom
			case 25438: // Zealot's helm
			case 25440: // Zealot's boots
			case 25597: // Spirit angler legs
			case 27031: // Smiths gloves (i)
			case 28172: // Forestry lumberjack legs
			case 28176: // Forestry lumberjack boots
				return skillingOutfit();

			case 235: // Unicorn horn dust
			case 243: // Blue dragon scale
			case 241: // Dragon scale dust
			case 245: // Wine of zamorak
			case 6049: // Yew roots
				return herbloreSecondary();

			case 227: // Vial of water
				return herbloreBase();

			case 9469: // Grand seed pod
			case 19564: // Royal seed pod
			case 29892: // Pendant of ates (inert)
			case 29893: // Pendant of ates
				return teleport();

			case 21387: // Master scroll book (empty)
			case 21389: // Master scroll book
				return teleportContainer();

			case 29895: // Frozen tear
				return teleportCharge();

			case 32399: // Sailors' amulet
				return teleport();

			case 10890: // Prayer book
			case 552: // Ghostspeak amulet
			case 4024: // Ninja monkey greegree
			case 4026: // Gorilla greegree
			case 4030: // Zombie monkey greegree
			case 4031: // Karamjan monkey greegree
			case 6465: // Ring of charos(a)
			case 6544: // Catspeak amulet(e)
			case 4657: // Ring of visibility
			case 4567: // Gold helmet
				return questUtility();

			case 4156: // Mirror shield
			case 4551: // Spiny helmet
			case 6720: // Slayer gloves
			case 7159: // Insulated boots
			case 31398: // Tortugan shield
			case 3337: // Pointed myre snelm
				return slayerTool();

			case 20712: // Warm gloves
			case 4600: // Willow blackjack
			case 6666: // Flippers
			case 10069: // Spotted cape
			case 10071: // Spottier cape
				return skillingUtility();

			case 22435: // Enchanted emerald sickle (b)
				return questUtility();

			// Wiki: https://oldschool.runescape.wiki/w/Blessed_axe?oldid=15254568
			case 10491: // Still chops undead trees and can damage vampyres after the quest
				return tool();

			// Wiki: https://oldschool.runescape.wiki/w/Emerald_lantern?oldid=15185115
			case 9064: // Unlit, fuelled
			case 9065: // Lit
			case 20722: // Empty, can be fuelled with lamp oil
				return lightSource();

			case 27023: // Smiths tunic
			case 27025: // Smiths trousers
			case 27027: // Smiths boots
			case 27029: // Smiths gloves
				return skillingOutfit();

			case 1923: // Bowl
				return cookingTool();

			case 25580: // Tackle box
				return resourceContainer();

			case 9419: // Mith grapple
			case 13116: // Bonecrusher
				return skillingUtility();

			case 6664: // Fishing explosive
				return slayerTool();

			case 26822: // Abyssal lantern
				return runecraftingUtility();

			case 2309: // Bread
				return food();

			case 22081: // Locator orb
				return pvmUtility();

			case 4286: // Bucket of slime
				return skillingPrayerResource();

			case 21134: // Ring of returning(3)
			case 21136: // Ring of returning(2)
			case 21138: // Ring of returning(1)
				return teleport();

			case 772: // Dramen staff
				return transportAccess();

			case 31986: // Captain's log
			case 31989: // Boat bottle (empty)
				return sailingUtility();

			case 31733: // Barrel stand
			case 31745: // Captured wind mote
			case 31757: // Heart of ithell
				return sailingUpgrade();

			case 31511: // Elkhorn frag
			case 31515: // Umbral frag
				return coralFragment();

			case 985: // Tooth half of key
			case 987: // Loop half of key
				return keyMaterial();

			case 31732: // Stormy key
			case 31744: // Fetid key
			case 31756: // Serrated key
				return rewardKey();

			case 32865: // Dull knife (salvaging relic)
			case 32870: // Smashed mirror
				return salvagingRelic();

			case 22710: // Curator's medallion
				return constructionMaterial();

			case 1540: // Anti-dragon shield
			case 3839: // Damaged book (Saradomin)
			case 3841: // Damaged book (Zamorak)
			case 3843: // Damaged book (Guthix)
			case 12607: // Damaged book (Bandos)
			case 12609: // Damaged book (Armadyl)
			case 12611: // Damaged book (Zaros)
				return shield();

			case 19677: // Ancient shard
				return equipmentCharge();

			// Wiki: https://oldschool.runescape.wiki/w/Ring_of_wealth_scroll?oldid=15186408
			case 12783: // Repeatable Last Man Standing reward used to upgrade a ring of wealth
			case 22969: // Hydra's heart
			case 22971: // Hydra's fang
			case 22973: // Hydra's eye
				return equipmentUpgrade();

			case 11942: // Ecumenical key
			case 19679: // Dark totem base
			case 19681: // Dark totem middle
			case 19683: // Dark totem top
			case 19685: // Dark totem
			case 20754: // Giant key
			case 21724: // Brittle key
			case 26356: // Frozen key
				return bossAccessKey();

			// Complete player-facing champion-scroll collection.
			case 6798:
			case 6799:
			case 6800: // Giant champion scroll
			case 6801:
			case 6802:
			case 6803:
			case 6804:
			case 6805:
			case 6806:
			case 6807: // Zombie champion scroll
			case 6808:
			case 7975: // Crawling hand
			case 7977: // Basilisk head
			case 7981: // Kq head
			case 11258: // Jar generator
			case 12007: // Jar of dirt
			case 21275: // Dark claw
			case 23064: // Jar of chemicals
			case 23077: // Hydra heads
				return collectionTrophy();

			case 21347: // Amethyst
				return skillingResource();

			case 9375: // Bronze bolts (unf)
			case 9376: // Blurite bolts (unf)
			case 9377: // Iron bolts (unf)
			case 9378: // Steel bolts (unf)
			case 9379: // Mithril bolts (unf)
			case 9380: // Adamant bolts(unf)
			case 9381: // Runite bolts (unf)
			case 9382: // Silver bolts (unf)
			case 21930: // Dragon bolts (unf)
			case 48: // Longbow (u)
			case 50: // Shortbow (u)
			case 54: // Oak shortbow (u)
			case 56: // Oak longbow (u)
			case 58: // Willow longbow (u)
			case 60: // Willow shortbow (u)
			case 62: // Maple longbow (u)
			case 64: // Maple shortbow (u)
			case 66: // Yew longbow (u)
			case 68: // Yew shortbow (u)
			case 70: // Magic longbow (u)
			case 72: // Magic shortbow (u)
				return skillingAmmoComponent();

			case 5076: // Bird's egg
			case 5077: // Bird's egg
			case 5078: // Bird's egg
				return skillingPrayerResource();

			// Wiki: https://oldschool.runescape.wiki/w/Unfinished_broad_bolts?oldid=15185058
			case 11876: // Unfinished broad bolts
			// Wiki: https://oldschool.runescape.wiki/w/Unfinished_broad_bolt_pack?oldid=15215059
			case 11887: // Unfinished broad bolt pack
				return skillingAmmoComponent();

			// Wiki: https://oldschool.runescape.wiki/w/Broken_antler?oldid=15194017
			case 31086: // Fletched into atlatl dart tips
				return skillingAmmoComponent();

			// Wiki: https://oldschool.runescape.wiki/w/Unstrung_light_ballista?oldid=15187508
			case 19604: // Unstrung light ballista
			// Wiki: https://oldschool.runescape.wiki/w/Unstrung_heavy_ballista?oldid=15187507
			case 19607: // Unstrung heavy ballista
				return skillingAmmoComponent();

			// Wiki: https://oldschool.runescape.wiki/w/Cadava_berries?oldid=15183459
			case 753: // Cadava berries: repeatable Farming payment, compost, and antipoison exchange
				return farmingProduce();

			// Wiki: https://oldschool.runescape.wiki/w/Grimy_snake_weed?oldid=15186652
			case 1525: // Grimy snake weed
			// Wiki: https://oldschool.runescape.wiki/w/Snake_weed?oldid=15184203
			case 1526: // Snake weed
			// Wiki: https://oldschool.runescape.wiki/w/Grimy_rogue's_purse?oldid=15186655
			case 1533: // Grimy rogue's purse
			// Wiki: https://oldschool.runescape.wiki/w/Goutweed?oldid=15183815
			case 3261: // Goutweed
				return herbloreSecondary();

			// Wiki: https://oldschool.runescape.wiki/w/Unfired_pot_lid?oldid=15187385
			case 4438: // Unfired pot lid: repeatable Crafting intermediate
				return craftingMaterial();

			// Wiki: https://oldschool.runescape.wiki/w/Herb_tea_mix?oldid=15185742
			case 4464:
			case 4466:
			case 4468:
			case 4470:
			case 4472:
			case 4474:
			case 4476:
			case 4478:
			case 4480:
			case 4482: // Herb tea mix variants used to make Guthix rest
			// Wiki: https://oldschool.runescape.wiki/w/Magic_essence_(unf)?oldid=15186269
			case 9019: // Magic essence (unf)
				return herbloreWorkflow();

			// Wiki: https://oldschool.runescape.wiki/w/Goblin_potion?oldid=15195296
			case 26581: // Goblin potion(4)
				return potionDose(4);
			case 26583: // Goblin potion(3)
				return potionDose(3);
			case 26585: // Goblin potion(2)
				return potionDose(2);
			case 26587: // Goblin potion(1)
				return potionDose(1);

			// Wiki: https://oldschool.runescape.wiki/w/Ardrigal_mixture?oldid=15185810
			case 738: // Ardrigal mixture
			// Wiki: https://oldschool.runescape.wiki/w/Unicorn_horn_(Underground_Pass)?oldid=15185533
			case 1487: // Unicorn horn
			// Wiki: https://oldschool.runescape.wiki/w/Grimy_ardrigal?oldid=15186651
			case 1527: // Grimy ardrigal
			// Wiki: https://oldschool.runescape.wiki/w/Grimy_sito_foil?oldid=15186653
			case 1529: // Grimy sito foil
			// Wiki: https://oldschool.runescape.wiki/w/Grimy_volencia_moss?oldid=15186654
			case 1531: // Grimy volencia moss
			// Wiki: https://oldschool.runescape.wiki/w/Mysterious_herb?oldid=15188842
			case 22402: // Mysterious herb
			// Wiki: https://oldschool.runescape.wiki/w/Unfinished_blood_potion?oldid=15188844
			case 22406: // Unfinished blood potion
			// Wiki: https://oldschool.runescape.wiki/w/Unfinished_potion_(A_Taste_of_Hope)?oldid=15188845
			case 22408: // Unfinished potion
			// Wiki: https://oldschool.runescape.wiki/w/Elder_cadantine?oldid=15189342
			case 23798: // Elder cadantine
			// Wiki: https://oldschool.runescape.wiki/w/Elder_cadantine_potion_(unf)?oldid=15189343
			case 23800: // Elder cadantine potion (unf)
			// Wiki: https://oldschool.runescape.wiki/w/Korbal_herb?oldid=15191527
			case 28384: // Korbal herb
			// Wiki: https://oldschool.runescape.wiki/w/Smooth_leaf?oldid=15192435
			case 28978: // Smooth leaf
			// Wiki: https://oldschool.runescape.wiki/w/Sticky_leaf?oldid=15192437
			case 28979: // Sticky leaf
			// Wiki: https://oldschool.runescape.wiki/w/Enriched_snapdragon?oldid=15192547
			case 29530: // Enriched snapdragon
			// Wiki: https://oldschool.runescape.wiki/w/Enriched_snapdragon_seed?oldid=15192546
			case 29538: // Enriched snapdragon seed
			// Wiki: https://oldschool.runescape.wiki/w/Grimy_note?oldid=15192550
			case 29558: // Grimy note
			// Wiki: https://oldschool.runescape.wiki/w/Putrid_sticky_potion?oldid=15254431
			case 33803:
			case 33804:
			case 33805:
			case 33806: // Putrid sticky potion variants
			// Wiki: https://oldschool.runescape.wiki/w/Foul_chunky_potion?oldid=15254430
			case 33807:
			case 33808:
			case 33809:
			case 33810: // Foul chunky potion variants
			// Wiki: https://oldschool.runescape.wiki/w/Rancid_slimy_potion?oldid=15263434
			case 33811:
			case 33812:
			case 33813:
			case 33814: // Rancid slimy potion variants
			// Wiki: https://oldschool.runescape.wiki/w/Rank_frothy_potion?oldid=15254428
			case 33815:
			case 33816:
			case 33817:
			case 33818: // Rank frothy potion variants
				return cleanupQuestItem();

			/*
			 * Phase 2 quest/farming audit. Full revision pages were checked for
			 * post-quest use; these exact objects only serve their quest step.
			 */
			// Wiki: https://oldschool.runescape.wiki/w/Yommi_tree_seeds?oldid=15184984
			// "not possible to reobtain ... after completion"; used for the Legends' Quest totem.
			case 735:
			case 736:
			// Wiki: https://oldschool.runescape.wiki/w/Consecration_seed?oldid=15184516
			// Used for the Roving Elves consecration; replacements are described only during the quest.
			case 4205:
			case 4206:
			// Wiki: https://oldschool.runescape.wiki/w/White_pearl_seed?oldid=15184378
			// "can not be planted in any farming patches".
			case 4486:
			// Wiki: https://oldschool.runescape.wiki/w/Kelda_seed?oldid=15184586
			// Grown to brew the Kelda stout required during Forgettable Tale.
			case 6112:
			// Wiki: https://oldschool.runescape.wiki/w/White_rose_seed?oldid=15184617
			case 6453:
			// Wiki: https://oldschool.runescape.wiki/w/Red_rose_seed?oldid=15184615
			case 6454:
			// Wiki: https://oldschool.runescape.wiki/w/Pink_rose_seed?oldid=15184614
			case 6455:
			// Wiki: https://oldschool.runescape.wiki/w/Vine_seed?oldid=15184616
			case 6456:
			// Wiki: https://oldschool.runescape.wiki/w/Delphinium_seed?oldid=15184613
			case 6457:
			// Wiki: https://oldschool.runescape.wiki/w/Orchid_seed_(pink)?oldid=15186005
			case 6458:
			// Wiki: https://oldschool.runescape.wiki/w/Orchid_seed_(yellow)?oldid=15186006
			case 6459:
			// Wiki: https://oldschool.runescape.wiki/w/Snowdrop_seed?oldid=15183912
			case 6460:
			// Wiki: https://oldschool.runescape.wiki/w/White_tree_sapling?oldid=15184618
			case 6464:
			// The garden family is planted in Queen Ellamaria's quest garden; no continuing use is recorded.
			// Wiki: https://oldschool.runescape.wiki/w/Plant_cure_(Garden_of_Tranquillity)?oldid=15184621
			// It can still be created later, but its recorded use is curing the quest's Burthorpe vines.
			case 6468:
			// Wiki: https://oldschool.runescape.wiki/w/Blindweed_seed?oldid=15185824
			// The player "must grow" it during Rum Deal for the quest's unsanitary swill.
			case 6710:
			// Wiki: https://oldschool.runescape.wiki/w/Auguste's_sapling?oldid=15185627
			// "cannot be re-obtained after completing the quest".
			case 9932:
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_(Song_of_the_Elves)?oldid=15189419
			// Quest item ground into the crystal dust needed for the inversion potion.
			case 23802:
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_seed_(Song_of_the_Elves)?oldid=15189418
			// Quest item used to locate Lord Amlodd before Eluned enchants it.
			case 23808:
			case 23810:
				return cleanupQuestItem();

			// Wiki: https://oldschool.runescape.wiki/w/Bone_seeds?oldid=15184790
			// "obtained during and after" Swan Song; repeatedly summons an emote-performing skeleton.
			case 7950:
				return clueCosmetic();

			// Wiki: https://oldschool.runescape.wiki/w/Crystal_saw_seed?oldid=15203824
			// Quest reward that "can be enchanted into a crystal saw" and can be replaced if lost.
			case 9626:
				return tool();

			/*
			 * Phase 2 quest/potions-food audit. Every cited revision was read in
			 * full, including post-quest and re-obtainability sections.
			 */
			// Wiki: https://oldschool.runescape.wiki/w/%3F%3F%3F_mixture?oldid=15188933
			// "used only in Recruitment Drive"; none of the failed mixtures help complete the trial.
			case 5591:
			// Wiki: https://oldschool.runescape.wiki/w/Agility_dolmen?oldid=15192526
			case 29539:
			// Wiki: https://oldschool.runescape.wiki/w/Attack_dolmen?oldid=15192529
			case 29542:
			// Wiki: https://oldschool.runescape.wiki/w/Balance_dolmen?oldid=15192530
			case 29551:
			// Wiki: https://oldschool.runescape.wiki/w/Combat_dolmen?oldid=15192534
			case 29545:
			// Wiki: https://oldschool.runescape.wiki/w/Defence_dolmen?oldid=15192538
			case 29544:
			// Wiki: https://oldschool.runescape.wiki/w/Energy_dolmen?oldid=15192545
			case 29540:
			// Wiki: https://oldschool.runescape.wiki/w/Hunter_dolmen?oldid=15192551
			case 29548:
			// Wiki: https://oldschool.runescape.wiki/w/Inversion_potion?oldid=15189413
			// Made only to enter the crystallised Grand Library; attempting to drink it is refused.
			case 23806:
			// Wiki: https://oldschool.runescape.wiki/w/Magic_dolmen?oldid=15192553
			case 29550:
			// Wiki: https://oldschool.runescape.wiki/w/Prayer_dolmen?oldid=15192557
			case 29547:
			// Wiki: https://oldschool.runescape.wiki/w/Ranged_dolmen?oldid=15192558
			case 29546:
			// Wiki: https://oldschool.runescape.wiki/w/Restoration_dolmen?oldid=15192559
			case 29541:
			// Wiki: https://oldschool.runescape.wiki/w/Strength_dolmen?oldid=15192566
			// Each dolmen is used once on the central table to open the While Guthix Sleeps door.
			case 29543:
			// Wiki: https://oldschool.runescape.wiki/w/Amitire_stew?oldid=15252771
			// Made during The Blood Moon Rises and given to Safalaan so he can recover.
			case 33797:
			// Wiki: https://oldschool.runescape.wiki/w/Apricot_cream_pie?oldid=15209189
			// "unobtainable item" shown only in a Fremennik Isles cutscene.
			case 10841:
			// Wiki: https://oldschool.runescape.wiki/w/Arder-musca_poison?oldid=15191551
			// Two are fed to Duke Sucellus for the one quest encounter; no normal-bank use is recorded.
			case 28351:
			// Wiki: https://oldschool.runescape.wiki/w/Arder-resper_poison?oldid=15191547
			case 28355:
			// Wiki: https://oldschool.runescape.wiki/w/Holos-arder_poison?oldid=15191549
			case 28353:
			// Wiki: https://oldschool.runescape.wiki/w/Musca-holos_poison?oldid=15191552
			case 28350:
			// Wiki: https://oldschool.runescape.wiki/w/Musca-resper_poison?oldid=15191550
			case 28352:
			// Wiki: https://oldschool.runescape.wiki/w/Resper-holos_poison?oldid=15191548
			// These five poison combinations are explicitly described as scrapped Duke-fight items.
			case 28354:
			// Wiki: https://oldschool.runescape.wiki/w/Blood_potion?oldid=15188847
			// Used on Serafina's door during A Taste of Hope, not drunk as a repeatable potion.
			case 22407:
			// Wiki: https://oldschool.runescape.wiki/w/Bravery_potion?oldid=15185855
			// It can still be made later, but "has no uses aside from the quest".
			case 739:
			// Wiki: https://oldschool.runescape.wiki/w/Cadava_potion?oldid=15185450
			// Quest-only sleeping potion; it cannot be made through normal Herblore.
			case 756:
			// Wiki: https://oldschool.runescape.wiki/w/Cake_of_guidance?oldid=15185652
			// Used on the Lumbridge Guide and "cannot be eaten".
			case 7542:
			// Wiki: https://oldschool.runescape.wiki/w/Canvas_piece?oldid=14960776
			// Used once on The Final Dawn's sun-and-moon mural.
			case 30950:
			// Wiki: https://oldschool.runescape.wiki/w/Cat_antipoison?oldid=15185820
			// Cures Pox and "can only be obtained during the quest".
			case 6766:
			// Wiki: https://oldschool.runescape.wiki/w/Cloudy_grey_potion?oldid=15254404
			case 33769:
			// Wiki: https://oldschool.runescape.wiki/w/Cold_bluish-white_potion?oldid=15254407
			case 33772:
			// Wiki: https://oldschool.runescape.wiki/w/Thick_red_potion?oldid=15254406
			case 33771:
			// Wiki: https://oldschool.runescape.wiki/w/Weightless_black_potion?oldid=15254405
			// These four shelf potions exist only as pieces of the Blood Moon refiner puzzle.
			case 33770:
			// Wiki: https://oldschool.runescape.wiki/w/Code_converter?oldid=15191498
			// Used with the magic lantern to decode one Desert Treasure II vault password.
			case 28425:
			// Wiki: https://oldschool.runescape.wiki/w/Crocodile_emblem?oldid=15190910
			// Quest emblem obtained for the Beneath Cursed Sands progression puzzle.
			case 26959:
			// Wiki: https://oldschool.runescape.wiki/w/Decoder_strips?oldid=14906664
			// Used with the code key to open The Curse of Arrav's vault.
			case 30317:
			// Wiki: https://oldschool.runescape.wiki/w/Dream_potion?oldid=15184994
			// Consumed by the two recorded quest dream sequences; no post-quest use is listed.
			case 11154:
			// Wiki: https://oldschool.runescape.wiki/w/Drinking_flask?oldid=14764548
			// Death on the Isle evidence item whose liquid cannot be identified or consumed.
			case 29925:
			// Wiki: https://oldschool.runescape.wiki/w/Dwarf_brew?oldid=15185538
			// Used to burn Iban's tomb; Kamen's offered drink only drains Agility.
			case 1501:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_bear?oldid=15185653
			case 524:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_beef?oldid=15185656
			case 522:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_chicken?oldid=15185654
			case 525:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_rat?oldid=15185655
			// Each meat is "used only in Druidic Ritual" and cannot be made again after the quest.
			case 523:
			// Wiki: https://oldschool.runescape.wiki/w/Explosive_potion_(Song_of_the_Elves)?oldid=15189412
			// Prepared by Elena solely to reinforce holes during Song of the Elves.
			case 23818:
			// Wiki: https://oldschool.runescape.wiki/w/Fresh_monkfish?oldid=15184848
			// Quest-specific stock fish: only 1 Fishing XP and 1 Hitpoint when eaten.
			case 7942:
			case 7943:
			// Wiki: https://oldschool.runescape.wiki/w/Giant_carp?oldid=15185614
			// Raw giant carp "can only be caught" before receiving the Fishing Contest trophy.
			case 337:
			// Wiki: https://oldschool.runescape.wiki/w/Karambwan_paste?oldid=15184059
			// Raw and cooked paste "cannot be used to poison weaponry"; only ID 3153 remains functional.
			case 3152:
			case 3154:
			// Wiki: https://oldschool.runescape.wiki/w/Karambwanji?oldid=15183757
			// After its quest step, cooking it reports "You don't feel that would be useful now."
			case 21394:
			// Wiki: https://oldschool.runescape.wiki/w/Karambwanji_paste?oldid=15185107
			// Raw paste is a quest ingredient; cooked paste is explicitly "useless".
			case 3155:
			case 3156:
			// Wiki: https://oldschool.runescape.wiki/w/Khali_brew?oldid=15184623
			// Cannot be drunk; it only intoxicates the Fight Arena head guard.
			case 77:
			// Wiki: https://oldschool.runescape.wiki/w/Magic_ogre_potion?oldid=15184411
			// "cannot be made after the quest" and the wizard confiscates retained copies.
			case 2395:
			// Wiki: https://oldschool.runescape.wiki/w/Map_piece?oldid=15188595
			// The 24 pieces combine into Dragon Slayer II's one Lithkren map.
			case 22009:
			case 22010:
			case 22011:
			case 22012:
			case 22013:
			case 22014:
			case 22015:
			case 22016:
			case 22017:
			case 22018:
			case 22019:
			case 22020:
			case 22021:
			case 22022:
			case 22023:
			case 22024:
			case 22025:
			case 22026:
			case 22027:
			case 22028:
			case 22029:
			case 22030:
			case 22031:
			case 22032:
			// Wiki: https://oldschool.runescape.wiki/w/Piece_of_railing?oldid=15185534
			// Used once to break the Underground Pass unicorn cage.
			case 1486:
			// Wiki: https://oldschool.runescape.wiki/w/Potion_(Watchtower)?oldid=15185888
			// ID 2394 can be remade later but "has no further use".
			case 2394:
			// Wiki: https://oldschool.runescape.wiki/w/Potion_(A_Taste_of_Hope)?oldid=15188846
			// ID 22409 is used only on Serafina's door.
			case 22409:
			// Wiki: https://oldschool.runescape.wiki/w/Potion_note?oldid=15191529
			// A Desert Treasure II desk clue, not a consumable potion.
			case 28382:
			// Wiki: https://oldschool.runescape.wiki/w/Potion_of_sealegs?oldid=15188445
			// Made for and handed to Bone Voyage's Lead Navigator.
			case 21531:
			// Wiki: https://oldschool.runescape.wiki/w/Red_herring?oldid=15185671
			// Quest puzzle item; its later one-off conversion to ordinary herring is not a repeatable recipe.
			case 3742:
			// Wiki: https://oldschool.runescape.wiki/w/Reduced_cadava_potion?oldid=15188965
			// Calibrated once for the Wise Old Man during Making Friends with My Arm.
			case 22589:
			// Wiki: https://oldschool.runescape.wiki/w/Revitalisation_potion_(Dragon_Slayer_II)?oldid=15188654
			// Only heals injured Fremennik warriors during the Ungael voyage.
			case 22096:
			// Wiki: https://oldschool.runescape.wiki/w/Scarred_scraps?oldid=15191481
			// "CANNOT be taken out of the Scar" and is removed when a quest level completes.
			case 28443:
			// Wiki: https://oldschool.runescape.wiki/w/Seasoned_chompy?oldid=15185551
			// Cooked for and handed to Rantz to complete Big Chompy Bird Hunting.
			case 2882:
			// Wiki: https://oldschool.runescape.wiki/w/Seasoned_sardine?oldid=15186279
			// "serves no further use after the quest".
			case 1552:
			// Wiki: https://oldschool.runescape.wiki/w/Sheep_bones_(1)?oldid=15187374
			// Wiki: https://oldschool.runescape.wiki/w/Sheep_bones_(2)?oldid=15187375
			// Wiki: https://oldschool.runescape.wiki/w/Sheep_bones_(3)?oldid=15187376
			// Each colour is "used only in Sheep Herder" and cannot be buried.
			case 280:
			case 281:
			case 282:
			case 283:
			// Wiki: https://oldschool.runescape.wiki/w/Shielding_potion?oldid=15190236
			// Used once to reinforce the Doors of Dinh.
			case 25813:
			// Wiki: https://oldschool.runescape.wiki/w/Smelly_kebab?oldid=15254427
			// Realm-limited quest supply that cannot be consumed in the normal world.
			case 33820:
			// Wiki: https://oldschool.runescape.wiki/w/Strange_potion?oldid=15185281
			// ID 4836 can only transform Sithik during Zogre Flesh Eaters.
			case 4836:
			// Wiki: https://oldschool.runescape.wiki/w/Strange_potion_(Desert_Treasure_II)?oldid=15191528
			// ID 28383 is drunk once to enter the Stranglewood during Desert Treasure II.
			case 28383:
			// Wiki: https://oldschool.runescape.wiki/w/Strangler_serum?oldid=15191524
			// Prevents quest infection while traversing the Stranglewood.
			case 28388:
			// Wiki: https://oldschool.runescape.wiki/w/Strong_cup_of_tea?oldid=15190855
			// A regular tea relabelled and delivered to Herbert during Temple of the Eye.
			case 26904:
			// Wiki: https://oldschool.runescape.wiki/w/Stuffed_snake?oldid=15232804
			// Ingredients cannot be procured after the subquest; only existing leftovers can be cooked.
			case 7579:
			// Wiki: https://oldschool.runescape.wiki/w/Sulphur_potion?oldid=15190237
			// Quest intermediate enchanted into the one-use shielding potion.
			case 25812:
			// Wiki: https://oldschool.runescape.wiki/w/Test_kebab?oldid=14764698
			// Two identical quest-stage samples delivered during Meat and Greet.
			case 29898:
			case 29899:
			// Wiki: https://oldschool.runescape.wiki/w/Tome_of_experience_(Darkness_of_Hallowvale)?oldid=15185702
			// Three one-use chapters; the tome "cannot be banked" and crumbles after reading.
			case 9656:
			case 9657:
			case 9658:
			// Wiki: https://oldschool.runescape.wiki/w/Troll_potion?oldid=15185541
			// After the quest it "can no longer be created": "You don't need to make any more."
			case 3265:
			// Wiki: https://oldschool.runescape.wiki/w/Unfinished_serum?oldid=15191525
			// Kasonde's quest intermediate for the Strangler serum.
			case 28386:
			case 28387:
			// Wiki: https://oldschool.runescape.wiki/w/Wine_labels?oldid=14764724
			// Death on the Isle evidence used to identify the cellar's wine jugs.
			case 29928:
				return cleanupQuestItem();

			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_lyre?oldid=15263869
			// "After the quest" it remains a rechargeable teleport to several Fremennik destinations.
			case 3691:
			case 6125:
			case 6126:
			case 6127:
				return teleport();

			// Wiki: https://oldschool.runescape.wiki/w/Ground_cod?oldid=15183709
			// Repeatably made Cooking ingredient used to produce fishcakes.
			case 7528:
			// Wiki: https://oldschool.runescape.wiki/w/Olive_oil?oldid=15185750
			// Repeatably bought minigame material converted into sacred oil for pyre logs.
			case 3422:
			case 3424:
			case 3426:
			case 3428:
				return skillingResource();

			// Wiki: https://oldschool.runescape.wiki/w/Holy_water?oldid=15242493
			// Repeatably craftable demonbane Ranged weapon with a 60% damage multiplier.
			case 732:
				return thrownWeapon();

			// Wiki: https://oldschool.runescape.wiki/w/Rod_of_ivandis?oldid=15252513
			// Charged silver staff, autocasting weapon, and tier-2 vampyre weapon after the quest.
			case 7637: // Silvthrill rod
			case 7638: // Enchanted Silvthrill rod
			case 7639:
			case 7640:
			case 7641:
			case 7642:
			case 7643:
			case 7644:
			case 7645:
			case 7646:
			case 7647:
			case 7648:
				return weapon();

			/*
			 * Phase 2, batch 11 (Resources quest-items, part 1).
			 * Full revision-pinned Wiki pages were reviewed for every family below.
			 * These exact quest-stage materials have no repeatable normal-bank
			 * resource workflow after their documented quest or puzzle use.
			 */
			// Wiki: https://oldschool.runescape.wiki/w/Magnet_(Recruitment_Drive)?oldid=15188932
			// The Recruitment Drive copies are deleted after their one-time puzzle use.
			case 2410:
			case 3718:
			case 5604:
			// Wiki: https://oldschool.runescape.wiki/w/Cupric_ore_powder?oldid=15186923
			// Wiki: https://oldschool.runescape.wiki/w/Tin_ore_powder?oldid=15186924
			// Used only to solve the Recruitment Drive chemistry puzzle.
			case 5584:
			case 5583:
			// Wiki: https://oldschool.runescape.wiki/w/Fishing_pass?oldid=15239594
			// Wiki: https://oldschool.runescape.wiki/w/Fishing_trophy?oldid=15185693
			// Wiki: https://oldschool.runescape.wiki/w/Raw_giant_carp?oldid=15185613
			// Fishing Contest-only access, proof, and catch; the carp cannot be caught afterward.
			case 27:
			case 26:
			case 338:
			// Wiki: https://oldschool.runescape.wiki/w/Rose-tinted_lens?oldid=15250402
			// The Hand in the Sand evidence chain has no repeatable post-quest workflow.
			case 6956:
			case 6947:
			case 6958:
			case 6954:
			case 6948:
			case 6950:
			case 6951:
			case 6946:
			case 6953:
			case 6945:
			case 6952:
			case 6957:
			// Wiki: https://oldschool.runescape.wiki/w/Orb_of_light?oldid=15185532
			// Wiki: https://oldschool.runescape.wiki/w/Orb_of_light_(Song_of_the_Elves)?oldid=15189428
			// Quest-puzzle objects that are destroyed or consumed while opening their route.
			case 1481:
			case 1482:
			case 1483:
			case 1484:
			case 23812:
			// Wiki: https://oldschool.runescape.wiki/w/Magic_gold_feather?oldid=15183365
			// The Holy Grail trail marker explicitly has no use after the quest.
			case 18:
			// Wiki: https://oldschool.runescape.wiki/w/Silver_necklace?oldid=15183215
			// Murder Mystery evidence pieces are used only to identify the culprit.
			case 1797:
			case 1799:
			case 1801:
			case 1803:
			// Wiki: https://oldschool.runescape.wiki/w/Burnt_fishcake?oldid=15188252
			// The burnt result explicitly has no use.
			case 7531:
			// Wiki: https://oldschool.runescape.wiki/w/Keg_(The_Great_Brain_Robbery)?oldid=15186953
			// The quest keg cannot be picked up after completion; the second ID is a dummy copy.
			case 10885:
			case 10898:
			// Wiki: https://oldschool.runescape.wiki/w/Metal_feather?oldid=15183683
			// Rendered useless by the Eagles' Peak mechanism and not re-obtainable afterward.
			case 10174:
			// Wiki: https://oldschool.runescape.wiki/w/Mysterious_orb_(Client_of_Kourend)?oldid=15188336
			// Client of Kourend-only locator; extra copies are removed after the quest.
			case 21261:
			// Wiki: https://oldschool.runescape.wiki/w/Vial_(jangerberries_and_guam_leaf)?oldid=15186989
			// Watchtower potion intermediates have no further use after the quest.
			case 2389:
			case 2390:
			// Wiki: https://oldschool.runescape.wiki/w/Display_cabinet_key?oldid=15185909
			// The museum puzzle key is explicitly useless after the quest.
			case 4617:
			// Wiki: https://oldschool.runescape.wiki/w/Waxwood_log?oldid=15189856
			// Wiki: https://oldschool.runescape.wiki/w/Waxwood_plank?oldid=15189857
			// Daddy's Home-only processing materials; duplicate logs cannot be obtained.
			case 24938:
			case 24939:
			// Wiki: https://oldschool.runescape.wiki/w/Golden_feather_(Priest_in_Peril)?oldid=15186885
			// ID 2950 is the Priest in Peril trail item and has no post-quest use.
			case 2950:
			// Wiki: https://oldschool.runescape.wiki/w/Barrel_bomb?oldid=15238286
			// Regicide explosives and their intermediates are only used by the quest chain.
			case 3218:
			case 3219:
			case 3220:
			case 3221:
			case 3215:
			case 6095:
			case 3222:
			case 3223:
			case 6093:
			// Wiki: https://oldschool.runescape.wiki/w/Ana_in_a_barrel?oldid=15238258
			// The barrel is the one-time Tourist Trap rescue transport.
			case 1842:
			// Wiki: https://oldschool.runescape.wiki/w/Ardougne_knight_tabard?oldid=15189341
			// Song of the Elves disguise material, consumed by the quest workflow.
			case 23791:
			// Wiki: https://oldschool.runescape.wiki/w/Axe_head?oldid=15185054
			// The Dorgesh-Kaan quest specimen has no normal axe/resource use.
			case 11050:
			// Wiki: https://oldschool.runescape.wiki/w/Selected_iron?oldid=15183695
			// Wiki: https://oldschool.runescape.wiki/w/Bar_magnet?oldid=15183696
			// Animal Magnetism-only magnetisation intermediates.
			case 10488:
			case 10489:
			// Wiki: https://oldschool.runescape.wiki/w/Bone_key_(Ghosts_Ahoy)?oldid=15185195
			// The Ghosts Ahoy key is explicitly useless after the quest.
			case 4272:
			// Wiki: https://oldschool.runescape.wiki/w/Bone_beads?oldid=15185259
			// Wiki: https://oldschool.runescape.wiki/w/Bone_shard_(Shilo_Village)?oldid=15185260
			// Shilo Village quest-stage remains and necklace components.
			case 618:
			case 604:
			case 609:
			case 610:
			// Wiki: https://oldschool.runescape.wiki/w/Bone_charm?oldid=15188444
			// The Great Brain Robbery navigation charm is a one-time quest object.
			case 21530:
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_barrel_(Cabin_Fever)?oldid=15184798
			// Cabin Fever repair material, not a functional dwarf multicannon part.
			case 7145:
			// Wiki: https://oldschool.runescape.wiki/w/Clay_head?oldid=15189933
			// Destroyed during Getting Ahead and unavailable after mounting the replacement.
			case 25145:
			// Wiki: https://oldschool.runescape.wiki/w/Dwarven_battleaxe?oldid=15183724
			// Quest/animation states, not usable battleaxes.
			case 5056:
			case 5057:
			case 5058:
			case 5059:
			case 5060:
			case 5061:
			// Wiki: https://oldschool.runescape.wiki/w/Fishbowl_and_net?oldid=15185826
			// Sea Slug quest capture object; it only untangles back into ordinary components.
			case 6673:
			// Wiki: https://oldschool.runescape.wiki/w/Ground_charcoal?oldid=15184009
			// A quest-only ingredient for the Watchtower and Zogre Flesh Eaters chains.
			case 704:
			// Wiki: https://oldschool.runescape.wiki/w/Iban's_ashes?oldid=15185539
			// Underground Pass-only remains used to enchant the doll.
			case 1502:
			// Wiki: https://oldschool.runescape.wiki/w/Orb_of_protection?oldid=15183408
			// Fight Arena-only protection object with no post-quest workflow.
			case 587:
			// Wiki: https://oldschool.runescape.wiki/w/Prototype_dart_tip?oldid=15185562
			// Tourist Trap quest prototype rather than a normal Fletching component.
			case 1853:
			// Wiki: https://oldschool.runescape.wiki/w/Raw_guide_cake?oldid=15185651
			// Recipe for Disaster quest intermediate, not repeatable food production.
			case 7543:
			// Wiki: https://oldschool.runescape.wiki/w/Custom_bow_string?oldid=15186838
			// Fremennik Trials merchant-chain barter objects.
			case 3702:
			case 3703:
			case 3704:
			case 3705:
			// Wiki: https://oldschool.runescape.wiki/w/Weapon_store_key?oldid=15185730
			// The Grand Tree quest access key has no documented repeatable function.
			case 759:
			// Wiki: https://oldschool.runescape.wiki/w/Battleaxe_(The_Blood_Moon_Rises)?oldid=15255255
			// Blood Moon realm/quest-only construction, combat, and puzzle objects.
			case 28390:
			case 33759:
			case 33791:
			case 33743:
			case 33765:
			case 33766:
			case 33790:
			case 33792:
			case 33793:
			case 33794:
			// Wiki: https://oldschool.runescape.wiki/w/Bone_in_vinegar?oldid=15186165
			// Rag and Bone Man II museum specimens and prepared states have only the wishlist use.
			case 7812:
			case 7813:
			case 7814:
			case 7815:
			case 7816:
			case 7817:
			case 7818:
			case 7819:
			case 7820:
			case 7821:
			case 7822:
			case 7823:
			case 7824:
			case 7825:
			case 7826:
			case 7827:
			case 7828:
			case 7829:
			case 7830:
			case 7831:
			case 7832:
			case 7833:
			case 7834:
			case 7835:
			case 7836:
			case 7837:
			case 7838:
			case 7839:
			case 7840:
			case 7841:
			case 7842:
			case 7843:
			case 7844:
			case 7845:
			case 7846:
			case 7847:
			case 7848:
			case 7849:
			case 7850:
			case 7851:
			case 7852:
			case 7853:
			case 7854:
			case 7855:
			case 7856:
			case 7857:
			case 7858:
			case 7859:
			case 7860:
			case 7861:
			case 7862:
			case 7863:
			case 7864:
			case 7865:
			case 7866:
			case 7867:
			case 7868:
			case 7869:
			case 7870:
			case 7871:
			case 7872:
			case 7873:
			case 7874:
			case 7875:
			case 7876:
			case 7877:
			case 7878:
			case 7879:
			case 7880:
			case 7881:
			case 7882:
			case 7883:
			case 7884:
			case 7885:
			case 7886:
			case 7887:
			case 7888:
			case 7889:
			case 7890:
			case 7891:
			case 7892:
			case 7893:
			case 7894:
			case 7895:
			case 7896:
			case 7897:
			case 7898:
			case 7899:
			case 7900:
			case 7901:
			case 7902:
			case 7903:
			case 7904:
			case 7905:
			case 7906:
			case 7907:
			case 7908:
			case 7909:
			case 7910:
			case 7911:
			case 7912:
			case 7913:
			case 7914:
			case 7915:
			case 7916:
			// Wiki: https://oldschool.runescape.wiki/w/Cave_goblin_skull?oldid=15186330
			// Hopespear's Will leader remains are buried once in the goblin crypt.
			case 26589:
			case 26590:
			case 26591:
			case 26592:
			case 26593:
			// Wiki: https://oldschool.runescape.wiki/w/Pulley_beam?oldid=15184592
			// Elemental Workshop II machine-repair components.
			case 7967:
			case 7969:
			case 7970:
			case 7971:
			// Wiki: https://oldschool.runescape.wiki/w/Fishing_dolmen?oldid=15192549
			// One-time Twilight's Promise trial object.
			case 29549:
			// Wiki: https://oldschool.runescape.wiki/w/Strange_icon?oldid=15236739
			// Disintegrates after the miniquest offering.
			case 28130:
			// Wiki: https://oldschool.runescape.wiki/w/Gooey_note?oldid=15191459
			// Desert Treasure II puzzle notes explicitly report no further need on destruction.
			case 28468:
			case 28469:
			case 28470:
			// Wiki: https://oldschool.runescape.wiki/w/Feathered_journal?oldid=15185357
			// Wiki: https://oldschool.runescape.wiki/w/Astronomy_book?oldid=15182703
			// Wiki: https://oldschool.runescape.wiki/w/Book_of_spyology?oldid=15187475
			// Quest books whose permanent readable copies can be stored in a POH bookcase.
			case 10179:
			case 600:
			case 19515:
			// Wiki: https://oldschool.runescape.wiki/w/Old_note_(Sins_of_the_Father)?oldid=15189785
			// Wiki: https://oldschool.runescape.wiki/w/Mucky_note?oldid=15191569
			// Completed quest-puzzle notes with no repeatable material function.
			case 24682:
			case 28401:
				return cleanupQuestItem();

			// Wiki: https://oldschool.runescape.wiki/w/Raw_fishcake?oldid=15184847
			// Repeatably produced Cooking intermediate; remains a Fishing/Cooking resource.
			case 7529:
				return skillingResource();

			// Wiki: https://oldschool.runescape.wiki/w/Cooked_fishcake?oldid=15183721
			// Repeatably cooked food that heals 11 hitpoints.
			case 7530:
				return food();

			// Wiki: https://oldschool.runescape.wiki/w/Panning_tray?oldid=15238922
			// Both normal tray states support repeatable panning for Mining/Fishing rewards.
			case 678:
			case 679:
				return skillingUtility();

			// Wiki: https://oldschool.runescape.wiki/w/Bone_key_(Shilo_Village)?oldid=15236887
			// Repeatable access to Rashiliyia's tomb; can also be stored on the key ring.
			case 605:
				return questUtility();

			// Wiki: https://oldschool.runescape.wiki/w/The_fisher's_flute?oldid=15188540
			// Wiki: https://oldschool.runescape.wiki/w/Kharedst's_memoirs?oldid=15240499
			// This torn page is consumed into the memoirs: it unlocks Port Piscarilius,
			// adds 20 teleport charges, and raises the book's maximum by 20.
			case 21764:
				return teleportCharge();

			/*
			 * Phase 2, batch 12 (Resources quest-items, part 2).
			 * Every linked revision page was read in full, including post-quest,
			 * re-obtainability, destruction, and later-use sections.
			 */
			// One-time quest/puzzle materials with no repeatable normal-bank function.
			// Sources: https://oldschool.runescape.wiki/w/Ancient_roots?oldid=14960565
			// https://oldschool.runescape.wiki/w/Bark_sample?oldid=15185187
			// https://oldschool.runescape.wiki/w/Barrel_(The_Tourist_Trap)?oldid=15238260
			// https://oldschool.runescape.wiki/w/Bone_(A_Kingdom_Divided)?oldid=15190251
			case 30963:
			case 783:
			case 1841:
			case 3216:
			case 25794:
			// Perilous Moons request, Eagles' Peak door feathers, and completed chore list.
			// https://oldschool.runescape.wiki/w/Bream_scales?oldid=15192354
			// https://oldschool.runescape.wiki/w/Golden_feather_(Eagles'_Peak)?oldid=15185630
			// https://oldschool.runescape.wiki/w/Chores?oldid=15185466
			case 28970:
			case 10177:
			case 10175:
			case 10176:
			case 6545:
			// Quest containers, repair fibres, dust, tea states, and Castle Drakan materials.
			// https://oldschool.runescape.wiki/w/Crate_(In_Aid_of_the_Myreque)?oldid=15185575
			// https://oldschool.runescape.wiki/w/Crimson_fibre?oldid=15191463
			// https://oldschool.runescape.wiki/w/Crystal_dust_(Song_of_the_Elves)?oldid=15189420
			// https://oldschool.runescape.wiki/w/Cup_of_tea_(Ghosts_Ahoy)?oldid=15185786
			// https://oldschool.runescape.wiki/w/Daeyalt_ore_(The_Blood_Moon_Rises)?oldid=15248692
			case 7630:
			case 28462:
			case 28463:
			case 23804:
			case 4245:
			case 4246:
			case 33777:
			case 33776:
			// Explicitly exhausted quest-stage planks, tools, nerves, books, and intermediates.
			// https://oldschool.runescape.wiki/w/Damp_planks?oldid=15184220
			// https://oldschool.runescape.wiki/w/Dinh's_hammer?oldid=15189075
			// https://oldschool.runescape.wiki/w/Dust_nerve?oldid=15191466
			// https://oldschool.runescape.wiki/w/Dwarven_lore?oldid=15184938
			// https://oldschool.runescape.wiki/w/Enchanted_bar?oldid=15185537
			case 11031:
			case 22761:
			case 28458:
			case 4568:
			case 4007:
			// Watchtower evidence, quest feathers, notes, bark, barronite, and orb intermediate.
			// https://oldschool.runescape.wiki/w/Fingernails?oldid=15184409
			// https://oldschool.runescape.wiki/w/Fire_feather?oldid=15183799
			// https://oldschool.runescape.wiki/w/Griffin_feather?oldid=15184997
			// https://oldschool.runescape.wiki/w/Ground_bat_bones?oldid=15183771
			// https://oldschool.runescape.wiki/w/Herbalist's_notes?oldid=15192389
			// https://oldschool.runescape.wiki/w/Imbued_barronite?oldid=15192250
			// https://oldschool.runescape.wiki/w/Inert_locator_orb?oldid=15188651
			case 2384:
			case 1583:
			case 11196:
			case 2391:
			case 29427:
			case 25968:
			case 28806:
			case 22079:
			// The Final Dawn, Current Affairs, Tower of Life, Fremennik, and Devious Minds objects.
			// https://oldschool.runescape.wiki/w/Kuhu_essence?oldid=15208315
			// https://oldschool.runescape.wiki/w/Mayoral_fishbowl?oldid=15119517
			// https://oldschool.runescape.wiki/w/Metal_bar?oldid=15185712
			// https://oldschool.runescape.wiki/w/Molten_glass_(i)?oldid=15189545
			// https://oldschool.runescape.wiki/w/Orb_(Devious_Minds)?oldid=15184946
			case 30962:
			case 31330:
			case 10876:
			case 24260:
			case 6821:
			// Additional copies are deleted, or the item only creates a quest-specific result.
			// https://oldschool.runescape.wiki/w/Orbs_of_protection?oldid=15183616
			// https://oldschool.runescape.wiki/w/Phoenix_feather?oldid=15183702
			// https://oldschool.runescape.wiki/w/Raw_stuffed_snake?oldid=15184439
			// https://oldschool.runescape.wiki/w/Red_mahogany_log?oldid=15184568
			case 588:
			case 4621:
			case 7577:
			case 4445:
			// Cabin Fever/Olaf repair parts and re-creatable but quest-only dust/sand.
			// https://oldschool.runescape.wiki/w/Repair_plank?oldid=15184804
			// https://oldschool.runescape.wiki/w/Rotten_barrel?oldid=15185728
			// https://oldschool.runescape.wiki/w/Rune_dust?oldid=15185622
			// https://oldschool.runescape.wiki/w/Sandbag?oldid=15183842
			case 7121:
			case 7148:
			case 11045:
			case 6467:
			case 9943:
			// Final Dawn purse, post-quest-useless keys/leather, and quest-instance supplies.
			// https://oldschool.runescape.wiki/w/Coin_purse?oldid=14963097
			// https://oldschool.runescape.wiki/w/Storeroom_key?oldid=15185703
			// https://oldschool.runescape.wiki/w/Suqah_leather?oldid=15185039
			// https://oldschool.runescape.wiki/w/Swamp_paste_(Dragon_Slayer_II)?oldid=15190523
			case 30943:
			case 3269:
			case 29906:
			case 9080:
			case 9081:
			case 22095:
			// Lore pages, NPC-planted teleorbs, quest fur/crest/vials, and final quest materials.
			// https://oldschool.runescape.wiki/w/Tatty_page?oldid=15191519
			// https://oldschool.runescape.wiki/w/Teleorb?oldid=15192569
			// https://oldschool.runescape.wiki/w/Vial_of_water_(Lunar_Diplomacy)?oldid=15188389
			// https://oldschool.runescape.wiki/w/Whitefish?oldid=15190755
			// https://oldschool.runescape.wiki/w/Wood_carving?oldid=15191135
			case 28394:
			case 28395:
			case 28396:
			case 28397:
			case 28398:
			case 28399:
			case 28400:
			case 29536:
			case 29537:
			case 28982:
			case 28973:
			case 9086:
			case 33774:
			case 26579:
			case 11035:
			case 27525:
				return cleanupQuestItem();

			// Non-bankable, instance-only, or cache/failsafe objects.
			// https://oldschool.runescape.wiki/w/Dream_log?oldid=15185033
			// https://oldschool.runescape.wiki/w/Dusty_lamp?oldid=15191414
			// https://oldschool.runescape.wiki/w/Knight_of_varlamore_(item)?oldid=15192781
			// https://oldschool.runescape.wiki/w/Mudskipper_hide?oldid=15184200
			// https://oldschool.runescape.wiki/w/Raw_moss_lizard?oldid=15192428
			// https://oldschool.runescape.wiki/w/Rope_(animation_item)?oldid=15190426
			case 9067:
			case 28132:
			case 28977:
			case 7532:
			case 29076:
			case 4498:
				return cleanupJunk();

			// Repeatably produced secondary for blamish oil and the reusable oily rod.
			// https://oldschool.runescape.wiki/w/Blamish_snail_slime?oldid=15185853
			case 1581:
				return herbloreSecondary();

			// Drinkable Sailing XP reward and repeatably cookable food.
			// https://oldschool.runescape.wiki/w/Bottle_of_fish_bladder_stout?oldid=15193076
			// https://oldschool.runescape.wiki/w/Steak_sandwich?oldid=15190178
			case 31833:
			case 25631:
				return food();

			// Persistent Sailing task/chart tracker and repeatable sea-charting crowbar.
			// https://oldschool.runescape.wiki/w/Captain's_log?oldid=15239575
			// https://oldschool.runescape.wiki/w/Crowbar?oldid=15193098
			case 31985:
			case 31807:
				return sailingUtility();

			// Retained quest utilities with an explicit ongoing contact, unlock, or access use.
			// https://oldschool.runescape.wiki/w/Commorb?oldid=15184055
			// https://oldschool.runescape.wiki/w/Commorb_v2?oldid=15186980
			// https://oldschool.runescape.wiki/w/Crypt_map?oldid=15191413
			// https://oldschool.runescape.wiki/w/Very_long_rope?oldid=15191543
			// https://oldschool.runescape.wiki/w/Varlamore_envoy?oldid=15188536
			case 6635:
			case 9681:
			case 28133:
			case 28363:
			case 21756:
				return questUtility();

			// Repeatable Crafting materials for additional eagle capes and lyres.
			// https://oldschool.runescape.wiki/w/Eagle_feather?oldid=15185631
			// https://oldschool.runescape.wiki/w/Golden_wool?oldid=15184097
			case 10167:
			case 3694:
				return craftingMaterial();

			// Repeatably gathered Cooking ingredient for nettle tea.
			// https://oldschool.runescape.wiki/w/Nettles?oldid=15184037
			case 4241:
				return cookingMaterial();

			// Post-quest pet/costume pieces with explicit cosmetic storage/use.
			// https://oldschool.runescape.wiki/w/Humphrey_Dumphrey?oldid=15197495
			// https://oldschool.runescape.wiki/w/Emissary_sandals?oldid=15192741
			case 30970:
			case 29874:
				return clueCosmetic();

			// Repeatable Mage Arena II component for later Guthix cape imbues.
			// https://oldschool.runescape.wiki/w/Ent's_roots?oldid=15188752
			case 21798:
				return equipmentUpgrade();

			// Required by both Ernest the Chicken and the Robes of Ruin clue hunt.
			// https://oldschool.runescape.wiki/w/Poisoned_fish_food?oldid=15183936
			case 274:
				return treasureTrail();

			// Remains obtainable and usable after the quest to revisit Lucien's camp.
			// https://oldschool.runescape.wiki/w/Strange_teleorb?oldid=15192565
			case 29535:
				return teleport();

			// Repeatable Digsite and Varrock Museum excavation/cleaning tool.
			// https://oldschool.runescape.wiki/w/Trowel?oldid=15183902
			case 676:
				return skillingUtility();

			// Repeatably fletched component for functional ogre arrows.
			// https://oldschool.runescape.wiki/w/Wolfbone_arrowtips?oldid=15186067
			case 2861:
				return skillingAmmoComponent();

			/*
			 * Phase 2 quest/tools pilot. These exact quest-stage objects have no
			 * normal repeatable tool or utility function after their quest step.
			 */
			// Wiki: https://oldschool.runescape.wiki/w/Lens_mould?oldid=15185521
			case 602:
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_talisman?oldid=15182682
			case 681:
			// Wiki: https://oldschool.runescape.wiki/w/Silver_needle?oldid=15186811
			case 1804:
			case 1805:
			// Wiki: https://oldschool.runescape.wiki/w/Criminal's_thread?oldid=15184054
			case 1808:
			case 1809:
			case 1810:
			// Wiki: https://oldschool.runescape.wiki/w/Golden_tinderbox?oldid=15187397
			case 2946:
			// Wiki: https://oldschool.runescape.wiki/w/Golden_hammer?oldid=15187399
			case 2949:
			// Wiki: https://oldschool.runescape.wiki/w/Golden_needle?oldid=15187400
			case 2951:
			// Wiki: https://oldschool.runescape.wiki/w/Spiked_boots?oldid=15239316
			case 3107:
			// Wiki: https://oldschool.runescape.wiki/w/Hunters'_talisman?oldid=15184168
			// The Draugen objective is one-time; additional talismans have no repeatable function.
			case 3696:
			case 3697:
			// Wiki: https://oldschool.runescape.wiki/w/Blue_thread?oldid=15185670
			case 3719:
			// Wiki: https://oldschool.runescape.wiki/w/Conductor_mould?oldid=15184048
			case 4200:
			// Wiki: https://oldschool.runescape.wiki/w/Blunt_axe?oldid=15183865
			case 4415:
			// Wiki: https://oldschool.runescape.wiki/w/Sharpened_axe?oldid=15185589
			case 4444:
			// Wiki: https://oldschool.runescape.wiki/w/Metal_spade?oldid=15188927
			case 5586:
			case 5587:
			// Wiki: https://oldschool.runescape.wiki/w/Chisel_(Recruitment_Drive)?oldid=15188928
			case 5601:
			// Wiki: https://oldschool.runescape.wiki/w/Knife_(Recruitment_Drive)?oldid=15188930
			case 5605:
			// Wiki: https://oldschool.runescape.wiki/w/Bucket_of_water_(Rum_Deal)?oldid=15184577
			case 6712:
			// Wiki: https://oldschool.runescape.wiki/w/Demonic_sigil_mould?oldid=15185192
			case 6747:
			// Wiki: https://oldschool.runescape.wiki/w/Camel_mould_(p)?oldid=15184452
			case 7001:
			// Wiki: https://oldschool.runescape.wiki/w/Queen's_secateurs?oldid=15184661
			case 7410:
			case 9020:
			// Wiki: https://oldschool.runescape.wiki/w/Anger_battleaxe?oldid=15182770
			case 7807:
			// Wiki: https://oldschool.runescape.wiki/w/A_special_tiara?oldid=15185032
			case 9103:
			// Wiki: https://oldschool.runescape.wiki/w/Scarab_mould?oldid=15190914
			case 26952:
			// Wiki: https://oldschool.runescape.wiki/w/Knife_(Desert_Treasure_II)?oldid=15191510
			case 28413:
			// Wiki: https://oldschool.runescape.wiki/w/Chisel_(Desert_Treasure_II)?oldid=15191509
			case 28414:
			// Wiki: https://oldschool.runescape.wiki/w/Lockpick_(Desert_Treasure_II)?oldid=15191508
			case 28415:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_water_talisman?oldid=15232314
			case 28964:
			// Wiki: https://oldschool.runescape.wiki/w/Enchanted_earth_talisman?oldid=15232315
			case 28965:
			// Wiki: https://oldschool.runescape.wiki/w/Infused_water_talisman?oldid=15192401
			case 28966:
			// Wiki: https://oldschool.runescape.wiki/w/Infused_earth_talisman?oldid=15192400
			case 28967:
			// Wiki: https://oldschool.runescape.wiki/w/Arrav's_axe?oldid=14798554
			case 30320:
			// Wiki: https://oldschool.runescape.wiki/w/Acatzin's_axe?oldid=15131634
			case 30989:
			case 30990:
				return cleanupQuestItem();

			// Wiki: https://oldschool.runescape.wiki/w/Prayer_potion_(Last_Man_Standing)?oldid=15209257
			case 20393:
			case 20394:
			case 20395:
			case 20396: // Prayer potion (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Super_energy_(Last_Man_Standing)?oldid=15239453
			case 20548:
			case 20549:
			case 20550:
			case 20551: // Super energy (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Super_combat_potion_(Last_Man_Standing)?oldid=15239452
			case 23543:
			case 23545:
			case 23547:
			case 23549: // Super combat potion (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Ranging_potion_(Last_Man_Standing)?oldid=15214947
			case 23551:
			case 23553:
			case 23555:
			case 23557: // Ranging potion (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Sanfew_serum_(Last_Man_Standing)?oldid=15214946
			case 23559:
			case 23561:
			case 23563:
			case 23565: // Sanfew serum (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Super_restore_(Last_Man_Standing)?oldid=15239451
			case 23567:
			case 23569:
			case 23571:
			case 23573: // Super restore (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Saradomin_brew_(Last_Man_Standing)?oldid=15214944
			case 23575:
			case 23577:
			case 23579:
			case 23581: // Saradomin brew (LMS)
			// Wiki: https://oldschool.runescape.wiki/w/Stamina_potion_(Last_Man_Standing)?oldid=15239456
			case 23583:
			case 23585:
			case 23587:
			case 23589: // Stamina potion (LMS)
				return cleanupJunk();

			// Wiki: https://oldschool.runescape.wiki/w/Robe_of_elidinis_(top)?oldid=15184480
			case 6786:
			// Wiki: https://oldschool.runescape.wiki/w/Robe_of_elidinis_(bottom)?oldid=15184479
			case 6787:
			// Wiki: https://oldschool.runescape.wiki/w/Ram_skull_helm?oldid=15183173
			case 7917:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(1)?oldid=15189530
			case 24207:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(10)?oldid=15189531
			case 24209:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(50)?oldid=15189532
			case 24211:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(100)?oldid=15189533
			case 24213:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(500)?oldid=15189534
			case 24215:
			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(1000)?oldid=15189665
			case 24520:
				return clueCosmetic();

			// Wiki: https://oldschool.runescape.wiki/w/Explosive_potion?oldid=15187163
			case 4045: // Castle Wars-only gameplay item
			// Wiki: https://oldschool.runescape.wiki/w/Deadman_teleport_tablet?oldid=15188858
			case 13666: // Deadman Mode-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Shark_(Last_Man_Standing)?oldid=15208878
			case 20390: // Last Man Standing-only food
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_magicks_tablet?oldid=15211531
			case 20430: // Last Man Standing-only spellbook tablet
			// Wiki: https://oldschool.runescape.wiki/w/Survival_token?oldid=15187813
			case 20527: // Last Man Standing-only currency
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_glory_(Last_Man_Standing)?oldid=15208961
			case 20586: // Last Man Standing-only jewellery
			// Wiki: https://oldschool.runescape.wiki/w/Cooked_karambwan_(Last_Man_Standing)?oldid=15208877
			case 23533: // Last Man Standing-only food
			// Wiki: https://oldschool.runescape.wiki/w/Ghrazi_rapier_(Last_Man_Standing)?oldid=15208833
			case 23628: // Last Man Standing-only weapon
			// Wiki: https://oldschool.runescape.wiki/w/Rune_pouch_(Last_Man_Standing)?oldid=15208869
			case 23650: // Last Man Standing-only rune pouch
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_spike?oldid=15189350
			case 23831: // Corrupted Gauntlet-only weapon component
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_teleport_crystal?oldid=15189491
			case 23858: // Corrupted Gauntlet-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Mithril_seeds_(Last_Man_Standing)?oldid=15208871
			case 24534: // Last Man Standing-only seed copy
			// Wiki: https://oldschool.runescape.wiki/w/Trailblazer_teleport_scroll?oldid=15239427
			case 25087: // Trailblazer League-only unlock
			// Wiki: https://oldschool.runescape.wiki/w/Fairy_mushroom?oldid=15205871
			case 25102: // Trailblazer League-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_of_echoes?oldid=15192898
			case 25104: // Trailblazer League-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Shattered_teleport_scroll?oldid=15239819
			case 26500: // Shattered Relics League-only unlock
			// Wiki: https://oldschool.runescape.wiki/w/Portable_waystone?oldid=15190745
			case 26549: // Shattered Relics League-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Spiked_manacles_(Last_Man_Standing)?oldid=15208846
			case 27178: // Last Man Standing-only equipment
			// Wiki: https://oldschool.runescape.wiki/w/Trailblazer_reloaded_home_teleport_scroll?oldid=15191661
			case 28705: // Trailblazer Reloaded League-only unlock
			// Wiki: https://oldschool.runescape.wiki/w/Banker's_briefcase?oldid=15192895
			case 30361: // Raging Echoes League-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Clue_compass_(item)?oldid=15192896
			case 30363: // Raging Echoes League-only teleport
			// Wiki: https://oldschool.runescape.wiki/w/Echo_home_teleport_scroll?oldid=15214892
			case 30453: // Raging Echoes League-only unlock
			// Wiki: https://oldschool.runescape.wiki/w/Raging_echoes_portal_scroll?oldid=15214888
			case 30461: // Raging Echoes League-only unlock
			// Wiki: https://oldschool.runescape.wiki/w/Corrupted_shark?oldid=15237663
			case 31174: // Gridmaster League-only food
			// Wiki: https://oldschool.runescape.wiki/w/Flask_of_fervour_(item)?oldid=15231324
			case 33239:
			case 33241: // Demonic Pacts League-only flask states
				return cleanupJunk();

			// Wiki: https://oldschool.runescape.wiki/w/3rd_age_range_coif_(Last_Man_Standing)?oldid=15208853
			case 27201: // 3rd age range coif [BR_3A_RANGER_COIF]
			// Wiki: https://oldschool.runescape.wiki/w/3rd_age_range_legs_(Last_Man_Standing)?oldid=15208852
			case 27200: // 3rd age range legs [BR_3A_RANGER_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/3rd_age_robe_(Last_Man_Standing)?oldid=15208848
			case 20577: // 3rd age robe [BR_3A_MAGE_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/3rd_age_robe_top_(Last_Man_Standing)?oldid=15208847
			case 20576: // 3rd age robe top [BR_3A_MAGE_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Abyssal_whip_(Last_Man_Standing)?oldid=15208958
			case 20405: // Abyssal whip [BR_ABYSSAL_WHIP]
			// Wiki: https://oldschool.runescape.wiki/w/Ahrim's_robeskirt_(Last_Man_Standing)?oldid=15208957
			case 20599: // Ahrim's robeskirt [BR_AHRIM_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Ahrim's_robetop_(Last_Man_Standing)?oldid=15208959
			case 20598: // Ahrim's robetop [BR_AHRIM_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Ahrim's_staff_(Last_Man_Standing)?oldid=15208960
			case 23653: // Ahrim's staff [BR_BARROWS_AHRIM_WEAPON]
			// Wiki: https://oldschool.runescape.wiki/w/Adamant_arrow_pack?oldid=15209160
			case 20525: // Adamant arrow pack [BR_ADAMANT_ARROWPACK]
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_fury_(Last_Man_Standing)?oldid=15208927
			case 23640: // Amulet of fury [BR_ENCHANTED_ONYX_AMULET]
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_torture_(Last_Man_Standing)?oldid=15208928
			case 27173: // Amulet of torture [BR_TORTURE_AMULET]
			// Wiki: https://oldschool.runescape.wiki/w/Ancestral_robe_bottom_(Last_Man_Standing)?oldid=15262630
			case 27194: // Ancestral robe bottom [BR_ANCESTRAL_BOTTOM]
			// Wiki: https://oldschool.runescape.wiki/w/Ancestral_robe_top_(Last_Man_Standing)?oldid=15208861
			case 27193: // Ancestral robe top [BR_ANCESTRAL_TOP]
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_godsword_(Last_Man_Standing)?oldid=15208940
			case 27184: // Ancient godsword [BR_ANCIENT_GODSWORD]
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_staff_(Last_Man_Standing)?oldid=15208963
			case 20431: // Ancient staff [BR_ANCIENT_STAFF]
			// Wiki: https://oldschool.runescape.wiki/w/Aranea_boots_(Last_Man_Standing)?oldid=15208879
			case 33202: // Aranea boots [BR_ARANEA_BOOTS]
			// Wiki: https://oldschool.runescape.wiki/w/Armadyl_crossbow_(Last_Man_Standing)?oldid=15208914
			case 23611: // Armadyl crossbow [BR_ACB]
			// Wiki: https://oldschool.runescape.wiki/w/Armadyl_godsword_(Last_Man_Standing)?oldid=15208941
			case 20593: // Armadyl godsword [BR_AGS]
			// Wiki: https://oldschool.runescape.wiki/w/Atlatl_dart_(Last_Man_Standing)?oldid=15239574
			case 29852: // Atlatl dart [BR_ATLATL_DART]
			// Wiki: https://oldschool.runescape.wiki/w/Bandos_tassets_(Last_Man_Standing)?oldid=15208837
			case 23646: // Bandos tassets [BR_BANDOS_SKIRT]
			// Wiki: https://oldschool.runescape.wiki/w/Barrows_gloves_(Last_Man_Standing)?oldid=15208965
			case 23593: // Barrows gloves [BR_HUNDRED_GAUNTLETS_LEVEL_10]
			// Wiki: https://oldschool.runescape.wiki/w/Berserker_helm_(Last_Man_Standing)?oldid=15208966
			case 27169: // Berserker helm [BR_BERSERKER_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Berserker_ring_(i)_(Last_Man_Standing)?oldid=15237292
			case 23595: // Berserker ring (i) [BR_BERZERKER_RING]
			// Wiki: https://oldschool.runescape.wiki/w/Black_d'hide_body_(Last_Man_Standing)?oldid=15208968
			case 20423: // Black d'hide body [BR_BLACKDHIDE_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Black_d'hide_chaps_(Last_Man_Standing)?oldid=15208969
			case 20424: // Black d'hide chaps [BR_BLACKDHIDE_CHAPS]
			// Wiki: https://oldschool.runescape.wiki/w/Blessed_spirit_shield_(Last_Man_Standing)?oldid=15208929
			case 23642: // Blessed spirit shield [BR_BLESSED_SPIRIT_SHIELD]
			// Wiki: https://oldschool.runescape.wiki/w/Blood_moon_chestplate_(Last_Man_Standing)?oldid=15208930
			case 29846: // Blood moon chestplate [BR_BLOOD_MOON_CHESTPLATE]
			// Wiki: https://oldschool.runescape.wiki/w/Blood_moon_helm_(Last_Man_Standing)?oldid=15208932
			case 29848: // Blood moon helm [BR_BLOOD_MOON_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Blood_moon_tassets_(Last_Man_Standing)?oldid=15208933
			case 29847: // Blood moon tassets [BR_BLOOD_MOON_TASSETS]
			// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_chestplate_(Last_Man_Standing)?oldid=15208880
			case 29843: // Blue moon chestplate [BR_FROST_MOON_CHESTPLATE]
			// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_helm_(Last_Man_Standing)?oldid=15208881
			case 29845: // Blue moon helm [BR_FROST_MOON_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_spear_(Last_Man_Standing)?oldid=15208893
			case 29849: // Blue moon spear [BR_FROSTMOON_SPEAR]
			// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_tassets_(Last_Man_Standing)?oldid=15208882
			case 29844: // Blue moon tassets [BR_FROST_MOON_TASSETS]
			// Wiki: https://oldschool.runescape.wiki/w/Bow_of_faerdhinen_(c)_(Last_Man_Standing)?oldid=15208916
			case 27187: // Bow of faerdhinen (c) [BR_BOW_OF_FAERDHINEN]
			// Wiki: https://oldschool.runescape.wiki/w/Climbing_boots_(Last_Man_Standing)?oldid=15208979
			case 20578: // Climbing boots [BR_CLIMBING_BOOTS]
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_body_(Last_Man_Standing)?oldid=15208899
			case 33166: // Crystal body [BR_CRYSTAL_CHESTPLATE]
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_helm_(Last_Man_Standing)?oldid=15208901
			case 33170: // Crystal helm [BR_CRYSTAL_HELMET]
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_legs_(Last_Man_Standing)?oldid=15208900
			case 33168: // Crystal legs [BR_CRYSTAL_PLATELEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Dark_bow_(Last_Man_Standing)?oldid=15208917
			case 20408: // Dark bow [BR_DARKBOW]
			// Wiki: https://oldschool.runescape.wiki/w/Dharok's_greataxe_(Last_Man_Standing)?oldid=15208944
			case 25516: // Dharok's greataxe [BR_BARROWS_DHAROK_WEAPON]
			// Wiki: https://oldschool.runescape.wiki/w/Dharok's_helm_(Last_Man_Standing)?oldid=15208934
			case 23639: // Dharok's helm [BR_BARROWS_DHAROK_HEAD]
			// Wiki: https://oldschool.runescape.wiki/w/Dharok's_platebody_(Last_Man_Standing)?oldid=15208935
			case 25515: // Dharok's platebody [BR_BARROWS_DHAROK_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Dharok's_platelegs_(Last_Man_Standing)?oldid=15208936
			case 23633: // Dharok's platelegs [BR_BARROWS_DHAROK_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Diamond_bolts_(e)_(Last_Man_Standing)?oldid=15208978
			case 23649: // Diamond bolts (e) [BR_DSTONE_BOLTS_E]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_crossbow_(Last_Man_Standing)?oldid=15239569
			case 33460: // Dragon crossbow [BR_XBOWS_CROSSBOW_DRAGON]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_dagger_(Last_Man_Standing)?oldid=15208977
			case 20407: // Dragon dagger [BR_DRAGON_DAGGER]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_defender_(Last_Man_Standing)?oldid=15208976
			case 23597: // Dragon defender [BR_DRAGON_PARRYINGDAGGER]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_javelin_(Last_Man_Standing)?oldid=15208919
			case 23648: // Dragon javelin [BR_DRAGON_JAVELIN]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_knife_(Last_Man_Standing)?oldid=15208920
			case 27157: // Dragon knife [BR_DRAGON_KNIFE]
			// Wiki: https://oldschool.runescape.wiki/w/Dragon_scimitar_(Last_Man_Standing)?oldid=15208975
			case 20406: // Dragon scimitar [BR_DRAGON_SCIMITAR]
			// Wiki: https://oldschool.runescape.wiki/w/Dragonfire_shield_(Last_Man_Standing)?oldid=15208937
			case 33186: // Dragonfire shield [BR_DRAGONFIRE_SHIELD]
			// Wiki: https://oldschool.runescape.wiki/w/Eclipse_atlatl_(Last_Man_Standing)?oldid=15208921
			case 29851: // Eclipse atlatl [BR_ECLIPSE_ATLATL]
			// Wiki: https://oldschool.runescape.wiki/w/Eclipse_moon_chestplate_(Last_Man_Standing)?oldid=15208902
			case 29840: // Eclipse moon chestplate [BR_ECLIPSE_MOON_CHESTPLATE]
			// Wiki: https://oldschool.runescape.wiki/w/Eclipse_moon_helm_(Last_Man_Standing)?oldid=15208903
			case 29842: // Eclipse moon helm [BR_ECLIPSE_MOON_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Eclipse_moon_tassets_(Last_Man_Standing)?oldid=15208905
			case 29841: // Eclipse moon tassets [BR_ECLIPSE_MOON_TASSETS]
			// Wiki: https://oldschool.runescape.wiki/w/Elder_chaos_hood_(Last_Man_Standing)?oldid=15208883
			case 27176: // Elder chaos hood [BR_ELDERCHAOS_HOOD]
			// Wiki: https://oldschool.runescape.wiki/w/Elder_chaos_robe_(Last_Man_Standing)?oldid=15208884
			case 27175: // Elder chaos robe [BR_ELDERCHAOS_BOTTOM]
			// Wiki: https://oldschool.runescape.wiki/w/Elder_maul_(Last_Man_Standing)?oldid=15208947
			case 21205: // Elder maul [BR_ELDER_MAUL]
			// Wiki: https://oldschool.runescape.wiki/w/Eternal_boots_(Last_Man_Standing)?oldid=15208862
			case 23644: // Eternal boots [BR_ETERNAL_BOOTS]
			// Wiki: https://oldschool.runescape.wiki/w/Ghostly_hood_(Last_Man_Standing)?oldid=15208974
			case 27166: // Ghostly hood [BR_SECRET_GHOST_HAT]
			// Wiki: https://oldschool.runescape.wiki/w/Ghostly_robe_(top)_(Last_Man_Standing)?oldid=15208972
			case 27167: // Ghostly robe [BR_SECRET_GHOST_TOP]
			// Wiki: https://oldschool.runescape.wiki/w/Ghostly_robe_(bottom)_(Last_Man_Standing)?oldid=15208973
			case 27168: // Ghostly robe [BR_SECRET_GHOST_BOTTOM]
			// Wiki: https://oldschool.runescape.wiki/w/Granite_maul_(Last_Man_Standing)?oldid=15208948
			case 20557: // Granite maul [BR_GRANITE_MAUL]
			// Wiki: https://oldschool.runescape.wiki/w/Guthan's_helm_(Last_Man_Standing)?oldid=15208839
			case 23638: // Guthan's helm [BR_BARROWS_GUTHAN_HEAD]
			// Wiki: https://oldschool.runescape.wiki/w/Guthix_chaps_(Last_Man_Standing)?oldid=15208906
			case 27180: // Guthix chaps [BR_GUTHIX_CHAPS]
			// Wiki: https://oldschool.runescape.wiki/w/Helm_of_neitiznot_(Last_Man_Standing)?oldid=15208971
			case 23591: // Helm of neitiznot [BR_FRIS_KINGLY_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Imbued_guthix_cape_(Last_Man_Standing)?oldid=15208970
			case 23603: // Imbued guthix cape [BR_MA2_GUTHIX_CAPE]
			// Wiki: https://oldschool.runescape.wiki/w/Imbued_saradomin_cape_(Last_Man_Standing)?oldid=15190461
			case 23607: // Imbued saradomin cape [BR_MA2_SARADOMIN_CAPE]
			// Wiki: https://oldschool.runescape.wiki/w/Imbued_zamorak_cape_(Last_Man_Standing)?oldid=15190462
			case 23605: // Imbued zamorak cape [BR_MA2_ZAMORAK_CAPE]
			// Wiki: https://oldschool.runescape.wiki/w/Infernal_cape_(Last_Man_Standing)?oldid=15208938
			case 23622: // Infernal cape [BR_INFERNAL_CAPE]
			// Wiki: https://oldschool.runescape.wiki/w/Infinity_boots_(Last_Man_Standing)?oldid=15208863
			case 27170: // Infinity boots [BR_INFINITY_BOOTS]
			// Wiki: https://oldschool.runescape.wiki/w/Inquisitor's_great_helm_(Last_Man_Standing)?oldid=15208842
			case 27195: // Inquisitor's great helm [BR_INQUISITORS_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Inquisitor's_hauberk_(Last_Man_Standing)?oldid=15208843
			case 27196: // Inquisitor's hauberk [BR_INQUISITORS_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Inquisitor's_mace_(Last_Man_Standing)?oldid=15208834
			case 27198: // Inquisitor's mace [BR_INQUISITORS_MACE]
			// Wiki: https://oldschool.runescape.wiki/w/Inquisitor's_plateskirt_(Last_Man_Standing)?oldid=15208844
			case 27197: // Inquisitor's plateskirt [BR_INQUISITORS_SKIRT]
			// Wiki: https://oldschool.runescape.wiki/w/Karil's_leathertop_(Last_Man_Standing)?oldid=15208980
			case 23632: // Karil's leathertop [BR_BARROWS_KARIL_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Kodai_wand_(Last_Man_Standing)?oldid=15208894
			case 23626: // Kodai wand [BR_KODAI_WAND]
			// Wiki: https://oldschool.runescape.wiki/w/Lightbearer_(Last_Man_Standing)?oldid=15208870
			case 27870: // Lightbearer [BR_LIGHTBEARER]
			// Wiki: https://oldschool.runescape.wiki/w/Magus_ring_(Last_Man_Standing)?oldid=15208888
			case 33180: // Magus ring [BR_MAGUS_RING]
			// Wiki: https://oldschool.runescape.wiki/w/Masori_body_(f)_(Last_Man_Standing)?oldid=15208907
			case 33190: // Masori body (f) [BR_MASORI_BODY_FORTIFIED]
			// Wiki: https://oldschool.runescape.wiki/w/Masori_chaps_(f)_(Last_Man_Standing)?oldid=15208908
			case 33192: // Masori chaps (f) [BR_MASORI_CHAPS_FORTIFIED]
			// Wiki: https://oldschool.runescape.wiki/w/Mithril_gloves_(Last_Man_Standing)?oldid=15208981
			case 20581: // Mithril gloves [BR_MITHRIL_GLOVES]
			// Wiki: https://oldschool.runescape.wiki/w/Morrigan's_javelin_(Last_Man_Standing)?oldid=15208924
			case 23619: // Morrigan's javelin [BR_MORRIGANS_JAVELIN]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_bottom_(Last_Man_Standing)?oldid=15208983
			case 20426: // Mystic robe bottom [BR_MYSTIC_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_bottom_(dark)_(Last_Man_Standing)?oldid=15239544
			case 27159: // Mystic robe bottom (dark) [BR_MYSTIC_LEGS_DARK]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_bottom_(light)_(Last_Man_Standing)?oldid=15239543
			case 27161: // Mystic robe bottom (light) [BR_MYSTIC_LEGS_LIGHT]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_top_(Last_Man_Standing)?oldid=15208986
			case 20425: // Mystic robe top [BR_MYSTIC_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_top_(dark)_(Last_Man_Standing)?oldid=15239546
			case 27158: // Mystic robe top (dark) [BR_MYSTIC_BODY_DARK]
			// Wiki: https://oldschool.runescape.wiki/w/Mystic_robe_top_(light)_(Last_Man_Standing)?oldid=15239545
			case 27160: // Mystic robe top (light) [BR_MYSTIC_BODY_LIGHT]
			// Wiki: https://oldschool.runescape.wiki/w/Necklace_of_anguish_(Last_Man_Standing)?oldid=15208909
			case 27172: // Necklace of anguish [BR_ANGUISH_NECKLACE]
			// Wiki: https://oldschool.runescape.wiki/w/Noxious_halberd_(Last_Man_Standing)?oldid=15218677
			case 33178: // Noxious halberd [BR_NOXIOUS_HALBERD]
			// Wiki: https://oldschool.runescape.wiki/w/Oathplate_helm_(Last_Man_Standing)?oldid=15239571
			case 33462: // Oathplate helm [BR_OATHPLATE_HELM]
			// Wiki: https://oldschool.runescape.wiki/w/Occult_necklace_(Last_Man_Standing)?oldid=15208889
			case 23654: // Occult necklace [BR_OCCULT_NECKLACE]
			// Wiki: https://oldschool.runescape.wiki/w/Opal_dragon_bolts_(e)_(Last_Man_Standing)?oldid=15208925
			case 27192: // Opal dragon bolts (e) [BR_DRAGON_BOLTS_ENCHANTED_OPAL]
			// Wiki: https://oldschool.runescape.wiki/w/Purging_staff_(Last_Man_Standing)?oldid=15208895
			case 33184: // Purging staff [BR_PURGING_STAFF]
			// Wiki: https://oldschool.runescape.wiki/w/Rune_crossbow_(Last_Man_Standing)?oldid=15208989
			case 23601: // Rune crossbow [BR_XBOWS_CROSSBOW_RUNITE]
			// Wiki: https://oldschool.runescape.wiki/w/Rune_defender_(Last_Man_Standing)?oldid=15208990
			case 27185: // Rune defender [BR_RUNE_PARRYINGDAGGER]
			// Wiki: https://oldschool.runescape.wiki/w/Rune_arrow_pack?oldid=15209260
			case 20607: // Rune arrow pack [BR_RUNE_ARROWPACK]
			// Wiki: https://oldschool.runescape.wiki/w/Rune_platelegs_(Last_Man_Standing)?oldid=15208991
			case 20422: // Rune platelegs [BR_RUNE_PLATELEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Saradomin_chaps_(Last_Man_Standing)?oldid=15208910
			case 27182: // Saradomin chaps [BR_SARADOMIN_CHAPS]
			// Wiki: https://oldschool.runescape.wiki/w/Seers_ring_(i)_(Last_Man_Standing)?oldid=15208864
			case 23624: // Seers ring (i) [BR_NZONE_SEER_RING]
			// Wiki: https://oldschool.runescape.wiki/w/Spear_(Last_Man_Standing)?oldid=15187811
			case 20397: // Spear [BR_HALBERD]
			// Wiki: https://oldschool.runescape.wiki/w/Spirit_shield_(Last_Man_Standing)?oldid=15239457
			case 23599: // Spirit shield [BR_SPIRIT_SHIELD]
			// Wiki: https://oldschool.runescape.wiki/w/Staff_of_the_dead_(Last_Man_Standing)?oldid=15237641
			case 23613: // Staff of the dead [BR_SOTD]
			// Wiki: https://oldschool.runescape.wiki/w/Statius's_warhammer_(Last_Man_Standing)?oldid=15208951
			case 23620: // Statius's warhammer [BR_STATIUS_WARHAMMER]
			// Wiki: https://oldschool.runescape.wiki/w/Torag's_helm_(Last_Man_Standing)?oldid=15239442
			case 23637: // Torag's helm [BR_BARROWS_TORAG_HEAD]
			// Wiki: https://oldschool.runescape.wiki/w/Torag's_platelegs_(Last_Man_Standing)?oldid=15208994
			case 23634: // Torag's platelegs [BR_BARROWS_TORAG_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Tormented_bracelet_(Last_Man_Standing)?oldid=15208890
			case 27171: // Tormented bracelet [BR_TORMENTED_BRACELET]
			// Wiki: https://oldschool.runescape.wiki/w/Torva_platelegs_(Last_Man_Standing)?oldid=15208939
			case 33194: // Torva platelegs [BR_TORVA_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Ultor_ring_(Last_Man_Standing)?oldid=15208952
			case 33182: // Ultor ring [BR_ULTOR_RING]
			// Wiki: https://oldschool.runescape.wiki/w/Verac's_brassard_(Last_Man_Standing)?oldid=15208857
			case 27190: // Verac's brassard [BR_BARROWS_VERAC_BODY]
			// Wiki: https://oldschool.runescape.wiki/w/Verac's_flail_(Last_Man_Standing)?oldid=15208836
			case 27189: // Verac's flail [BR_BARROWS_VERAC_WEAPON]
			// Wiki: https://oldschool.runescape.wiki/w/Verac's_helm_(Last_Man_Standing)?oldid=15239369
			case 23636: // Verac's helm [BR_BARROWS_VERAC_HEAD]
			// Wiki: https://oldschool.runescape.wiki/w/Verac's_plateskirt_(Last_Man_Standing)?oldid=15208855
			case 23635: // Verac's plateskirt [BR_BARROWS_VERAC_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Vesta's_longsword_(Last_Man_Standing)?oldid=15208953
			case 23615: // Vesta's longsword [BR_VESTAS_LONGSWORD]
			// Wiki: https://oldschool.runescape.wiki/w/Virtus_robe_bottom_(Last_Man_Standing)?oldid=15208891
			case 33198: // Virtus robe bottom [BR_VIRTUS_LEGS]
			// Wiki: https://oldschool.runescape.wiki/w/Virtus_robe_top_(Last_Man_Standing)?oldid=15208892
			case 33196: // Virtus robe top [BR_VIRTUS_TOP]
			// Wiki: https://oldschool.runescape.wiki/w/Volatile_nightmare_staff_(Last_Man_Standing)?oldid=15208897
			case 25517: // Volatile nightmare staff [BR_NIGHTMARE_STAFF_VOLATILE]
			// Wiki: https://oldschool.runescape.wiki/w/Wizard_boots_(Last_Man_Standing)?oldid=15208865
			case 27162: // Wizard boots [BR_WIZARD_BOOTS]
			// Wiki: https://oldschool.runescape.wiki/w/Zamorak_chaps_(Last_Man_Standing)?oldid=15208913
			case 27181: // Zamorak chaps [BR_ZAMORAK_CHAPS]
			// Wiki: https://oldschool.runescape.wiki/w/Zaryte_crossbow_(Last_Man_Standing)?oldid=15208926
			case 27186: // Zaryte crossbow [BR_ZARYTE_XBOW]
			// Wiki: https://oldschool.runescape.wiki/w/Zuriel's_staff_(Last_Man_Standing)?oldid=15234702
			case 23617: // Zuriel's staff [BR_ZURIELS_STAFF]
				return cleanupJunk();

			// Wiki: https://oldschool.runescape.wiki/w/Eclipse_moon_tassets?oldid=15211425
			case 29052: // Eclipse moon tassets (broken): repairable combat equipment
			// Wiki: https://oldschool.runescape.wiki/w/Blue_moon_tassets?oldid=15211422
			case 29061: // Blue moon tassets (broken): repairable combat equipment
			// Wiki: https://oldschool.runescape.wiki/w/Blood_moon_tassets?oldid=15211419
			case 29070: // Blood moon tassets (broken): repairable combat equipment
				return legs();

			/*
			 * Phase 2, batch 13 (Combat Gear quest-items, part 1).
			 * The cited revision pages and linked creation/use chains were read
			 * in full. Quest origin alone never decides the route.
			 */
			// Permanent, normally banked Dwarf multicannon components.
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_base?oldid=15184076
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_stand?oldid=15184078
			case 6:
			case 8:
			// The furnace was missing from this list, so three quarters of the
			// cannon sat in combat gear and the fourth part in storage cleanup.
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_furnace?oldid=15184080
			case 12:
				return cannonPart();

			/*
			 * Every Void body piece is filed UNKNOWN in the registry while the
			 * helms, robes, gloves and mace are GEAR, so the tops alone failed
			 * closed into storage cleanup. That split the set across two tabs
			 * and made the remaining pieces unassemblable as a Void column.
			 * Wiki: https://oldschool.runescape.wiki/w/Void_knight_top?oldid=15190001
			 * Wiki: https://oldschool.runescape.wiki/w/Elite_void_top?oldid=15190004
			 */
			case 8839: // Void knight top
			case 13072: // Elite void top
			case 15737: // Elite void top, duplicate cache record
			case 20465: // Void knight top (broken)
			case 20467: // Elite void top (broken)
			case 20468: // Elite void top (broken), duplicate cache record
			case 24177: // Void knight top (l)
			case 24178: // Elite void top (l)
			case 26463: // Void knight top (or)
			case 26469: // Elite void top (or)
			case 27000: // Void knight top (l)(or)
			case 27003: // Elite void top (l)(or)
			case 33476: // Void knight top (l) (broken)
			case 33478: // Void knight top (l) (mangled)
			case 33480: // Elite void top (l) (broken)
			case 33481: // Elite void top (l) (broken), duplicate cache record
			case 33482: // Elite void top (l) (mangled)
			case 33483: // Elite void top (l) (mangled), duplicate cache record
				return body();

			// Repeatable post-quest instructions and holy-water production tools.
			// Wiki: https://oldschool.runescape.wiki/w/Battered_book?oldid=15261995
			// Wiki: https://oldschool.runescape.wiki/w/Slashed_book?oldid=15261994
			case 2886:
			case 9715:
			// Wiki: https://oldschool.runescape.wiki/w/Gold_bowl?oldid=15184879
			// Wiki: https://oldschool.runescape.wiki/w/Blessed_gold_bowl?oldid=15184880
			// Wiki: https://oldschool.runescape.wiki/w/Golden_bowl?oldid=15184878
			case 721:
			case 722:
			case 723:
			case 724:
			case 725:
			case 726:
			// The sketch remains useful after the quest because omitting it adds a failure chance.
			// Wiki: https://oldschool.runescape.wiki/w/Sketch?oldid=15186564
			case 720:
				return questUtility();

			// Repeatable utility, food, and skilling workflows that win over quest origin.
			// Wiki: https://oldschool.runescape.wiki/w/Damaged_dagger?oldid=15184405
			case 2387: // Can still slash webs after Watchtower
			// Wiki: https://oldschool.runescape.wiki/w/Cowbell_amulet?oldid=15242265
			case 33103:
			case 33104: // Repeatable faster milking, Brutus respawn, and charged teleport utility
				return skillingUtility();

			// Wiki: https://oldschool.runescape.wiki/w/Damiana_tea?oldid=15218655
			// Wiki: https://oldschool.runescape.wiki/w/Nettle_tea?oldid=15218650
			case 30981:
			case 4239:
				return food();

			// Wiki: https://oldschool.runescape.wiki/w/Damiana_water?oldid=15194001
			// Wiki: https://oldschool.runescape.wiki/w/Nettle-water?oldid=15184386
			case 30979:
			case 4237:
				return cookingMaterial();

			// Wiki: https://oldschool.runescape.wiki/w/Flighted_ogre_arrow?oldid=15185590
			case 2865:
				return skillingAmmoComponent();

			// Discontinued state still has an explicit Activate conversion into the current ring.
			// Wiki: https://oldschool.runescape.wiki/w/Ring_of_endurance_(discontinued)?oldid=15190517
			case 24735:
				return equipmentUpgrade();

			// Wearable/re-obtainable costumes with no combat role.
			// Wiki: https://oldschool.runescape.wiki/w/Black_desert_robe?oldid=15185624
			case 6752:
			// Wiki: https://oldschool.runescape.wiki/w/Builder's_boots?oldid=15226469
			case 10865:
			// Wiki: https://oldschool.runescape.wiki/w/Eagle_cape?oldid=15185043
			case 10171:
			// Wiki: https://oldschool.runescape.wiki/w/Emissary_hood?oldid=15192738
			// Wiki: https://oldschool.runescape.wiki/w/Emissary_robe_top?oldid=15192740
			// Wiki: https://oldschool.runescape.wiki/w/Emissary_robe_bottom?oldid=15192739
			case 29868:
			case 29870:
			case 29872:
			// Craftable and wearable, but explicitly has no use or stat bonuses.
			// Wiki: https://oldschool.runescape.wiki/w/'perfect'_ring?oldid=15186796
			case 773:
				return clueCosmetic();

			// Exact quest-stage objects whose full pages record no normal repeatable function.
			// Wiki: https://oldschool.runescape.wiki/w/A_stone_bowl?oldid=15182661
			case 2888:
			case 2889:
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_doomion?oldid=15182758
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_holthion?oldid=15182762
			// Wiki: https://oldschool.runescape.wiki/w/Amulet_of_othanian?oldid=15182766
			case 1498:
			case 1499:
			case 1497:
			// Wiki: https://oldschool.runescape.wiki/w/Ancestral_dagger?oldid=15252880
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_armour_(item)?oldid=15189739
			// Wiki: https://oldschool.runescape.wiki/w/Ancient_shield_(The_Blood_Moon_Rises)?oldid=15252942
			case 33762:
			case 24688:
			case 33738:
			// Wiki: https://oldschool.runescape.wiki/w/Armour_shard?oldid=15182694
			// Wiki: https://oldschool.runescape.wiki/w/Beaten_book?oldid=15261975
			// Wiki: https://oldschool.runescape.wiki/w/Black_knight_helm?oldid=15187005
			case 11048:
			case 9717:
			case 11678:
			// Wiki: https://oldschool.runescape.wiki/w/Bloody_knife?oldid=15190246
			// Wiki: https://oldschool.runescape.wiki/w/Bolt_cutters?oldid=15254416
			// Wiki: https://oldschool.runescape.wiki/w/Book_(Shield_of_Arrav)?oldid=15186895
			case 25799:
			case 33787:
			case 757:
			// Wiki: https://oldschool.runescape.wiki/w/Bow-sword?oldid=15184944
			// Wiki: https://oldschool.runescape.wiki/w/Broken_shield?oldid=15221167
			// Wiki: https://oldschool.runescape.wiki/w/Cannon_ball_(Between_a_Rock...)?oldid=15184937
			case 6818:
			case 763:
			case 765:
			case 4579:
			// Wiki: https://oldschool.runescape.wiki/w/Criminal's_dagger?oldid=15186576
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_(Watchtower)?oldid=15184426
			case 1813:
			case 1814:
			case 2380:
			case 2381:
			case 2382:
			case 2383:
			// Wiki: https://oldschool.runescape.wiki/w/Cultist_robe?oldid=15190245
			// Wiki: https://oldschool.runescape.wiki/w/Dirty_robe?oldid=15184216
			case 25800:
			case 3267:
			// Wiki: https://oldschool.runescape.wiki/w/Empty_syringe?oldid=15254396
			// Wiki: https://oldschool.runescape.wiki/w/Full_syringe?oldid=15245958
			case 33754:
			case 33755:
			// Wiki: https://oldschool.runescape.wiki/w/Exquisite_boots?oldid=15185571
			// Wiki: https://oldschool.runescape.wiki/w/Eye_amulet?oldid=15190854
			// Wiki: https://oldschool.runescape.wiki/w/Fever_spider_body?oldid=15183893
			case 5064:
			case 26903:
			case 6718:
			// Wiki: https://oldschool.runescape.wiki/w/Gilded_cross?oldid=15184410
			// Wiki: https://oldschool.runescape.wiki/w/Helmet_fragment?oldid=15184981
			// Wiki: https://oldschool.runescape.wiki/w/Infused_wand?oldid=15184925
			case 4674:
			case 11052:
			case 11013:
			// Wiki: https://oldschool.runescape.wiki/w/Knife_blade?oldid=14960790
			// Wiki: https://oldschool.runescape.wiki/w/Legs?oldid=15183874
			case 30965:
			case 4196:
			// Wiki: https://oldschool.runescape.wiki/w/Mace_(H.A.M.)?oldid=15189007
			// Wiki: https://oldschool.runescape.wiki/w/Mace_(The_Blood_Moon_Rises)?oldid=15255253
			case 11058:
			case 33760:
			// Wiki: https://oldschool.runescape.wiki/w/Mystical_robes?oldid=15185505
			case 4247:
			// Wiki: https://oldschool.runescape.wiki/w/Old_robe?oldid=15184414
			// Wiki: https://oldschool.runescape.wiki/w/Ornate_knife?oldid=15247354
			// Wiki: https://oldschool.runescape.wiki/w/Pipe_ring?oldid=15185710
			case 2385:
			case 33740:
			case 10872:
			// Wiki: https://oldschool.runescape.wiki/w/Prototype_dart?oldid=15185563
			// Wiki: https://oldschool.runescape.wiki/w/Robert_bust?oldid=15188656
			// Wiki: https://oldschool.runescape.wiki/w/Rupert's_helmet?oldid=15183196
			case 1849:
			case 22083:
			case 11199:
			// Wiki: https://oldschool.runescape.wiki/w/Sandstone_(20kg)?oldid=15184460
			// Wiki: https://oldschool.runescape.wiki/w/Sandstone_(32kg)?oldid=15184461
			// Wiki: https://oldschool.runescape.wiki/w/Sandstone_body?oldid=15184463
			// Wiki: https://oldschool.runescape.wiki/w/Sandstone_base?oldid=15184462
			case 6985:
			case 6986:
			case 6987:
			case 6988:
			// Wiki: https://oldschool.runescape.wiki/w/Shaman_robe?oldid=15186911
			// Wiki: https://oldschool.runescape.wiki/w/Sharp_knife?oldid=15254393
			case 2397:
			case 33749:
			// Wiki: https://oldschool.runescape.wiki/w/Shield_fragment?oldid=15184980
			// Wiki: https://oldschool.runescape.wiki/w/Shield_of_arrav_(item)?oldid=15192251
			// Wiki: https://oldschool.runescape.wiki/w/Shield_with_symbol?oldid=15252940
			case 11054:
			case 28807:
			case 33739:
			// Wiki: https://oldschool.runescape.wiki/w/Smouldering_pot?oldid=15185822
			// Wiki: https://oldschool.runescape.wiki/w/Spear_(The_Blood_Moon_Rises)?oldid=15255254
			// Wiki: https://oldschool.runescape.wiki/w/Staff_of_armadyl?oldid=15185386
			case 6772:
			case 33758:
			case 84:
			// Wiki: https://oldschool.runescape.wiki/w/Stolen_amulet?oldid=15192438
			// Wiki: https://oldschool.runescape.wiki/w/Stone_ball?oldid=15183566
			case 28976:
			case 3109:
			case 3110:
			case 3111:
			case 3112:
			case 3113:
			// Wiki: https://oldschool.runescape.wiki/w/Sturdy_boots?oldid=15186835
			// Wiki: https://oldschool.runescape.wiki/w/Sword_(The_Blood_Moon_Rises)?oldid=15255252
			// Wiki: https://oldschool.runescape.wiki/w/Sword_fragment?oldid=15184982
			case 3700:
			case 33757:
			case 11056:
			// Wiki: https://oldschool.runescape.wiki/w/Sword_pommel?oldid=15185265
			// Wiki: https://oldschool.runescape.wiki/w/Torn_robe_(top)?oldid=15185490
			// Wiki: https://oldschool.runescape.wiki/w/Torn_robe_(bottom)?oldid=15185491
			case 623:
			case 6788:
			case 6789:
			// Wiki: https://oldschool.runescape.wiki/w/Syringe_needle?oldid=15247407
			// Wiki: https://oldschool.runescape.wiki/w/Syringe_barrel?oldid=15247404
			// Wiki: https://oldschool.runescape.wiki/w/Syringe_plunger?oldid=15247405
			case 33751:
			case 33752:
			case 33753:
			// Wiki: https://oldschool.runescape.wiki/w/Unusual_armour?oldid=15184424
			// Wiki: https://oldschool.runescape.wiki/w/Wand_(What_Lies_Below)?oldid=15184926
			// Wiki: https://oldschool.runescape.wiki/w/Washing_bowl?oldid=15204117
			case 2386:
			case 11012:
			case 2964:
			// Wiki: https://oldschool.runescape.wiki/w/Crone-made_amulet?oldid=15183398
			// Wiki: https://oldschool.runescape.wiki/w/Killer's_knife?oldid=15188034
			// Wiki: https://oldschool.runescape.wiki/w/Pendant_of_lucien?oldid=15183161
			// Wiki: https://oldschool.runescape.wiki/w/Prop_sword_(Death_on_the_Isle)?oldid=15251600
			case 10500:
			case 20781:
			case 21059:
			case 86:
			case 29911:
				return cleanupQuestItem();

			// Non-bankable, scrapped, failsafe, or discontinued quest/cache objects.
			// Wiki: https://oldschool.runescape.wiki/w/Elysian_spirit_shield_(Monkey_Madness_II)?oldid=15187512
			// Wiki: https://oldschool.runescape.wiki/w/Offering_bouquet?oldid=15194077
			// Wiki: https://oldschool.runescape.wiki/w/Silif_(item)?oldid=15192561
			case 19559:
			case 31400:
			case 29572:
			// Wiki: https://oldschool.runescape.wiki/w/Deep_sea_helmet?oldid=15213596
			// Wiki: https://oldschool.runescape.wiki/w/Deep_sea_helmet_(The_Red_Reef)?oldid=15218399
			case 31298:
			case 31401:
			// Given by Juna only inside Tears of Guthix and deleted on leaving the area.
			// Wiki: https://oldschool.runescape.wiki/w/Stone_bowl?oldid=15184533
			case 4704:
				return cleanupJunk();

			/*
			 * Phase 2, batch 14 (Combat Gear quest-items, part 2).
			 * Every cited page and linked function/variant chain was read in
			 * full. These are the non-Gear exceptions in the final 128-ID tail.
			 */
			// The inventory/worn IDs are two representations of the same durable
			// weight-reduction utility; ID 88 was already classified identically.
			// Wiki: https://oldschool.runescape.wiki/w/Boots_of_lightness?oldid=15182914
			case 89:
				return skillingUtility();

			// Persistent communication, access, and travel utilities rather than combat gear.
			// Wiki: https://oldschool.runescape.wiki/w/Catspeak_amulet?oldid=15182936
			case 4677:
			// Wiki: https://oldschool.runescape.wiki/w/Ghostspeak_amulet?oldid=15183035
			case 4250:
			// Wiki: https://oldschool.runescape.wiki/w/Ring_of_charos?oldid=15217181
			case 4202:
				return questUtility();

			// Complete player-facing M'speak family plus the repeatable Lunar access seal.
			// Wiki: https://oldschool.runescape.wiki/w/M'speak_amulet?oldid=15183584
			case 4021:
			case 4022:
			// Wiki: https://oldschool.runescape.wiki/w/Seal_of_passage
			case 9083:
				return questUtility();

			// Ongoing desert-heat protection, but no combat bonuses.
			// Wiki: https://oldschool.runescape.wiki/w/Slave_robe?oldid=15183223
			// Wiki: https://oldschool.runescape.wiki/w/Slave_boots?oldid=15183222
			case 1845:
			case 1846:
				return skillingUtility();

			// Zero-stat costume family; the ripped state is directly repairable
			// into the costume-room-storable repaired trousers.
			// Wiki: https://oldschool.runescape.wiki/w/Ripped_mourner_trousers?oldid=15238152
			// Wiki: https://oldschool.runescape.wiki/w/Mourner_trousers?oldid=15238157
			case 6066:
			case 6067:
				return clueCosmetic();

			// The Star amulet unlocks the cave during the quest. Afterwards the
			// cave is permanently entered by pushing the easternmost memorial;
			// using another Star amulet only makes this chain unobtainable.
			// Wiki: https://oldschool.runescape.wiki/w/Marble_amulet?oldid=15183880
			// "if a star amulet is used ... again ... the marble amulet will no longer be obtainable."
			// Wiki: https://oldschool.runescape.wiki/w/Obsidian_amulet?oldid=15183881
			// "if a star amulet is used ... again ... the obsidian amulet will no longer be obtainable."
			// Wiki: https://oldschool.runescape.wiki/w/Star_amulet?oldid=15183534
			// Wiki: https://oldschool.runescape.wiki/w/Experiment_cave?oldid=15234162
			// "By pushing the easternmost memorial, players may enter the experiment caves below."
			case 4187:
			case 4188:
			case 4183:
				return cleanupQuestItem();

			// Quest-instance weapons that cannot enter a normal bank.
			// Wiki: https://oldschool.runescape.wiki/w/Anger_sword?oldid=15182773
			// Wiki: https://oldschool.runescape.wiki/w/Anger_mace?oldid=15182771
			// Wiki: https://oldschool.runescape.wiki/w/Anger_spear?oldid=15182772
			case 7806:
			case 7808:
			case 7809:
			// Removed from the inventory when its Blood Moon quest segment ends.
			// Wiki: https://oldschool.runescape.wiki/w/Silvthrill_javelin?oldid=15254523
			case 33801:
				return cleanupJunk();

			// Historical gradual-degradation crystal bows. Song of the Elves
			// reverted every existing copy into a weapon seed; only cache records remain.
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_bow_(historical)?oldid=15221194
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
			// The old Nightmare Zone imbued family is likewise unobtainable
			// after the 2019 conversion/removal of crystal imbuing.
			// Wiki: https://oldschool.runescape.wiki/w/Crystal_bow_(i)?oldid=15187529
			case 11748:
			case 11749:
			case 11750:
			case 11751:
			case 11752:
			case 11753:
			case 11754:
			case 11755:
			case 11756:
			case 11757:
			case 11758:
				return cleanupJunk();

			// Focused corrections from the 757-item Ironman blueprint review.
			// Wiki: https://oldschool.runescape.wiki/w/Bone_spear?oldid=14797773
			case 5016: // Equipable melee weapon, not a bone/resource material
				return weapon();

			// Wiki: https://oldschool.runescape.wiki/w/Collection_log?oldid=15161984
			case 22711:
				return collectionTrophy();

			// Wiki: https://oldschool.runescape.wiki/w/Long_kebbit_spike
			case 10107: // Chiselled into long kebbit bolts for Fletching XP
				return skillingAmmoComponent();

			// Wiki: https://oldschool.runescape.wiki/w/Impling
			case 11260: // Required to retain caught implings in Puro-Puro
			// Wiki: https://oldschool.runescape.wiki/w/Small_fur_pouch
			case 29466: // Open small fur pouch remains a reusable Hunter container
				return resourceContainer();

			// Wiki: https://oldschool.runescape.wiki/w/Potions
			case 10109: // Ground into the secondary for Hunter potions
				return herbloreSecondary();

			// Repeatable Farming, Firemaking and cave-light utilities.
			// Wiki: https://oldschool.runescape.wiki/w/Shears
			case 1735:
				return tool();
			// Wiki: https://oldschool.runescape.wiki/w/Bruma_torch
			case 20720:
			// Wiki: https://oldschool.runescape.wiki/w/Bullseye_lantern
			case 4550:
				return lightSource();

			// Wiki: https://oldschool.runescape.wiki/w/Purple_sweets
			case 10476:
				return food();

			// Repeatably grown Farming produce, not Cleanup review items.
			case 1955: // Cooking apple
			case 1982: // Tomato
			case 5986: // Sweetcorn
				return farmingProduce();

			case 5291: // Guam seed belongs to the Herblore recipe workflow
				return herbSeed();

			// Complete Recipe for Disaster spice-dose families: cooking ingredients, not potions.
			case 7480:
			case 7481:
			case 7482:
			case 7483: // Red spice, 4 -> 1
			// Wiki: https://oldschool.runescape.wiki/w/Orange_spice?oldid=15241475
			case 7484:
			case 7485:
			case 7486:
			case 7487: // Orange spice, 4 -> 1
			case 7488:
			case 7489:
			case 7490:
			case 7491: // Brown spice, 4 -> 1
			case 7492:
			case 7493:
			case 7494:
			case 7495: // Yellow spice, 4 -> 1
				return cookingMaterial();

			/*
			 * Simulator-driven cleanup review, 2026-07-18. These are exact
			 * player-facing IDs from the bundled gameval registry. Adjacent CERT
			 * and PLACEHOLDER records intentionally remain fail-closed.
			 */
			// Complete kyatt, larupia, and graahk hunter-fur outfit family.
			case 10035:
			case 10037:
			case 10039:
			case 10041:
			case 10043:
			case 10045:
			case 10047:
			case 10049:
			case 10051:
				return gear();

			case 10595: // Clockwork suit, unwound
			case 10596: // Clockwork suit, wound
				return questUtility();

			case 5350: // Empty plant pot; later plant-pot stages were already FARMING
				return farmingSupply();
			case 6311: // Gout tuber
				return farmingProduce();

			case 11095: // Abyssal bracelet(5)
			case 11097: // Abyssal bracelet(4)
			case 11099: // Abyssal bracelet(3)
			case 11101: // Abyssal bracelet(2)
			case 11103: // Abyssal bracelet(1)
				return runecraftingUtility();

			// Standalone player-facing partyhat cosmetics.
			case 1038:
			case 1040:
			case 1042:
			case 1044:
			case 1046:
			case 1048:
			case 11862: // Black partyhat
			case 11863: // Rainbow partyhat
			case 27828: // Silver partyhat
				return clueCosmetic();

			// Complete player-facing metal/chromatic dragon-mask family.
			case 12363:
			case 12365:
			case 12367:
			case 12369:
			case 12371:
			case 12518:
			case 12520:
			case 12522:
			case 12524:
			case 23270:
			case 23273:
			case 12249: // Imp mask
			case 21211: // 4th birthday hat
			case 27820: // 10th birthday balloons
				return clueCosmetic();

			case 1794: // Bronze wire
				return craftingMaterial();
			case 1933: // Pot of flour
				return cookingMaterial();

			// Complete player-facing bird-house tier run.
			case 21512:
			case 21515:
			case 21518:
			case 21521:
			case 22192:
			case 22195:
			case 22198:
			case 22201:
			case 22204:
				return skillingResource();

			// All current player-facing uncharged trident states.
			case 11908:
			case 22290:
			case 33328:
			case 33434:
				return weapon();

			// Complete page runs for all six god-book families.
			case 3827:
			case 3828:
			case 3829:
			case 3830:
			case 3831:
			case 3832:
			case 3833:
			case 3834:
			case 3835:
			case 3836:
			case 3837:
			case 3838:
			case 12613:
			case 12614:
			case 12615:
			case 12616:
			case 12617:
			case 12618:
			case 12619:
			case 12620:
			case 12621:
			case 12622:
			case 12623:
			case 12624:
				return treasureTrail();

			case 3840: // Holy book
			case 3842: // Unholy book
			case 3844: // Book of balance
			case 12608: // Book of war
			case 12610: // Book of law
			case 12612: // Book of darkness
				return magicOffhand();

			case 12863: // Dwarf cannon set
				return cannonPart();

			// Player-facing ballista weapons and previously unclassified assembly stages.
			case 19478: // Light ballista
			case 19481: // Heavy ballista
			case 26712: // Heavy ballista (or)
				return weapon();
			case 19586: // Light frame
			case 19589: // Heavy frame
			case 19592: // Ballista limbs
			case 19595: // Incomplete light ballista
			case 19598: // Incomplete heavy ballista
				return skillingAmmoComponent();

			// Complete current player-facing prayer-scroll family.
			case 21034: // Dexterous prayer scroll
			case 21047: // Torn prayer scroll
			case 21079: // Arcane prayer scroll
			case 30626: // Deadeye prayer scroll
			case 30627: // Mystic vigour prayer scroll
			case 21804: // Ancient crystal
				return equipmentUpgrade();

			/*
			 * Cleanup benchmark protocol v1, curation round 2. Exact player-facing
			 * IDs only; adjacent CERT and PLACEHOLDER records remain fail-closed.
			 * This adds category/tab routing only, never ordered family metadata.
			 */
			case 33: // Lit candle
			case 36: // Candle
				return lightSource();

			// Complete candle-lantern state family, including both candle colours.
			case 4527:
			case 4529:
			case 4531:
			case 4532:
			case 4534:
				return lightSource();

			case 2025: // Cocktail shaker
				return cookingTool();

			case 2574: // Treasure Trails sextant
				return treasureTrail();

			case 989: // Crystal key
			case 23951: // Enhanced crystal key
				return rewardKey();

			// Complete bronze/steel/black/silver/gold Shade key matrix.
			case 3450:
			case 3451:
			case 3452:
			case 3453:
			case 3454:
			case 3455:
			case 3456:
			case 3457:
			case 3458:
			case 3459:
			case 3460:
			case 3461:
			case 3462:
			case 3463:
			case 3464:
			case 3465:
			case 3466:
			case 3467:
			case 3468:
			case 3469:
			case 25424:
			case 25426:
			case 25428:
			case 25430:
			case 25432:
				return bossAccessKey();

			// Bullseye-lantern components remain resources; usable states are tools.
			case 4542: // Lantern lens
			case 4544: // Bullseye lantern (unf)
				return craftingMaterial();
			case 4546: // Bullseye lantern (empty)
			case 4548: // Bullseye lantern (unlit)
				return lightSource();

			// Complete player-facing elegant clothing family.
			case 10400:
			case 10402:
			case 10404:
			case 10406:
			case 10408:
			case 10410:
			case 10412:
			case 10414:
			case 10416:
			case 10418:
			case 10420:
			case 10422:
			case 10424:
			case 10426:
			case 10428:
			case 10430:
			case 10432:
			case 10434:
			case 10436:
			case 10438:
			case 12315:
			case 12317:
			case 12339:
			case 12341:
			case 12343:
			case 12345:
			case 12347:
			case 12349:
				return clueCosmetic();

			case 9666: // Proselyte harness (male pack)
			case 9668: // Initiate harness (male pack)
			case 9670: // Proselyte harness (female pack)
				return gear();

			case 11891: // Saradomin banner
			case 11892: // Zamorak banner
				return clueCosmetic();

			// Complete six-god book page-set family.
			case 13149:
			case 13151:
			case 13153:
			case 13155:
			case 13157:
			case 13159:
				return treasureTrail();

			// Complete unidentified and identified Fossil Island remains family.
			case 21562:
			case 21564:
			case 21566:
			case 21568:
			case 21570:
			case 21572:
			case 21574:
			case 21576:
			case 21578:
			case 21580:
			case 21582:
			case 21584:
			case 21586:
			case 21588:
			case 21600:
			case 21602:
			case 21604:
			case 21606:
			case 21608:
			case 21610:
			case 21612:
			case 21614:
			case 21616:
			case 21618:
			case 21620:
				return skillingPrayerResource();

			case 21820: // Revenant ether
			// Wiki: https://oldschool.runescape.wiki/w/Soiled_page
			case 30068: // Soiled page, the earth-tome charge page; duplicate 30069 stays fail-closed
				return equipmentCharge();

			case 19939: // Master clue strange device
			case 23183: // Beginner clue strange device
				return treasureTrail();

			case 22494:
			case 22496:
			case 22498:
			case 22500:
			case 22502: // Sinhaza shroud tiers 1-5
				return clueCosmetic();

			// Thammaron's and accursed sceptres: charged/uncharged and upgraded states.
			case 22552:
			case 22555:
			case 27662:
			case 27665:
			case 27676:
			case 27679:
			case 27785:
			case 27788:
				return weapon();

			case 23911:
			case 23913:
			case 23915:
			case 23917:
			case 23919:
			case 23921:
			case 23923:
			case 23925: // Eight crystal crown colours
				return clueCosmetic();

			// Current repairable broken ancient-sceptre and trouver states.
			case 28238:
			case 28240:
			case 28242:
			case 28244:
			case 33504:
			case 33508:
			case 33512:
			case 33516:
			case 33827:
				return weapon();

			// Complete Dizana's quiver lifecycle, including locked and broken states.
			case 28826:
			case 28828:
			case 28947:
			case 28949:
			case 28951:
			case 28953:
			case 28955:
			case 28957:
			case 33524:
			case 33526:
			case 33528:
			case 33530:
				return gear();

			case 29084: // Sulphur blades
				return weapon();

			case 29224: // Blue butterfly wing
			case 29227: // White butterfly wing
			case 29230: // Black butterfly wing
				return skillingResource();

			case 29482: // Brimhaven voucher
				return currency();

			case 20020: // Lesser demon mask
			case 20023: // Greater demon mask
			case 20026: // Black demon mask
				return clueCosmetic();

			case 25712:
			case 25714:
			case 25715:
			case 25716:
			case 25717:
			case 25718:
			case 25719:
			case 25720: // Complete player-facing clan cloak colour family
				return clueCosmetic();

			/*
			 * Cleanup benchmark protocol v1, curation round 3. Exact player-facing
			 * IDs only; adjacent CERT, PLACEHOLDER, cache, and activity records
			 * remain fail-closed. This adds routing only, not family ordering.
			 */
			// Complete six-god Treasure Trails coif family.
			case 10374:
			case 10382:
			case 10390:
			case 12496:
			case 12504:
			case 12512:
				return clueCosmetic();

			// Complete six-god Treasure Trails crozier family.
			case 10440:
			case 10442:
			case 10444:
			case 12199:
			case 12263:
			case 12275:
				return clueCosmetic();

			// Complete six-god Treasure Trails cloak family.
			case 10446:
			case 10448:
			case 10450:
			case 12197:
			case 12261:
			case 12273:
				return clueCosmetic();

			// Complete six-god Treasure Trails mitre family.
			case 10452:
			case 10454:
			case 10456:
			case 12203:
			case 12259:
			case 12271:
				return clueCosmetic();

			// Complete six-god Treasure Trails robe-top family.
			case 10458:
			case 10460:
			case 10462:
			case 12193:
			case 12253:
			case 12265:
				return clueCosmetic();

			// Complete six-god Treasure Trails robe-bottom family.
			case 10464:
			case 10466:
			case 10468:
			case 12195:
			case 12255:
			case 12267:
				return clueCosmetic();

			// Complete six-god Treasure Trails stole family.
			case 10470:
			case 10472:
			case 10474:
			case 12201:
			case 12257:
			case 12269:
				return clueCosmetic();

			// Complete Treasure Trails metal-cane family.
			case 12373:
			case 12375:
			case 12377:
			case 12379:
				return clueCosmetic();

			// Complete Treasure Trails headband colour family.
			case 2645:
			case 2647:
			case 2649:
			case 12299:
			case 12301:
			case 12303:
			case 12305:
			case 12307:
				return clueCosmetic();

			// Complete Treasure Trails boater colour family.
			case 7319:
			case 7321:
			case 7323:
			case 7325:
			case 7327:
			case 12309:
			case 12311:
			case 12313:
				return clueCosmetic();

			// Complete Treasure Trails cavalier colour family.
			case 2639:
			case 2641:
			case 2643:
			case 12321:
			case 12323:
			case 12325:
				return clueCosmetic();

			// Complete Treasure Trails beret colour family.
			case 2633:
			case 2635:
			case 2637:
			case 12247:
				return clueCosmetic();

			// Player-facing standalone Treasure Trails novelty clothing.
			case 10392: // Powdered wig
			case 10394: // Flared trousers
			case 10396: // Pantaloons
			case 10398: // Sleeping cap
			case 12430: // Afro
				return clueCosmetic();

			// Complete Chompy-bird kill-count hat family.
			case 2978:
			case 2979:
			case 2980:
			case 2981:
			case 2982:
			case 2983:
			case 2984:
			case 2985:
			case 2986:
			case 2987:
			case 2988:
			case 2989:
			case 2990:
			case 2991:
			case 2992:
			case 2993:
			case 2994:
			case 2995:
				return collectionTrophy();

			// Complete player-facing Fremennik cloak colour family.
			case 3759:
			case 3761:
			case 3763:
			case 3765:
			case 3777:
			case 3779:
			case 3781:
			case 3783:
			case 3785:
			case 3787:
			case 3789:
				return clueCosmetic();

			// Complete Temple Trekking/Burgh de Rott skill-tome family.
			case 7779:
			case 7780:
			case 7781:
			case 7782:
			case 7783:
			case 7784:
			case 7785:
			case 7786:
			case 7787:
			case 7788:
			case 7789:
			case 7790:
			case 7791:
			case 7792:
			case 7793:
			case 7794:
			case 7795:
			case 7796:
			case 7797:
			case 7798:
			case 7799:
				return skillingResource();

			// Complete wood, jungle, desert, and polar camouflage outfit family.
			case 10053:
			case 10055:
			case 10057:
			case 10059:
			case 10061:
			case 10063:
			case 10065:
			case 10067:
				return skillingOutfit();

			case 12596: // Rangers' tunic
			case 23249: // Rangers' tights
				return gear();

			case 12640: // Amylase crystal
			case 12641: // Amylase pack
				return skillingResource();

			// Complete normal-game Voidwaker component and assembled family.
			case 27681:
			case 27684:
			case 27687:
				return equipmentUpgrade();
			case 27690:
				return weapon();

			// Remaining repeatable Rod of Ivandis production states.
			case 7636: // Rod dust
				return craftingMaterial();
			case 7649: // Rod mould
				return tool();

			/*
			 * Workstream A exit review, pass 1. Complete normal-game player-facing
			 * families only; spoilt/burnt food and ALUFT activity/cache copies stay
			 * fail-closed. Routing only, with no recipe or variant ordering metadata.
			 */
			// Complete cocktail-spirit family.
			case 2015: // Vodka
			case 2017: // Whisky
			case 2019: // Gin
			case 2021: // Brandy
				return food();

			// Complete usable and premade Gnome cocktail family.
			case 2028:
			case 2030:
			case 2032:
			case 2034:
			case 2036:
			case 2038:
			case 2040:
			case 2048:
			case 2054:
			case 2064:
			case 2074:
			case 2080:
			case 2084:
			case 2092:
				return food();
			// Complete unfinished Gnome cocktail workflow.
			case 2042:
			case 2044:
			case 2046:
			case 2050:
			case 2052:
			case 2056:
			case 2058:
			case 2060:
			case 2062:
			case 2066:
			case 2068:
			case 2070:
			case 2072:
			case 2076:
			case 2078:
			case 2082:
			case 2086:
			case 2088:
			case 2090:
				return cookingMaterial();
			case 2094:
			case 2096:
			case 2098:
			case 2100: // Spoilt cocktail states
				return cleanupJunk();

			// Complete usable and premade Gnome bowl family.
			case 2185:
			case 2187:
			case 2191:
			case 2195:
			case 2229:
			case 2231:
			case 2233:
			case 2235:
				return food();
			// Complete unfinished Gnome bowl workflow.
			case 2177:
			case 2178:
			case 2179:
			case 2181:
			case 2183:
			case 2189:
			case 2193:
				return cookingMaterial();
			case 2173: // Spoilt gnomebowl
			case 2175: // Burnt gnomebowl
				return cleanupJunk();

			// Complete usable and premade Gnome crunchies family.
			case 2205:
			case 2209:
			case 2213:
			case 2217:
			case 2237:
			case 2239:
			case 2241:
			case 2243:
				return food();
			// Complete unfinished Gnome crunchies workflow.
			case 2201:
			case 2202:
			case 2203:
			case 2207:
			case 2211:
			case 2215:
				return cookingMaterial();
			case 2197: // Spoilt crunchies
			case 2199: // Burnt crunchies
				return cleanupJunk();

			// Complete usable and premade Gnome batta family.
			case 2219:
			case 2221:
			case 2223:
			case 2225:
			case 2227:
			case 2253:
			case 2255:
			case 2259:
			case 2277:
			case 2281:
				return food();
			// Complete unfinished Gnome batta workflow.
			case 2249:
			case 2250:
			case 2251:
			case 2257:
			case 2261:
			case 2263:
			case 2265:
			case 2267:
			case 2269:
			case 2271:
			case 2273:
			case 2275:
			case 2279:
				return cookingMaterial();
			case 2245: // Spoilt batta
			case 2247: // Burnt batta
				return cleanupJunk();

			// Complete player-facing POH barrel drink family.
			case 7740:
			case 7744:
			case 7746:
			case 7750:
			case 7752:
			case 7754:
				return food();
			case 7742: // Empty beer glass
				return cookingTool();

			// Complete Keldagrim shirt and trouser colour family.
			case 5030:
			case 5032:
			case 5034:
			case 5036:
			case 5038:
			case 5040:
				return clueCosmetic();

			// Complete player-facing Tower of Life satchel family.
			case 10877:
			case 10878:
			case 10879:
			case 10880:
			case 10881:
			case 10882:
				return clueCosmetic();

			// Charged jewellery slice 1: exact player-facing states route independently of ordering.
			case 2552:
			case 2554:
			case 2556:
			case 2558:
			case 2560:
			case 2562:
			case 2564:
			case 2566: // Ring of dueling, 8 -> 1
			case 3853:
			case 3855:
			case 3857:
			case 3859:
			case 3861:
			case 3863:
			case 3865:
			case 3867: // Games necklace, 8 -> 1
			case 21166:
			case 21169:
			case 21171:
			case 21173:
			case 21175: // Burning amulet, 5 -> 1
			case 21146:
			case 21149:
			case 21151:
			case 21153:
			case 21155: // Necklace of passage, 5 -> 1
			case 11968:
			case 11970:
			case 11105:
			case 11107:
			case 11109:
			case 11111:
			case 11113: // Skills necklace, 6 -> 1 plus uncharged
			case 19707: // Amulet of eternal glory
			case 11978:
			case 11976:
			case 1712:
			case 1710:
			case 1708:
			case 1706:
			case 1704: // Standard amulet of glory, 6 -> 1 plus uncharged
			case 11964:
			case 11966:
			case 10354:
			case 10356:
			case 10358:
			case 10360:
			case 10362: // Trimmed amulet of glory, 6 -> 1 plus uncharged
				return teleport();

			// Charged jewellery slice 2: exact player-facing states route independently of ordering.
			case 11972:
			case 11974:
			case 11118:
			case 11120:
			case 11122:
			case 11124:
			case 11126: // Combat bracelet, 6 -> 1 plus uncharged
			case 11194:
			case 11193:
			case 11192:
			case 11191:
			case 11190: // Digsite pendant, 5 -> 1; final use destroys the pendant
			case 20786:
			case 20787:
			case 20788:
			case 20789:
			case 20790:
			case 12785: // Imbued ring of wealth, 5 -> 1 plus uncharged
			case 11980:
			case 11982:
			case 11984:
			case 11986:
			case 11988:
			case 2572: // Standard ring of wealth, 5 -> 1 plus uncharged
			case 21268: // Eternal slayer ring
			case 11866:
			case 11867:
			case 11868:
			case 11869:
			case 11870:
			case 11871:
			case 11872:
			case 11873: // Slayer ring, 8 -> 1; final use destroys the ring
				return teleport();

			/*
			 * Workstream A exit review, pass 2. Complete normal-game live-cat
			 * lifecycle plus the sole player-facing random-event mystery box.
			 * Quest cats, dummy declarations, and the placeholder box stay closed.
			 */
			case 1555:
			case 1556:
			case 1557:
			case 1558:
			case 1559:
			case 1560: // Kitten colour states
			case 1561:
			case 1562:
			case 1563:
			case 1564:
			case 1565:
			case 1566: // Grown cat colour states
			case 1567:
			case 1568:
			case 1569:
			case 1570:
			case 1571:
			case 1572: // Overgrown cat colour states
			case 6549:
			case 6550:
			case 6551:
			case 6552:
			case 6553:
			case 6554: // Lazy cat colour states
			case 6555:
			case 6556:
			case 6557:
			case 6558:
			case 6559:
			case 6560: // Wily cat colour states
			case 7581: // Overgrown hellcat
			case 7582: // Hell cat
			case 7583: // Hell-kitten
			case 7584: // Lazy hell cat
			case 7585: // Wily hellcat
				return clueCosmetic();

			case 6199: // Random-event mystery box
				return rewardDrop();

			default:
				return Optional.empty();
		}
	}

	private static Optional<ItemClassificationRefiner.Classification> weapon()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "weapon"));
	}

	private static Optional<ItemClassificationRefiner.Classification> gear()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "gear"));
	}

	private static Optional<ItemClassificationRefiner.Classification> ammo()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "ammo"));
	}

	private static Optional<ItemClassificationRefiner.Classification> feet()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "feet"));
	}

	private static Optional<ItemClassificationRefiner.Classification> body()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "body"));
	}

	private static Optional<ItemClassificationRefiner.Classification> head()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "head"));
	}

	private static Optional<ItemClassificationRefiner.Classification> legs()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "legs"));
	}

	private static Optional<ItemClassificationRefiner.Classification> cannonPart()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "cannon-part"));
	}

	private static Optional<ItemClassificationRefiner.Classification> thrownWeapon()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "thrown-weapon"));
	}

	private static Optional<ItemClassificationRefiner.Classification> magicOffhand()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "magic-offhand"));
	}

	private static Optional<ItemClassificationRefiner.Classification> cleanupQuestItem()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CLEANUP, "quest-item"));
	}

	private static Optional<ItemClassificationRefiner.Classification> cleanupJunk()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CLEANUP, "junk"));
	}

	private static Optional<ItemClassificationRefiner.Classification> clueCosmetic()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CLUE, "cosmetic"));
	}

	private static Optional<ItemClassificationRefiner.Classification> treasureTrail()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CLUE, "treasure-trail"));
	}

	private static Optional<ItemClassificationRefiner.Classification> herbloreSecondary()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.HERBLORE, "secondary"));
	}

	private static Optional<ItemClassificationRefiner.Classification> herbloreBase()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.HERBLORE, "herblore-base"));
	}

	private static Optional<ItemClassificationRefiner.Classification> herbloreWorkflow()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.HERBLORE, "herblore"));
	}

	private static Optional<ItemClassificationRefiner.Classification> farmingProduce()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.FARMING, "produce"));
	}

	private static Optional<ItemClassificationRefiner.Classification> farmingSupply()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.FARMING, "farming-supply"));
	}

	private static Optional<ItemClassificationRefiner.Classification> herbSeed()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.FARMING, "herb-seed"));
	}

	private static Optional<ItemClassificationRefiner.Classification> craftingMaterial()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "crafting-material"));
	}

	private static Optional<ItemClassificationRefiner.Classification> cookingMaterial()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "cooking-material"));
	}

	private static Optional<ItemClassificationRefiner.Classification> currency()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CURRENCY, "currency"));
	}

	private static Optional<ItemClassificationRefiner.Classification> potionDose(int doses)
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.POTION,
			"potion-dose-" + doses));
	}

	private static Optional<ItemClassificationRefiner.Classification> teleport()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TELEPORT, "teleport"));
	}

	private static Optional<ItemClassificationRefiner.Classification> teleportContainer()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TELEPORT, "teleport-container"));
	}

	private static Optional<ItemClassificationRefiner.Classification> teleportCharge()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TELEPORT, "teleport-charge"));
	}

	private static Optional<ItemClassificationRefiner.Classification> skillingResource()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "resource"));
	}

	private static Optional<ItemClassificationRefiner.Classification> skillingAmmoComponent()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "ammo-component"));
	}

	private static Optional<ItemClassificationRefiner.Classification> skillingPrayerResource()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "prayer-resource"));
	}

	private static Optional<ItemClassificationRefiner.Classification> questUtility()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "quest-utility"));
	}

	private static Optional<ItemClassificationRefiner.Classification> tool()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "tool"));
	}

	private static Optional<ItemClassificationRefiner.Classification> lightSource()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "light-source"));
	}

	private static Optional<ItemClassificationRefiner.Classification> slayerTool()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "slayer-tool"));
	}

	private static Optional<ItemClassificationRefiner.Classification> skillingUtility()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "skilling-utility"));
	}

	private static Optional<ItemClassificationRefiner.Classification> skillingOutfit()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "skilling-outfit"));
	}

	private static Optional<ItemClassificationRefiner.Classification> cookingTool()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "cooking-tool"));
	}

	private static Optional<ItemClassificationRefiner.Classification> resourceContainer()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "resource-container"));
	}

	private static Optional<ItemClassificationRefiner.Classification> runecraftingUtility()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "runecrafting-utility"));
	}

	private static Optional<ItemClassificationRefiner.Classification> food()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.POTION, "food"));
	}

	private static Optional<ItemClassificationRefiner.Classification> pvmUtility()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.POTION, "pvm-utility"));
	}

	private static Optional<ItemClassificationRefiner.Classification> sailingUtility()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "sailing-utility"));
	}

	private static Optional<ItemClassificationRefiner.Classification> transportAccess()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TELEPORT, "transport-access"));
	}

	private static Optional<ItemClassificationRefiner.Classification> sailingUpgrade()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.TOOL, "sailing-upgrade"));
	}

	private static Optional<ItemClassificationRefiner.Classification> coralFragment()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.FARMING, "coral-fragment"));
	}

	private static Optional<ItemClassificationRefiner.Classification> keyMaterial()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "key-material"));
	}

	private static Optional<ItemClassificationRefiner.Classification> rewardKey()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "reward-key"));
	}

	private static Optional<ItemClassificationRefiner.Classification> rewardDrop()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "reward-drop"));
	}

	private static Optional<ItemClassificationRefiner.Classification> salvagingRelic()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "salvaging-relic"));
	}

	private static Optional<ItemClassificationRefiner.Classification> constructionMaterial()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "construction-material"));
	}

	private static Optional<ItemClassificationRefiner.Classification> shield()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.GEAR, "shield"));
	}

	private static Optional<ItemClassificationRefiner.Classification> equipmentCharge()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "equipment-charge"));
	}

	private static Optional<ItemClassificationRefiner.Classification> equipmentUpgrade()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "equipment-upgrade"));
	}

	private static Optional<ItemClassificationRefiner.Classification> bossAccessKey()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.UNIQUE, "boss-access-key"));
	}

	private static Optional<ItemClassificationRefiner.Classification> collectionTrophy()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.CLUE, "collection-trophy"));
	}
}
