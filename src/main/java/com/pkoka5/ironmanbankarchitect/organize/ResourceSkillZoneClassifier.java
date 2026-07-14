package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import java.util.Optional;

/**
 * Assigns every bank item exactly one primary {@link ResourceSkillZone}.
 *
	 * <p>Curated metadata families decide first ({@code metal.*}, {@code wood.*}, {@code gem.*},
	 * {@code fletching.*});
 * narrow subcategory facts and the existing complete-suffix/token name rules follow. Secondary
 * skill use never moves an item: logs and planks stay Woodcutting, bars stay Mining/Smithing,
 * gems and glass stay Crafting, arrow and bow components stay Fletching, and nails stay
 * Construction. Bow and crossbow strings are exact Fletching components and are claimed before
 * the generic textile rule.</p>
 */
final class ResourceSkillZoneClassifier
{
	private ResourceSkillZoneClassifier()
	{
	}

	static ResourceSkillZone classify(BankPreviewItem item)
	{
		if (item.getItemId() == 32083 || item.getItemId() == 32085)
		{
			return ResourceSkillZone.WOODCUTTING;
		}
		if (item.getItemId() == 7936 || item.getItemId() == 24704)
		{
			return ResourceSkillZone.OTHER_RESOURCE;
		}
		Optional<ItemSortMetadata> metadata =
			ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId());
		if (metadata.isPresent())
		{
			String familyKey = metadata.get().getFamilyKey();
			if (familyKey.startsWith("metal."))
			{
				return ResourceSkillZone.MINING_SMITHING;
			}
			if (familyKey.startsWith("wood."))
			{
				return ResourceSkillZone.WOODCUTTING;
			}
			if (familyKey.startsWith("gem."))
			{
				return ResourceSkillZone.CRAFTING;
			}
			if (familyKey.startsWith("fletching."))
			{
				return ResourceSkillZone.FLETCHING;
			}
		}

		String name = ResourceItemSorter.normalized(item.getDisplayName());
		String subcategory = ResourceItemSorter.normalized(item.getSubcategory());

		if (ResourceItemSorter.isMiningResource(name))
		{
			return ResourceSkillZone.MINING_SMITHING;
		}
		if (ResourceItemSorter.isFishingResource(name))
		{
			return ResourceSkillZone.FISHING_COOKING;
		}
		if (ResourceItemSorter.isHunterResource(name, subcategory))
		{
			return ResourceSkillZone.HUNTER;
		}
		if (ResourceItemSorter.isSailingResource(name))
		{
			return ResourceSkillZone.SAILING;
		}
		if (subcategory.contains("ammo-component") || isExactFletchingString(name))
		{
			return ResourceSkillZone.FLETCHING;
		}
		if (subcategory.contains("construction") || ResourceItemSorter.isConstructionMaterial(name))
		{
			return ResourceSkillZone.CONSTRUCTION;
		}
		if (subcategory.contains("gem") || subcategory.contains("jewellery")
			|| subcategory.contains("glass-material") || subcategory.contains("crafting-material")
			|| subcategory.contains("textile"))
		{
			return ResourceSkillZone.CRAFTING;
		}
		if (ResourceItemSorter.isMetal(name))
		{
			return ResourceSkillZone.MINING_SMITHING;
		}
		if (ResourceItemSorter.isWood(name))
		{
			return ResourceSkillZone.WOODCUTTING;
		}
		if (ResourceItemSorter.containsAny(name, "hide", "leather") || name.endsWith(" fur")
			|| ResourceItemSorter.isGem(name)
			|| ResourceItemSorter.isGlassMaterial(name, subcategory)
			|| ResourceItemSorter.isTextile(name, subcategory))
		{
			return ResourceSkillZone.CRAFTING;
		}
		if (subcategory.contains("prayer") || ResourceItemSorter.containsWord(name, "bone")
			|| ResourceItemSorter.containsWord(name, "bones")
			|| ResourceItemSorter.containsWord(name, "remains")
			|| name.equals("ashes")
			|| (!name.equals("volcanic ash") && (name.endsWith(" ash") || name.endsWith(" ashes"))))
		{
			return ResourceSkillZone.PRAYER;
		}
		if (subcategory.contains("raw-food") || subcategory.contains("cooking")
			|| name.startsWith("raw ") || name.startsWith("leaping "))
		{
			return ResourceSkillZone.FISHING_COOKING;
		}
		return ResourceSkillZone.OTHER_RESOURCE;
	}

	private static boolean isExactFletchingString(String name)
	{
		return name.equals("bow string") || name.equals("crossbow string");
	}
}
