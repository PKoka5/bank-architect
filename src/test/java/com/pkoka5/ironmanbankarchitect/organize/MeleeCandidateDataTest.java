package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;

public class MeleeCandidateDataTest
{
	@Test public void bundledDecisionsHaveKnownExactIdentitiesAcrossAllTenPositions()
	{
		Map<Integer,int[]> facts = PrimaryGearSelector.load(getClass().getResourceAsStream("melee-primary-candidates.tsv"));
		assertEquals(624,facts.size());
		Set<Integer> slots = new HashSet<>();
		facts.forEach((id,fact) -> {
			assertTrue("Unregistered exact candidate " + id,CompositeItemCatalog.DEFAULT.findById(id).isPresent());
			slots.add(fact[0]);
			CombatQualityReport.Fact frozen = CombatQualityReport.FACTS.get(id);
			if (frozen != null && frozen.stats != null)
				assertEquals("Independent equipment slot for " + id,frozen.stats.slotRank(),fact[0]);
		});
		assertEquals(new HashSet<>(Arrays.asList(0,1,2,3,4,5,6,7,8,12)),slots);
		for (int excluded : new int[]{30386,24037,24040,24043,24046,11128,23240,27110,27111,27112,4178,26708,26710})
			assertFalse("Temporary/held/quest-copy identity " + excluded,facts.containsKey(excluded));
		assertThrows(UnsupportedOperationException.class,() -> facts.clear());
	}

	@Test public void unusableStatesAreDistinctFromTheirWearableVariants()
	{
		Map<Integer,int[]> facts = PrimaryGearSelector.load(getClass().getResourceAsStream("melee-primary-candidates.tsv"));
		for (int id : new int[]{12929,13196,13198,29073,26376,26378,26380,4890,4914,4986,23997,30305,20463,10888})
			assertEquals("Inactive ID " + id,0,facts.get(id)[1]);
		for (int id : new int[]{12931,29028,29047,26382,26384,26386,4726,23995,24143,31097})
			assertTrue("Wearable ID " + id,facts.get(id)[1] > 0);
		assertEquals(2,facts.get(34715)[5]);
		assertEquals(0,facts.get(34715)[4]); // Different attack styles have different speeds; no guessed common speed.
		assertEquals(1,facts.get(28035)[5]); // Released Bounty ornament, distinct from failed Arena reward.
		assertEquals(2,facts.get(28039)[5]);
	}

	@Test public void incompleteOrInvalidTablesCannotPublishPartialCandidates() throws Exception
	{
		String valid;
		try (java.io.InputStream input = Objects.requireNonNull(getClass().getResourceAsStream("melee-primary-candidates.tsv")))
		{
			valid = new String(input.readAllBytes(),StandardCharsets.UTF_8);
		}
		for (String invalid : Arrays.asList("",valid.replace("# schema=1","# schema=2"),
			valid.replace("0/2/0/1000/0/0/torva","0/2/0/1000/4/1/torva"),
			valid.replace("8/2/40/1000/4/1/inquisitor-mace","8/2/40/1000/4/0/inquisitor-mace"),
			valid.replace("8/0/0/0/0/0/inactive-weapons","8/0/0/100/0/0/inactive-weapons"),
			valid + "0/2/0/1000/0/0/repeated\t26382\n",
			"# schema=1\n0/2/0/1000/0/0/torva\t26382\n"))
			assertThrows(RuntimeException.class,() -> PrimaryGearSelector.load(
				new ByteArrayInputStream(invalid.getBytes(StandardCharsets.UTF_8))));
		assertThrows(RuntimeException.class,() -> PrimaryGearSelector.load(null));
	}
}
