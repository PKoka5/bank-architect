# Melee: huidige itemvoorkeur per slot

Controlelijst van de lokale implementatie, bijgewerkt op 25 oktober 2026.
Bron: `src/main/resources/com/pkoka5/ironmanbankarchitect/organize/melee-primary-candidates.tsv`.
Dit document wijzigt geen pluginregels.

## Hoe je deze lijst leest

De lijsten lopen van **lagere naar hogere voorkeur**. Ze tonen de vaste
terugvalvolgorde die wordt gebruikt als relevante stats binnen een vergelijkingsgroep
ontbreken. Het is geen universele schade- of boss-BIS-ranglijst.

Met complete stats vergelijkt de plugin binnen dezelfde groep:
**Melee Strength -> beste melee accuracy -> Prayer -> totale defence -> vast item-ID**.
Dan kan de uitkomst afwijken van de onderstaande terugvalrang.

- Bij armour en accessoires gaan algemene kandidaten voor specialistische/tank-terugvalopties.
- Hogere voorkeursgroepen gaan voor lagere groepen. Dit geldt voor neck,
  weapon en de Avernic/Dragon-defendergroep. Binnen een groep gelden stats
  of, bij ontbrekende stats, de vaste terugvalrang.
- Bij wapens bepaalt de voorkeursgroep als eerste de volgorde. Sterke
  tweehandige terugvalwapens gaan daardoor voor eerdere eenhanders, terwijl
  de algemene Whip/Abyssal-groep en hogere eenhanders hun voorkeur behouden.
- Regels met dezelfde rang zijn hieronder samen gezet; ze hoeven geen identieke
  werkelijke stats te hebben. Bij volledig gelijke voorkeur wint het lagere item-ID.
- Varianten achter dezelfde naam/regel delen de terugvalvoorkeur. Alle exacte
  IDs staan erbij. Het eerste ID bepaalt alleen de voorbeeldnaam.
- De kandidaat moet werkelijk in Combat terechtkomen en door de bestaande
  stijl- en rolfilters komen. Persoonlijke tags, Alch-keuzes en opgeslagen of
  vastgezette blueprints hebben hun bestaande voorrang.
- Lightbearer en andere expliciete supportrollen worden niet primair gekozen.
  Prayer-only armour komt normaal niet in de Melee-pool. Mixed hide/Blue/Eclipse
  Moon-body en -legs blijven in hun gedetecteerde Ranged/Magic-pool.
- Terugval betekent geen Alch. Alle niet-gekozen items blijven behouden.

## Early-game armour: helm, body en legs

Ook de vroege metalen uitrusting staat in de kandidatenlijst. De
materiaalvolgorde van laag naar hoog is:

**Bronze -> Iron -> Steel -> Black -> Mithril -> Adamant -> Rune**.

| Slot | Onderdelen per materiaal |
| --- | --- |
| Helm | Med helm en full helm |
| Body | Chainbody en platebody |
| Legs | Platelegs en plateskirt |

Dit zijn 42 basisitems: zeven materialen met elk zes onderdelen. In de vaste
terugvalvolgorde gaat binnen hetzelfde materiaal een full helm voor een med helm
en een platebody voor een chainbody. Platelegs en plateskirts delen hun rang.
Met complete stats geldt de vergelijking hierboven.

Een complete set is niet vereist: de plugin kiest per slot uit de aanwezige
Combat-kandidaten. Een account kan dus bijvoorbeeld een Iron helm, Steel body
en Mithril legs als beste beschikbare combinatie hebben. Daarna verschilt de
progressie per slot; de volledige lijsten hieronder bevatten ook de latere
algemene en specialistische opties. De bestaande persoonlijke keuzes en
Alch-regels blijven bepalen welke items Combat-kandidaten zijn.

## Vastgelegde afwegingen en resterende hiaten

1. De eerdere absolute voorkeur voor eenhanders is vervangen. De gecontroleerde
   sterke tweehanders in groep 24 gaan voor Bronze/Rune/Dragon, Zombie axe,
   Cudgel, Hasta en Lance. Whip/Abyssal dagger/Tentacle en hogere eenhanders
   blijven ervoor staan. Dit is een expliciete indelingsgrens, geen DPS-formule.
2. Fang heeft nu groep 35: boven Whip/Tentacle/Dagger, onder de snelle
   endgame-eenhanders in groep 40. Dat is een algemene indelingskeuze; door
   aanvalssnelheid, aanvalstype en tegenstander bestaat er geen universele
   DPS-volgorde. De overige wapens blijven behouden als alternatieven.
3. Fury krijgt bewust voorrang op een strength amulet voor de algemene set,
   ook al heeft het strength amulet een hogere ruwe Strength-bonus.
4. Alleen Avernic/Dragon defenders krijgen de expliciete voorkeursgroep boven
   Dragonfire shield. Lagere defenders hebben dat privilege niet.
5. De terugvalrangen zijn niet overal dezelfde volgorde als complete stats:
   bijvoorbeeld Brimstone boots staan in het vangnet boven Adamant boots,
   terwijl de Strength-eerstvergelijking die twee anders kan beoordelen.
6. Zes onderzochte maar nog niet goedgekeurde items blijven buiten de nieuwe
   kandidatenmap: Dragonstone body/legs/boots/gauntlets en Berserker necklace
   met ornament. Andere niet-onderzochte items houden de bestaande fallback;
   het is geen complete lijst van alle draagbare game-items.

## Lijsten per slot

### Helm

**Algemene kandidaten**

1. Warrior helm [IDs: 3753]
2. Fighter hat [IDs: 10548,24173]
3. Dwarven helmet [IDs: 11200]
4. Zombie helmet [IDs: 30321]
5. Obsidian helmet [IDs: 21298]
6. Berserker helm [IDs: 3751]
7. Helm of neitiznot [IDs: 10828]
8. Blood moon helm [IDs: 29028,29047]
9. Serpentine helm [IDs: 12931,13197,13199]
10. Neitiznot faceguard [IDs: 24271]
11. Oathplate helm [IDs: 30750,30777]
12. Inquisitor's great helm [IDs: 24419]
13. Torva full helm [IDs: 26382,28254]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Void melee helm [IDs: 11665,24185] / Slayer helmet [IDs: 11864,11865,25177,26674]
2. Bronze med helm [IDs: 1139]
3. Bronze full helm [IDs: 1155]
4. Iron med helm [IDs: 1137]
5. Iron full helm [IDs: 1153]
6. Steel med helm [IDs: 1141]
7. Steel full helm [IDs: 1157]
8. Black med helm [IDs: 1151]
9. Black full helm [IDs: 1165]
10. Mithril med helm [IDs: 1143]
11. Mithril full helm [IDs: 1159]
12. Adamant med helm [IDs: 1145]
13. Adamant full helm [IDs: 1161]
14. Rune med helm [IDs: 1147]
15. Rune full helm [IDs: 1163] / Rock-shell helm [IDs: 6128]
16. Dragon med helm [IDs: 1149]
17. Dragon full helm [IDs: 11335]
18. Granite helm [IDs: 10589]
19. Dharok's helm [IDs: 4716,4880,4881,4882,4883]
20. Guthan's helm [IDs: 4724,4904,4905,4906,4907]
21. Torag's helm [IDs: 4745,4952,4953,4954,4955]
22. Justiciar faceguard [IDs: 22326]
23. Verac's helm [IDs: 4753,4976,4977,4978,4979]
24. Initiate sallet [IDs: 5574]
25. Proselyte sallet [IDs: 9672]
26. Sunfire fanatic helm [IDs: 28933]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Serpentine helm (uncharged) — IDs: 12929,13196,13198
- Blood moon helm (broken) — IDs: 29073
- Torva full helm (damaged) — IDs: 26376
- Dharok's helm 0 — IDs: 4884,4908,4956,4980
- Broken zombie helmet — IDs: 30324


### Body

**Algemene kandidaten**

1. Blue moon chestplate [IDs: 29013,29037]
2. Mixed hide top [IDs: 29280]
3. Eclipse moon chestplate [IDs: 29004,29031]
4. Obsidian platebody [IDs: 21301]
5. Fighter torso [IDs: 10551,24175,28067,28069]
6. Blood moon chestplate [IDs: 29022,29043]
7. Bandos chestplate [IDs: 11832]
8. Oathplate chest [IDs: 30753,30779]
9. Inquisitor's hauberk [IDs: 24420]
10. Torva platebody [IDs: 26384,28256]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Bronze chainbody [IDs: 1103]
2. Bronze platebody [IDs: 1117,12205,12215]
3. Iron chainbody [IDs: 1101]
4. Iron platebody [IDs: 1115,12225,12235]
5. Steel chainbody [IDs: 1105]
6. Steel platebody [IDs: 1119,20169,20184]
7. Black chainbody [IDs: 1107]
8. Black platebody [IDs: 1125,2583,2591,23366,23369,23372,23375,23378]
9. Mithril chainbody [IDs: 1109]
10. Mithril platebody [IDs: 1121,12277,12287]
11. Adamant chainbody [IDs: 1111]
12. Adamant platebody [IDs: 1123,2599,2607,23392,23395,23398,23401,23404]
13. Rune chainbody [IDs: 1113]
14. Rune platebody [IDs: 1127,2615,2623,23209,23212,23215,23218,23221] / Gilded platebody [IDs: 3481]
15. White chainbody [IDs: 6615]
16. White platebody [IDs: 6617]
17. Zamorak platebody [IDs: 2653] / Saradomin platebody [IDs: 2661] / Guthix platebody [IDs: 2669] / Ancient platebody [IDs: 12460] / Armadyl platebody [IDs: 12470] / Bandos platebody [IDs: 12480]
18. Rock-shell plate [IDs: 6129]
19. Granite body [IDs: 10564]
20. Dragon chainbody [IDs: 3140] / Dragon chainbody (g) [IDs: 12414] / Dragon chainbody (cr) [IDs: 28065]
21. Dragon platebody [IDs: 21892] / Dragon platebody (g) [IDs: 22242]
22. 3rd age platebody [IDs: 10348]
23. Dharok's platebody [IDs: 4720,4892,4893,4894,4895] / Guthan's platebody [IDs: 4728,4916,4917,4918,4919] / Torag's platebody [IDs: 4749,4964,4965,4966,4967]
24. Verac's brassard [IDs: 4757,4988,4989,4990,4991]
25. Justiciar chestguard [IDs: 22327]
26. Monk's robe top [IDs: 544,20199,23303]
27. Initiate hauberk [IDs: 5575]
28. Proselyte hauberk [IDs: 9674]
29. Sunfire fanatic cuirass [IDs: 28936]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Dharok's platebody 0 — IDs: 4896,4920,4968,4992
- Fighter torso (broken) — IDs: 20513,33520,33522
- Torva platebody (damaged) — IDs: 26378
- Eclipse moon chestplate (broken) — IDs: 29049
- Blue moon chestplate (broken) — IDs: 29058
- Blood moon chestplate (broken) — IDs: 29067


### Legs

**Algemene kandidaten**

1. Mixed hide legs [IDs: 29283]
2. Fremennik kilt [IDs: 23246]
3. Blue moon tassets [IDs: 29016,29039]
4. Eclipse moon tassets [IDs: 29007,29033]
5. Obsidian platelegs [IDs: 21304]
6. Blood moon tassets [IDs: 29025,29045]
7. Bandos tassets [IDs: 11834]
8. Oathplate legs [IDs: 30756,30781]
9. Inquisitor's plateskirt [IDs: 24421]
10. Torva platelegs [IDs: 26386,28258]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Bronze platelegs [IDs: 1075,12207,12217] / Bronze plateskirt [IDs: 1087,12209,12219]
2. Iron platelegs [IDs: 1067,12227,12237] / Iron plateskirt [IDs: 1081,12229,12239]
3. Steel platelegs [IDs: 1069,20172,20187] / Steel plateskirt [IDs: 1083,20175,20190]
4. Black platelegs [IDs: 1077,2585,2593] / Black plateskirt [IDs: 1089,3472,3473]
5. Mithril platelegs [IDs: 1071,12279,12289] / Mithril plateskirt [IDs: 1085,12285,12295]
6. Adamant platelegs [IDs: 1073,2601,2609] / Adamant plateskirt [IDs: 1091,3474,3475]
7. Rune platelegs [IDs: 1079,2617,2625] / Rune plateskirt [IDs: 1093,3476,3477] / Gilded platelegs [IDs: 3483] / Gilded plateskirt [IDs: 3485]
8. White platelegs [IDs: 6625] / White plateskirt [IDs: 6627]
9. Zamorak platelegs [IDs: 2655] / Saradomin platelegs [IDs: 2663] / Guthix platelegs [IDs: 2671] / Zamorak plateskirt [IDs: 3478] / Saradomin plateskirt [IDs: 3479] / Guthix plateskirt [IDs: 3480] / Ancient platelegs [IDs: 12462] / Ancient plateskirt [IDs: 12464] / Armadyl platelegs [IDs: 12472] / Armadyl plateskirt [IDs: 12474] / Bandos platelegs [IDs: 12482] / Bandos plateskirt [IDs: 12484]
10. Rock-shell legs [IDs: 6130]
11. Granite legs [IDs: 6809]
12. Dragon platelegs [IDs: 4087,4585] / Dragon platelegs (g) [IDs: 12415,12416] / Dragon platelegs (cr) [IDs: 28061,28063]
13. 3rd age platelegs [IDs: 10346] / 3rd age plateskirt [IDs: 23242]
14. Dharok's platelegs [IDs: 4722,4898,4899,4900,4901] / Guthan's chainskirt [IDs: 4730,4922,4923,4924,4925] / Torag's platelegs [IDs: 4751,4970,4971,4972,4973]
15. Verac's plateskirt [IDs: 4759,4994,4995,4996,4997]
16. Justiciar legguards [IDs: 22328]
17. Monk's robe [IDs: 542,20202,23306]
18. Initiate cuisse [IDs: 5576]
19. Proselyte cuisse [IDs: 9676,9678]
20. Sunfire fanatic chausses [IDs: 28939]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Dharok's platelegs 0 — IDs: 4902,4926,4974,4998
- Torva platelegs (damaged) — IDs: 26380
- Eclipse moon tassets (broken) — IDs: 29052
- Blue moon tassets (broken) — IDs: 29061
- Blood moon tassets (broken) — IDs: 29070


### Cape

**Algemene kandidaten**

1. Ardougne cloak 1 [IDs: 13121]
2. Ardougne cloak 2 [IDs: 13122]
3. Ardougne cloak 3 [IDs: 13123]
4. Ardougne cloak 4 [IDs: 13124]
5. Mixed hide cape [IDs: 29289]
6. Mythical cape [IDs: 22114,24855]
7. Fire cape [IDs: 6570,13329,24134,24223]
8. Infernal max cape [IDs: 21285,21295,24133,24224]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Cape of legends [IDs: 1052]
2. Obsidian cape [IDs: 6568] / Obsidian cape (r) [IDs: 20050]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Fire cape (broken) — IDs: 20445,20447
- Infernal cape (broken) — IDs: 21287,21289,33496,33498,33500,33502


### Neck

**Algemene kandidaten**

Voorkeursgroep 10 (lagere groep eerst):

1. Amulet of power [IDs: 1731,23354]
2. Amulet of glory [IDs: 1704,1706,1708,1710,1712,10354,10356,10358,10360,10362,11964,11966,11976,11978,19707] / Amulet of the damned (full) [IDs: 12851,12853]
3. Amulet of strength [IDs: 1725,10364]

Voorkeursgroep 20 (lagere groep eerst):

1. Amulet of fury [IDs: 6585,12436,24780]
2. Amulet of torture [IDs: 19553,20366]
3. Amulet of rancour [IDs: 29801,29804]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Salve amulet [IDs: 4081,10588,12017,12018]


### Gloves

**Algemene kandidaten**

1. Bronze gloves [IDs: 7454]
2. Iron gloves [IDs: 7455]
3. Steel gloves [IDs: 7456]
4. Black gloves [IDs: 7457]
5. Mithril gloves [IDs: 7458]
6. Combat bracelet(4) [IDs: 11118,11120,11122,11124,11126,11972,11974]
7. Adamant gloves [IDs: 7459]
8. Regen bracelet [IDs: 11133]
9. Granite gloves [IDs: 21736]
10. Rune gloves [IDs: 7460]
11. Dragon gloves [IDs: 7461]
12. Barrows gloves [IDs: 7462]
13. Ferocious gloves [IDs: 22981]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Leather gloves [IDs: 1059]


### Boots

**Algemene kandidaten**

1. Adamant boots [IDs: 4129]
2. Boots of brimstone [IDs: 22951]
3. Climbing boots [IDs: 3105,23413]
4. Mixed hide boots [IDs: 29286]
5. Rune boots [IDs: 4131] / Gilded boots [IDs: 12391]
6. Granite boots [IDs: 21643]
7. Guardian boots [IDs: 21733]
8. Echo boots [IDs: 28945]
9. Spiked manacles [IDs: 23389]
10. Dragon boots [IDs: 11840,22234] / Dragon boots (cr) [IDs: 28055]
11. Aranea boots [IDs: 29806]
12. Avernic treads [IDs: 31088,31092,31093,31096]
13. Primordial boots [IDs: 13239]
14. Avernic treads (pr) [IDs: 31091,31094,31095,31097]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Fancy boots [IDs: 9005,28672] / Fighting boots [IDs: 9006]
2. Bronze boots [IDs: 4119]
3. Iron boots [IDs: 4121]
4. Steel boots [IDs: 4123]
5. Black boots [IDs: 4125]
6. Mithril boots [IDs: 4127]
7. White boots [IDs: 6619]
8. Bandos boots [IDs: 11836]


### Ring

**Algemene kandidaten**

1. Warrior ring [IDs: 6735] / Tyrannical ring [IDs: 12603] / Treasonous ring [IDs: 12605]
2. Warrior ring (i) [IDs: 11772,25262,26769] / Tyrannical ring (i) [IDs: 12691,25254,26765] / Treasonous ring (i) [IDs: 12692,25256,26766]
3. Ring of shadows [IDs: 28327] / Ring of shadows (uncharged) [IDs: 28329]
4. Berserker ring [IDs: 6737]
5. Brimstone ring [IDs: 22975]
6. Bellator ring [IDs: 28316]
7. Berserker ring (i) [IDs: 11773,25264,26770]
8. Ultor ring [IDs: 28307]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Ring of recoil [IDs: 2550]
2. Lightbearer [IDs: 25975]
3. Granite ring [IDs: 21739]
4. Granite ring (i) [IDs: 21752,25193,26685]
5. Ring of suffering [IDs: 19550,20655]
6. Ring of suffering (i) [IDs: 19710,20657,25246,25248,26761,26762]
7. Ring of the gods [IDs: 12601]
8. Ring of the gods (i) [IDs: 13202,25252,26764]


### Weapon

Bij wapens geldt de onderstaande volgorde van voorkeursgroepen voor alle
bruikbare kandidaten samen. De tweehandsreserve in groep 24 gaat dus voor de
eerdere eenhanders in groepen 10-22. Vanaf de Whip/Abyssal-groep blijft de
afgesproken eenhander-plus-defenderindeling de voorkeur houden. Alle overgebleven
wapens blijven beschikbaar in hun bestaande groepen.

**Specialistische terugval, voorkeursgroep 0 (lagere voorkeur eerst)**

1. Torag's hammers [IDs: 4747,4958,4959,4960,4961]
2. Verac's flail [IDs: 4755,4982,4983,4984,4985]
3. Guthan's warspear [IDs: 4726,4910,4911,4912,4913]
4. Dharok's greataxe [IDs: 4718,4886,4887,4888,4889]
5. Barrelchest anchor [IDs: 10887]
6. Dragon battleaxe [IDs: 1377,28037]
7. Dragon dagger [IDs: 1215,1231,5680,5698,28019,28021,28023,28025]
8. Crystal halberd [IDs: 23987,24125]
9. Dragon claws [IDs: 13652,28039,29577]
10. Voidwaker [IDs: 27690]
11. Dragon warhammer [IDs: 13576,28035]
12. Elder maul [IDs: 21003,27100]
13. Armadyl godsword [IDs: 11802,11804,11806,11808,20368,20370,20372,20374]
14. Leaf-bladed sword [IDs: 11902]
15. Arclight [IDs: 19675]
16. Leaf-bladed battleaxe [IDs: 20727]
17. Emberlight [IDs: 29589]
18. Obligator (uncharged) [IDs: 34717]
19. Scythe of vitur (uncharged) [IDs: 22486,25738,25741]

**Algemene eenhanders en gecontroleerde tweehandsreserve**

Voorkeursgroep 10 (lagere groep eerst):

1. Bronze scimitar [IDs: 1321]
2. Iron scimitar [IDs: 1323]
3. Steel scimitar [IDs: 1325]
4. Black scimitar [IDs: 1327]
5. Mithril scimitar [IDs: 1329]
6. Adamant scimitar [IDs: 1331]
7. Rune scimitar [IDs: 1333,12389,23330,23332,23334]

Voorkeursgroep 14 (lagere groep eerst):

1. Dragon longsword [IDs: 1305,28033]

Voorkeursgroep 15 (lagere groep eerst):

1. Dragon scimitar [IDs: 4587,20000,28031]

Voorkeursgroep 21 (lagere groep eerst):

1. Zombie axe [IDs: 28810]

Voorkeursgroep 22 (lagere groep eerst):

1. Zamorakian hasta [IDs: 11889]
2. Sarachnis cudgel [IDs: 23528] / Dragon hunter lance [IDs: 22978]

Voorkeursgroep 24, sterke tweehandsreserve (lagere voorkeur eerst):

1. Abyssal bludgeon [IDs: 13263]
2. Noxious halberd [IDs: 29796]
3. Scythe of vitur [IDs: 22325,25736,25739]
4. Obligator [IDs: 34715]
5. Soulreaper axe [IDs: 28338,33335]

Dit is een vaste terugvalvoorkeur; doelgrootte, stacks en andere voorwaardelijke
effecten worden niet geactiveerd. Ongelaadde varianten krijgen deze groep niet.

Voorkeursgroep 30 (lagere groep eerst):

1. Abyssal dagger [IDs: 13265,13267,13269,13271]
2. Abyssal whip [IDs: 4151,12773,12774,26482]
3. Abyssal tentacle [IDs: 12006,26484]

Voorkeursgroep 35 (lagere groep eerst):

1. Osmumten's fang [IDs: 26219,27246]

Deze voorkeur voor Fang is een indelingskeuze. Door zijn vijf-tickaanval en
Stab-mechaniek is hij niet tegen iedere tegenstander beter dan de vier-tickwapens.

Voorkeursgroep 40 (lagere groep eerst):

1. Ghrazi rapier [IDs: 22324,25734] / Blade of saeldor [IDs: 23995,24551,25870,25872,25874,25876,25878,25880,25882]
2. Inquisitor's mace [IDs: 24417]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Blade of saeldor (inactive) — IDs: 23997,23989,30305,4890,4914,4962,4986,10888


### Shield / offhand

**Algemene kandidaten**

Voorkeursgroep 0 (lagere groep eerst):

1. Bronze defender [IDs: 8844,24136]
2. Iron defender [IDs: 8845,24137]
3. Steel defender [IDs: 8846,24138]
4. Black defender [IDs: 8847,24139]
5. Mithril defender [IDs: 8848,24140]
6. Adamant defender [IDs: 8849,24141]
7. Toktz-ket-xil [IDs: 6524]
8. Rune defender [IDs: 8850,24142,23230,27009]
9. Dragonfire shield [IDs: 11283,11284]

Voorkeursgroep 10 (lagere groep eerst):

1. Dragon defender [IDs: 12954,24143,19722,27008]
2. Avernic defender [IDs: 22322,24186,27550,27551,27552,27553]

**Terugvalopties; alleen zonder geschikte algemene kandidaat**

1. Anti-dragon shield [IDs: 1540]
2. Book of balance [IDs: 3844]
3. Unholy book [IDs: 3842]
4. Dragon sq shield [IDs: 1187]
5. Spirit shield [IDs: 12829]
6. Blessed spirit shield [IDs: 12831]

**Onbruikbare staten; blijven buiten de primaire selectie**

- Avernic defender (broken) — IDs: 22441,20449,20451,20453,20455,20457,20459,20461,20463
