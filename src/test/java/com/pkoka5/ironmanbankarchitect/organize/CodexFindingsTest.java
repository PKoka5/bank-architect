package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/** Temporary: reproduces the two findings from the external review. */
public class CodexFindingsTest
{
	@Test
	public void unknownVersionDoesNotSwallowANewerEdit()
	{
		String stored = BlockArrangements.parse("v2!gear>item:1")
			.withTag("gear", Collections.singletonList("item:2"))
			.serialize();

		assertEquals("the saved choice must survive a reload",
			Arrays.asList("item:2"), BlockArrangements.parse(stored).orders().get("gear"));
	}

	@Test
	public void anUntouchedUnknownVersionRoundTripsVerbatim()
	{
		assertEquals("v10!gear>item:1", BlockArrangements.parse("v10!gear>item:1").serialize());
	}
}
