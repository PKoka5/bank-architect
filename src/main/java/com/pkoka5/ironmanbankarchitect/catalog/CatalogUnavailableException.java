package com.pkoka5.ironmanbankarchitect.catalog;

/** A required bundled table is unusable; never fall back to partial classification. */
public final class CatalogUnavailableException extends IllegalStateException
{
	public static final String PLAYER_MESSAGE =
		"Bank analysis is unavailable because its required item override table is missing or invalid.";

	public CatalogUnavailableException(Throwable cause)
	{
		this(PLAYER_MESSAGE, cause);
	}

	public CatalogUnavailableException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
