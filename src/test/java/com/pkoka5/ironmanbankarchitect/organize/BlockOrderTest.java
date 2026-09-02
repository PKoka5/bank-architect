package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The block-order law: curated output stands byte for byte until the player
 * saves an arrangement, and from then on their block sequence wins, newcomers
 * anchoring after their nearest curated predecessor.
 */
public class BlockOrderTest
{
	private static final String PARTYHATS = "set:cosmetic-family.partyhats";
	private static final String DYES = "set:cosmetic-family.dyes";
	private static final String FIRELIGHTERS = "set:cosmetic-family.firelighters";
	private static final String KITTEN = "item:1559";

	// Blue and red partyhats, blue and red dye, the kitten.
	private static final BankSnapshot COSMETICS = new BankSnapshot(Arrays.asList(
		new BankItemSnapshot(1042, 1, 0),
		new BankItemSnapshot(1038, 1, 1),
		new BankItemSnapshot(1767, 1, 2),
		new BankItemSnapshot(1763, 1, 3),
		new BankItemSnapshot(1559, 1, 4)));

	@Test
	public void withoutAnArrangementTheCuratedOrderStandsByteForByte()
	{
		BankLayoutOptions unrelated = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("teleports", Arrays.asList("item:8007")));

		assertEquals(idsOn(build(COSMETICS, BankLayoutOptions.DEFAULTS), cosmeticsTab()),
			idsOn(build(COSMETICS, unrelated), cosmeticsTab()));
	}

	@Test
	public void aSavedSequenceReordersWholeBlocks()
	{
		BankLayoutOptions arranged = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("cosmetics",
				Arrays.asList(DYES, KITTEN, PARTYHATS)));

		assertEquals(Arrays.asList(1767, 1763, 1559, 1042, 1038),
			idsOn(build(COSMETICS, arranged), cosmeticsTab()));
	}

	/**
	 * A block the player never saw slots in after its nearest curated-order
	 * predecessor among the arranged blocks, not at the tail: the firelighter
	 * follows the partyhats because curation files it after them.
	 */
	@Test
	public void aNewcomerBlockAnchorsAfterItsCuratedPredecessor()
	{
		List<BankItemSnapshot> withFirelighter = new ArrayList<>(COSMETICS.getItems());
		withFirelighter.add(new BankItemSnapshot(7329, 1, 5));
		BankLayoutOptions arranged = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("cosmetics",
				Arrays.asList(KITTEN, PARTYHATS, DYES)));

		assertEquals(Arrays.asList(1559, 1042, 1038, 7329, 1767, 1763),
			idsOn(build(new BankSnapshot(withFirelighter), arranged), cosmeticsTab()));
	}

	/**
	 * An arrangement saved before an item joined a catalogued family still
	 * places it: the stored item key resolves to the family that now holds it.
	 */
	@Test
	public void anItemKeyResolvesToTheFamilyThatNowContainsIt()
	{
		BankLayoutOptions arranged = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("cosmetics",
				Arrays.asList("item:1042", DYES)));

		assertEquals(Arrays.asList(1042, 1038, 1767, 1763, 1559),
			idsOn(build(COSMETICS, arranged), cosmeticsTab()));
	}

	@Test
	public void gearListPlaysTheSetsInTheArrangedOrder()
	{
		BankSnapshot gear = new BankSnapshot(Arrays.asList(
			new BankItemSnapshot(1161, 1, 0),   // Adamant full helm
			new BankItemSnapshot(1123, 1, 1),   // Adamant platebody
			new BankItemSnapshot(9674, 1, 2),   // Proselyte hauberk
			new BankItemSnapshot(9676, 1, 3))); // Proselyte cuisse
		BankLayoutOptions arranged = new BankLayoutOptions(true, true, true,
			Collections.emptyMap(), GearLayout.LIST, PotionDoseOrder.GRAB_AREA,
			RuneOrder.ALPHABETICAL, TeleportOrder.ALPHABETICAL)
			.withBlockArrangements(BlockArrangements.EMPTY.withTag("gear",
				Arrays.asList("set:gear.proselyte", "set:gear.adamant-armour")));

		assertEquals(Arrays.asList(9674, 9676, 1161, 1123),
			idsOn(build(gear, arranged), gearTab()));
	}

	@Test
	public void theBlockDescriptorsMirrorTheEffectiveOrder()
	{
		BankLayoutOptions arranged = BankLayoutOptions.DEFAULTS.withBlockArrangements(
			BlockArrangements.EMPTY.withTag("cosmetics",
				Arrays.asList(DYES, KITTEN, PARTYHATS)));

		List<BankBlockDescriptor> blocks =
			build(COSMETICS, arranged).getBlockDescriptors().get("cosmetics");
		List<String> keys = new ArrayList<>();
		for (BankBlockDescriptor block : blocks)
		{
			keys.add(block.getBlockKey());
		}

		assertEquals(Arrays.asList(DYES, KITTEN, PARTYHATS), keys);
		assertEquals(2, blocks.get(0).getMemberCount());
		assertTrue(blocks.get(2).getDisplayName().length() > 0);
	}

	@Test
	public void arrangementsRoundTripAndPreserveWhatTheyDoNotUnderstand()
	{
		BlockArrangements arrangements = BlockArrangements.EMPTY
			.withTag("cosmetics", Arrays.asList(DYES, PARTYHATS))
			.withTag("teleports", Arrays.asList("name:varrock teleport"));

		assertEquals(arrangements, BlockArrangements.parse(arrangements.serialize()));
		assertEquals(Collections.emptyList(),
			new ArrayList<>(arrangements.withoutTag("cosmetics")
				.withoutTag("teleports").orders().keySet()));

		// A value written by a newer format survives a parse-serialize cycle.
		String newer = "v9?whatever&the&future&stores";
		assertEquals(newer, BlockArrangements.parse(newer).serialize());
	}

	private static int cosmeticsTab()
	{
		return BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("cosmetics");
	}

	private static int gearTab()
	{
		return BankLayoutPlan.defaultFor(BankPresets.IRONMAN).destinationOf("gear");
	}

	private static List<Integer> idsOn(BankOrganizationPreview preview, int destination)
	{
		BankCategoryPreview tab = preview.getCategories().get(destination);
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : tab.getItems())
		{
			ids.add(item.getItemId());
		}
		return ids;
	}

	private static BankOrganizationPreview build(BankSnapshot snapshot, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(snapshot, CompositeItemCatalog.DEFAULT,
			BankPresets.IRONMAN, GearStatsSource.NONE, ItemValueSource.NONE,
			CategoryOverrideSource.NONE, BankLayoutPlan.defaultFor(BankPresets.IRONMAN), options);
	}
}
