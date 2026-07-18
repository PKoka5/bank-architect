package com.pkoka5.ironmanbankarchitect.simulate;

/** Fixed inputs for the proposed long-term cleanup-review benchmark. */
final class CleanupReviewBenchmarkProtocol
{
	static final String VERSION = "1";
	static final long[] SEEDS = {20260718L, 314159265L, 271828182L};
	static final int BANKS_PER_SEED = 200;
	static final int MIN_ITEMS = 6;
	static final int MAX_ITEMS = 120;
	static final String REGISTRY_SHA256 =
		"449712144c522f622f975c9b7667a9f84c43da57260da40fa428ea2d7515b038";

	private CleanupReviewBenchmarkProtocol()
	{
	}
}
