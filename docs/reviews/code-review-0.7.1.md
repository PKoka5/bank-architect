# Code-review Bank Architect 0.7.1

Review op 2026-10-02 van commit `3cd61736a7e7779279299521ef5a41695c1c8757`.
Geen productiecode of bestaande tests gewijzigd; geen commit, push of publicatie.
De bevindingen hieronder zijn met afzonderlijke lokale voorbeelden gereproduceerd.

P1 betekent: gegevens kunnen worden overschreven; eerst oplossen.
P2 betekent: aantoonbaar verkeerd gedrag; meenemen in de volgende fixronde.

## Bevindingen

### 1. P1 — RuneLite-profielwisseling kan opgeslagen itemcorrecties overschrijven

Bron: [IronmanBankArchitectPlugin.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/IronmanBankArchitectPlugin.java),
regels 126, 295 en 710.

`categoryOverrides` wordt alleen bij het starten uit de configuratie geladen.
De plugin verwerkt geen `ProfileChanged` of `ConfigChanged`. Bij een wissel naar
een ander RuneLite-configuratieprofiel blijft daardoor de oude verzameling in het
geheugen staan. Een volgende correctie schrijft die oude verzameling naar het
nieuwe profiel en overschrijft diens correcties.

Reproductie met een config-testdubbel en de echte RuneLite EventBus:

```text
Profiel A: 2347=frequently-used
Profiel B vóór wijziging: 1755=tools
Wissel naar B; publiceer ProfileChanged; wijs spade 952 aan Tools toe.
Verwacht B: 1755=tools,952=tools
Werkelijk B: 2347=frequently-used,952=tools
```

RuneLite start een plugin die in beide configuratieprofielen ingeschakeld blijft
niet opnieuw op. Dat volgt uit de enable-statuscontrole in
[de officiële PluginManager](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/PluginManager.java#L114).
Dit betreft RuneLite-configuratieprofielen; een andere ingame-accountnaam alleen
is niet de geteste trigger.

Fix: laad correcties opnieuw bij een profielwisseling/relevante configuratiewijziging,
invalideer de oude analyse en publiceer het bijgewerkte aantal correcties. Plan dit
op de client thread en voorkom een analyse voor iedere interne setter.

### 2. P1 — Import kan een bestaand profiel met een naam van 40 tekens overschrijven

Bron: [IronmanBankArchitectPanel.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/IronmanBankArchitectPanel.java),
regel 1275; [BankLayoutShareCode.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/BankLayoutShareCode.java),
regel 82; [BankLayoutProfiles.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/BankLayoutProfiles.java),
regel 116.

De importfunctie maakt een vrije naam door ` 2` toe te voegen. Bij het opslaan
wordt deze naam opnieuw ingekort tot 40 tekens. Bij een oorspronkelijke naam van
40 tekens verdwijnt het achtervoegsel en wordt juist het bestaande profiel vervangen.
De implementatie van `BankLayoutProfiles.freeName()` heeft hetzelfde probleem.

```text
Bestaand: 1234567890123456789012345678901234567890 -> v1:original
Vrije importnaam: 1234567890123456789012345678901234567890 2
Na withProfile: oorspronkelijke naam -> v1:imported
Aantal profielen is niet toegenomen.
```

Fix: maak ruimte voor het achtervoegsel vóór de controle op vrije namen. Gebruik
één gedeelde naamallocator voor UI en opslag, en controleer de uiteindelijke
gesanitiseerde naam. Test ook behoud van gekoppelde item- en blockvolgordes.

### 3. P2 — Twee Graceful-kleuren kunnen Main eindeloos opnieuw laten sorteren

Bron: [MainQuickAccessSemanticRuleSet.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/layout/MainQuickAccessSemanticRuleSet.java),
regel 218.

Bij twee even complete recolours wint de set die als eerste in de banksnapshot
staat. Alleen de oorspronkelijke Graceful-set krijgt een expliciete voorkeur.
De gekozen set wordt in een kolom gelegd, waardoor de andere set na het sorteren
eerder kan verschijnen. De volgende analyse kiest dan die andere set.

Reproductie: volledige Arceuus- en Hosidius-sets, coins, hammer, chisel, spade,
rune pouch en voldoende runes/teleports; standaard Ironman-plan, geen correcties.
Een Main van 53 items wisselt bij opeenvolgende analyses tussen twee volgordes:
`eerste != tweede`, `derde == eerste`. Ook een volledige heranalyse met de andere
tabitems behouden reproduceert dit.

Fix: vergelijk bij gelijke compleetheid en gelijke basisvoorkeur ook een stabiele
set-key. Test heranalyse van de gesorteerde uitkomst en geschudde invoer.

### 4. P2 — Potion met een oude categoriecorrectie verdwijnt uit de blueprint

Bron: [BankOrganizationPreviewBuilder.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/BankOrganizationPreviewBuilder.java),
regel 248.

Bij `BY_FAMILY` wordt een gedeeltelijke potion aan de tag `potions` gekoppeld,
ook nadat een oude categoriecorrectie de uiteindelijke categorie heeft veranderd.
Het item belandt in een bucket waarvan de categorie en bestemming niet overeenkomen.
De tabopbouw neemt die bucket niet mee.

```text
Prayer potion(3), 139=skilling-tools, BY_FAMILY: 1 bankitem -> 0 preview-items
139=combat-gear en 139=herblore: hetzelfde probleem
139=tools (huidige tagcorrectie): 1 preview-item, correct
```

Met Attack potion(3) 121 en coins 995 blijft alleen 995 in de blueprint over.
Het echte bankitem wordt niet verwijderd; de incomplete blueprint verhindert
wel betrouwbare move guidance en vraagt opnieuw om een analyse.

Fix: forceer de potion-tag alleen als de uiteindelijke categorie nog `potions-food`
is. Test alle ondersteunde oude categoriecorrecties en behoud van het itemaantal.

### 5. P2 — Puntkomma in profielnaam beschadigt de opgeslagen naam

Bron: [BankLayoutShareCode.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/BankLayoutShareCode.java),
regel 75; opslagseparator in `BankLayoutProfiles.java`, regel 16.

De sanitizer laat `;` toe, terwijl profielopslag juist `;` als scheidingsteken
gebruikt. `Raid; prep` wordt opgeslagen als `Raid; prep~<plan>`. Bij de volgende
parse verdwijnt `Raid`, blijft `prep` over en valt het actieve profiel terug naar
de standaard. Item- en blockvolgordes kunnen hun koppeling met de profielnaam verliezen.
Een herstart is niet nodig: de volgende configuratielezing is al voldoende.

Fix: scheidingstekens consequent weigeren of encoderen. Neem bestaande beschadigde
opslag mee in de herstelstrategie; alleen namen achteraf inkorten repareert dit niet.

### 6. P2 — Klikken onderin een lange blueprint-tab reset de scrollpositie

Bron: [BlueprintEditorPanel.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/BlueprintEditorPanel.java),
regel 234; nieuw scrollpaneel op regel 332.

Iedere selectie vervangt de volledige grid en `JScrollPane`. In een test met
400 items springt de viewport bij het selecteren van item 300 van y=1400 naar y=0.
De groene selectie verdwijnt daarmee buiten beeld voordat de speler de doelslot
kan aanklikken. Dit veroorzaakt ook onnodige aanmaak van Swing-componenten.

Fix: behoud per tab de viewportpositie, of pas alleen gewijzigde cellen/borders aan.
Test selectie en swap/insert met een gescrollde viewport.

### 7. P2 — Bulkregel adviseert alching zonder bewezen vervangbaarheid

Bron: [BankOrganizationPreviewBuilder.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/organize/BankOrganizationPreviewBuilder.java),
regel 587.

De bedoelde uitzondering voor productievoorraad geldt voor alle ongetagde
draagbare stacks vanaf acht exemplaren, behalve weapons/ammo. Daarbij vervalt de
controle of een ander item op alle relevante stats minstens even goed is; één
hoger scorend item volstaat.

Met Torva platebody 26384 x1 en Dharok's platebody 4720, plus bonus- en alchwaarden
uit de lokale Wiki-infoboxsnapshot:

```text
Dharok tags=[]
Torva dominates Dharok=false
Dharok x3 -> combat-gear
Dharok x8 -> slayer-boss-loot (alchselectie)
```

Dit is een expliciet bedoelde quantity-uitzondering in de code, maar de toepassing
is breder dan de genoemde productie-items. Een hoge stack bewijst niet dat het
item geen andere voordelen heeft. De plugin adviseert alleen; hij alcht niets zelf.

Fix: beperk de productie-uitzondering tot onafhankelijk beoordeelde voorraaditems.
Behoud voor overige gear de dominantiecontrole en vereiste betere alternatieven.

### 8. P2 — Spiky vambraces worden als potions geclassificeerd

Bron: [registry-name-rules.tsv](../../src/main/resources/com/pkoka5/ironmanbankarchitect/catalog/registry-name-rules.tsv),
regel 5; [ResourceItemRegistry.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/catalog/ResourceItemRegistry.java),
regel 282.

IDs 10077, 10079, 10081, 10083 en 10085 worden onder een Engelse locale allemaal
`POTION/potion`. De visnaam `pike` matcht als substring in de constante
`SPIKED_VAMBRACES`. Het zijn hands-slot gear-items volgens de lokale canonical
Wiki-bonusgegevens. De live gear-statfallback corrigeert `POTION` niet.

Fix: nauwkeurige naamregels/woordgrenzen en/of exacte `GEAR/hands`-overrides voor
deze familie. Controleer vergelijkbare substring-botsingen voordat een brede
wijziging van de regelvolgorde wordt gekozen.

### 9. P2 — Itemclassificatie hangt af van de systeemtaal

Bron: `ResourceItemRegistry.java`, regels 212/246/254/276/282;
[ItemClassificationRefiner.java](../../src/main/java/com/pkoka5/ironmanbankarchitect/catalog/ItemClassificationRefiner.java),
regels 11–13 en 455.

`toLowerCase()` zonder vaste locale verandert onder Turks de letter I anders dan
onder Engels. De Engelse catalogusregels matchen daardoor andere items.
Afzonderlijke JVM-reproducties bevestigen:

| Item | en_US | tr_TR |
|---|---|---|
| Ice cooler 6696 | TOOL/slayer-tool | CLEANUP/cleanup |
| Bronze limbs 9420 | SKILLING/ammo-component | GEAR/gear |
| Iron limbs 9423 | SKILLING/ammo-component | GEAR/gear |
| Spiky vambraces 10077 | POTION/potion | GEAR/hands |

Fix: gebruik `Locale.ROOT` voor Engelse namen, constanten en semantische keys.
Test geïsoleerd in verschillende locales; singletoncatalogi moeten voor de test
na het instellen van de locale worden geladen.

## Efficiëntie en onderhoudbaarheid

- **Verouderde achtergrondanalyses overslaan.** `BankAnalysis.java:97` controleert
  de generation pas bij publicatie, niet vóór de achtergrondberekening. In een
  gecontroleerde queue leest een oude aanvraag nog de catalogus; ook na `close()`
  rekent een al ingeplande aanvraag verder. De juiste resultaten worden beschermd,
  maar onnodig werk kan de nieuwste aanvraag vertragen. Controleer `isLatestRequest`
  ook bij het begin van de worker. Een lopende berekening stopt daarmee nog niet.
- **Iconcache begrenzen.** `IronmanBankArchitectPlugin.java:771` bewaart ieder
  verschillend `itemId:quantity` tot shutdown. Wisselende hoeveelheden laten oude
  afbeeldingen staan. Gebruik een begrensde cache; quantity blind uit de key halen
  zou de getekende hoeveelheden veranderen.
- **Gearvergelijkingen alleen bouwen indien nodig.** De pass in
  `BankOrganizationPreviewBuilder.java:165` loopt ook bij `alchPile=false`.
  Beperk hem tot analyses die deze alchvergelijkingen daadwerkelijk gebruiken.
- **Profielnaamlogica delen.** UI en model hebben nu een eigen vrije-naamfunctie
  met dezelfde fout. Eén implementatie verhelpt de inconsistentie en bespaart code.

Dit zijn gerichte verbeteringen. Er is geen FPS- of heapbenchmark uitgevoerd;
er wordt daarom geen concrete procentuele snelheidswinst geclaimd.

## Verificatie en grenzen

- Bestaande 1.125 tests zijn groen op deze ongewijzigde revision. De Gradle-reviewrun
  hergebruikt de geslaagde testresultaten; de tests zijn niet gewijzigd.
- De 1.950 banksimulaties zijn opnieuw uitgevoerd: allemaal completed, alle vier
  bestaande baseline-hashes gelijk. De vaste simulaties gebruiken relatief kleine
  banken en dekken bovenstaande randgevallen niet.
- Afzonderlijke voorbeelden bevestigen alle negen bevindingen. Tijdelijke helpers
  en uitvoer staan onder het genegeerde `build/review-*`; niets daarvan wordt gebundeld.
- De profielwisseling gebruikt een config-testdubbel/EventBus; de editor een
  headless Swing-fixture. Er is geen nieuwe ingame testronde uitgevoerd.
- De alchreproductie gebruikt lokaal opgeslagen Wiki-stats; geen actuele ingame
  bonuscapture. De quantity/dominance-tegenstrijdigheid is met die vectors bevestigd.
- Hoogste lokale review-tokenraming blijft **196.973**, ruimte **3.027** tot 200.000,
  gekalibreerd op `def1e856e101ff0e57dd96adccab2cf1d886b076` / 200.414 van de maintainer.
  Dit is geen officiële Hub-telling. Dit rapport valt buiten de main-Java-raming.
- Een tijdelijk classpath-exportscript had eerst een verkeerd uitvoerpad; dat is
  gecorrigeerd. Dit was een review-helperfout, geen plugin-buildfout.
- Geen nieuwe bankautomatisering, game-statewijziging of runtime-netwerkfunctionaliteit.

## Aanbevolen volgorde

1. Bescherm profielwisseling en import tegen overschrijven.
2. Maak Graceful-keuze stabiel en behoud alle items bij oude potioncorrecties.
3. Herstel profielnamen en editor-scrollpositie.
4. Corrigeer de bulk-alchuitzondering, spiky vambraces en locale-afhankelijkheid.
5. Voeg de kleine efficiëntieverbeteringen toe en meet het tokenbudget opnieuw.

Na een fixronde: gerichte regressietests, volledige build/simulaties, ingame checks
voor profielwisseling/editor/guidance, en de review-size estimator vóór publicatie.
