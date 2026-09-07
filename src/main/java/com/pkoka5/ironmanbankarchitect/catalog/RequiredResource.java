package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Objects;
import java.util.function.Supplier;

/** Defers required data until use; a broken resource never poisons class initialization. */
public final class RequiredResource<T>
{
	private final String name;
	private Supplier<T> loader;
	private T value;
	private CatalogUnavailableException failure;

	public RequiredResource(String name, Supplier<T> loader)
	{
		this.name = name;
		this.loader = Objects.requireNonNull(loader);
	}

	public synchronized T get()
	{
		if (loader != null)
		{
			try
			{
				value = Objects.requireNonNull(loader.get());
			}
			catch (CatalogUnavailableException ex)
			{
				failure = ex;
			}
			catch (RuntimeException ex)
			{
				failure = new CatalogUnavailableException(
					"Bank analysis is unavailable because its " + name + " data is missing or invalid.", ex);
			}
			finally
			{
				loader = null;
			}
		}
		if (failure != null) throw failure;
		return value;
	}
}
