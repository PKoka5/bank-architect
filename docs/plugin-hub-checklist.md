# Plugin Hub submission checklist

Reviewed locally: 2026-07-19

This checklist records the RuneLite Plugin Hub requirements as locally
understood for the Bank Architect submission package. It is not a permanent
substitute for the live rules. The maintainer must recheck the current
[Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md),
the current RuneLite rejected/rolled-back feature guidance, and all submission
CI feedback immediately before opening the submission pull request.

Status summary for this candidate: **30 done / 1 TODO-maintainer / 7
verify-at-submission**.

| # | Check | Status | Local evidence / required action |
|---:|---|---|---|
| 1 | Standard repository layout | done | Root Gradle files, wrapper, `src/main/java`, `src/main/resources`, and tests follow the Plugin Hub template shape. |
| 2 | Public repository is reachable | verify-at-submission | `origin` is `https://github.com/PKoka5/ironman-bank-architect.git`; confirm public visibility without maintainer credentials. |
| 3 | Supported Java/RuneLite baseline | done | Java 11 and `latest.release` match the current official setup guidance. |
| 4 | Standard Plugin Hub build type | done | `runelite-plugin.properties` declares `build=standard`; no custom submission build is required. |
| 5 | Runtime dependency boundary | done | Production has only RuneLite `compileOnly`; JUnit and the RuneLite test client are test-only. No dependency was added for Phase 6B. |
| 6 | Plugin properties are complete | done | `displayName`, `author`, `description`, `tags`, `plugins`, and `build` are populated; version is intentionally pending maintainer confirmation. |
| 7 | Plugin descriptor agrees with properties | done | Name, description, and tags match the Plugin Hub-facing metadata. |
| 8 | Single plugin entry point | done | One descriptor and one properties entry point: `IronmanBankArchitectPlugin`. |
| 9 | User-facing name and concise description | done | `Bank Architect`; 71-character description explains the Ironman blueprint and read-only manual guidance. |
| 10 | Current name/description uniqueness and limits | verify-at-submission | The current public guide does not state every enforced limit; recheck tooling/CI and collisions immediately before submission. |
| 11 | Sidebar/config terminology is consistent | done | Sidebar, tooltip, README, and configuration use Bank Architect, blueprint, and manual-move wording. |
| 12 | Configuration default reviewed locally | done | `Show next manual move` defaults on, but guidance itself still requires the user to enable the Bank Guide and never acts automatically. |
| 13 | Shipped preset claims are accurate | done | Only Ironman — All-Round Bank is described as selectable; Main/PvM/PvP/Skiller are explicitly unavailable. |
| 14 | README covers installation and complete workflow | done | Plugin Hub installation plus scan → blueprint → safe preparation → guided manual moves → completion are documented. |
| 15 | Original screenshots | done | Three maintainer-captured screenshots (2026-07-19) in `docs/screenshots/`: distribution guidance with sidebar, blueprint dialog, and sorting guidance. Title bars with the account name were cropped and chat areas blacked out before publication review. |
| 16 | Original Plugin Hub icon | done | Root `icon.png` (48×48, maintainer-approved 2026-07-19) is an original generated design — a gold coin landing in a blueprint bank grid — built from simple shapes with no Jagex or third-party art. The guide's 48×72 px limit is satisfied; recheck the limit at submission. |
| 17 | Read-only/manual-only contract is prominent | done | README states supported bank reads and explicitly excludes clicks, drags, typing, packets, widget/game-state mutation, and automated bank actions. |
| 18 | Forbidden implementation scan | done | Production scan found no reflection, JNI/native loading, `Runtime.exec`, `ProcessBuilder`, or external-process path. |
| 19 | Bundled resources are jar-safe | done | All seven production datasets are loaded with `getResourceAsStream`; runtime never assumes an unpacked resource path. |
| 20 | No runtime network or telemetry | done | No production network client, analytics, telemetry, or runtime dataset download exists. URL/URI text is pinned source-manifest validation only. |
| 21 | Fail-closed gates are disclosed | done | README documents All items, Swap mode, cleared search/tag filters, safe geometry, supported tab state, and plan consistency gates. |
| 22 | Local data behaviour is disclosed | done | README describes bundled pinned datasets, no runtime Wiki calls, and unknown/weak classification routing to Storage & Cleanup. |
| 23 | Current Jagex/RuneLite feature policy | verify-at-submission | Recheck current third-party-client and rejected/rolled-back guidance; respond to reviewer concerns rather than relying on this dated review. |
| 24 | Repository licence | done | Root `LICENSE` is BSD-2-Clause, the licence recommended by the current Plugin Hub guide, with the repository author/year. |
| 25 | Bundled third-party data provenance | done | Pinned source manifest records source URLs, retrieval dates, revisions, and licences; README links it. |
| 26 | User support channel | done | GitHub Issues on the plugin repository, published in the README with an explicit note that RuneLite does not provide support (maintainer-delegated decision, 2026-07-19). |
| 27 | Release version | done | Initial release version confirmed as `0.1.0` (the Gradle project value). The optional properties `version` field is deliberately omitted; Plugin Hub builds from the pinned commit (maintainer-delegated decision, 2026-07-19). |
| 28 | Final metadata/default approval | done | Name `Bank Architect`, the 71-character description, tags, and the enabled-by-default `Show next manual move` setting approved as-is (maintainer-delegated decision, 2026-07-19). |
| 29 | Jar contains production classes only | done | Built jar contains 179 production `.class` files and no test/simulator/research class. |
| 30 | Jar resource hygiene | done | Nine non-class files: manifest, properties, and seven required bundled datasets; no fixtures, reports, research caches, docs, screenshots, or tmp material. |
| 31 | Generated/scratch hygiene | done | `.gitignore` excludes Gradle/build output, local audit caches, and `tmp/`; these paths are absent from the jar. |
| 32 | Phase 6A release gate disposition | done | Maintainer waiver dated 2026-07-19 is recorded in the roadmap with mechanics-probe and 770-item real-bank substitute evidence. |
| 33 | Local candidate verification | done | Full tests, fixed simulation (150/150), and build pass on 2026-07-19; rerun after any later candidate change. |
| 34 | Screenshot requirement and presentation rules | verify-at-submission | The current Plugin Hub README requires a useful README but does not publish a hard screenshot format; check the live submission template/reviewer guidance. |
| 35 | Build/dependency verification rules | verify-at-submission | Confirm `build=standard` is still accepted and no transitive/runtime dependency has introduced a verification requirement. |
| 36 | Marker, pinned commit, and CI | verify-at-submission | Confirm the current marker format, use the final full commit hash, and require both build and Plugin Hub checks to pass. |
| 37 | Actual publication actions | TODO-maintainer | After approval, commit/push the candidate, fork/update Plugin Hub, and open the submission PR. This task performs none of those actions. |
| 38 | Final live-requirements refresh | verify-at-submission | Refresh every `verify-at-submission` row on the submission date and record any changed requirement before requesting review. |

## Maintainer TODOs

1. Commit and push the approved candidate, create the Plugin Hub marker at the
   final commit, and open the actual submission pull request.

Resolved 2026-07-19: the original `icon.png` (gold coin landing in a blueprint
bank grid) was generated in-repo from simple shapes and approved by the
maintainer. The three README screenshots were captured by the maintainer the
same day and redacted (account-name title bars cropped, chat areas blacked
out) before being added to `docs/screenshots/`.

No asset, support destination, release decision, commit, or pull request is
fabricated by this checklist.
