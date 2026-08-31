package com.pkoka5.ironmanbankarchitect.guide;

import com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus;
import com.pkoka5.ironmanbankarchitect.blueprint.BankProfile;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSection;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintTab;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.match.BlockMatchResult;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class BankGuideController
{
	public static final String BANK_CLOSED_STATUS = "Open your bank to preview the guide.";
	public static final String GUIDE_ACTIVE_STATUS = "Guide preview active.";
	public static final String GUIDE_DISABLED_STATUS = "Guide preview off.";
	public static final String NO_ANALYSIS_STATUS = "No bank analysis yet.";
	public static final String ANALYSIS_BANK_CLOSED_STATUS = "Open your bank before analyzing.";
	public static final String ANALYSIS_RUNNING_STATUS = BankAnalysisStatus.RUNNING_TEXT;
	public static final String ANALYSIS_FAILED_STATUS = BankAnalysisStatus.FAILED_TEXT;
	public static final String NO_CATALOG_SUMMARY_STATUS = BankAnalysisStatus.NO_CATALOG_SUMMARY_TEXT;
	public static final String NO_ORGANIZATION_PREVIEW_STATUS = BankAnalysisStatus.NO_ORGANIZATION_PREVIEW_TEXT;
	public static final String NO_GUIDANCE_PROGRESS_STATUS = "Analyze your bank to start item-order guidance.";
	public static final String GUIDANCE_BANK_CLOSED_STATUS = "Open your bank for item-order guidance.";
	public static final String GUIDANCE_DISABLED_STATUS = "Enable Bank Guide to show the next manual move.";
	public static final String GUIDANCE_READY_STATUS = "Open the vanilla All items view to begin item-order guidance.";
	public static final String NO_OVERRIDES_STATUS = "No category corrections yet.";
	public static final String ASSIGN_MODE_HINT =
		"Right-click a bank item and pick a tag to correct where it lands.";

	private final List<VisualBlock> availableBlocks;
	private final AtomicReference<BankGuideState> state;
	private final AtomicReference<BlockMatchResult> latestMatchResult = new AtomicReference<>();
	private final AtomicReference<String> analysisText = new AtomicReference<>(NO_ANALYSIS_STATUS);
	private final AtomicReference<String> analysisDetailText = new AtomicReference<>("");
	private final AtomicReference<BankAnalysisStatus> bankAnalysisStatus =
		new AtomicReference<>(BankAnalysisStatus.notStarted());
	private final AtomicReference<String> guideProgressText = new AtomicReference<>(NO_GUIDANCE_PROGRESS_STATUS);
	private final AtomicInteger guideProgressPercent = new AtomicInteger(-1);
	private final AtomicBoolean bankOpen = new AtomicBoolean(false);
	private final AtomicBoolean guideArmedAutomatically = new AtomicBoolean(false);
	private final AtomicReference<Runnable> bankOpenedListener = new AtomicReference<>();
	private final AtomicBoolean categoryAssignMode = new AtomicBoolean(false);
	private final AtomicInteger categoryOverrideCount = new AtomicInteger(0);

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
		guideArmedAutomatically.set(false);
		state.updateAndGet(current -> current.withGuideEnabled(enabled));
		if (enabled)
		{
			categoryAssignMode.set(false);
		}
	}

	public void toggleGuide()
	{
		guideArmedAutomatically.set(false);
		boolean enabled = state
			.updateAndGet(current -> current.withGuideEnabled(!current.isGuideEnabled()))
			.isGuideEnabled();
		if (enabled)
		{
			categoryAssignMode.set(false);
		}
	}

	/**
	 * While assign mode is on, the bank right-click menu offers the blueprint
	 * destinations so the player can correct a classification.
	 *
	 * <p>Assign mode and the guide are mutually exclusive. Each one paints the
	 * bank in a different colour language: assign mode tints an item with the
	 * colour of the tab it is headed for, the guide tints a slot by whether it
	 * is already correct. Both at once would leave the player unable to tell
	 * which question a colour is answering, so switching one on switches the
	 * other off.</p>
	 */
	public boolean isCategoryAssignMode()
	{
		return categoryAssignMode.get();
	}

	public void toggleCategoryAssignMode()
	{
		setCategoryAssignMode(!categoryAssignMode.get());
	}

	public void setCategoryAssignMode(boolean enabled)
	{
		categoryAssignMode.set(enabled);
		if (enabled)
		{
			state.updateAndGet(current -> current.withGuideEnabled(false));
		}
	}

	public void publishCategoryOverrideCount(int count)
	{
		categoryOverrideCount.set(Math.max(0, count));
	}

	public int getCategoryOverrideCount()
	{
		return categoryOverrideCount.get();
	}

	public String getCategoryOverrideText()
	{
		int count = categoryOverrideCount.get();
		String summary = count == 0 ? NO_OVERRIDES_STATUS
			: count + (count == 1 ? " item is" : " items are") + " corrected by hand.";
		return categoryAssignMode.get() ? summary + " " + ASSIGN_MODE_HINT : summary;
	}

	public boolean isBankOpen()
	{
		return bankOpen.get();
	}

	public void setBankOpen(boolean open)
	{
		boolean was = bankOpen.getAndSet(open);
		if (open && !was)
		{
			Runnable listener = bankOpenedListener.get();
			if (listener != null)
			{
				listener.run();
			}
		}
	}

	/**
	 * Runs once each time the bank goes from closed to open. The overlay reports
	 * the bank state every frame, so the transition is detected here rather than
	 * by the caller.
	 */
	public void setBankOpenedListener(Runnable listener)
	{
		bankOpenedListener.set(listener);
	}

	/**
	 * Arms the guide the way opening the bank does under the auto-guide setting.
	 * Guidance armed this way renders quietly: no banners on unsortable views
	 * and no confirmation on sorted slots. Any manual guide action reverts the
	 * guide to its usual, fully spoken form.
	 */
	public void enableGuideAutomatically()
	{
		setGuideEnabled(true);
		guideArmedAutomatically.set(true);
	}

	public boolean isGuideArmedAutomatically()
	{
		return guideArmedAutomatically.get() && isGuideEnabled();
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

	public synchronized void publishBankAnalysis(BankAnalysisStatus status)
	{
		Objects.requireNonNull(status, "status");
		if (status.kind() == BankAnalysisStatus.Kind.BANK_CLOSED)
		{
			latestMatchResult.set(null);
			analysisText.set(ANALYSIS_BANK_CLOSED_STATUS);
			analysisDetailText.set("");
		}

		if (status.kind() == BankAnalysisStatus.Kind.SUCCESS)
		{
			guideProgressText.set(GUIDANCE_READY_STATUS);
			guideProgressPercent.set(-1);
			bankAnalysisStatus.set(status);
			return;
		}

		bankAnalysisStatus.set(status);
		guideProgressText.set(NO_GUIDANCE_PROGRESS_STATUS);
		guideProgressPercent.set(-1);
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

	public BankAnalysisStatus bankAnalysisStatus()
	{
		return bankAnalysisStatus.get();
	}

	public BankOrganizationPreview organizationPreview()
	{
		return bankAnalysisStatus.get().organizationPreview().orElse(null);
	}

	public void publishGuideProgressText(String text)
	{
		guideProgressText.set(Objects.requireNonNull(text, "text"));
	}

	/**
	 * Publishes the guidance status line together with an overall progress
	 * percentage; pass -1 when no meaningful percentage is available.
	 */
	public void publishGuideProgress(String text, int percent)
	{
		publishGuideProgressText(text);
		guideProgressPercent.set(Math.max(-1, Math.min(100, percent)));
	}

	public int getGuideProgressPercent()
	{
		BankAnalysisStatus analysis = bankAnalysisStatus.get();
		if (analysis.kind() != BankAnalysisStatus.Kind.SUCCESS
			|| !bankOpen.get() || !isGuideEnabled())
		{
			return -1;
		}
		return guideProgressPercent.get();
	}

	public String getGuideProgressText()
	{
		BankAnalysisStatus analysis = bankAnalysisStatus.get();
		if (analysis.kind() == BankAnalysisStatus.Kind.RUNNING)
		{
			return ANALYSIS_RUNNING_STATUS;
		}
		if (analysis.kind() == BankAnalysisStatus.Kind.FAILED)
		{
			return ANALYSIS_FAILED_STATUS;
		}
		if (analysis.kind() != BankAnalysisStatus.Kind.SUCCESS)
		{
			return NO_GUIDANCE_PROGRESS_STATUS;
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
