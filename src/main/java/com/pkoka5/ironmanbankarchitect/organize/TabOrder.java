package com.pkoka5.ironmanbankarchitect.organize;

/**
 * How a category lays its items onto the tab.
 *
 * <p>{@link #PACKED} is the shipped behaviour: families stay whole as
 * rectangles and rows are completed, at the price of a run of singles
 * sometimes being interrupted. {@link #SEQUENTIAL} keeps the sorter's order
 * and simply wraps row by row: adjacent kinds stay adjacent, and a charge set
 * may break across a row boundary instead.</p>
 */
public enum TabOrder
{
	PACKED("Packed"),
	SEQUENTIAL("Sorted");

	private final String label;

	TabOrder(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
