package com.pkoka5.ironmanbankarchitect.research;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CatalogItem;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankCategoryPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.BankPreviewItem;
import com.pkoka5.ironmanbankarchitect.organize.BankTags;
import com.pkoka5.ironmanbankarchitect.organize.CategoryOverrideSource;
import com.pkoka5.ironmanbankarchitect.organize.GearStatsSource;
import com.pkoka5.ironmanbankarchitect.organize.ItemValueSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/** Expectations are reviewed facts, maintained separately from generated placement observations. */
public class ReviewedItemRolesTest
{
	private static final String RESOURCE = "/com/pkoka5/ironmanbankarchitect/research/reviewed-item-roles.tsv";
	private static final String HEADER = "itemId\tname\tcatalogCategory\tcatalogSubcategory\tironmanTag"
		+ "\tmainTag\tironmanTab\tmainTab\tsourceUrl\tsourceRevision\treviewedOn\trationale";

	@Test
	public void reviewedReferenceHasStrictSchemaProvenanceAndCurrencyCounterexamples() throws Exception
	{
		List<ReviewedRole> roles = readRoles();
		assertFalse("The independently reviewed reference must not be empty", roles.isEmpty());
		assertTrue("Keep positive examples of actual currency in the reviewed reference",
			roles.stream().anyMatch(role -> role.category == ItemCategory.CURRENCY
				&& "currency".equals(role.ironmanTag) && "currency".equals(role.mainTag)));
		assertTrue("Keep counterexamples that must not be mislabeled as currency",
			roles.stream().anyMatch(role -> role.category != ItemCategory.CURRENCY
				&& !"currency".equals(role.ironmanTag) && !"currency".equals(role.mainTag)));
	}

	@Test
	public void catalogAndActualDefaultBlueprintsMatchIndependentlyReviewedRoles() throws Exception
	{
		for (ReviewedRole role : readRoles())
		{
			String context = role.itemId + " " + role.name;
			CatalogItem catalog = CompositeItemCatalog.DEFAULT.findById(role.itemId)
				.orElseThrow(() -> new AssertionError("Reviewed item is missing from catalog: " + context));
			assertEquals(context + " catalog name", role.name, catalog.getDisplayName());
			assertEquals(context + " catalog category", role.category, catalog.getCategory());
			assertEquals(context + " catalog subcategory", role.subcategory, catalog.getSubcategory());
			assertPlacement(role, BankPresets.IRONMAN, role.ironmanTag, role.ironmanTab);
			assertPlacement(role, BankPresets.MAIN, role.mainTag, role.mainTab);
		}
	}

	private static void assertPlacement(ReviewedRole role, BankPreset preset, String expectedTag,
		int expectedTab)
	{
		String context = role.itemId + " " + role.name + " / " + preset.getType();
		BankSnapshot bank = new BankSnapshot(Collections.singletonList(new BankItemSnapshot(role.itemId, 1, 0)));
		BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(bank, CompositeItemCatalog.DEFAULT,
			preset, GearStatsSource.NONE, ItemValueSource.NONE, CategoryOverrideSource.NONE,
			BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset));
		assertEquals(context + " must remain exactly one owned physical bank item", 1, preview.getPlannedItemCount());
		BankPreviewItem found = null;
		int physicalTab = -1;
		List<BankCategoryPreview> destinations = preview.getCategories();
		assertEquals(context + " default layout destination count", BankLayoutPlan.DESTINATION_COUNT, destinations.size());
		for (int tab = 0; tab < destinations.size(); tab++)
		{
			for (BankPreviewItem item : destinations.get(tab).getItems())
			{
				if (!item.isBlank() && item.getItemId() == role.itemId)
				{
					assertTrue(context + " must not be duplicated in the final blueprint", found == null);
					found = item;
					physicalTab = tab;
				}
			}
		}
		assertNotNull(context + " must be present in the final blueprint", found);
		assertEquals(context + " effective name", role.name, found.getDisplayName());
		assertEquals(context + " effective category without runtime stats", role.category, found.getItemCategory());
		assertEquals(context + " effective subcategory without runtime stats", role.subcategory, found.getSubcategory());
		assertEquals(context + " final layout tag", expectedTag, found.getLayoutTagKey());
		assertEquals(context + " physical destination (0=Main)", expectedTab, physicalTab);
		assertFalse(context + " is a real owned item in this scenario", found.isPlaceholder());
	}

	private static List<ReviewedRole> readRoles() throws Exception
	{
		InputStream stream = ReviewedItemRolesTest.class.getResourceAsStream(RESOURCE);
		assertNotNull("Missing independently reviewed item reference " + RESOURCE, stream);
		List<ReviewedRole> roles = new ArrayList<>();
		Set<Integer> itemIds = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String header = reader.readLine();
			assertNotNull("Reviewed reference requires its explicit schema header", header);
			assertEquals("Reviewed reference schema", HEADER, header.replace("\uFEFF", ""));
			String[] fieldNames = HEADER.split("\t", -1);
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null)
			{
				lineNumber++;
				if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
				String context = "Reviewed reference line " + lineNumber;
				String[] cells = line.split("\t", -1);
				assertEquals(context + " field count", fieldNames.length, cells.length);
				for (int field = 0; field < cells.length; field++)
				{
					assertFalse(context + " requires " + fieldNames[field], cells[field].trim().isEmpty());
					assertEquals(context + " has surrounding whitespace in " + fieldNames[field],
						cells[field].trim(), cells[field]);
				}
				assertTrue(context + " requires a positive canonical item ID", cells[0].matches("[1-9][0-9]*"));
				int itemId = Integer.parseInt(cells[0]);
				assertTrue(context + " duplicate item ID " + itemId, itemIds.add(itemId));
				ItemCategory category;
				try
				{
					category = ItemCategory.valueOf(cells[2]);
				}
				catch (IllegalArgumentException ex)
				{
					throw new AssertionError(context + " has invalid catalogCategory " + cells[2], ex);
				}
				assertTrue(context + " unknown Ironman tag " + cells[4], BankTags.isKnown(cells[4]));
				assertTrue(context + " unknown Main tag " + cells[5], BankTags.isKnown(cells[5]));
				assertTrue(context + " Ironman destination must be 0..9", cells[6].matches("[0-9]"));
				assertTrue(context + " Main destination must be 0..9", cells[7].matches("[0-9]"));
				URI source = URI.create(cells[8]);
				assertTrue(context + " sourceUrl must be an absolute HTTP(S) source",
					source.isAbsolute() && ("https".equals(source.getScheme()) || "http".equals(source.getScheme()))
						&& source.getHost() != null && !source.getHost().isEmpty());
				assertTrue(context + " reviewedOn must use YYYY-MM-DD", cells[10].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"));
				try
				{
					LocalDate.parse(cells[10]);
				}
				catch (RuntimeException ex)
				{
					throw new AssertionError(context + " has invalid reviewedOn " + cells[10], ex);
				}
				roles.add(new ReviewedRole(itemId, cells[1], category, cells[3], cells[4], cells[5],
					Integer.parseInt(cells[6]), Integer.parseInt(cells[7])));
			}
		}
		return roles;
	}

	private static final class ReviewedRole
	{
		private final int itemId;
		private final String name;
		private final ItemCategory category;
		private final String subcategory;
		private final String ironmanTag;
		private final String mainTag;
		private final int ironmanTab;
		private final int mainTab;

		private ReviewedRole(int itemId, String name, ItemCategory category, String subcategory,
			String ironmanTag, String mainTag, int ironmanTab, int mainTab)
		{
			this.itemId = itemId;
			this.name = name;
			this.category = category;
			this.subcategory = subcategory;
			this.ironmanTag = ironmanTag;
			this.mainTag = mainTag;
			this.ironmanTab = ironmanTab;
			this.mainTab = mainTab;
		}
	}
}
