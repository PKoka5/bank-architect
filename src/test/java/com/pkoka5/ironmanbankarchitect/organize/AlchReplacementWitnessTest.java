package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class AlchReplacementWitnessTest
{
	@Test
	public void elementalStaffWitnessNamesAnOwnedHigherMemberOfTheSameElement()
	{
		for (int[] family : new int[][] {
			{1405, 1397, 1381}, {1403, 1395, 1383}, {1407, 1399, 1385}, {1401, 1393, 1387}
		})
		{
			assertEquals(family[0], role(family[2], ids(family[2], family[1], family[0])));
			assertEquals(family[1], role(family[2], ids(family[1], family[2])));
			assertEquals(family[0], role(family[1], ids(family[1], family[0])));
			assertEquals(0, role(family[0], ids(family[1], family[2])));
			assertEquals(0, role(family[1], ids(family[1], family[2])));
		}
	}

	@Test
	public void staffLookupDoesNotCarryAnOwnedWitnessFromAnotherElement()
	{
		assertEquals(0, role(1387, ids(1405, 1395, 1387)));
		assertEquals(1393, role(1387, ids(1405, 1387, 1393)));
		assertEquals(0, role(4675, ids(1405, 1403, 1407, 1401)));
		assertEquals(0, role(1387, ids(1387)));
	}

	@Test
	public void mysticColoursKeepOneActualOwnedCanonicalSurvivorPerSlot()
	{
		for (int[] family : new int[][] {
			{4089, 4099, 4109}, {4091, 4101, 4111}, {4093, 4103, 4113},
			{4095, 4105, 4115}, {4097, 4107, 4117}
		})
		{
			Set<Integer> all = ids(family[2], family[1], family[0]);
			assertEquals(0, role(family[0], all));
			assertEquals(family[0], role(family[1], all));
			assertEquals(family[0], role(family[2], all));
			assertEquals(family[1], role(family[2], ids(family[2], family[1])));
			assertEquals(0, role(family[2], ids(family[2])));
		}
		assertEquals(0, role(4113, ids(4113, 4091, 4101)));
	}

	@Test
	public void aPinnedMysticKeeperTakesPrecedenceButMustBeRecorded()
	{
		for (String gear : Arrays.asList("gear", "combat-gear"))
		{
			Map<Integer, String> choices = Collections.singletonMap(4103, gear);
			assertEquals(4103, IronmanAlchCandidateCatalog.roleReplacementId(4093,
				ids(4093, 4103, 4113), choices));
			assertEquals(4103, IronmanAlchCandidateCatalog.roleReplacementId(4113,
				ids(4113, 4103, 4093), choices));
			assertEquals(0, IronmanAlchCandidateCatalog.roleReplacementId(4103,
				ids(4093, 4103, 4113), choices));
			assertEquals(4093, IronmanAlchCandidateCatalog.roleReplacementId(4113,
				ids(4113, 4093), choices));
		}
	}

	@Test
	public void mysticChoicesOutsideCombatCannotBeUsedAsReplacementEvidence()
	{
		for (String elsewhere : Arrays.asList("alch", "tools", "cosmetics", "quest-items"))
		{
			Map<Integer, String> choices = new HashMap<>();
			choices.put(4093, elsewhere);
			assertEquals(4103, IronmanAlchCandidateCatalog.roleReplacementId(4113,
				ids(4093, 4103, 4113), choices));
			choices.put(4103, elsewhere);
			assertEquals(0, IronmanAlchCandidateCatalog.roleReplacementId(4113,
				ids(4093, 4103, 4113), choices));
		}
	}

	@Test
	public void slashStockUsesReviewedUpgradePriorityRatherThanBankIterationOrder()
	{
		for (int stock : new int[] {1373, 1319, 1303, 1333, 1289, 1213, 1371, 1317, 1331})
		{
			assertEquals(4587, role(stock, ids(26482, 12006, 4151, 4587, stock)));
			assertEquals(4151, role(stock, ids(stock, 26482, 12006, 4151)));
			assertEquals(12006, role(stock, ids(26482, stock, 12006)));
			assertEquals(26482, role(stock, ids(stock, 26482)));
			assertEquals(0, role(stock, ids(stock, 11920, 1393)));
		}
		assertEquals(0, role(1432, ids(4587, 4151)));
	}

	@Test
	public void toolWitnessUsesARecordedHigherFunctionalTier()
	{
		assertEquals(11920, IronmanQuickToolSelector.ownedUpgradeId(1275, ids(1275, 11920)));
		assertEquals(6739, IronmanQuickToolSelector.ownedUpgradeId(1359, ids(1359, 6739)));
		assertEquals(6739, IronmanQuickToolSelector.ownedUpgradeId(1357, ids(1357, 1359, 6739)));
		assertEquals(23680, IronmanQuickToolSelector.ownedUpgradeId(1275, ids(11920, 23680)));
		assertEquals(28220, IronmanQuickToolSelector.ownedUpgradeId(1359, ids(6739, 23673, 28220)));
	}

	@Test
	public void ornamentsInTheTargetsOwnTierAreNotAnUpgrade()
	{
		for (int[] pair : new int[][] {{1275, 23276}, {1359, 23279}, {11920, 12797}, {6739, 28217}})
		{
			assertEquals(0, IronmanQuickToolSelector.ownedUpgradeId(pair[0], ids(pair[1], pair[0])));
			assertEquals(0, IronmanQuickToolSelector.ownedUpgradeId(pair[1], ids(pair[0])));
		}
		assertEquals(11920, IronmanQuickToolSelector.ownedUpgradeId(1275, ids(23276, 11920)));
	}

	@Test
	public void toolLookupResetsBetweenFamiliesAndExcludesInactiveOrUnrelatedItems()
	{
		for (int[] pair : new int[][] {
			{1275, 6739}, {1359, 11920}, {1359, 23680}, {1275, 27695},
			{1275, 23682}, {1359, 23675}, {2347, 25644}, {1755, 34024}, {952, 6739}
		})
			assertEquals(0, IronmanQuickToolSelector.ownedUpgradeId(pair[0], ids(pair[0], pair[1])));
		assertEquals(0, IronmanQuickToolSelector.ownedUpgradeId(1359, Collections.emptySet()));
		assertEquals(0, role(-1, ids(4587, 4093, 1405)));
	}

	@Test
	public void witnessQueriesAcceptUnmodifiableRecordedPlaceholderIdsAndChoices()
	{
		Set<Integer> recorded = ids(4103, 4093, 1275, 11920, 1387, 1393);
		Map<Integer, String> choices = Collections.singletonMap(4103, "gear");
		Set<Integer> before = new LinkedHashSet<>(recorded);
		assertEquals(4103, IronmanAlchCandidateCatalog.roleReplacementId(4093, recorded, choices));
		assertEquals(1393, IronmanAlchCandidateCatalog.roleReplacementId(1387, recorded, choices));
		assertEquals(11920, IronmanQuickToolSelector.ownedUpgradeId(1275, recorded));
		assertEquals(before, recorded);
		assertEquals(Collections.singletonMap(4103, "gear"), choices);
	}

	@Test
	public void everyFrozenRoleDecisionHasTheSameResultAndAnActualRecordedWitness() throws IOException
	{
		InputStream stream = getClass().getResourceAsStream("alch-catalog-compatibility.tsv");
		assertNotNull("Frozen pre-migration role behavior", stream);
		int checked = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] fields = line.split("\t", -1);
				if (!"ROLE".equals(fields[0])) continue;
				int target = Integer.parseInt(fields[1]);
				Set<Integer> recorded = new LinkedHashSet<>();
				if (!fields[2].isEmpty())
					for (String id : fields[2].split(",")) recorded.add(Integer.parseInt(id));
				Map<Integer, String> choices = new HashMap<>();
				if (!fields[3].isEmpty())
					for (String choice : fields[3].split(","))
					{
						String[] pair = choice.split("=", 2);
						choices.put(Integer.parseInt(pair[0]), pair[1]);
					}
				int witness = IronmanAlchCandidateCatalog.roleReplacementId(target, recorded, choices);
				assertEquals(line, Boolean.parseBoolean(fields[4]), witness > 0);
				if (witness > 0)
				{
					assertTrue(line, recorded.contains(witness));
					assertNotEquals(line, target, witness);
				}
				checked++;
			}
		}
		assertEquals(1811, checked);
	}

	private static int role(int id, Set<Integer> recorded)
	{
		return IronmanAlchCandidateCatalog.roleReplacementId(id, recorded, Collections.emptyMap());
	}

	private static Set<Integer> ids(Integer... ids)
	{
		return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(ids)));
	}
}
