package com.pkoka5.ironmanbankarchitect.match;

public enum SlotMatchState
{
	OWNED,
	MISSING,
	RESERVED_EMPTY,
	ROLE_ONLY;

	public String getDisplayLabel()
	{
		switch (this)
		{
			case OWNED:
				return "owned";
			case MISSING:
				return "missing";
			case RESERVED_EMPTY:
				return "reserved";
			case ROLE_ONLY:
			default:
				return "role-only";
		}
	}
}
