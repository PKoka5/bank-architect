# September 22 placement corrections

Community report: Sunfire armour routed to Herblore; mixed Farmer outfit and
Eye boots split across columns; lantern requested beside Eye robes; Sunfire and
Aether runes outside the four-wide rune block.

- Exact canonical Sunfire armour IDs 28933/28936/28939 now classify as gear.
- Farmer male/female IDs share a head-to-feet family, keeping mixed variants together.
- Shared Eye boots and canonical lantern states attach to the Eye colour with the
  most owned robe pieces. Ties prefer table order (uncoloured first). No item is
  duplicated across colour sets. Lit lanterns now classify as Runecrafting tools.
- Rune rows include Sunfire 28929 and Aether 30843. Tiny banks still use dense
  fallback when a shaped block cannot fit; manual destinations/orders still win.

Regression tests cover both mixed Farmer variants, all four Eye colours with
unlit/normal/redwood lanterns, Sunfire/lantern destinations, and both added runes
in the full four-wide block. These are synthetic reproductions; the reporter's
complete bank/settings were not supplied.

## Reviewed simulation change

All 1,950 scenarios complete. Only four rows in `report.tsv` change:

| Seed | Scenario | Moves before → after | Relevant corrected item |
| --- | --- | --- | --- |
| 20260740 | SHUFFLED_NO_TABS | 95 → 94 | Sunfire fanatic chausses 28939 |
| 20260740 | RANDOM_TABS | 130 → 127 | Sunfire fanatic chausses 28939 |
| 20260751 | RANDOM_TABS | 216 → 212 | Abyssal lantern (oak logs) 26836 |
| 20260751 | NEARLY_SORTED | 33 → 34 | Abyssal lantern (oak logs) 26836 |

Reconstructed both seeded item samples to verify these IDs. Only swap counts
change; tab creation/distribution/transfer counts and completed outcomes remain
the same. Both cleanup reports and aggregate metadata are byte-identical.
Accepted the new `report.tsv` hash after this review; the other three remain unchanged.
The tool-family fingerprint was also updated for the reviewed data edits.
