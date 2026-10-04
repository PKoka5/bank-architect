package com.pkoka5.ironmanbankarchitect.util;

import java.util.Locale;

/** Shared matching rules for English catalog names and semantic keys. */
public final class NameMatching
{
	private NameMatching() { }

	public static String normalized(String value)
	{
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	public static int numericSuffix(String value, int fallback)
	{
		int open = value.lastIndexOf('(');
		if (open < 0 || !value.endsWith(")")) return fallback;
		try { return Integer.parseInt(value.substring(open + 1, value.length() - 1)); }
		catch (NumberFormatException ignored) { return fallback; }
	}

	public static boolean containsAny(String value, String... needles)
	{
		for (String needle : needles)
		{
			if (value.contains(needle)) return true;
		}
		return false;
	}

	public static boolean containsWord(String value, String word)
	{
		int fromIndex = 0;
		while (fromIndex < value.length())
		{
			int index = value.indexOf(word, fromIndex);
			if (index < 0) return false;
			int end = index + word.length();
			boolean startBoundary = index == 0 || !Character.isLetterOrDigit(value.charAt(index - 1));
			boolean endBoundary = end == value.length() || !Character.isLetterOrDigit(value.charAt(end));
			if (startBoundary && endBoundary) return true;
			fromIndex = index + 1;
		}
		return false;
	}
}
