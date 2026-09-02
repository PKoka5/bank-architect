package com.pkoka5.ironmanbankarchitect.organize;

import java.util.Objects;

/**
 * One arrangeable block as the arrange editor sees it: its stable key, a
 * display name, and how many logical items it holds. Emitted per tag in the
 * effective render order, because block structure cannot be recovered from
 * the physical grid where an anchored column's members sit rows apart.
 */
public final class BankBlockDescriptor
{
	private final String tagKey;
	private final String blockKey;
	private final String displayName;
	private final int memberCount;

	BankBlockDescriptor(String tagKey, String blockKey, String displayName, int memberCount)
	{
		this.tagKey = Objects.requireNonNull(tagKey, "tagKey");
		this.blockKey = Objects.requireNonNull(blockKey, "blockKey");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.memberCount = memberCount;
	}

	public String getTagKey()
	{
		return tagKey;
	}

	public String getBlockKey()
	{
		return blockKey;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int getMemberCount()
	{
		return memberCount;
	}
}
