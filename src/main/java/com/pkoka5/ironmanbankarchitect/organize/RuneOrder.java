package com.pkoka5.ironmanbankarchitect.organize;

/**
 * How runes order among themselves.
 *
 * <p>{@link #ALPHABETICAL} is the shipped behaviour. {@link #ELEMENTAL} is
 * the canonical sequence players know from spellbooks and shops: air, water,
 * earth, fire, then mind, body, cosmic, chaos, nature, law, death, blood,
 * soul, astral, wrath, with anything unrecognized following alphabetically.</p>
 */
public enum RuneOrder
{
	ALPHABETICAL("Alphabetical"),
	ELEMENTAL("Elemental");

	private final String label;

	RuneOrder(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
