package com.pkoka5.ironmanbankarchitect.guide;

/**
 * Player-selected rearrange mode: SWAP exchanges two slots; INSERT shifts intervening slots.
 * Fixed-pairing swap distance is n minus permutation cycles, an estimate with duplicate IDs. Insert
 * lower bound is n minus LIS for stable occurrence order.
 */
public enum RearrangeMode
{
	SWAP,
	INSERT
}
