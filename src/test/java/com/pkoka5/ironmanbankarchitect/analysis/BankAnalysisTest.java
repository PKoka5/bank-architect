package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.ItemCatalog;
import com.pkoka5.ironmanbankarchitect.catalog.StaticItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutOptions;
import com.pkoka5.ironmanbankarchitect.organize.BankLayoutPlan;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import com.pkoka5.ironmanbankarchitect.organize.GearStats;
import com.pkoka5.ironmanbankarchitect.organize.GearSlot;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class BankAnalysisTest
{
	@Test
	public void successfulRequestPublishesOneCompleteOutcome()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		ControlledExecutor analysisExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = analysis(clientExecutor, analysisExecutor,
			requests(request(5297, 209)), statuses);

		analysis.analyzeBank();
		assertEquals(BankAnalysisStatus.Kind.RUNNING, last(statuses).kind());
		assertFalse(last(statuses).catalogSummary().isPresent());
		assertFalse(last(statuses).organizationPreview().isPresent());

		clientExecutor.runNext();
		analysisExecutor.runNext();

		BankAnalysisStatus success = last(statuses);
		assertEquals(2, statuses.size());
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, success.kind());
		assertEquals(2, success.catalogSummary().get().getTotalScannedIdCount());
		assertEquals(2, success.organizationPreview().get().getPlannedItemCount());
		assertFalse(success.organizationPreview().get().getTagCounts().isEmpty());
	}

	@Test
	public void newerRequestWinsWhenOlderWorkFinishesLast()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		ControlledExecutor analysisExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = analysis(clientExecutor, analysisExecutor,
			requests(request(5297), request(5297, 209)), statuses);

		analysis.analyzeBank();
		clientExecutor.runNext();
		analysis.analyzeBank();
		clientExecutor.runNext();

		analysisExecutor.runLast();
		BankAnalysisStatus newest = last(statuses);
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, newest.kind());
		assertEquals(2, newest.catalogSummary().get().getTotalScannedIdCount());

		analysisExecutor.runNext();
		assertSame(newest, last(statuses));
		assertEquals(2, last(statuses).organizationPreview().get().getPlannedItemCount());
	}

	@Test
	public void newerClosedBankObservationInvalidatesOlderWork()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		ControlledExecutor analysisExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = analysis(clientExecutor, analysisExecutor,
			requests(Optional.of(request(5297)), Optional.empty()), statuses);

		analysis.analyzeBank();
		clientExecutor.runNext();
		analysis.analyzeBank();
		clientExecutor.runNext();

		BankAnalysisStatus bankClosed = last(statuses);
		assertEquals(BankAnalysisStatus.Kind.BANK_CLOSED, bankClosed.kind());
		analysisExecutor.runNext();
		assertSame(bankClosed, last(statuses));
	}

	@Test
	public void captureFailurePublishesGenericFailureWithoutData()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = new BankAnalysis(clientExecutor, Runnable::run,
			() ->
			{
				throw new IllegalStateException("private detail");
			}, statuses::add, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		analysis.analyzeBank();
		clientExecutor.runNext();

		BankAnalysisStatus failed = last(statuses);
		assertEquals(BankAnalysisStatus.Kind.FAILED, failed.kind());
		assertEquals(BankAnalysisStatus.FAILED_TEXT, failed.catalogSummaryText());
		assertFalse(failed.catalogSummary().isPresent());
		assertFalse(failed.organizationPreview().isPresent());
	}

	@Test
	public void rejectedBackgroundWorkPublishesFailure()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		Executor rejectingExecutor = command ->
		{
			throw new RejectedExecutionException("stopped");
		};
		BankAnalysis analysis = new BankAnalysis(clientExecutor, rejectingExecutor,
			requests(request(5297)), statuses::add, StaticItemCatalog.INSTANCE,
			BankPresets.IRONMAN);

		analysis.analyzeBank();
		clientExecutor.runNext();

		assertEquals(BankAnalysisStatus.Kind.FAILED, last(statuses).kind());
	}

	@Test
	public void rejectedClientWorkPublishesFailureWithoutCapturingBankFacts()
	{
		AtomicInteger captures = new AtomicInteger();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		Executor rejectingExecutor = command ->
		{
			throw new RejectedExecutionException("stopped");
		};
		BankAnalysis analysis = new BankAnalysis(rejectingExecutor, Runnable::run,
			() ->
			{
				captures.incrementAndGet();
				return Optional.of(request(5297));
			}, statuses::add, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		analysis.analyzeBank();

		assertEquals(0, captures.get());
		assertEquals(BankAnalysisStatus.Kind.FAILED, last(statuses).kind());
	}

	@Test
	public void staleComputationFailureCannotReplaceNewerSuccess()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		ControlledExecutor analysisExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		ItemCatalog catalog = itemId ->
		{
			if (itemId == 5297)
			{
				throw new IllegalStateException("older request failed");
			}
			return StaticItemCatalog.INSTANCE.findById(itemId);
		};
		BankAnalysis analysis = new BankAnalysis(clientExecutor, analysisExecutor,
			requests(request(5297), request(209)), statuses::add, catalog,
			BankPresets.IRONMAN);

		analysis.analyzeBank();
		clientExecutor.runNext();
		analysis.analyzeBank();
		clientExecutor.runNext();
		analysisExecutor.runLast();
		BankAnalysisStatus newest = last(statuses);

		analysisExecutor.runNext();

		assertSame(newest, last(statuses));
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, newest.kind());
		assertEquals(1, newest.catalogSummary().get().getTotalScannedIdCount());
	}

	@Test
	public void closePreventsPendingAndFuturePublication()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		ControlledExecutor analysisExecutor = new ControlledExecutor();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = analysis(clientExecutor, analysisExecutor,
			requests(request(5297)), statuses);

		analysis.analyzeBank();
		clientExecutor.runNext();
		analysis.close();
		analysisExecutor.runNext();
		analysis.analyzeBank();

		assertEquals(1, statuses.size());
		assertEquals(BankAnalysisStatus.Kind.RUNNING, last(statuses).kind());
	}

	@Test
	public void closePreventsQueuedClientCapture()
	{
		ControlledExecutor clientExecutor = new ControlledExecutor();
		AtomicInteger captures = new AtomicInteger();
		List<BankAnalysisStatus> statuses = new ArrayList<>();
		BankAnalysis analysis = new BankAnalysis(clientExecutor, Runnable::run,
			() ->
			{
				captures.incrementAndGet();
				return Optional.of(request(5297));
			}, statuses::add, StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);

		analysis.analyzeBank();
		analysis.close();
		clientExecutor.runNext();

		assertEquals(0, captures.get());
		assertEquals(1, statuses.size());
		assertEquals(BankAnalysisStatus.Kind.RUNNING, last(statuses).kind());
	}

	@Test
	public void requestOwnsSnapshotsOfEveryMutableInputMap()
	{
		Map<Integer, GearStats> gearStats = new LinkedHashMap<>();
		Map<Integer, Integer> alchValues = new LinkedHashMap<>();
		Map<Integer, String> categoryKeys = new LinkedHashMap<>();
		GearStats capturedStats = new GearStats(GearSlot.HEAD, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		gearStats.put(5297, capturedStats);
		alchValues.put(5297, 100);
		categoryKeys.put(5297, "seeds-herbs-farming");
		BankAnalysisRequest request = new BankAnalysisRequest(snapshot(5297), gearStats,
			alchValues, categoryKeys, BankLayoutPlan.defaultFor(BankPresets.IRONMAN),
			BankLayoutOptions.DEFAULTS);

		gearStats.clear();
		alchValues.put(5297, 1);
		categoryKeys.put(5297, "cleanup-review");

		assertSame(capturedStats, request.gearStats(5297).get());
		assertEquals(100, request.alchValue(5297));
		assertEquals(Optional.of("seeds-herbs-farming"), request.categoryKey(5297));
	}

	private static BankAnalysis analysis(Executor clientExecutor, Executor analysisExecutor,
		java.util.function.Supplier<Optional<BankAnalysisRequest>> requests,
		List<BankAnalysisStatus> statuses)
	{
		return new BankAnalysis(clientExecutor, analysisExecutor, requests, statuses::add,
			StaticItemCatalog.INSTANCE, BankPresets.IRONMAN);
	}

	@SafeVarargs
	private static java.util.function.Supplier<Optional<BankAnalysisRequest>> requests(
		Optional<BankAnalysisRequest>... requests)
	{
		Deque<Optional<BankAnalysisRequest>> pending = new ArrayDeque<>(Arrays.asList(requests));
		return pending::removeFirst;
	}

	private static java.util.function.Supplier<Optional<BankAnalysisRequest>> requests(
		BankAnalysisRequest... requests)
	{
		Deque<BankAnalysisRequest> pending = new ArrayDeque<>(Arrays.asList(requests));
		return () -> Optional.of(pending.removeFirst());
	}

	private static BankAnalysisRequest request(int... itemIds)
	{
		return new BankAnalysisRequest(snapshot(itemIds), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(),
			BankLayoutPlan.defaultFor(BankPresets.IRONMAN), BankLayoutOptions.DEFAULTS);
	}

	private static BankSnapshot snapshot(int... itemIds)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int index = 0; index < itemIds.length; index++)
		{
			items.add(new BankItemSnapshot(itemIds[index], 1, index));
		}
		return new BankSnapshot(items);
	}

	private static BankAnalysisStatus last(List<BankAnalysisStatus> statuses)
	{
		return statuses.get(statuses.size() - 1);
	}

	private static final class ControlledExecutor implements Executor
	{
		private final List<Runnable> pending = new ArrayList<>();

		@Override
		public void execute(Runnable command)
		{
			pending.add(command);
		}

		private void runNext()
		{
			pending.remove(0).run();
		}

		private void runLast()
		{
			pending.remove(pending.size() - 1).run();
		}
	}
}
