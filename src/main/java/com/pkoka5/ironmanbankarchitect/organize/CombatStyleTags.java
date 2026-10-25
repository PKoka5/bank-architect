package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.OrderedItemFamilies;
import com.pkoka5.ironmanbankarchitect.catalog.RequiredResource;
import com.pkoka5.ironmanbankarchitect.organize.layout.GearSetSemanticRuleSet;
import java.util.*;

/** Optional destinations only; never changes equipment preference or disposal evidence. */
final class CombatStyleTags
{
	private static final String[] TAGS = {"combat-melee","combat-ranged","combat-magic"};
	private static final RequiredResource<Map<Integer,String>> EXACT = new RequiredResource<>("combat style groups", () -> {
		Map<Integer,String> facts = new HashMap<>();
		new OrderedItemFamilies(CombatStyleTags.class.getResourceAsStream("combat-style-tags.tsv"),0).entries().forEach((key,ids) -> {
			if (!key.equals("gear") && !Arrays.asList(TAGS).contains(key)) throw new IllegalArgumentException("Unknown combat style tag");
			for (int id : ids) facts.put(id,key);
		});
		return Collections.unmodifiableMap(facts);
	});
	private CombatStyleTags() { }

	static String forItem(BankPreviewItem item, GearStatsSource source)
	{
		String exact = EXACT.get().get(item.getItemId());
		if (exact != null) return exact;
		if (GearSetSemanticRuleSet.isCrystalArmour(item.getItemId())) return TAGS[1];
		String role = GearSetSemanticRuleSet.roleOf(item.getItemId());
		if (role.startsWith("support-")) return "gear";
		if (role.startsWith("weapon-")) return role.startsWith("weapon-ranged") ? TAGS[1]
			: role.startsWith("weapon-magic") ? TAGS[2] : TAGS[0];
		Optional<GearStats> stats = source.statsFor(item.getItemId());
		int slot = GearItemSorter.slotRankOf(item,source);
		if (stats.isPresent())
		{
			int count = 0, style = -1;
			for (int i=0;i<3;i++) if (stats.get().supports(GearStyle.values()[i])) { count++; style=i; }
			if (slot < 3 || stats.get().getSlot() == GearSlot.WEAPON)
			{
				if (count == 0 && !PrimaryGearSelector.candidate(item.getItemId(),slot)) return "gear";
				int primary = GearItemSorter.styleRankOf(item,source);
				return primary < TAGS.length ? TAGS[primary] : "gear";
			}
			return count == 1 ? TAGS[style] : "gear";
		}
		boolean ranged = PrimaryGearSelector.slot(GearStyle.RANGED,item.getItemId()) >= 0;
		boolean magic = PrimaryGearSelector.slot(GearStyle.MAGIC,item.getItemId()) >= 0;
		if (ranged != magic) return ranged ? TAGS[1] : TAGS[2];
		return slot < 3 && PrimaryGearSelector.candidate(item.getItemId(),slot) ? TAGS[0] : "gear";
	}
}
