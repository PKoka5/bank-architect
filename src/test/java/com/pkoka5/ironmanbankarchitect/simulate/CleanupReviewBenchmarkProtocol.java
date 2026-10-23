package com.pkoka5.ironmanbankarchitect.simulate;

/** Fixed inputs for the proposed long-term cleanup-review benchmark. */
final class CleanupReviewBenchmarkProtocol
{
	static final String VERSION = "2";
	static final long[] SEEDS = {20260718L, 314159265L, 271828182L};
	static final int BANKS_PER_SEED = 200;
	static final int MIN_ITEMS = 6;
	static final int MAX_ITEMS = 120;
	/** Of the registry with line endings normalised to LF, so every checkout agrees. */
	static final String REGISTRY_SHA256 =
		"2f577111a105866c26b03b142895ac719ee89a455d060535252580b0b329f028";

	private CleanupReviewBenchmarkProtocol()
	{
	}
}
