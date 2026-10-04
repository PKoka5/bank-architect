# Analysefout met fishing tools - 4 oktober 2026

De gebruiker deelde een Reddit-bericht van mushroman69: een bank met fishing rod,
small/big fishing net, lobster pot, fly fishing rod of tinderbox kon niet worden
geanalyseerd. De meegestuurde fout was:

```text
Semantic layout failed for category skilling-tools:
[LOCK_TARGET_OUT_OF_RANGE(itemId=305): locked target 32 is outside 0..29]
```

## Reproductie en fix

De publieke previewbuilder reproduceerde exact dezelfde fout met een Tools-tab
van 30 items: vier Angler-stukken, de zes genoemde tools en twintig bestaande
catalogusitems die in de fixture aan Tools zijn toegewezen. Een kleinere bank
reproduceerde dezelfde fout met een bereik van 0..6. Er is geen private bankexport
van de melder gebruikt.

`ToolOutfitSemanticRuleSet.anchoredEntries()` reserveerde het begin van een
toolgroep direct onder de hoogste outfitkolom. Vier outfitstukken betekenen
vier rijen, dus doelindex 32 op het raster van acht kolommen. Die rij hoeft in een
kleine, dichte tools-tab niet te bestaan.

De gegenereerde toolankerregel geldt nu alleen als `maxOutfitHeight * 8 < entries.size()`.
Een passende ankerpositie blijft behouden. De bestaande planner groepeert de
overige items binnen de beschikbare tab; alle items blijven aanwezig.
De strikte validatie en expliciet aangeleverde locks blijven actief.

## Verificatie

- Drie gerichte tests faalden voor de fix, waaronder de exacte gemelde fouttekst.
- Vijf nieuwe regressietests dekken de gemelde tools, kleine tabs, gedeeltelijke
  en grotere outfits, de grens van 32 naar 33 items, geschudde invoer, Grid/List
  en Frequently Used aan/uit. De publieke previewchecks gebruiken de echte catalogus.
- `./gradlew.bat build --offline`: geslaagd; **1.156 tests**, nul failures, errors of skips.
- **1.950 banksimulaties completed**; alle vier bestaande baseline-hashes gelijk.
- `git diff --check`: geslaagd; alleen lokale LF/CRLF-normalisatiewaarschuwingen.
- De eerdere [0.7.1-reviewfixes](code-review-fixes-0.7.1.md) zitten in dezelfde werkboom en build.

## Tokenbudget en status

`npm.cmd run check --prefix tools/review-size`: hoogste lokale raming **197.309**,
dus **2.691 tokens ruimte** tot 200.000. Dit is 12 geschatte tokens meer dan voor
deze fix. Kalibratie: maintainer-telling 200.414 voor
`def1e856e101ff0e57dd96adccab2cf1d886b076` van 2026-09-30.
De officiële Hub-telling en reviewscope blijven onbekend.

Buiten de main-Java-raming zijn alleen de twee regressietestbestanden en dit
rapport toegevoegd of gewijzigd voor deze incidentfix. Deze worden niet met de
pluginjar gebundeld. Er zijn geen resource- of dependencywijzigingen nodig.

De fix staat lokaal; geen commit, push, Hub-publicatie of Reddit-bericht uitgevoerd.
Ingame verificatie blijft open: laat de genoemde items in de bank staan en voer
Analyze Bank uit. Controleer ook de eerdere profiel-, editor- en heropeningschecks.
