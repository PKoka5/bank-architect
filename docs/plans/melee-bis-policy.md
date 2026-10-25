# Melee BIS: kandidaten, selectie en bewijs

Status: lokale implementatie en verificatie, 24 oktober 2026. De selectie voor
alle tien Melee-posities is gebouwd; de definitieve regressiecontrole en in-game
acceptatie worden vastgelegd in `docs/testing/melee-equipment-2026-10-24.md`.
De daaropvolgende wapenverfijning van 25 oktober staat in
`docs/testing/melee-weapons-2026-10-25.md`; deze vervangt de eerdere absolute
voorkeur voor eenhanders en de eerdere plaats van Fang.
De eigenaar wil eerst Melee aantoonbaar goed krijgen en daarna hetzelfde ontwerp
gebruiken voor Ranged en Magic. Publicatie blijft uit.

## 1. Doel en vaste uitgangspunten

De voorste Melee-kolom wordt een **algemene, offensieve Melee-uitrusting uit de
bank van de speler**. Dit is een indelingsadvies, geen berekening van universele
BIS of maximale DPS voor iedere tegenstander.

- Per slot bepalen gecontroleerde item-ID's welke items geschikt zijn.
- Binnen vergelijkbare kandidaten bepalen relevante stats de voorkeur.
- Tank-, healing-, Slayer-, slash-, stab-, crush- en special-attackfuncties krijgen
  een eigen rol. Ze blijven beschikbaar; ongebruikte sets blijven gegroepeerd.
- Main en Ironman gebruiken dezelfde Melee-selectie. Hun tabverdeling blijft
  onafhankelijk daarvan bestaan.
- Een bankplaceholder telt mee als vastgelegde aanwezigheid. Een gedragen item
  zonder bankplaceholder is niet bekend. We lezen geen inventory, equipment of
  spelerslevels en claimen niet dat ieder bankitem nu draagbaar is.
- Gedeelde accessoires gaan zoals afgesproken eerst naar Melee en staan maar
  eenmaal in de echte bank. Geen fictieve duplicaten of automatische bankacties.

### Bevestigde fout

`GearItemSorter.plan` geeft momenteel vaste rolprioriteit voorrang, gevolgd door
speciale ring/Ranged-helmregels en vervolgens `gearTier * 200 + stats.score`.
De Melee-score bevat ook de totale defence. Guthan's helm heeft tier 4 en Helm
of Neitiznot tier 3; Neitiznot krijgt geen aparte Strength-voorkeur. Daardoor kan
Guthan winnen terwijl Neitiznot de bedoelde offensieve helm is.

Een progressiestadium zegt waarvoor een item nuttig kan zijn. Het bewijst geen
voorrang in een algemene offensieve kolom. We veranderen daarom de primaire
selectie, in plaats van algemene tiers voor alle bestaande gebruikers te herdefiniëren.

## 2. Eerst de kandidatenlijst vastleggen

Maak een controleerbare ontwikkeltabel voor **alle tien Melee-posities**:
head, body, legs, cape, neck, hands, feet, ring, weapon en shield/offhand.
Elke relevante starter-, early-, mid-, late- en endgame-optie krijgt een
expliciete beslissing. Ook alternatieven die niet vooraan horen worden vastgelegd.

| Gegeven | Vereiste |
| --- | --- |
| Identiteit | Exact canoniek item-ID, naam en geverifieerde varianten |
| Slot | Werkelijk equipment-slot; afwijking van RuneLite-data is een te onderzoeken fout |
| Functie | Algemeen, specialistisch of terugval; meerdere functies kunnen bestaan |
| Vergelijking | Vergelijkbare groep, eventuele gemotiveerde voorkeursband en vaste terugvalrang |
| Voorwaarden | Bijvoorbeeld complete set, Slayer-taak, charge/state, attack type of special attack |
| Wapencompatibiliteit | Handedness, aanvalssnelheid en bekende functie |
| Bewijs | Bronpagina, bronrevisie/datum en reden voor opname of uitsluiting |

Gebruik de Wiki-progressie als ingang en controleer afwegingen tegen itemstats
en relevante strategy-/setpagina's. Neem een Wiki-tier of tabelrang niet zonder
de bijbehorende context over. Ontbreken in een Wiki-tabel bewijst evenmin dat
een item geen combat gear is.

Primaire onderzoekspunten:

- [Melee Gear Progression](https://oldschool.runescape.wiki/w/Guide:Melee_Gear_Progression)
- [Melee training en equipment choice](https://oldschool.runescape.wiki/w/Pay-to-play_Melee_training)
- [Helm of Neitiznot](https://oldschool.runescape.wiki/w/Helm_of_Neitiznot)
- [Guthan's equipment](https://oldschool.runescape.wiki/w/Guthan%27s_equipment)
- Bestaande ontwikkelresearch in `tools/research/gear-progression-exact-ids.tsv`
  en `tools/research/wiki-recommended-equipment/`.

De specifieke progressiepagina kon bij de eerste controle niet volledig worden
opgehaald. De aanvullende Wiki-tabellen bevestigen het helmverschil; de complete
kandidatenlijst en bronrevisies zijn nog te onderzoeken. Bestaande research is
een startpunt, geen reeds goedgekeurde volledige BIS-lijst.

Lever eerst een leesbaar overzicht per slot op met algemene kandidaten,
specialisten, terugvalopties en onderbouwde verwachte winnaars. Geen stilzwijgende
uitsluitingen en geen lijst met alleen het absolute endgame-item.

## 3. Selectieregels die we vooraf vastleggen

### Armour en accessoires

Selecteer uitsluitend items die na de bestaande persoonlijke keuzes en
categorisering daadwerkelijk in Combat terechtkomen.

1. Kies per slot eerst uit gecontroleerde algemene kandidaten.
2. Pas expliciete voorkeursbanden toe waar stats verschillende functies afwegen.
   Voor deze **algemene** indeling krijgt Fury bijvoorbeeld voorrang op een
   strength-only budgetamulet wanneer beide aanwezig zijn. Dit is een benoemde
   indelingskeuze, geen universele DPS-claim.
3. Vergelijk binnen dezelfde vergelijkbare groep op een vaste tuple:
   Melee Strength, algemene melee accuracy, Prayer, defence, eventueel bestaand
   setverband en een stabiel canoniek item-ID. Hogere waarden winnen, behalve ID.
   Algemene accuracy gebruikt de beste melee-aanvalbonus; dit kiest geen
   tegenstander of slash/stab/crush-loadout.
4. Een hogere algemene progressietier of grotere defence mag een geschikte
   offensieve kandidaat niet vooraf verslaan. De gedeelde oude score blijft
   beschikbaar voor bestaande doeleinden buiten deze selectie.
5. Ontbreekt iedere algemene kandidaat, kies dan een geverifieerde terugvaloptie.
   Alleen Guthan's helm in de bank blijft dus een bruikbare helm. Een specialist
   als terugval presenteren activeert zijn voorwaardelijke effect niet.
6. Alle niet-gekozen items blijven behouden. De bestaande verticale setindeling
   ordent de rest; een item krijgt geen Alch-tag omdat het geen primaire kandidaat is.

Bij een schaarse bank krijgt het vullen van de primaire kolom met bruikbare
terugvalgear voorrang op het bijeenhouden van een specialistische set. Met alleen
Neitiznot en Guthan kunnen Guthan-body/legs/wapen dus vooraan nodig zijn, terwijl
Guthan's helm overblijft. Een fysiek item kan niet tegelijk op twee plekken staan.
Als er aparte algemene alternatieven zijn, blijft de ongebruikte Guthan-set als
geheel bij elkaar. Deze twee situaties krijgen afzonderlijke tests.

Leg iedere echte trade-off vast in de ontwikkeltabel. Voor neck, shields en
hybride accessoires toetsen we dus expliciet de uitkomst, in plaats van overal
blind Strength eerst toe te passen. Verander deze keuze niet achteraf om alleen
een individuele voorbeeldbank te laten passen.

### Wapens en offhands apart

Een langzamer wapen met grotere bonussen is niet automatisch de betere algemene
keuze. Armour en wapens delen daarom geen blinde score.

- Leg algemene hoofdwapenkandidaten en hun onderbouwde voorkeursbanden vast.
- Vergelijk stats alleen binnen passende wapenrollen en compatibele groepen;
  neem aanvalssnelheid mee volgens de gecontroleerde functie.
- Houd slash, stab, crush, demonbane, defence reduction en special attacks
  herkenbaar bij elkaar in het overige Combat-gedeelte.
- Behandel een primair tweehandig wapen en een defender niet als een tegelijk
  draagbare hoofdset. Offhands voor alternatieve wapens blijven beschikbaar.
- Voeg geverifieerde handedness-feiten toe waar de bestaande metadata tekortschiet;
  leid die niet alleen af uit de itemnaam. `GearStats` bevat nu nog geen handedness.
- Activeer Slayer-/monster-/setvoorwaarden niet zonder bewijs van die context.
  Voorwaardelijke effecten worden als functie vastgelegd en niet in een fictieve
  algemene DPS-score verwerkt.

Wapens en offhands zijn onderdeel van de Melee-afronding. Alleen de helm
repareren is een tussenstap; daarna noemen we de volledige Melee-selectie nog niet af.

### Ontbrekende stats, onbekende items en varianten

- Gebruik RuneLite-itemmetadata uit de bestaande bankanalyse; runtimeonderzoek
  op internet of nieuwe databronnen is niet nodig.
- Ontbrekende stats zijn **onbekend**, geen nul. Kies per vergelijkbare groep één
  vergelijkingsmodus: complete relevante stats of de volledig gecontroleerde
  terugvalvolgorde. Geen pairwise wisseling tussen score en rang die een cyclische
  comparator kan opleveren.
- Aesthetische varianten met dezelfde werking krijgen dezelfde voorkeur.
  Gebroken, gedegradeerde, geladen en ongeladen vormen krijgen een expliciete
  functionele beoordeling; ze zijn niet automatisch uitwisselbaar.
- Nieuwe/onbekende items blijven zichtbaar onder de passende bestaande Combat-
  indeling wanneer hun classificatie Combat is. Zonder review krijgen ze geen
  nieuwe primaire-BIS- of Alch-claim. De researchcontrole moet zulke hiaten tonen.
- Ontbrekende of beschadigde vereiste catalogusdata gebruikt de bestaande
  fail-closed foutafhandeling; geen stille overstap naar naamgissingen.

## 4. Compact inpassen in de bestaande plugin

Beoogde productiepunten bij implementatie:

- `GearItemSorter.java`: scheid de primaire Melee-keuze van de huidige algemene
  score en behoud de selectie/geometrie-scheiding en eenmalige itemtoewijzing.
- Bestaande rollen-, tier- en familietabellen, met hun loaders: één gecontroleerde
  bron voor functie/varianten. Inspecteer eerst of het huidige enkelvoudige
  rolmodel meerdere functies en per-slotkeuzes kan dragen. Breid het bewust uit
  of vervang het waar nodig; voeg geen losse concurrerende Java-whitelist toe.
- Alleen een kleine eigen selectiehelper indien dat de bestaande comparator
  daadwerkelijk vervangt en eenvoudiger maakt. Geen nieuw algemeen framework.
- `AcceptedBlueprint.RULES`: verhogen zodra de nieuwe regels daadwerkelijk landen.
- Bestaande placement reasons: vermeld bij de gekozen primaire kandidaat de
  functie en doorslaggevende vergelijking of gecontroleerde terugval.

De selectie raakt niet ongemerkt `GearItemSorter.score`, `GearStats.dominates`
of de bewijsregels van Flexible Alch. Handmatige categorisering, Fixed Alch,
opgeslagen itemvolgorde en vastgezette blueprints behouden hun bestaande prioriteit.
Een vastgezet target blijft actief; Review toont het nieuwe Melee-voorstel en de
speler neemt dit bewust over. Er komen geen automatische gameacties.

## 5. Testmatrix en bewijs van kwaliteit

Verwachte winnaars komen uit vooraf gecontroleerde fixtures met echte item-ID's
en vastgelegde stats. De tests berekenen hun verwachting niet opnieuw met dezelfde
productieformule. Iedere primaire positie krijgt minimaal een vroege/midden-
en eindspelvergelijking en een set relevante trade-offs.

| Geval | Vereiste uitkomst |
| --- | --- |
| Neitiznot + Guthan's helm | Neitiznot vooraan; Guthan blijft behouden |
| Alleen Guthan's helm | Geldige terugval; helm verdwijnt niet |
| Neitiznot + complete Guthan-set + algemene body/legs/wapenalternatieven | Neitiznot en algemene gear vooraan; de vier ongebruikte Guthan-items blijven bij elkaar |
| Alleen Neitiznot + complete Guthan-set | Neitiznot vooraan; nodige Guthan-stukken mogen primaire terugval zijn; geen fictieve dubbele items |
| Tankhelm versus offensieve helm | Tier/defence winnen niet zelfstandig van de geschikte offensieve kandidaat |
| Torva, faceguard, Neitiznot en lagere opties | Gecontroleerde algemene helmprogressie; geen naam- of invoervolgorde-effect |
| Oathplate en Inquisitor naast algemene gear | Specialistische functie en verticale families blijven herkenbaar |
| Fury + strength amulet | De benoemde algemene neck-voorkeur, niet een toevallige score |
| Boots, capes, gloves en rings | Alle bijbehorende progressie- en variantgevallen afgedekt, inclusief Avernic treads |
| Slow weapon + sneller alternatief | De gecontroleerde wapenrol/snelheidsafweging; niet simpelweg hoogste Strength |
| Tweehandig hoofdwapen + defender | Geen voorgestelde tegelijk draagbare combinatie |
| Wapens met special attacks | Behouden en gegroepeerd; primaire afwezigheid is geen Alch-bewijs |
| Volledige en onvolledige stats | Juiste modus, vaste uitkomst en transitieve comparator |
| Identieke stats / geschudde bankvolgorde | Exact dezelfde primaire keuzes en stabiele verdere ordening |
| Toevoegen van een ongerelateerd item | Bestaande primaire keuzes blijven gelijk zonder nieuwe relevante kandidaat of veranderde reservering |
| Placeholder wordt item of andersom | Dezelfde keuze en behoud van actuele aantallen/status |
| Varianten en dubbele fysieke voorkomens | Juist functioneel gedrag; ieder echt item eenmaal behouden |
| Onbekend nieuw item | Zichtbaar, gerapporteerd als reviewhiaat, geen ongefundeerde claim |
| Midgame bank + grote gemengde endgame bank | Complete primaire kolom; overige sets verticaal en wapens/ringen gegroepeerd |
| Main en Ironman | Dezelfde Melee-keuze bij dezelfde beschikbare Combat-items |
| Persoonlijke keuzes / Alch / vastgezet target | Bestaande prioriteit blijft gelden; voorstel kan apart worden beoordeeld |
| Ranged en Magic met dezelfde kandidaten/reserveringen | Selectie en interne relatieve volgorde blijven gelijk |
| Overige tabs | Classificatie en interne relatieve volgorde blijven gelijk |

Melee kiest gedeelde accessoires eerst. Een nieuwe Melee-keuze of tweehandig
hoofdwapen kan een accessoire reserveren of vrijmaken en daardoor ook een andere
Ranged/Magic-keuze opleveren. Leg zulke gevolgen afzonderlijk vast in gemengde
fixtures met de oude/nieuwe reservering en verwachte winnaar. Hun eigen
vergelijkingsregels wijzigen niet; voor dezelfde kandidaten en reserveringen
blijven de uitkomsten identiek. Iedere onbedoelde nevenwijziging blokkeert afronding.

Naast unit-/regressietests maken we leesbare exports en afbeeldingen van een
early bank, een midgame Ironman, een Main en een grote gemengde endgame bank.
Beoordeel per voorbeeld de hoofdset, complete alternatieve sets en de indeling
van specialistische wapens. Simulaties die eindigen zijn niet alleen voldoende
bewijs voor een begrijpelijke combat-tab.

## 6. Uitvoering en afrondingsvoorwaarden

1. **Tokenruimte:** de huidige hoogste schatting is 199.957. Maak eerst een
   gecontroleerd plan voor codevereenvoudiging en een gemeten reserve voor deze
   wijziging. Werkdoel na implementatie: hoogste schatting maximaal 195.000.
   Behoud functionaliteit en noodzakelijke tests; verplaats geen logica of data
   uitsluitend om de estimator te omzeilen. Inspecteer resources en overige
   wijzigingen apart. Een build is geen officiële Hub-tokentelling.
2. **Datareview:** complete kandidaten-/uitzonderingentabel per slot, bronbewijs,
   variant- en statecontrole, vaste verwachte winnaars. Los open trade-offs op
   voordat hun productiegedrag wordt vastgelegd.
3. **Eerste verticale slice:** helmselectie met Neitiznot/Guthan, echte fixtures,
   terugval, placeholders en volledige Guthan-set. Bewijs dat de oorzaak is
   opgelost met algemene regels, niet met één hardcoded uitzondering.
4. **Alle armour/accessoires:** hetzelfde gecontroleerde model voor de overige
   draagbare slots, inclusief expliciete neck-/offhandafwegingen.
5. **Wapens en compatibiliteit:** rond hoofdwapens, gespecialiseerde groepen,
   snelheid, functionele states en tweehandigheid af.
6. **Volledige controle:** voer passende tests, complete build en banksimulaties
   uit. Beoordeel iedere baselinewijziging inhoudelijk; verwachtte nieuwe
   Melee-orders mogen fingerprints veranderen. Vernieuw baselines pas na review.
   Controleer de geïsoleerde Ranged/Magic-/Alch-regressies en review-tokenraming.
7. **In-game ronde:** eigenaar test de eigen bank en exports; simulaties dekken de
   niet-beschikbare endgame-gear. Leg resterende verschillen expliciet vast.

**Melee is pas af** als alle tien posities een gecontroleerd besluit hebben,
de testmatrix slaagt, exports begrijpelijk zijn, persoonlijke/vastgezette
indelingen behouden blijven, tokenruimte voldoende is en de eigenaar de in-game
uitkomst goedkeurt. Daarna plannen we Ranged en vervolgens Magic met dezelfde
gegevensstructuur en bewijsaanpak, maar hun eigen relevante stats en voorwaarden.
Deze planfase geeft geen opdracht tot committen, pushen of publiceren.
