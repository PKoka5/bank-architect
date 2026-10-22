# Strategy contexts and crafting protection — 22 October 2026

Local development checkpoint above published 0.9.6 source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d`. Includes the earlier
[Ranged head correction](ranged-head-selection-2026-10-22.md), Wiki d'hide pilot
and selected GitHub fixes. No commit, push, merge or publication performed.

## Scoped change

The development-only [context exporter](../../tools/research/wiki-recommended-equipment/README.md)
preserves page/method/slot/conditions, unresolved variants and source evidence.
The [corpus report](../research/wiki-strategy-corpus-2026-10-22.md) records all
483 captured contexts and remaining manual-review boundaries. The 13-row original
context ledger has 12 columns, unique keys and no example IDs missing from the
registry. It is not a runtime item whitelist or automatic replacement graph.

`BankOrganizationPreviewBuilder.needsScurriusMace` supplies a bank-only veto for
Rune mace 1432: a real spine 28798 and no Bone mace 28792, including placeholder.
`BankAnalysis.retainedCategories` removes only this automatic retained decision
when the predicate is true. Explicit player choices and saved physical orders
retain priority. Other retained Alch decisions and quantity invalidation remain
unchanged. No new positive disposal rule or production resource is added in this
context pass, and the plugin makes no new network call.

## Verification

- Java 11 offline full build: **1,968 tests**, zero failures, errors or skips.
- **1,950 scenario banks** completed; all four simulation fingerprints unchanged.
- New `ScurriusMaterialProtectionTest`: 14 cases across Main and Ironman. Covers
  replaced stock as a control, real/placeholder ingredients and products, Rune
  mace quantities 25/1/0, depositing the spine after automatic Alch, repeated
  analysis, unrelated retained Alch, explicit Alch, valid saved destinations,
  captured physical order, product creation, and item/quantity conservation.
- Its deliberately synthetic complete stat vectors isolate the new routing
  policy. They are not a claim about live weapon balance or a recorded player bug.
  Recipe and exact identity evidence are separately pinned in the context review.
- The warm regression asserts the same layout context with unchanged Rune mace
  quantity, active placeholder retention and an unrelated Rune sword whose
  retained Alch differs from its fresh result. It therefore checks the retention
  bypass rather than only a cold builder invocation.
- Node exporter: **12 tests passed**, including exact versions, ambiguous names,
  links inside conditions/footnotes, method separation, deterministic export,
  source hashes, invalid inputs and no network use.
- Independent read-only review found and resolved an inactive Crystal bow example,
  ambiguous dependency-column semantics and non-atomic source revision wording.
  Production guard and override precedence review found no blocker.
- `git diff --check` passes. No new build warning; estimator output includes only
  Git line-ending notices. The prior sidebar unchecked-operation compiler note
  is unrelated to this change.

Logs: `build/tmp/wiki-context-build-2026-10-22.log` and
`build/tmp/wiki-context-review-size-2026-10-22.log`. Source downloads and raw corpus
remain ignored under `build/tmp/wiki-strategy-snapshot-2026-10-22/`.

## Review budget and artifact

Highest local main-Java estimate: **197,237**, leaving **2,763 estimated tokens**.
Other estimates: 195,217 / 196,876 (cl100k) and 195,568 (o200k preserved).
Calibration remains the maintainer's official 200,414 for
`def1e856e101ff0e57dd96adccab2cf1d886b076`; this is not an official Hub count or
release approval. Tests, tools, documents and previous resource changes were
inspected separately because the estimator does not count those inputs.

Development JAR: `build/libs/ironman-bank-architect-0.9.6.jar`, 1,116,210 bytes,
264 entries. No test, research, documentation, simulation or tooling entries.
SHA-256: `9d442a2f95437d2f4d7f063df0a8b07a41e7b2b01da9852388f53e95a7eba9b6`.
The filename retains 0.9.6 and differs from the published artifact. After the
requested Main/Ironman, Ranged-head, Alch-placeholder and blueprint-tab test round,
the owner reported: "top ziet er goed uit". This records owner acceptance of the
local checkpoint; individual live actions were not independently observed.
