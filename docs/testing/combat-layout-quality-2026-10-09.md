# Combat layout quality — 2026-10-09

The owner approved improving the Combat tab after the real-bank simulation
exposed caster-role errors, unsuitable fillers and incomplete variant families.
These changes are local and unreleased, alongside the earlier gear audit.

## Behavior

- Casting weapons use their Magic role before their raw melee attack bonuses.
  The same decision supplies the weapon lane and both sides of Alch's comparison
  key. Other equipment retains stat-based style; Blue moon spear is explicitly
  included as a casting weapon.
- Placement scoring uses the selected style's attack/strength bonuses and
  includes Magic damage in tenths of a percent. Prayer and defence remain
  secondary scoring evidence. This is an all-round placement preference, not
  a damage calculator or proof that a weaker-ranked item should be alched.
  Full-vector dominance and the existing replacement/ownership guards remain.
- Only loose items matching the equipment row can fill aligned rows. Rings,
  ammunition, cannon parts, reserved best pieces and secondary-family members
  cannot pad those rows. The first nonempty row that cannot be completed ends
  alignment; later shields/weapons cannot jump ahead of compact armour.
- Every actual best candidate is reserved, including Prayer weapons. A compact
  remainder stays in style/slot order. Cannon geometry retains its boundary
  protection, which can place the cannon before compact wearables.
- Remaining sets sort by armour style, strength and deterministic identity.
  Each style section is packed at its actual physical start column independently.
  A shape is accepted only if each owned family remains an ordered run or filled
  column-major rectangle; otherwise its dense grouped order is retained.
- Explicit assignments and saved/captured orders still apply last. Quantity
  never decides setup priority; placeholders continue representing the planned kit.

## Reviewed item data

The override table gains **71 ordinary exact-ID role corrections**:

- 42 skill/achievement/music/max hoods: Cosmetics, including combination max
  hoods and Sailing. Functional skillcapes remain under their existing roles.
- Five Kourend Treasure Trail banners: Cosmetics. Western diary banners keep
  their existing utility/teleport treatment.
- Cape pouch: Tools, as a Forestry basket upgrade container.
- 17 event costumes: Corrupted armour (5), Ornate (6), Robes of Ruin (6).
  Corrupted and Ornate have real low-tier combat bonuses; their Cosmetics
  placement is the product's event-costume policy, not a zero-stat rule.
- Dark flippers and Flamtaer bracelet: skilling utility. Karamja gloves 1/2:
  achievement utility; gloves 3/4: Teleports.

Seven coloured Crystal families add **42 ordinary active/inactive IDs**, ordered
helm/body/legs, with states adjacent within each slot. These remain functional
equipment. Actual inactive stats are retained; no active stats are synthesized.
CERT, placeholder, Deadman and Last Man Standing IDs were not added.

Primary Wiki references, checked on October 9:

- [Magic weapons](https://oldschool.runescape.wiki/w/Magic_weapons),
  [Iban's staff](https://oldschool.runescape.wiki/w/Iban%27s_staff),
  [Blue moon spear](https://oldschool.runescape.wiki/w/Blue_moon_spear).
- [Cape of Accomplishment](https://oldschool.runescape.wiki/w/Cape_of_Accomplishment),
  [Cooking hood](https://oldschool.runescape.wiki/w/Cooking_hood),
  [Crafting hood](https://oldschool.runescape.wiki/w/Crafting_hood).
- [Lovakengj banner](https://oldschool.runescape.wiki/w/Lovakengj_banner),
  [Shayzien banner](https://oldschool.runescape.wiki/w/Shayzien_banner),
  [Cape pouch](https://oldschool.runescape.wiki/w/Cape_pouch).
- [Corrupted armour](https://oldschool.runescape.wiki/w/Corrupted_armour),
  [Ornate armour](https://oldschool.runescape.wiki/w/Ornate_armour),
  [Robes of Ruin](https://oldschool.runescape.wiki/w/Robes_of_Ruin).
- [Dark flippers](https://oldschool.runescape.wiki/w/Dark_flippers),
  [Flamtaer bracelet](https://oldschool.runescape.wiki/w/Flamtaer_bracelet),
  [Karamja gloves 3](https://oldschool.runescape.wiki/w/Karamja_gloves_3),
  [Karamja gloves 4](https://oldschool.runescape.wiki/w/Karamja_gloves_4).
- Coloured Crystal helm/body/legs pages for Hefin, Ithell, Iorwerth, Trahaearn,
  Cadarn, Crwys and Amlodd; for example
  [Hefin helm](https://oldschool.runescape.wiki/w/Crystal_helm_(Hefin)),
  [body](https://oldschool.runescape.wiki/w/Crystal_body_(Hefin)),
  [legs](https://oldschool.runescape.wiki/w/Crystal_legs_(Hefin)).

The family table now has 77 rows; its canonical LF SHA-256 is
`dda60c07bbb30d6f1194f2cf2d27ac31d27b3cf5e298bd9b2584e017ed4af11d`.

## Verification and baseline review

- Full JUnit run: **1,544 tests**, 153 suites, zero failures/errors/skips.
- Final offline `build` passes, including all four simulation baseline checks.
- Browser preview controls work for both presets, Combat and Alch; the reviewed
  previews load every embedded item icon. The busy scenario keeps all four cannon
  pieces together. This browser view is a development simulation, not an in-game test.
- The built `ironman-bank-architect-0.9.2.jar` contains production classes and the
  exact source resource tables; development runners, HTML, caches, research and
  tests are absent. `git diff --check` passes.
- Fixed random simulation: **150 COMPLETE**; aggregate: **1,800 COMPLETE**,
  zero unsupported/build-error/blocked/stalled/nonterminating outcomes.
- Twelve actual preview simulations pass ID, quantity, placeholder, reviewed
  secondary-family and cannon preservation checks. Equipment and Alch data
  fingerprints are unchanged from the earlier visual run.
- Focused coverage checks caster role/weapon lane/Alch-key consistency, relevant
  stats/Magic damage, sparse row behavior, style-section geometry, all 71 role
  overrides, all 42 coloured Crystal IDs, explicit pin reload/clear and
  placeholder withdrawal/return stability in both presets.

Compared with `build/tmp/combat-quality-before`, the fixed report changes only
20 rows' move/swap/minimum counters, and transfers in two of them. All item/tab
counts, outcomes and other counters remain unchanged. Total moves 12,171 to
12,151; swaps 5,334 to 5,316; transfers 980 to 978. All 149 actual sorting runs
still match the exact swap lower bound. The unchanged no-sort run retains its
documented -1 minimum sentinel. Different target layouts mean this does not
establish a general speed improvement.

The single cleanup report is unchanged. Aggregate cleanup removes only
Shayzien banner (6 occurrences), Lovakengj banner (3), Piscarilius banner (3)
and Dark flippers (3); no rows are added or otherwise changed. Only the
corresponding metadata totals change: distinct IDs 9,277 to 9,273, occurrences
46,461 to 46,446. The three changed canonical CRLF report fingerprints were
updated in `build.gradle` after independent content reviews.

The final real-bank preview counts are:

| Input | Main Combat / Alch | Ironman Combat / Alch |
| --- | --- | --- |
| Partial friend's export, 323 complete entries | 170 / 3 | 168 / 3 |
| Complete owner's export, 821 entries | 114 / 20 | 107 / 20 |

All twelve previews keep exactly the prior Alch membership/quantities/placeholders.
Combat removals are the reviewed role corrections; those items remain elsewhere
in the complete blueprint. The friend's export remains truncated, so it cannot
prove the outcome for missing parts of that bank. This is simulation, not a
running-client screenshot; worn/charged ItemManager canonicalization is not
reconstructed by the development exporter.

## Review-token planning

Highest current main-Java estimate: **196,797**, leaving **3,203** estimated tokens.
Other estimates: 195,545 / 196,763 / 195,557. Calibration remains the official
200,414 count for `def1e856e101ff0e57dd96adccab2cf1d886b076`.

Resources/tests/research/docs are outside this estimator's scope. Separately
reviewed resource growth includes the 71 exact corrections and seven six-ID
families above; prior local audit additions also remain unreleased. No new
production dependency or external-call capability was added. Research runners,
cached public data, sprites and conversation visualizations remain development
files outside the plugin sources/JAR. The estimator is not the official Hub
count and does not certify a submission.

Run an in-game visual check before publishing. Compact gear intentionally takes
over when suitable fillers are unavailable. This audit improves reviewed items
and the general rules; it does not claim every game item or boss-specific setup
has been exhaustively verified.
