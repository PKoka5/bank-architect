package com.pkoka5.ironmanbankarchitect.analysis;

import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.*;
import org.slf4j.*;


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
	private BankAnalysisRequest successfulRequest;
	private Map<Integer, String> establishedAlch = Collections.emptyMap();

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
		if (!isLatestRequest(requestGeneration)) return;
		try
		{
			itemCatalog.requireAvailable();
			BankCatalogSummary summary = BankCatalogSummarizer.summarize(
				analysisRequest.bankSnapshot(), itemCatalog, analysisRequest.presetOr(bankPreset));
			BankOrganizationPreview preview = BankOrganizationPreviewBuilder.build(
				analysisRequest.bankSnapshot(), itemCatalog, analysisRequest.presetOr(bankPreset),
				analysisRequest::gearStats, analysisRequest::alchValue,
				retainedCategories(analysisRequest), analysisRequest.layoutPlan(),
				analysisRequest.layoutOptions());
			synchronized (this)
			{
				if (!isLatestRequest(requestGeneration)) return;
				successfulRequest = analysisRequest;
				establishedAlch = new HashMap<>();
				for (BankPreviewItem item : preview.getPlannedItems())
					if ("alch".equals(item.getLayoutTagKey())) establishedAlch.put(item.getItemId(), "alch");
				publishAnalysisIfLatest(requestGeneration, BankAnalysisStatus.success(summary, preview));
			}
		}
		catch (RuntimeException ex)
		{
			publishFailure(requestGeneration, ex);
		}
	}

	private synchronized CategoryOverrideSource retainedCategories(BankAnalysisRequest request)
	{
		Map<Integer, String> retained = new HashMap<>(request.sameLayoutContext(successfulRequest, bankPreset)
			&& request.bankSnapshot().getItems().stream().anyMatch(item -> item.isPlaceholder())
			? establishedAlch : Collections.emptyMap());
		retained.keySet().removeIf(id -> request.bankSnapshot().getTotalQuantity(id) > 0
			&& request.bankSnapshot().getTotalQuantity(id) != successfulRequest.bankSnapshot().getTotalQuantity(id));
		return id -> request.categoryKey(id).isPresent() ? request.categoryKey(id) : Optional.ofNullable(retained.get(id));
	}

	private void publishFailure(long requestGeneration, RuntimeException failure)
	{
		log.error("Bank analysis failed", failure);
		publishAnalysisIfLatest(requestGeneration, failure instanceof CatalogUnavailableException
			? BankAnalysisStatus.failed(failure.getMessage())
			: BankAnalysisStatus.failed());
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
		successfulRequest = null;
		establishedAlch = Collections.emptyMap();
		currentRequestGeneration++;
	}

	public synchronized void invalidate()
	{
		if (closed) return;
		successfulRequest = null;
		establishedAlch = Collections.emptyMap();
		currentRequestGeneration++;
		statusPublisher.accept(BankAnalysisStatus.notStarted());
	}
}
