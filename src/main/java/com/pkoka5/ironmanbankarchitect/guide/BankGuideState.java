package com.pkoka5.ironmanbankarchitect.guide;

import java.util.Objects;

public final class BankGuideState
{
	private final String selectedBlockKey;
	private final boolean guideEnabled;

	public BankGuideState(String selectedBlockKey, boolean guideEnabled)
	{
		this.selectedBlockKey = requireText(selectedBlockKey, "selectedBlockKey");
		this.guideEnabled = guideEnabled;
	}

	public String getSelectedBlockKey()
	{
		return selectedBlockKey;
	}

	public boolean isGuideEnabled()
	{
		return guideEnabled;
	}

	public BankGuideState withSelectedBlockKey(String blockKey)
	{
		return new BankGuideState(blockKey, guideEnabled);
	}

	public BankGuideState withGuideEnabled(boolean enabled)
	{
		return new BankGuideState(selectedBlockKey, enabled);
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof BankGuideState))
		{
			return false;
		}

		BankGuideState that = (BankGuideState) other;
		return guideEnabled == that.guideEnabled && selectedBlockKey.equals(that.selectedBlockKey);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(selectedBlockKey, guideEnabled);
	}

	private static String requireText(String value, String name)
	{
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}

		return value;
	}
}
