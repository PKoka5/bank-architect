package com.pkoka5.ironmanbankarchitect.organize.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

public class SemanticAtomTest
{
	@Test
	public void preservesMemberKeysIdsAndOrder()
	{
		SemanticAtom.Member raw = new SemanticAtom.Member("stage.raw", 10);
		SemanticAtom.Member processed = new SemanticAtom.Member("stage.processed", 20);
		SemanticAtom atom = new SemanticAtom("gem.sapphire", Arrays.asList(raw, processed));

		assertEquals("gem.sapphire", atom.getAtomKey());
		assertEquals(Arrays.asList(raw, processed), atom.getMembers());
		assertEquals(Arrays.asList(10, 20), atom.getItemIds());
		assertEquals(atom, new SemanticAtom("gem.sapphire", Arrays.asList(raw, processed)));
		assertEquals(atom.hashCode(), new SemanticAtom("gem.sapphire", Arrays.asList(raw, processed)).hashCode());
	}

	@Test
	public void atomDefensivelyCopiesMembers()
	{
		List<SemanticAtom.Member> members = new ArrayList<>(Collections.singletonList(member("stage.raw", 10)));
		SemanticAtom atom = new SemanticAtom("gem.sapphire", members);
		members.add(member("stage.processed", 20));

		assertEquals(1, atom.getMembers().size());
		try
		{
			atom.getMembers().add(member("stage.extra", 30));
			fail("expected UnsupportedOperationException");
		}
		catch (UnsupportedOperationException expected)
		{
			// expected
		}
	}

	@Test
	public void rejectsMalformedAtoms()
	{
		assertAtomFails("Bad Key", Collections.singletonList(member("stage.raw", 10)));
		assertAtomFails("gem.sapphire", null);
		assertAtomFails("gem.sapphire", Collections.emptyList());
		assertAtomFails("gem.sapphire", Arrays.asList(member("stage.raw", 10), null));
		assertAtomFails("gem.sapphire", Arrays.asList(member("stage.raw", 10), member("stage.raw", 20)));
		assertAtomFails("gem.sapphire", Arrays.asList(member("stage.raw", 10), member("stage.cut", 10)));
	}

	@Test
	public void rejectsMalformedMembers()
	{
		assertMemberFails("Bad Key", 10);
		assertMemberFails("stage.raw", 0);
		assertMemberFails("stage.raw", -1);
		assertMemberFails("stage.raw", ItemID.BANK_FILLER);
	}

	private static SemanticAtom.Member member(String key, int itemId)
	{
		return new SemanticAtom.Member(key, itemId);
	}

	private static void assertAtomFails(String key, List<SemanticAtom.Member> members)
	{
		try
		{
			new SemanticAtom(key, members);
			fail("expected construction failure");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	private static void assertMemberFails(String key, int itemId)
	{
		try
		{
			new SemanticAtom.Member(key, itemId);
			fail("expected construction failure");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}
}
