# Fixronde na de code-review van 0.7.1

Datum: 2026-10-02. Werkboom bovenop `3cd61736a7e7779279299521ef5a41695c1c8757`.
Versie blijft 0.7.1; deze fixronde is lokaal gebouwd, niet gecommit of gepubliceerd.
De oorspronkelijke bevindingen staan in [code-review-0.7.1.md](code-review-0.7.1.md).

## Opgeloste bevindingen

| Reviewpunt | Resultaat |
| --- | --- |
| 1. RuneLite-profielwisseling | Itemcorrecties worden uit de actuele configuratie gelezen. Een profielwisseling of relevante configuratiewijziging maakt de oude blueprint direct ongeldig. De client-thread bundelt vervolgwerk en de UI krijgt een refresh, ook bij dezelfde indeling zonder bankpreview. Tabbewerkingen lezen eerst het actuele plan. |
| 2. Import met een lange naam | UI en opslag delen één naamallocator. Achtervoegsels passen binnen de limiet van 40 tekens, zodat een import het bestaande profiel behoudt. |
| 3. Graceful-heranalyse | Bij even complete recolours bepaalt een vaste set-key de keuze. De voorkeur voor het basisset blijft behouden. Geschudde invoer en heranalyse van de gesorteerde bank leveren dezelfde Main-indeling. |
| 4. Oude potioncorrecties | BY_FAMILY forceert de potion-tag alleen binnen de uiteindelijke potioncategorie. Correcties naar andere oude categorieën behouden zowel bestemming als itemaantal. |
| 5. Puntkomma in profielnamen | Nieuwe namen sluiten de opslagseparator uit. Een nog bekende actieve oude naam wordt hersteld; profielwisseling en Save As slaan dat herstel duurzaam op. Gekoppelde item- en blockvolgordes blijven bereikbaar. Exacte bestaande itemorderkeys krijgen voorrang bij naamcollisies. |
| 6. Scrollpositie in de editor | Selecteren, Swap, Insert en Undo behouden de viewport; elke tab behoudt zijn eigen positie. |
| 7. Bulk-alchadvies | Een hoge hoeveelheid ongereviewde gear is onvoldoende. Gear moet daarnaast aantoonbaar gedomineerd worden door owned gear of expliciet beoordeeld zijn. Dharok blijft bij gear als Torva niet op alle relevante stats beter is. Beoordeelde productievoorraad blijft ondersteund. |
| 8. Spiky vambraces | Exacte GEAR/hands-correcties voor 10077, 10079, 10081, 10083 en 10085. |
| 9. Systeemtaal | Engelse classificatie en semantische keys gebruiken een vaste locale. Tests laden verse catalogi onder Engels en Turks. |

## Efficiëntie en onderhoud

- Verouderde achtergrondaanvragen beginnen niet meer aan de catalogusberekening.
- Gearvergelijkingen worden alleen opgebouwd voor Ironman-analyses met alchadvies ingeschakeld.
- De hoeveelheidafhankelijke iconcache bewaart maximaal 2.048 entries; aantallen blijven in de cachekey.
- Identieke substring-, woordgrens- en Locale.ROOT-normalisatiefuncties delen de originele helper `util/NameMatching.java`. Regelvolgorde en matching blijven gelijk. Bestaande Locale.ENGLISH-helpers vallen buiten deze mechanische samenvoeging.
- Geen concrete FPS- of heapwinst gemeten. Een analyse die al begonnen is wordt niet tussentijds onderbroken; haar verouderde resultaat kan wel niet publiceren.

## Verificatie

- `./gradlew.bat build --offline`: geslaagd.
- **1.151 tests**, nul failures, errors of skips. Het vorige totaal was 1.125.
- **1.950 banksimulaties completed**: 1.800 aggregate en 150 random scenarios.
- Alle vier bestaande simulatiebaselines onveranderd; geen hashes aangepast.
- `git diff --check`: geen whitespacefouten. Git geeft lokale LF/CRLF-normalisatiewaarschuwingen.
- Bestaande compilerwaarschuwing: `IronmanBankArchitectPanel.java` gebruikt unchecked/unsafe operations.
- De eerste compilaties vonden twee ontbrekende referenties tijdens de wijzigingen; die zijn opgelost. Eén nieuwe testfixture koos onbedoeld Default doordat zijn plan daaraan identiek was; de fixture gebruikt nu dezelfde aangepaste indeling voor beide profielen. De uiteindelijke volledige build is groen.
- Reflectie en config-/client-thread-testdubbels komen uitsluitend in tests voor. De productiecode krijgt geen bankautomatisering, game-statewijziging, netwerkverkeer of telemetry.

## Reviewtokenbudget

`npm.cmd run check --prefix tools/review-size`: geslaagd.
Kalibratie blijft de maintainer-telling **200.414** voor
`def1e856e101ff0e57dd96adccab2cf1d886b076` van 2026-09-30.

| Tokenizer en normalisatie | Lokale raming |
| --- | ---: |
| cl100k_base, whitespace behouden | 196.667 |
| cl100k_base, whitespace samengevoegd | 197.255 |
| o200k_base, whitespace behouden | 196.699 |
| o200k_base, whitespace samengevoegd | **197.297** |

Plan op de hoogste raming: **2.703 tokens ruimte** tot 200.000.
Dit is geen officiële Hub-telling en geen publicatiegoedkeuring.
De samenvoeging van identieke naamfuncties heeft ruimte teruggewonnen; inkorten
van comments is niet als tokenbesparing gebruikt.

Buiten de main-Java-scope zijn de vijf exacte catalogusoverride-rijen, gerichte
regressietests en deze lokale reviewdocumentatie geïnspecteerd. Tests en docs
worden niet met de pluginjar gebundeld; de resourcecorrecties wel. De estimator
kent de officiële reviewscope niet. Meet opnieuw op de uiteindelijke release en
verwerk een nieuwe maintainer-telling met haar exacte bronrevision.

## Ingame controle voor de volgende publicatie

1. Maak een itemcorrectie, wissel RuneLite-configuratieprofiel en maak daar een andere correctie. Controleer dat beide profielen hun eigen correcties en indeling behouden, ook met Tab Layout open.
2. Selecteer onderin een lange blueprint een item; voer Swap, Insert en Undo uit en wissel tabs. De scrollpositie en groene selectie moeten bruikbaar blijven.
3. Rond Main af met meerdere Graceful-recolours. Sluit en heropen de bank zonder wijzigingen; er mag geen nieuwe herschikking ontstaan.
4. Controleer een potion met een oude correctie onder BY_FAMILY, de spiky vambraces en behoud van niche gear bij bulk-alchadvies.

Beschadigde oude profielnamen kunnen alleen automatisch worden gereconstrueerd
als de oorspronkelijke actieve naam nog bekend is. Eerder overschreven indelingen
of onherkenbare losse fragmenten zijn niet uit de bestaande configuratie terug te halen.
Er is in deze ronde geen nieuwe ingame test uitgevoerd.
