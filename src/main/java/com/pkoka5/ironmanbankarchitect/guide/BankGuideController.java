package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.blueprint.BankProfile;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSection;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintTab;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.match.BlockMatchResult;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class BankGuideController
{
	public static final String BANK_CLOSED_STATUS = "Open your bank to preview the guide.";
	public static final String GUIDE_ACTIVE_STATUS = "Guide preview active.";
	public static final String GUIDE_DISABLED_STATUS = "Guide preview off.";
	public static final String NO_ANALYSIS_STATUS = "No bank analysis yet.";
	public static final String ANALYSIS_BANK_CLOSED_STATUS = "Open your bank before analyzing.";
	public static final String ANALYSIS_RUNNING_STATUS = "Analyzing bank snapshot...";
	public static final String NO_CATALOG_SUMMARY_STATUS = "Analyze your bank to see catalog overview.";
	public static final String NO_ORGANIZATION_PREVIEW_STATUS = "Analyze your bank to preview owned-item blueprint.";
	public static final String NO_GUIDANCE_PROGRESS_STATUS = "Analyze your bank to start item-order guidance.";
	public static final String GUIDANCE_BANK_CLOSED_STATUS = "Open your bank for item-order guidance.";
	public static final String GUIDANCE_DISABLED_STATUS = "Enable Bank Guide to show the next manual move.";
	public static final String GUIDANCE_READY_STATUS = "Open the vanilla All items view to begin item-order guidance.";

	private final List<VisualBlock> availableBlocks;
	private final AtomicReference<BankGuideState> state;
	private final AtomicReference<BlockMatchResult> latestMatchResult = new AtomicReference<>();
	private final AtomicReference<String> analysisText = new AtomicReference<>(NO_ANALYSIS_STATUS);
	private final AtomicReference<String> analysisDetailText = new AtomicReference<>("");
	private final AtomicReference<BankCatalogSummary> latestCatalogSummary = new AtomicReference<>();
	private final AtomicReference<String> catalogSummaryText = new AtomicReference<>(NO_CATALOG_SUMMARY_STATUS);
	private final AtomicReference<BankOrganizationPreview> latestOrganizationPreview = new AtomicReference<>();
	private final AtomicReference<String> organizationPreviewText = new AtomicReference<>(NO_ORGANIZATION_PREVIEW_STATUS);
	private final AtomicReference<String> guideProgressText = new AtomicReference<>(NO_GUIDANCE_PROGRESS_STATUS);
	private final AtomicBoolean bankOpen = new AtomicBoolean(false);

	public BankGuideController(BankProfile profile)
	{
		Objects.requireNonNull(profile, "profile");
		this.availableBlocks = collectBlocks(profile);
		if (availableBlocks.isEmpty())
		{
			throw new IllegalArgumentException("profile must contain at least one block");
		}

		this.state = new AtomicReference<>(new BankGuideState(availableBlocks.get(0).getKey(), false));
	}

	public List<VisualBlock> getAvailableBlocks()
	{
		return availableBlocks;
	}

	public VisualBlock getSelectedBlock()
	{
		return findBlock(state.get().getSelectedBlockKey());
	}

	public void selectBlock(String blockKey)
	{
		findBlock(blockKey);
		state.updateAndGet(current -> current.withSelectedBlockKey(blockKey));
	}

	public boolean isGuideEnabled()
	{
		return state.get().isGuideEnabled();
	}

	public void setGuideEnabled(boolean enabled)
	{
		state.updateAndGet(current -> current.withGuideEnabled(enabled));
	}

	public void toggleGuide()
	{
		state.updateAndGet(current -> current.withGuideEnabled(!current.isGuideEnabled()));
	}

	public boolean isBankOpen()
	{
		return bankOpen.get();
	}

	public void setBankOpen(boolean open)
	{
		bankOpen.set(open);
	}

	public String getStatusText()
	{
		if (!bankOpen.get())
		{
			return BANK_CLOSED_STATUS;
		}

		return isGuideEnabled() ? GUIDE_ACTIVE_STATUS : GUIDE_DISABLED_STATUS;
	}

	public BankGuideState getState()
	{
		return state.get();
	}

	public void publishBankClosedAnalysis()
	{
		latestMatchResult.set(null);
		analysisText.set(ANALYSIS_BANK_CLOSED_STATUS);
		analysisDetailText.set("");
		latestCatalogSummary.set(null);
		catalogSummaryText.set(NO_CATALOG_SUMMARY_STATUS);
		latestOrganizationPreview.set(null);
		organizationPreviewText.set(NO_ORGANIZATION_PREVIEW_STATUS);
		guideProgressText.set(NO_GUIDANCE_PROGRESS_STATUS);
	}

	public void publishAnalysisStarted()
	{
		latestCatalogSummary.set(null);
		catalogSummaryText.set(ANALYSIS_RUNNING_STATUS);
		latestOrganizationPreview.set(null);
		organizationPreviewText.set(ANALYSIS_RUNNING_STATUS);
		guideProgressText.set(ANALYSIS_RUNNING_STATUS);
	}

	public void publishMatchResult(BlockMatchResult result)
	{
		Objects.requireNonNull(result, "result");
		latestMatchResult.set(result);
		analysisText.set(result.toCompactSummary());
		analysisDetailText.set(result.toSlotDetailText());
	}

	public BlockMatchResult getLatestMatchResult()
	{
		return latestMatchResult.get();
	}

	public String getAnalysisText()
	{
		return analysisText.get();
	}

	public String getAnalysisDetailText()
	{
		return analysisDetailText.get();
	}

	public void publishCatalogSummary(BankCatalogSummary summary)
	{
		Objects.requireNonNull(summary, "summary");
		latestCatalogSummary.set(summary);
		catalogSummaryText.set(summary.toOverviewText());
	}

	public BankCatalogSummary getLatestCatalogSummary()
	{
		return latestCatalogSummary.get();
	}

	public String getCatalogSummaryText()
	{
		return catalogSummaryText.get();
	}

	public void publishOrganizationPreview(BankOrganizationPreview preview)
	{
		Objects.requireNonNull(preview, "preview");
		latestOrganizationPreview.set(preview);
		organizationPreviewText.set(preview.toPreviewText());
		guideProgressText.set(GUIDANCE_READY_STATUS);
	}

	public BankOrganizationPreview getLatestOrganizationPreview()
	{
		return latestOrganizationPreview.get();
	}

	public String getOrganizationPreviewText()
	{
		return organizationPreviewText.get();
	}

	public void publishGuideProgressText(String text)
	{
		guideProgressText.set(Objects.requireNonNull(text, "text"));
	}

	public String getGuideProgressText()
	{
		if (latestOrganizationPreview.get() == null)
		{
			return guideProgressText.get();
		}
		if (!bankOpen.get())
		{
			return GUIDANCE_BANK_CLOSED_STATUS;
		}
		if (!isGuideEnabled())
		{
			return GUIDANCE_DISABLED_STATUS;
		}
		return guideProgressText.get();
	}

	private VisualBlock findBlock(String key)
	{
		for (VisualBlock block : availableBlocks)
		{
			if (block.getKey().equals(key))
			{
				return block;
			}
		}

		throw new IllegalArgumentException("Unknown block key: " + key);
	}

	private static List<VisualBlock> collectBlocks(BankProfile profile)
	{
		List<VisualBlock> blocks = new ArrayList<>();
		for (BlueprintTab tab : profile.getTabs())
		{
			for (BlueprintSection section : tab.getSections())
			{
				blocks.addAll(section.getBlocks());
			}
		}

		return Collections.unmodifiableList(blocks);
	}
}
