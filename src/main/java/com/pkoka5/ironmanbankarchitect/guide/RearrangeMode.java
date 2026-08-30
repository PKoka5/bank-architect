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
 * For a fixed occurrence pairing, swap distance is {@code n - permutation cycles}.
 * Duplicate IDs can admit a shorter pairing, so swap guidance labels this value
 * as an estimate. The insert lower bound is
 * {@code n - longest increasing subsequence} for the stable occurrence order.
 */
public enum RearrangeMode
{
	SWAP,
	INSERT
}
