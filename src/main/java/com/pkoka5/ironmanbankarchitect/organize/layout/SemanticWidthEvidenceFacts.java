package com.pkoka5.ironmanbankarchitect.organize.layout;

import java.util.Arrays;

/**
 * Reviewed aggregate width facts from the local ten-template research cohort. These facts contain
 * no imported layouts or item IDs; they are only the exact class-level support vectors recorded in
 * {@code docs/research/local-import-family-candidates.md}.
 */
final class SemanticWidthEvidenceFacts
{
	static final WidthEvidence GEM_RAW_PROCESSED = new WidthEvidence(7,
		Arrays.asList(0, 2, 0, 0, 5, 0, 0, 0),
		Arrays.asList(0, 5, 0, 0, 5, 0, 0, 0));

	static final WidthEvidence HERB_WORKFLOW = new WidthEvidence(7,
		Arrays.asList(0, 1, 6, 1, 0, 0, 0, 0),
		Arrays.asList(0, 10, 14, 4, 0, 0, 0, 0));

	private SemanticWidthEvidenceFacts()
	{
	}
}
