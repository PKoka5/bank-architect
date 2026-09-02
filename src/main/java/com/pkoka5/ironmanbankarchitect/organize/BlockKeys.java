package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.catalog.ItemSortMetadata;
import com.pkoka5.ironmanbankarchitect.catalog.ResourceItemSortMetadataCatalog;
import com.pkoka5.ironmanbankarchitect.organize.layout.ItemSetCatalog;
import java.util.Optional;

/**
 * One shared identity for the blocks a player can arrange: the units the
 * sorters never split. Priority order is catalogued set membership, then a
 * charge/dose/servings family from the sort metadata, then a name family for
 * charge-suffixed potions and teleports, then the item itself. Both the
 * arrangement pass and the arrange editor derive keys through here, so what
 * the player drags is exactly what the layout moves.
 */
final class BlockKeys
{
	private BlockKeys()
	{
	}

	static String blockKeyOf(BankPreviewItem item)
	{
		Optional<String> setKey = ItemSetCatalog.setKeyOf(item.getItemId());
		if (setKey.isPresent())
		{
			return "set:" + setKey.get();
		}

		Optional<ItemSortMetadata> metadata =
			ResourceItemSortMetadataCatalog.INSTANCE.findById(item.getItemId());
		if (metadata.isPresent()
			&& metadata.get().getVariantKind() != ItemSortMetadata.VariantKind.NONE
			&& metadata.get().getFamilyKey() != null
			&& !metadata.get().getFamilyKey().isEmpty())
		{
			return "family:" + metadata.get().getFamilyKey();
		}

		if (item.getItemCategory() == ItemCategory.POTION
			|| item.getItemCategory() == ItemCategory.TELEPORT)
		{
			String stripped = strippedName(item);
			if (!stripped.equals(normalized(item.getDisplayName())))
			{
				return "name:" + stripped;
			}
		}

		return "item:" + item.getItemId();
	}

	/** What the arrange editor shows for the item's block. */
	static String blockNameOf(BankPreviewItem item)
	{
		Optional<String> setName = ItemSetCatalog.setNameOf(item.getItemId());
		if (setName.isPresent())
		{
			return setName.get();
		}

		String key = blockKeyOf(item);
		if (key.startsWith("family:") || key.startsWith("name:"))
		{
			String stripped = strippedName(item);
			return stripped.isEmpty() ? item.getDisplayName()
				: Character.toUpperCase(stripped.charAt(0)) + stripped.substring(1);
		}

		return item.getDisplayName();
	}

	/** The display name without a charge suffix or a half-portion prefix. */
	private static String strippedName(BankPreviewItem item)
	{
		String name = normalized(item.getDisplayName());
		if (name.startsWith("half a "))
		{
			name = name.substring("half a ".length());
		}
		return name.replaceFirst("\\s*\\([0-9]+\\)$", "");
	}

	private static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}
}
