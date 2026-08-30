package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummarizer;
import com.pkoka5.ironmanbankarchitect.catalog.BankCatalogSummary;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreviewBuilder;
import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the complete lifecycle of bank analysis. Only the newest request may
 * publish, and closing the module prevents every pending request from publishing.
 */
public final class BankAnalysis implements AutoCloseable
{
	private static final Logger log = LoggerFactory.getLogger(BankAnalysis.class);

	private final Executor clientExecutor;
	private final Executor analysisExecutor;
	private final Supplier<Optional<BankAnalysisRequest>> bankAnalysisRequest;
	private final Consumer<BankAnalysisStatus> statusPublisher;
	private final ItemCatalog itemCatalog;
	private final BankPreset bankPreset;

	private long currentRequestGeneration;
	private boolean closed;

	public BankAnalysis(Executor clientExecutor, Executor analysisExecutor,
		Supplier<Optional<BankAnalysisRequest>> bankAnalysisRequest,
		Consumer<BankAnalysisStatus> statusPublisher,
		ItemCatalog itemCatalog, BankPreset bankPreset)
	{
		this.clientExecutor = Objects.requireNonNull(clientExecutor, "clientExecutor");
		this.analysisExecutor = Objects.requireNonNull(analysisExecutor, "analysisExecutor");
		this.bankAnalysisRequest = Objects.requireNonNull(bankAnalysisRequest, "bankAnalysisRequest");
		this.statusPublisher = Objects.requireNonNull(statusPublisher, "statusPublisher");
		this.itemCatalog = Objects.requireNonNull(itemCatalog, "itemCatalog");
		this.bankPreset = Objects.requireNonNull(bankPreset, "bankPreset");
	}

	public void analyzeBank()
	{
		long requestGeneration;
		synchronized (this)
		{
			if (closed)
			{
				return;
			}
			requestGeneration = ++currentRequestGeneration;
			statusPublisher.accept(BankAnalysisStatus.running());
		}

		try
		{
			clientExecutor.execute(() -> captureBank(requestGeneration));
		}
		catch (RuntimeException ex)
		{
			publishFailure(requestGeneration, ex);
		}
	}

	private void captureBank(long requestGeneration)
	{
		if (!isLatestRequest(requestGeneration))
		{
			return;
		}

		try
		{
			Optional<BankAnalysisRequest> analysisRequest = Objects.requireNonNull(
				bankAnalysisRequest.get(), "bankAnalysisRequest returned null");
			if (!analysisRequest.isPresent())
			{
				publishAnalysisIfLatest(requestGeneration, BankAnalysisStatus.bankClosed());
				return;
			}

			analysisExecutor.execute(() -> analyzeBank(requestGeneration, analysisRequest.get()));
		}
		catch (RuntimeException ex)
		{
			publishFailure(requestGeneration, ex);
		}
	}

	private void analyzeBank(long requestGeneration, BankAnalysisRequest analysisRequest)
	{
		try
		{
			BankCatalogSummary summary = BankCatalogSummarizer.summarize(
				analysisRequest.bankSnapshot(), itemCatalog, bankPreset);
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				analysisRequest.bankSnapshot(), itemCatalog, bankPreset,
				analysisRequest::gearStats, analysisRequest::alchValue,
				analysisRequest::categoryKey, analysisRequest.layoutPlan(),
				analysisRequest.layoutOptions());
			publishAnalysisIfLatest(requestGeneration, BankAnalysisStatus.success(summary, preview));
		}
		catch (RuntimeException ex)
		{
			publishFailure(requestGeneration, ex);
		}
	}

	private void publishFailure(long requestGeneration, RuntimeException failure)
	{
		log.error("Bank analysis failed", failure);
		publishAnalysisIfLatest(requestGeneration, BankAnalysisStatus.failed());
	}

	private synchronized boolean isLatestRequest(long requestGeneration)
	{
		return !closed && requestGeneration == currentRequestGeneration;
	}

	private synchronized void publishAnalysisIfLatest(long requestGeneration,
		BankAnalysisStatus status)
	{
		if (!closed && requestGeneration == currentRequestGeneration)
		{
			statusPublisher.accept(status);
		}
	}

	@Override
	public synchronized void close()
	{
		closed = true;
		currentRequestGeneration++;
	}
}
