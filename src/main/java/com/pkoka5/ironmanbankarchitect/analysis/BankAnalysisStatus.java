package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import java.util.Objects;
import java.util.Optional;

/** One atomic published state of bank analysis and its complete successful outcome. */
public final class BankAnalysisStatus
{
	public static final String NO_CATALOG_SUMMARY_TEXT = "Analyze your bank to see catalog overview.";
	public static final String NO_ORGANIZATION_PREVIEW_TEXT =
		"Analyze your bank to preview owned-item blueprint.";
	public static final String RUNNING_TEXT = "Analyzing bank snapshot...";
	public static final String FAILED_TEXT = "Bank analysis failed. Try again.";

	public enum Kind
	{
		NOT_STARTED,
		RUNNING,
		BANK_CLOSED,
		SUCCESS,
		FAILED
	}

	private static final BankAnalysisStatus NOT_STARTED =
		new BankAnalysisStatus(Kind.NOT_STARTED, null, null,
			NO_CATALOG_SUMMARY_TEXT, NO_ORGANIZATION_PREVIEW_TEXT);
	private static final BankAnalysisStatus RUNNING_STATUS =
		new BankAnalysisStatus(Kind.RUNNING, null, null, RUNNING_TEXT, RUNNING_TEXT);
	private static final BankAnalysisStatus BANK_CLOSED =
		new BankAnalysisStatus(Kind.BANK_CLOSED, null, null,
			NO_CATALOG_SUMMARY_TEXT, NO_ORGANIZATION_PREVIEW_TEXT);
	private static final BankAnalysisStatus FAILED_STATUS =
		new BankAnalysisStatus(Kind.FAILED, null, null, FAILED_TEXT, FAILED_TEXT);

	private final Kind kind;
	private final BankCatalogSummary catalogSummary;
	private final BankOrganizationPreview organizationPreview;
	private final String catalogSummaryText;
	private final String organizationPreviewText;

	private BankAnalysisStatus(Kind kind, BankCatalogSummary catalogSummary,
		BankOrganizationPreview organizationPreview, String catalogSummaryText,
		String organizationPreviewText)
	{
		this.kind = Objects.requireNonNull(kind, "kind");
		this.catalogSummary = catalogSummary;
		this.organizationPreview = organizationPreview;
		this.catalogSummaryText = Objects.requireNonNull(catalogSummaryText, "catalogSummaryText");
		this.organizationPreviewText = Objects.requireNonNull(
			organizationPreviewText, "organizationPreviewText");
	}

	public static BankAnalysisStatus notStarted()
	{
		return NOT_STARTED;
	}

	public static BankAnalysisStatus running()
	{
		return RUNNING_STATUS;
	}

	public static BankAnalysisStatus bankClosed()
	{
		return BANK_CLOSED;
	}

	public static BankAnalysisStatus success(BankCatalogSummary catalogSummary,
		BankOrganizationPreview organizationPreview)
	{
		BankCatalogSummary summary = Objects.requireNonNull(catalogSummary, "catalogSummary");
		BankOrganizationPreview preview = Objects.requireNonNull(
			organizationPreview, "organizationPreview");
		return new BankAnalysisStatus(Kind.SUCCESS, summary, preview,
			summary.toOverviewText(), preview.toPreviewText());
	}

	public static BankAnalysisStatus failed()
	{
		return FAILED_STATUS;
	}

	public Kind kind()
	{
		return kind;
	}

	public Optional<BankCatalogSummary> catalogSummary()
	{
		return Optional.ofNullable(catalogSummary);
	}

	public Optional<BankOrganizationPreview> organizationPreview()
	{
		return Optional.ofNullable(organizationPreview);
	}

	public String catalogSummaryText()
	{
		return catalogSummaryText;
	}

	public String organizationPreviewText()
	{
		return organizationPreviewText;
	}
}
