package com.pkoka5.ironmanbankarchitect.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

public class BundledTableTest
{
	private static final String HEADER = "# schema=1\n";

	@Test
	public void preservesUtf8DeclarationOrderAndTrailingEmptyFields() throws IOException
	{
		TrackingInput input = new TrackingInput(HEADER + "één\tβ\t\nsecond\t\tlast\n");
		List<String> lines = new ArrayList<>();
		List<List<String>> rows = new ArrayList<>();
		BundledTable.read(input, "test", false, false, (line, fields) ->
		{
			lines.add(line);
			rows.add(Arrays.asList(fields));
		});
		assertEquals(List.of("één\tβ\t", "second\t\tlast"), lines);
		assertEquals(List.of(List.of("één", "β", ""), List.of("second", "", "last")), rows);
		assertTrue(input.closed);
	}

	@Test
	public void trimmingTheHeaderDoesNotEnableIgnoringRows() throws IOException
	{
		TrackingInput input = new TrackingInput(" \t# schema=1 \t\n\n# note\nrow\n");
		List<String> lines = new ArrayList<>();
		BundledTable.read(input, "test", true, false, (line, fields) -> lines.add(line));
		assertEquals(List.of("", "# note", "row"), lines);
		assertTrue(input.closed);
	}

	@Test
	public void ignoringRowsDoesNotTrimTheHeaderOrTheData() throws IOException
	{
		TrackingInput paddedHeader = new TrackingInput(" # schema=1\nrow\n");
		assertThrows(IOException.class, () -> BundledTable.read(paddedHeader, "test", false, true,
			(line, fields) -> fail("Rows must not be read after an invalid header")));
		assertTrue(paddedHeader.closed);

		TrackingInput input = new TrackingInput(HEADER + "\n# note\n   \n # data\n\t\nvalue \n");
		List<String> lines = new ArrayList<>();
		BundledTable.read(input, "test", false, true, (line, fields) -> lines.add(line));
		assertEquals(List.of("   ", " # data", "\t", "value "), lines);
		assertTrue(input.closed);
	}

	@Test
	public void rejectsMissingUnsupportedAnnotatedAndBomHeadersBeforeReadingRows()
	{
		for (String text : List.of("", "\n" + HEADER + "row", "# note\n" + HEADER,
			"# schema=2\nrow", "# schema=10\nrow", "# schema=1 annotation\nrow",
			"\uFEFF" + HEADER + "row"))
		{
			for (boolean trim : new boolean[]{false, true})
			{
				TrackingInput input = new TrackingInput(text);
				assertThrows(text, IOException.class, () -> BundledTable.read(input, "test", trim, true,
					(line, fields) -> fail("Callback invoked before a valid schema")));
				assertTrue(text, input.closed);
			}
		}
	}

	@Test
	public void missingStreamFailsBeforeCallback()
	{
		assertThrows(IOException.class, () -> BundledTable.read(null, "test", false, false,
			(line, fields) -> fail("Missing input cannot produce a row")));
	}

	@Test
	public void closesTheStreamAndPropagatesTheSameCheckedRowFailure()
	{
		TrackingInput input = new TrackingInput(HEADER + "bad\nlater\n");
		IOException failure = new IOException("domain row failure");
		List<String> visited = new ArrayList<>();
		IOException actual = assertThrows(IOException.class, () ->
			BundledTable.read(input, "test", false, false, (line, fields) ->
			{
				visited.add(line);
				throw failure;
			}));
		assertSame(failure, actual);
		assertEquals(List.of("bad"), visited);
		assertTrue(input.closed);
	}

	@Test
	public void closesTheStreamAndPropagatesTheSameUncheckedDomainFailure()
	{
		TrackingInput input = new TrackingInput(HEADER + "bad\n");
		IllegalArgumentException failure = new IllegalArgumentException("duplicate item ID");
		assertSame(failure, assertThrows(IllegalArgumentException.class, () ->
			BundledTable.read(input, "test", false, false, (line, fields) -> { throw failure; })));
		assertTrue(input.closed);
	}

	@Test
	public void closesTheStreamAndPropagatesReadFailuresAfterAValidHeader()
	{
		TrackingInput input = new TrackingInput(HEADER + "unread\n");
		input.failAt = HEADER.getBytes(StandardCharsets.UTF_8).length;
		IOException actual = assertThrows(IOException.class, () ->
			BundledTable.read(input, "test", false, false,
				(line, fields) -> fail("A failed row read must not publish a partial row")));
		assertSame(input.failure, actual);
		assertTrue(input.closed);
	}

	private static final class TrackingInput extends InputStream
	{
		private final ByteArrayInputStream delegate;
		private final IOException failure = new IOException("input read failure");
		private int position;
		private int failAt = Integer.MAX_VALUE;
		private boolean closed;

		private TrackingInput(String content)
		{
			delegate = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		public int read() throws IOException
		{
			if (position >= failAt) throw failure;
			int value = delegate.read();
			if (value >= 0) position++;
			return value;
		}

		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException
		{
			if (position >= failAt) throw failure;
			int count = delegate.read(bytes, offset, Math.min(length, failAt - position));
			if (count > 0) position += count;
			return count;
		}

		@Override
		public void close() throws IOException
		{
			closed = true;
			delegate.close();
		}
	}
}
