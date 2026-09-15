package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** Saved arrangements preserve future data without overriding current edits. */
public class BlockArrangementsTest
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

	@Test
	public void futurePayloadSurvivesRepeatedEditsAndCannotResurrectARemovedTag()
	{
		String future = "v2!gear>item:1!opaque>你好^!";
		String stored = BlockArrangements.parse(future)
			.withTag("gear", Arrays.asList("item:2")).serialize();
		String payload = Base64.getUrlEncoder().withoutPadding()
			.encodeToString(future.getBytes(StandardCharsets.UTF_8));
		assertTrue(stored.contains("!U" + payload));
		stored = BlockArrangements.parse(stored).withTag("tools", Arrays.asList("item:3"))
			.withoutTag("gear").serialize();
		assertTrue(stored.contains("!U" + payload));
		assertEquals(Collections.singletonMap("tools", Arrays.asList("item:3")),
			BlockArrangements.parse(stored).orders());
	}

	@Test
	public void legacyValuesStayReadableAndUntouchedInputStaysVerbatim()
	{
		String legacy = "v1!gear>item:1^item:2!unrecognized";
		BlockArrangements parsed = BlockArrangements.parse(legacy);
		assertEquals(Arrays.asList("item:1", "item:2"), parsed.orders().get("gear"));
		assertEquals(legacy, parsed.serialize());
		assertEquals(legacy, parsed.withTag("gear", Arrays.asList("item:1", "item:2")).serialize());
		String edited = parsed.withTag("tools", Arrays.asList("name:!>^~;+你好")).serialize();
		assertEquals(Arrays.asList("name:!>^~;+你好"),
			BlockArrangements.parse(edited).orders().get("tools"));
	}

	@Test
	public void malformedNewRecordsStayOpaqueAfterEdits()
	{
		String input = "BAblocks2!Tnot-base64>%%%";
		assertEquals(input, BlockArrangements.parse(input).serialize());
		String edited = BlockArrangements.parse(input).withTag("gear", Arrays.asList("item:2"))
			.serialize();
		assertEquals(Collections.singletonMap("gear", Arrays.asList("item:2")),
			BlockArrangements.parse(edited).orders());
	}
}
