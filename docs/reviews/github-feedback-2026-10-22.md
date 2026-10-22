# GitHub feedback review — 22 October 2026

Repository main and both PR bases are published source
`d0c4348921d2ca71883f2dd9a5bcadaa4c1c015d` (0.9.6).
[Hub PR #18461](https://github.com/runelite/plugin-hub/pull/18461) merged on
14 October. Its thread supplies no new official token count.

## #46 and PR #47: Obsidian platebody

[Issue #46](https://github.com/PKoka5/bank-architect/issues/46) reports Rune
chainbody winning the primary Melee body position over Obsidian platebody while
the legs already favor Obsidian.

[PR #47](https://github.com/PKoka5/bank-architect/pull/47), reviewed head
`f5a8981129dad31edda92c7b23786227371b9d08`, adds exact item 21301 at tier 3.
This matches the legs and corrects the missing tier. Existing score weights give
Obsidian 846 versus Rune platebody 708 and Rune chainbody 675; Fighter torso
still wins at 882. No blocking defect was found in static review.

Before release, add actual Main/Ironman placement tests for the primary body
slot, replaced Rune bodies moving to Alch, Obsidian placeholders and manual
assignments. The submitted test checks scores rather than complete placement.
Obsidian helmet 21298 still lacks an exact tier; this is an existing follow-up,
not a regression introduced by this PR.

## #48 and PR #49: withdrawn Alch stock

[Issue #48](https://github.com/PKoka5/bank-architect/issues/48) describes Rune
longswords and other Alch stock returning to Combat when quantities fall or
placeholders remain, plus a shield/defender arrangement changing during gearing.

[PR #49](https://github.com/PKoka5/bank-architect/pull/49), reviewed head
`80b1ab56c199f5cb4ed3da13c2829977db4d4d6f`, consistently considers placeholders
and removes quantity-dependent eligibility and session history. Unknown-item,
known-role and manual-choice guards remain. The central direction addresses the
reported instability.

The PR also introduces a separate market-price policy for unreviewed gear:
positive GE price no greater than 1.25 times High Alchemy, HA at least 500 gp,
full dominance and two better alternatives. A singleton may now qualify; the
old bulk rule with one upgrade disappears. Reviewed IDs bypass the market gate.

**Change/decision needed:** repeated analyses can classify identical item IDs
differently when prices cross that threshold or become unavailable. For HA
10,000, GE 12,500 qualifies while 12,501 or 0 does not. Separate the withdrawal
fix from the new price strategy, or explicitly define stable routing across
price updates. Test missing/known/threshold-crossing price snapshots and the
reported quantity/placeholder sequences, including custom orders and overrides.

No PR check-runs or reviews were recorded for either inspected head. Review was
static; the PR code was not integrated or executed during this assessment.

## #45: custom columns

[Issue #45](https://github.com/PKoka5/bank-architect/issues/45) requests 16
columns with bank-resizing plugins. The specific plugins remain unnamed.

Eight-column assumptions exist in the semantic request/packer, curated Combat,
Tools, Main and Herblore shapes, editor, export and bank preview. Dense captured
orders survive, but their displayed row wrapping changes. A dropdown alone is
insufficient; width must be carried through layout, preview and profile context.

The current preview geometry proves only that eight columns fit and does not
reject a genuine wider grid. A preliminary compatibility safeguard could suppress
preview for a positively proven unsupported live grid, while retaining sidebar
planning. Do not infer actual column count solely from viewport width or a sparse
bank. Full support needs 8/16-column regressions and unchanged eight-column
baseline behavior, within the review-token budget.

## #43: local work

Whole blueprint-tab reordering is now implemented locally. See the
[validation record](../testing/blueprint-tab-reorder-2026-10-22.md): 1,881 tests,
1,950 completed scenario banks, all four reviewed baselines unchanged. It remains
open on GitHub until the feature is tested in game and published.

Recommended order: validate/integrate #47, refine #49, combine with the tested
#43 work for in-game review, then address #45 as a separate layout feature.

## Selected local implementation

The owner approved that selection. Obsidian's exact tier correction and the
reviewed-placeholder portion of #49 are implemented locally, with the old
unreviewed Alch guards and session history retained. No GE-price rule was added.
Neither PR was merged. Whole-tab reordering is included; wider grids stay deferred.

The [combined validation record](../testing/selected-alch-obsidian-2026-10-22.md)
reports 1,910 passing tests, 1,950 completed scenario banks and unchanged reviewed
baselines. Highest local token estimate is 196,968, leaving 3,032 estimated tokens
below the limit. The work awaits an in-game check and publication.
