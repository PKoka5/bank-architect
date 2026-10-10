# Dragging through long bank tabs

Reviewed on **10 October 2026** for Bank Architect **0.9.4**.

## Problem and change

When scrolling put both advised endpoints outside the bank viewport,
`BankGuideOverlay` drew only the yellow source direction. A player already
holding that item lost the blue destination marker, and the sidebar told them
to scroll back to FROM.

The overlay now checks RuneLite's observed drag state, bank widget ID, source
slot and canonical item ID. While the advised source is being dragged, it draws
the destination cell or a blue direction badge at the viewport edge. The
sidebar scroll hint follows DROP in Insert mode or TO in Swap mode. Cancelling
the drag restores the original source guidance.

This changes presentation only. Bank contents, route selection, transition
checks, filters, duplicate handling and live widget validation retain their
existing behaviour. The player still performs every bank action manually.

## Regression coverage

`DragScrollOverlayTest` exercises the actual overlay renderer and controller
with supported API fixtures and pixel/text assertions, parameterized over Swap
and Insert. Its 20 cases cover:

- natural scrolling in both directions, including both endpoints outside view;
- a visible target and a partly clipped target becoming fully visible;
- cancelling a drag and a click below the client's drag threshold;
- wrong source slot, item, interface, missing widget and invalid item ID;
- a placeholder source matched through its canonical identity;
- missing/hidden source geometry and changed bank contents blocking stale hints.

The focused run passes **65 tests**: 20 new render cases, 43 existing overlay
cases and two update-notice cases, with no failures, errors or skips.
Generated images in `build/reports/drag-scroll/` show the blue edge badge and
visible target for both modes. The update notice also fits a 204px sidebar.
Images and test-only API proxies are excluded from the plugin JAR.

The production patch received an independent code review with no blockers.
These checks are simulated renders; no live-client drag/scroll test is claimed.
The complete release build and artifact checks are recorded in
[release 0.9.4](../release-0.9.4.md).
