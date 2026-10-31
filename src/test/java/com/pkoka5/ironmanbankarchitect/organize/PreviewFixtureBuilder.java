package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;

/** Default arguments for planner fixtures; production always supplies all inputs. */
public final class PreviewFixtureBuilder
{
	private PreviewFixtureBuilder() {}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset)
	{
		return build(snapshot, catalog, preset, GearStatsSource.NONE, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats)
	{
		return build(snapshot, catalog, preset, gearStats, ItemValueSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, CategoryOverrideSource.NONE);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, overrides, null);
	}

	/**
	 * The blueprint arranged into the destinations of a plan, rather than into
	 * the preset's own ten categories. Pass {@code null} for the plan to get the
	 * per-category blueprint the rest of the analysis is built from.
	 */
	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides,
		BankLayoutPlan layoutPlan)
	{
		return build(snapshot, catalog, preset, gearStats, itemValues, overrides, layoutPlan,
			BankLayoutOptions.DEFAULTS);
	}

	public static BankOrganizationPreview build(BankSnapshot snapshot, ItemCatalog catalog, BankPreset preset,
		GearStatsSource gearStats, ItemValueSource itemValues, CategoryOverrideSource overrides,
		BankLayoutPlan layoutPlan, BankLayoutOptions options)
	{
		return BankOrganizationPreviewBuilder.build(snapshot, catalog, preset, gearStats, itemValues,
			overrides, layoutPlan, options);
	}
}
