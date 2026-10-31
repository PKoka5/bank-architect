# Sidebar mouse-wheel regression

The owner accepted the preceding review changes in game but reported that the
mouse wheel did not scroll the sidebar / Tab Layout. This is a local follow-up
above published `b3583ca`; nothing is committed or published.

## Cause and correction

Our sidebar owns a JScrollPane and a fixed identity header, but implicitly used
PluginPanel's default constructor, which adds another scrolling wrapper and
places the plugin at its preferred height. The inner pane then has no overflow
to scroll. Previous tests mounted the panel directly and missed the real wrapper.

The supported `super(false)` constructor disables the redundant RuneLite wrapper.
Our own pane receives the visible sidebar height and scrolls its content. Its
vertical unit increment is set to 16 pixels rather than the generic JPanel's
one-pixel default. No extra mouse listeners or global event interception is added.
The bank preview, real bank scrolling and blueprint dialog are unchanged.

The official cached RuneLite 1.13.4 PluginPanel API was inspected to verify the
wrapper behavior. No third-party plugin code, UI or layout was used.

## Regression evidence

The new `mouseWheelScrollsTheMountedSidebarWithoutMovingItsHeaderOrChangingPreset`
test mounts `getWrappedPanel()` as RuneLite does, opens Tab Layout, and dispatches
wheel events. It checks a scrollable visible viewport, readable movement in both
directions, an unchanged preset and a stationary header. It runs with both the
existing Swing theme and RuneLiteLAF. Before the production correction it failed
on "The visible sidebar must own a scrollable viewport"; after the correction it
passes. The full sidebar, release notice and header-action suites pass as well.

Logs: `build/tmp/sidebar-wheel-before.log`, `sidebar-wheel-after.log`,
`sidebar-wheel-full.log` and `sidebar-wheel-size.log` in the same directory.

Highest main-Java estimate: **196,980**, leaving **3,020 estimated tokens**.
Other estimates: 193,905 / 196,625 / 194,255. Calibration remains the maintainer's
200,414 at `def1e856e101ff0e57dd96adccab2cf1d886b076`. This is not the official
Hub count; the new test and documentation were inspected separately.

Final Java 11 offline build passes **2,400 tests**, zero failures/errors/skips,
**1,950 completed scenario banks**, and all four unchanged simulation baselines.
The build reports no compiler warning and whitespace checks pass.

The previous review-follow-up checkpoint's JAR identity is superseded. The local
development JAR (still named 0.9.9) has **937,178 bytes**, SHA-256
`b1ea04c7d3acaeb0c5d578d6be817f904167321d241285aba4e5e55b6e263636`.
It has not been uploaded and is not the published 0.9.9 artifact. HEAD remains
`b3583ca8e48bedb37514e89006cb677e5b7ea75a`.
The owner subsequently confirmed that mouse-wheel scrolling works in game
("top dat werkt!"). This completes the reported sidebar-scroll smoke check.
