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
			case 4021: // M'speak amulet
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

			case 6800: // Giant champion scroll
			case 6807: // Zombie champion scroll
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

	private static Optional<ItemClassificationRefiner.Classification> craftingMaterial()
	{
		return Optional.of(new ItemClassificationRefiner.Classification(ItemCategory.SKILLING, "crafting-material"));
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
