# Ironman Bank Architect — agent instructions

## Identity
This is an original RuneLite plugin owned by this repository.
Package root: com.pkoka5.ironmanbankarchitect

Do not copy, import, adapt, or mirror code, UI, resources, naming, layouts, configuration, or project structure from Bank Templates or any other third-party plugin.

Standard RuneLite API usage and original code written in this repository are allowed.

## Product goal
Build an Ironman-oriented bank blueprint and organization assistant.

The plugin may:
- read the player's bank through supported RuneLite APIs;
- create and save local layout blueprints;
- show a sidebar planner, checklist, and manual organization guidance.

The plugin must not:
- automate mouse, keyboard, clicks, drags, packets, or bank actions;
- manipulate game state;
- use reflection, Runtime.exec, native code, external processes, network calls, or telemetry;
- read inventory or equipment unless a later documented feature explicitly allows it.

The player always moves bank items manually.

## Workflow
- Explain proposed files and changes before editing.
- Keep changes small and focused.
- Do not commit, push, merge, or change branches unless explicitly asked.
- Only one coding agent edits production files at a time.
- Claude is implementation/tests; Codex is research/design/review.
- Run tests after code changes and report warnings or failures.

## Plugin Hub review token budget
- Keep every Plugin Hub submission strictly below 200,000 review tokens. Treat this as a standing requirement for all future updates.
- Preserve several thousand tokens of headroom; simplify repeated code as features grow without removing functionality or necessary tests.
- Comments and JavaDoc are removed by the Hub before tokenization. Shortening them does not reduce the review token count.
- Before publishing code updates, run the development-only estimator documented in `tools/review-size/README.md` and inspect changes outside its main-Java scope separately.
- Local estimates are not the official Hub count. Use the highest estimate for planning, report uncertainty, and do not treat a passing build as token-limit approval.
- When a maintainer provides a new count, record its exact source revision and use that pair to recalibrate subsequent estimates.
