package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * One immutable semantic atom: the smallest indivisible ordered member group of a rule. Its
 * meaning depends on the rule's shape primitive:
 *
 * <ul>
 * <li>{@code HORIZONTAL_RUN} / {@code VERTICAL_RUN}: the atom is the projected item family;</li>
 * <li>{@code STAGE_MATRIX}: each atom is one family column and member keys identify the stages;</li>
 * <li>{@code ROW_GROUP_MATRIX}: each atom is one explicit row.</li>
 * </ul>
 *
 * <p>Atoms are curated in code, so malformed atoms fail fast: keys must be stable lowercase keys,
 * item IDs must be positive, unique within the atom, and never Bank Filler.</p>
 */
public final class SemanticAtom
{
	private final String atomKey;
	private final List<Member> members;

	public SemanticAtom(String atomKey, List<Member> members)
	{
		this.atomKey = SemanticRule.requireRuleKey(atomKey, "atomKey");
		this.members = requireMembers(members);
	}

	public String getAtomKey()
	{
		return atomKey;
	}

	public List<Member> getMembers()
	{
		return members;
	}

	/**
	 * The atom's item IDs in reviewed member order.
	 */
	public List<Integer> getItemIds()
	{
		List<Integer> itemIds = new ArrayList<>(members.size());
		for (Member member : members)
		{
			itemIds.add(member.getItemId());
		}
		return Collections.unmodifiableList(itemIds);
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof SemanticAtom))
		{
			return false;
		}

		SemanticAtom atom = (SemanticAtom) other;
		return atomKey.equals(atom.atomKey) && members.equals(atom.members);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(atomKey, members);
	}

	@Override
	public String toString()
	{
		return "SemanticAtom{" + atomKey + ", members=" + members + "}";
	}

	private static List<Member> requireMembers(List<Member> members)
	{
		if (members == null || members.isEmpty())
		{
			throw new IllegalArgumentException("members must not be empty");
		}

		Set<String> seenMemberKeys = new HashSet<>();
		Set<Integer> seenItemIds = new HashSet<>();
		for (Member member : members)
		{
			if (member == null)
			{
				throw new IllegalArgumentException("members must not contain null");
			}
			if (!seenMemberKeys.add(member.getMemberKey()))
			{
				throw new IllegalArgumentException("duplicate member key " + member.getMemberKey());
			}
			if (!seenItemIds.add(member.getItemId()))
			{
				throw new IllegalArgumentException("duplicate member item ID " + member.getItemId());
			}
		}

		return Collections.unmodifiableList(new ArrayList<>(members));
	}

	/**
	 * One ordered atom member: a stable member/stage key plus exactly one canonical item ID.
	 */
	public static final class Member
	{
		private final String memberKey;
		private final int itemId;

		public Member(String memberKey, int itemId)
		{
			this.memberKey = SemanticRule.requireRuleKey(memberKey, "memberKey");
			if (itemId <= 0)
			{
				throw new IllegalArgumentException("itemId must be positive");
			}
			if (itemId == ItemID.BANK_FILLER)
			{
				throw new IllegalArgumentException("Bank Filler is never an atom member");
			}
			this.itemId = itemId;
		}

		public String getMemberKey()
		{
			return memberKey;
		}

		public int getItemId()
		{
			return itemId;
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof Member))
			{
				return false;
			}

			Member member = (Member) other;
			return itemId == member.itemId && memberKey.equals(member.memberKey);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(memberKey, itemId);
		}

		@Override
		public String toString()
		{
			return memberKey + "=" + itemId;
		}
	}
}
