package com.pkoka5.ironmanbankarchitect.organize;

/**
 * How single teleport items order among themselves.
 *
 * <p>{@link #ALPHABETICAL} is the shipped behaviour. {@link #SPELLBOOK_FIRST}
 * leads with the standard spellbook's city teleports in their casting order -
 * Varrock, Lumbridge, Falador, House, Camelot, Ardougne, Watchtower, and so
 * on - with every other teleport following alphabetically, and charged
 * jewellery families after the singles as always.</p>
 */
public enum TeleportOrder
{
	ALPHABETICAL("Alphabetical"),
	SPELLBOOK_FIRST("Spellbook first");

	private final String label;

	TeleportOrder(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
