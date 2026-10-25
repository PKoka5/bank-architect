package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemSortMetadataInvariantTest
{
	private static final int ITEM_ID = 171;

	@Test
	public void acceptsReviewedPotionDosesAndWorkflowStages()
	{
		for (int dose = 1; dose <= 4; dose++)
			catalog(metadata("potion.prayer", ItemSortMetadata.VariantKind.DOSE, dose))
				.requireVariant(ITEM_ID, "potion.prayer", ItemSortMetadata.VariantKind.DOSE, dose);
		for (int stage : new int[] {0, 1, 3})
			catalog(metadata("herb.ranarr", ItemSortMetadata.VariantKind.WORKFLOW_STAGE, stage))
				.requireVariant(ITEM_ID, "herb.ranarr", ItemSortMetadata.VariantKind.WORKFLOW_STAGE, stage);
	}

	@Test
	public void missingItemCannotValidateAgainstAnotherItemsMetadata()
	{
		ItemSortMetadataCatalog catalog = catalog(metadata("potion.prayer", ItemSortMetadata.VariantKind.DOSE, 3));
		IllegalStateException failure = assertThrows(IllegalStateException.class,
			() -> catalog.requireVariant(173, "potion.prayer", ItemSortMetadata.VariantKind.DOSE, 3));
		assertTrue(failure.getMessage().contains("173"));
	}

	@Test
	public void rejectsIncorrectFamilyEvenWhenKindAndValueAgree()
	{
		assertMismatch(metadata("potion.prayer", ItemSortMetadata.VariantKind.DOSE, 3),
			"potion.restore", ItemSortMetadata.VariantKind.DOSE, 3,
			"potion.prayer", "potion.restore", "DOSE", "3");
	}

	@Test
	public void rejectsIncorrectKindEvenWhenFamilyAndValueAgree()
	{
		assertMismatch(metadata("potion.prayer", ItemSortMetadata.VariantKind.CHARGE, 3),
			"potion.prayer", ItemSortMetadata.VariantKind.DOSE, 3,
			"potion.prayer", "CHARGE", "DOSE", "3");
	}

	@Test
	public void rejectsIncorrectValueEvenWhenFamilyAndKindAgree()
	{
		assertMismatch(metadata("potion.prayer", ItemSortMetadata.VariantKind.DOSE, 1),
			"potion.prayer", ItemSortMetadata.VariantKind.DOSE, 4,
			"potion.prayer", "DOSE", "value=1", "value=4");
	}

	@Test
	public void requiredCatalogFailurePropagatesWithoutBecomingAnOrdinaryMissingItem()
	{
		IllegalArgumentException cause = new IllegalArgumentException("invalid required metadata row");
		CatalogUnavailableException unavailable = new CatalogUnavailableException(cause);
		ItemSortMetadataCatalog catalog = id -> { throw unavailable; };
		for (int attempt = 0; attempt < 2; attempt++)
		{
			assertSame(unavailable, assertThrows(CatalogUnavailableException.class,
				() -> catalog.requireVariant(ITEM_ID, "potion.prayer", ItemSortMetadata.VariantKind.DOSE, 3)));
			assertSame(cause, unavailable.getCause());
		}
	}

	private static void assertMismatch(ItemSortMetadata actual, String expectedFamily,
		ItemSortMetadata.VariantKind expectedKind, int expectedValue, String... diagnosticFacts)
	{
		IllegalStateException failure = assertThrows(IllegalStateException.class,
			() -> catalog(actual).requireVariant(ITEM_ID, expectedFamily, expectedKind, expectedValue));
		String message = failure.getMessage();
		assertTrue("diagnostic identifies the failing item", message.contains(Integer.toString(ITEM_ID)));
		for (String fact : diagnosticFacts)
			assertTrue("diagnostic must retain " + fact + ": " + message, message.contains(fact));
	}

	private static ItemSortMetadataCatalog catalog(ItemSortMetadata metadata)
	{
		return id -> id == metadata.getItemId() ? Optional.of(metadata) : Optional.empty();
	}

	private static ItemSortMetadata metadata(String family, ItemSortMetadata.VariantKind kind, int value)
	{
		return new ItemSortMetadata(ITEM_ID, family, kind, value,
			ItemSortMetadata.FoodRole.NONE, ItemSortMetadata.HealModel.NONE, 0, 0, 0,
			ItemSortMetadata.AreaRestriction.NONE, "test.reviewed");
	}
}
