package com.pkoka5.ironmanbankarchitect.guide;

/**
 * Bank rearrange mode the player has selected. Guidance plans same-section
 * reordering differently per mode because the game applies a different
 * transform to the item container:
 *
 * <ul>
 * <li>{@link #SWAP} exchanges the dragged item with the drop slot occupant.</li>
 * <li>{@link #INSERT} removes the dragged item and reinserts it at the drop
 * slot index, shifting everything in between.</li>
 * </ul>
 *
 * Insert needs strictly fewer drags on a shuffled section: the swap lower bound
 * is {@code n - permutation cycles} while the insert lower bound is
 * {@code n - longest increasing subsequence}.
 */
public enum RearrangeMode
{
	SWAP,
	INSERT
}
