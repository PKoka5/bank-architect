package com.pkoka5.ironmanbankarchitect.util;

/** Validates required model text without changing its original whitespace. */
public final class TextValidation
{
	private TextValidation() { }

	public static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
	}
}
