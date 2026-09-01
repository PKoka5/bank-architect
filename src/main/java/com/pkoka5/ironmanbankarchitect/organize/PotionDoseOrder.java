package com.pkoka5.ironmanbankarchitect.organize;

/**
 * Where part doses sit on the supplies tab.
 *
 * <p>{@link #GRAB_AREA} is the shipped behaviour: full potions lead the tab
 * and part doses trail behind the food as the to-decant pile, so the front of
 * the tab is safe to grab from blind. {@link #BY_FAMILY} runs each potion
 * 4 to 1 in one place instead.</p>
 */
public enum PotionDoseOrder
{
	GRAB_AREA("Grab area"),
	BY_FAMILY("By family");

	private final String label;

	PotionDoseOrder(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
