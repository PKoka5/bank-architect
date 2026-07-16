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
				return cannonPart();

			case 805: // Rune thrownaxe
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

			case 10498: // Ava's attractor
			case 10499: // Ava's accumulator
			case 22109: // Ava's assembler
			case 24222: // Ava's assembler (l)
			case 27374: // Masori assembler
			case 27376: // Masori assembler (l)
				return gear();

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
			case 3107: // Spiked boots
			case 4021: // M'speak amulet
			case 4024: // Ninja monkey greegree
			case 4026: // Gorilla greegree
			case 4030: // Zombie monkey greegree
			case 4031: // Karamjan monkey greegree
			case 6465: // Ring of charos(a)
			case 6544: // Catspeak amulet(e)
			case 6786: // Robe of elidinis (top)
			case 6787: // Robe of elidinis (bottom)
			case 4657: // Ring of visibility
			case 4567: // Gold helmet
			case 7917: // Ram skull helm
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
