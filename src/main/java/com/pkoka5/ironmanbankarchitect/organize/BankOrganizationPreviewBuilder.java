package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BankOrganizationPreviewBuilder
{
	private BankOrganizationPreviewBuilder()
	{
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset)
	{
		Objects.requireNonNull(snapshot, "snapshot");
		Objects.requireNonNull(catalog, "catalog");
		Objects.requireNonNull(preset, "preset");

		Map<String, MutableCategoryPreview> previewsByCategory = new LinkedHashMap<>();
		for (BankCategory category : preset.getCategories())
		{
			previewsByCategory.put(category.getKey(), new MutableCategoryPreview(category));
		}

		for (BankItemSnapshot bankItem : snapshot.getItems())
		{
			CatalogItem catalogItem = catalog.describeOrUnknown(bankItem.getItemId());
			BankCategory category = PresetCategoryMapper.map(preset, catalogItem);
			MutableCategoryPreview preview = previewsByCategory.get(category.getKey());
			if (preview == null)
			{
				throw new IllegalStateException("Preset mapper returned unknown category: " + category.getKey());
			}

			preview.add(catalogItem, bankItem.getQuantity());
		}

		List<BankCategoryPreview> categories = new ArrayList<>();
		for (MutableCategoryPreview preview : previewsByCategory.values())
		{
			categories.add(preview.toImmutable());
		}

		return new BankOrganizationPreview(preset, categories);
	}

	private static final class MutableCategoryPreview
	{
		private final BankCategory category;
		private final List<BankPreviewItem> items = new ArrayList<>();

		private MutableCategoryPreview(BankCategory category)
		{
			this.category = category;
		}

		private void add(CatalogItem catalogItem, int quantity)
		{
			items.add(new BankPreviewItem(catalogItem, quantity));
		}

		private BankCategoryPreview toImmutable()
		{
			return new BankCategoryPreview(category, PresetItemSorter.sort(category, items));
		}
	}
}
