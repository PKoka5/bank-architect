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
