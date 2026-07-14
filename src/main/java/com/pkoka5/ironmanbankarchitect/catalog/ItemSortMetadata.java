package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.Objects;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

/**
 * Curated, item-ID-based facts used by deterministic bank sorters.
 *
 * <p>Display names are deliberately absent: names may be used as a stable fallback, but they are
 * never evidence for healing, servings, restrictions, or other functional item behaviour.</p>
 */
public final class ItemSortMetadata
{
	private static final Pattern KEY_PATTERN = Pattern.compile("[a-z0-9]+(?:[._-][a-z0-9]+)*");

	public enum VariantKind
	{
		NONE,
		DOSE,
		CHARGE,
		SERVINGS,
		WORKFLOW_STAGE
	}

	public enum FoodRole
	{
		NONE,
		STANDARD,
		COMBO,
		DELAYED,
		MULTI_BITE
	}

	public enum HealModel
	{
		NONE,
		FIXED,
		VARIABLE
	}

	public enum AreaRestriction
	{
		NONE,
		BLIGHTED_AREAS
	}

	private final int itemId;
	private final String familyKey;
	private final VariantKind variantKind;
	private final int variantValue;
	private final FoodRole foodRole;
	private final HealModel healModel;
	private final int immediateHealMin;
	private final int immediateHealMax;
	private final int secondaryHeal;
	private final AreaRestriction areaRestriction;
	private final String sourceKey;

	ItemSortMetadata(int itemId, String familyKey, VariantKind variantKind, int variantValue,
		FoodRole foodRole, HealModel healModel, int immediateHealMin, int immediateHealMax,
		int secondaryHeal, AreaRestriction areaRestriction, String sourceKey)
	{
		if (itemId <= 0)
		{
			throw new IllegalArgumentException("itemId must be positive");
		}
		if (itemId == ItemID.BANK_FILLER)
		{
			throw new IllegalArgumentException("Bank Filler metadata is forbidden");
		}

		this.itemId = itemId;
		this.familyKey = requireKey(familyKey, "familyKey");
		this.variantKind = Objects.requireNonNull(variantKind, "variantKind");
		this.variantValue = variantValue;
		this.foodRole = Objects.requireNonNull(foodRole, "foodRole");
		this.healModel = Objects.requireNonNull(healModel, "healModel");
		this.immediateHealMin = immediateHealMin;
		this.immediateHealMax = immediateHealMax;
		this.secondaryHeal = secondaryHeal;
		this.areaRestriction = Objects.requireNonNull(areaRestriction, "areaRestriction");
		this.sourceKey = requireKey(sourceKey, "sourceKey");

		validateVariant();
		validateFoodFacts();
	}

	private void validateVariant()
	{
		switch (variantKind)
		{
			case NONE:
				if (variantValue != 0)
				{
					throw new IllegalArgumentException("NONE variant must have value 0");
				}
				break;
			case CHARGE:
				if (variantValue < 0)
				{
					throw new IllegalArgumentException("CHARGE variant must not be negative");
				}
				break;
			case WORKFLOW_STAGE:
				if (variantValue < 0)
				{
					throw new IllegalArgumentException("WORKFLOW_STAGE variant must not be negative");
				}
				break;
			case DOSE:
				if (variantValue <= 0 || variantValue > 4)
				{
					throw new IllegalArgumentException("DOSE variant must be between 1 and 4");
				}
				break;
			case SERVINGS:
				if (variantValue <= 0)
				{
					throw new IllegalArgumentException(variantKind + " variant must be positive");
				}
				break;
			default:
				throw new IllegalStateException("Unhandled variant kind: " + variantKind);
		}
	}

	private void validateFoodFacts()
	{
		if (foodRole != FoodRole.MULTI_BITE && variantKind == VariantKind.SERVINGS)
		{
			throw new IllegalArgumentException("SERVINGS metadata requires the MULTI_BITE food role");
		}

		if (foodRole == FoodRole.NONE)
		{
			if (healModel != HealModel.NONE || immediateHealMin != 0 || immediateHealMax != 0
				|| secondaryHeal != 0)
			{
				throw new IllegalArgumentException("non-food metadata must not contain healing facts");
			}
			return;
		}

		if (healModel == HealModel.NONE)
		{
			throw new IllegalArgumentException("food metadata requires a healing model");
		}
		if (immediateHealMin <= 0 || immediateHealMax < immediateHealMin)
		{
			throw new IllegalArgumentException("food healing range must be positive and ordered");
		}
		if (healModel == HealModel.FIXED && immediateHealMin != immediateHealMax)
		{
			throw new IllegalArgumentException("FIXED healing requires equal minimum and maximum");
		}
		if (healModel == HealModel.VARIABLE && immediateHealMin == immediateHealMax)
		{
			throw new IllegalArgumentException("VARIABLE healing requires a genuine range");
		}

		if (foodRole == FoodRole.DELAYED)
		{
			if (secondaryHeal <= 0)
			{
				throw new IllegalArgumentException("DELAYED food requires positive secondary healing");
			}
		}
		else if (secondaryHeal != 0)
		{
			throw new IllegalArgumentException("only DELAYED food may have secondary healing");
		}

		if (foodRole == FoodRole.MULTI_BITE && variantKind != VariantKind.SERVINGS)
		{
			throw new IllegalArgumentException("MULTI_BITE food requires SERVINGS metadata");
		}
	}

	static String requireKey(String value, String field)
	{
		if (value == null || !KEY_PATTERN.matcher(value).matches())
		{
			throw new IllegalArgumentException(field + " must be a lowercase stable key");
		}
		return value;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getFamilyKey()
	{
		return familyKey;
	}

	public VariantKind getVariantKind()
	{
		return variantKind;
	}

	public int getVariantValue()
	{
		return variantValue;
	}

	public FoodRole getFoodRole()
	{
		return foodRole;
	}

	public HealModel getHealModel()
	{
		return healModel;
	}

	public int getImmediateHealMin()
	{
		return immediateHealMin;
	}

	public int getImmediateHealMax()
	{
		return immediateHealMax;
	}

	public int getSecondaryHeal()
	{
		return secondaryHeal;
	}

	public AreaRestriction getAreaRestriction()
	{
		return areaRestriction;
	}

	public String getSourceKey()
	{
		return sourceKey;
	}

	public boolean isFood()
	{
		return foodRole != FoodRole.NONE;
	}
}
