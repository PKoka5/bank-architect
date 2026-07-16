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

			// Wiki: https://oldschool.runescape.wiki/w/Victor's_cape_(1)?oldid=15189530
			case 24207: // Permanent Last Man Standing achievement cosmetic
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
