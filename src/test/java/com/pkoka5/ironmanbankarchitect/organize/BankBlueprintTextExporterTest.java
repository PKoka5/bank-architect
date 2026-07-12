package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BankBlueprintTextExporterTest
{
	@Test
	public void exportsTabCoordinatesAndClassificationDetails()
	{
		BankCategory layoutCategory = new BankCategory("combat-gear", "Combat Gear");
		BankPreset preset = new BankPreset(BankPresetType.IRONMAN, "test", "Test preset", Arrays.asList(
			layoutCategory, layoutCategory, layoutCategory, layoutCategory, layoutCategory,
			layoutCategory, layoutCategory, layoutCategory, layoutCategory, layoutCategory));
		CatalogItem catalogItem = new CatalogItem(11832, "Bandos chestplate", ItemCategory.GEAR,
			"melee-body", Collections.singleton("melee"), null);
		BankCategoryPreview category = new BankCategoryPreview(layoutCategory, Arrays.asList(
			new BankPreviewItem(catalogItem, 1),
			BankPreviewItem.blank()));
		BankOrganizationPreview preview = new BankOrganizationPreview(preset, Collections.singletonList(category));

		String export = BankBlueprintTextExporter.export(preview);

		assertTrue(export.contains("TAB 1 | key=combat-gear | name=Combat Gear | items=1"));
		assertTrue(export.contains("row=1 col=1 slot=1 | id=11832 | name=Bandos chestplate | quantity=1"
			+ " | placeholder=false"
			+ " | catalogCategory=GEAR | subcategory=melee-body"));
		assertTrue(export.contains("row=1 col=2 slot=2 | EMPTY"));
	}

	@Test
	public void marksPlaceholderItemsWithoutInventingOwnedQuantity()
	{
		BankCategory layoutCategory = new BankCategory("potions-food", "Supplies");
		BankPreset preset = new BankPreset(BankPresetType.IRONMAN, "test", "Test preset", Arrays.asList(
			layoutCategory, layoutCategory, layoutCategory, layoutCategory, layoutCategory,
			layoutCategory, layoutCategory, layoutCategory, layoutCategory, layoutCategory));
		CatalogItem brew = new CatalogItem(6687, "Saradomin brew(3)", ItemCategory.POTION,
			"potion-dose-3", Collections.emptySet(), null);
		BankCategoryPreview category = new BankCategoryPreview(layoutCategory,
			Collections.singletonList(new BankPreviewItem(brew, 0, true)));

		String export = BankBlueprintTextExporter.export(
			new BankOrganizationPreview(preset, Collections.singletonList(category)));

		assertTrue(export.contains("name=Saradomin brew(3) | quantity=0 | placeholder=true"));
	}
}
