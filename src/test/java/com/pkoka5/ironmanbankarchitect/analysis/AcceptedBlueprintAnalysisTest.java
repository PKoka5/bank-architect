package com.pkoka5.ironmanbankarchitect.analysis;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot;
import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** The accepted target must never replace the independently computed proposal. */
@RunWith(Parameterized.class)
public class AcceptedBlueprintAnalysisTest
{
	private static final String CONTEXT = "personal settings at acceptance";
	private final BankPreset preset;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> presets()
	{
		return Arrays.asList(new Object[] {BankPresets.IRONMAN}, new Object[] {BankPresets.MAIN});
	}

	public AcceptedBlueprintAnalysisTest(BankPreset preset) { this.preset = preset; }

	@Test public void legacyRequestsPublishTheSameObjectAsTargetAndProposal()
	{
		Session session = new Session();
		BankAnalysisRequest request = new BankAnalysisRequest(bank(5297, 209), Collections.emptyMap(),
			Collections.emptyMap(), Collections.emptyMap(), BankLayoutPlan.defaultFor(preset),
			BankLayoutOptions.defaultFor(preset), preset);
		assertEquals("", request.acceptedBlueprint());
		assertEquals("", request.acceptanceContext());
		session.submit(request);
		assertSame(session.proposal(), session.preview());
		assertEquals(2, session.preview().getPlannedItemCount());
	}

	@Test public void acceptedRoutingAndFreshProposalArePublishedTogether()
	{
		Session session = new Session();
		session.submit(request(bank(5297, 209), "", CONTEXT));
		BankOrganizationPreview natural = session.preview();
		int alchTab = BankLayoutPlan.defaultFor(preset).destinationOf("alch");
		String accepted = AcceptedBlueprint.capture(relocated(natural, 5297, alchTab, "alch"), CONTEXT);
		BankAnalysisRequest request = request(bank(5297, 209), accepted, CONTEXT);
		assertEquals(accepted, request.acceptedBlueprint());
		assertEquals(CONTEXT, request.acceptanceContext());

		session.submit(request);

		assertNotSame(session.preview(), session.proposal());
		assertEquals("alch", item(session.preview(), 5297).getLayoutTagKey());
		assertEquals(alchTab, destination(session.preview(), 5297));
		assertEquals(item(natural, 5297).getLayoutTagKey(), item(session.proposal(), 5297).getLayoutTagKey());
		assertEquals(destination(natural, 5297), destination(session.proposal(), 5297));
		assertEquals(2, session.status.get().catalogSummary().get().getTotalScannedIdCount());
		assertEquals(2, session.preview().getPlannedItemCount());
		assertEquals(2, session.proposal().getPlannedItemCount());
	}

	@Test public void newItemsAppendAfterAcceptedItemsWithoutFreezingTheirQuantities()
	{
		Session session = new Session();
		session.submit(request(bank(5298, 5299), "", CONTEXT));
		List<BankCategoryPreview> reversed = new ArrayList<>();
		for (BankCategoryPreview category : session.preview().getCategories())
		{
			List<BankPreviewItem> items = new ArrayList<>(category.getItems());
			Collections.reverse(items);
			reversed.add(new BankCategoryPreview(category.getCategory(), items));
		}
		BankOrganizationPreview saved = new BankOrganizationPreview(preset, reversed);
		String accepted = AcceptedBlueprint.capture(saved, CONTEXT);
		BankSnapshot changed = new BankSnapshot(Arrays.asList(new BankItemSnapshot(5298, 0, 0, true),
			new BankItemSnapshot(5299, 7, 1), new BankItemSnapshot(5297, 12, 2)));

		session.submit(request(changed, accepted, CONTEXT));

		int tab = destination(saved, 5298);
		List<Integer> expected = ids(saved.getCategories().get(tab));
		expected.add(5297);
		assertEquals(expected, ids(session.preview().getCategories().get(tab)));
		assertEquals(destination(session.proposal(), 5297), destination(session.preview(), 5297));
		assertTrue(item(session.preview(), 5298).isPlaceholder());
		assertEquals(0, item(session.preview(), 5298).getQuantity());
		assertEquals(7, item(session.preview(), 5299).getQuantity());
		assertEquals(12, item(session.preview(), 5297).getQuantity());
		assertEquals(3, session.proposal().getPlannedItemCount());
	}

	@Test public void changedPersonalContextUsesTheFreshProposal()
	{
		Session session = new Session();
		session.submit(request(bank(5297), "", CONTEXT));
		String accepted = AcceptedBlueprint.capture(relocated(session.preview(), 5297,
			BankLayoutPlan.defaultFor(preset).destinationOf("alch"), "alch"), CONTEXT);

		session.submit(request(bank(5297), accepted, "later personal settings"));

		assertSame(session.proposal(), session.preview());
		assertNotEquals("alch", item(session.preview(), 5297).getLayoutTagKey());
	}

	@Test public void unsupportedSnapshotFailsWithoutPublishingEitherPartialPreview()
	{
		Session session = new Session();
		session.submit(request(bank(5297), "BAkeep99|unknown", CONTEXT));
		assertEquals(BankAnalysisStatus.Kind.FAILED, session.status.get().kind());
		assertTrue(session.status.get().organizationPreviewText().contains("Saved blueprint"));
		assertFalse(session.status.get().organizationPreview().isPresent());
		assertFalse(session.status.get().proposal().isPresent());
		assertFalse(session.status.get().catalogSummary().isPresent());
	}

	@Test public void obsoleteInvalidSnapshotCannotReplaceANewerSuccessfulPair()
	{
		Deque<Runnable> pending = new ArrayDeque<>();
		Session session = new Session(pending::addLast);
		session.submit(request(bank(5297), "BAkeep99|unknown", CONTEXT));
		assertFalse(session.status.get().proposal().isPresent());
		session.submit(request(bank(5297, 209), "", CONTEXT));
		pending.removeLast().run();
		BankAnalysisStatus latest = session.status.get();
		assertEquals(BankAnalysisStatus.Kind.SUCCESS, latest.kind());
		assertSame(session.preview(), session.proposal());

		pending.removeFirst().run();

		assertSame(latest, session.status.get());
		assertEquals(2, session.proposal().getPlannedItemCount());
	}

	@Test public void acceptedAlchRoutingNeverBecomesRetainedAutomaticClassification()
	{
		Session session = new Session();
		session.submit(request(bank(5297, 209), "", CONTEXT));
		String accepted = AcceptedBlueprint.capture(relocated(session.preview(), 5297,
			BankLayoutPlan.defaultFor(preset).destinationOf("alch"), "alch"), CONTEXT);
		BankAnalysisRequest before = request(bank(5297, 209), accepted, CONTEXT);
		session.submit(before);
		assertEquals("alch", item(session.preview(), 5297).getLayoutTagKey());
		assertNotEquals("alch", item(session.proposal(), 5297).getLayoutTagKey());
		BankSnapshot withdrawn = new BankSnapshot(Arrays.asList(new BankItemSnapshot(5297, 1, 0),
			new BankItemSnapshot(209, 0, 1, true)));
		BankAnalysisRequest after = request(withdrawn, accepted, CONTEXT);
		assertTrue("Exercise the established-Alch retention path", after.sameLayoutContext(before, preset));

		session.submit(after);

		assertEquals("alch", item(session.preview(), 5297).getLayoutTagKey());
		assertNotEquals("alch", item(session.proposal(), 5297).getLayoutTagKey());
		session.submit(request(withdrawn, "", CONTEXT));
		assertSame(session.preview(), session.proposal());
		assertNotEquals("alch", item(session.preview(), 5297).getLayoutTagKey());
	}

	private BankAnalysisRequest request(BankSnapshot bank, String accepted, String context)
	{
		return new BankAnalysisRequest(bank, Collections.emptyMap(), Collections.emptyMap(),
			Collections.emptyMap(), BankLayoutPlan.defaultFor(preset), BankLayoutOptions.defaultFor(preset),
			preset, accepted, context);
	}

	private static BankSnapshot bank(int... ids)
	{
		List<BankItemSnapshot> items = new ArrayList<>();
		for (int id : ids) items.add(new BankItemSnapshot(id, 1, items.size()));
		return new BankSnapshot(items);
	}

	private static BankOrganizationPreview relocated(BankOrganizationPreview preview, int id, int tab, String tag)
	{
		List<BankCategoryPreview> categories = new ArrayList<>();
		for (int index = 0; index < preview.getCategories().size(); index++)
		{
			BankCategoryPreview category = preview.getCategories().get(index);
			List<BankPreviewItem> items = new ArrayList<>();
			for (BankPreviewItem item : category.getItems()) if (item.getItemId() != id) items.add(item);
			if (index == tab) items.add(item(preview, id).withLayoutTag(tag));
			categories.add(new BankCategoryPreview(category.getCategory(), items));
		}
		return new BankOrganizationPreview(preview.getPreset(), categories);
	}

	private static BankPreviewItem item(BankOrganizationPreview preview, int id)
	{
		return preview.getPlannedItems().stream().filter(item -> item.getItemId() == id)
			.findFirst().orElseThrow(AssertionError::new);
	}

	private static int destination(BankOrganizationPreview preview, int id)
	{
		for (int tab = 0; tab < preview.getCategories().size(); tab++)
			if (ids(preview.getCategories().get(tab)).contains(id)) return tab;
		throw new AssertionError("Item not placed: " + id);
	}

	private static List<Integer> ids(BankCategoryPreview category)
	{
		List<Integer> ids = new ArrayList<>();
		for (BankPreviewItem item : category.getItems()) if (!item.isBlank()) ids.add(item.getItemId());
		return ids;
	}

	private final class Session
	{
		private final AtomicReference<Optional<BankAnalysisRequest>> request = new AtomicReference<>();
		private final AtomicReference<BankAnalysisStatus> status = new AtomicReference<>();
		private final BankAnalysis analysis;

		private Session() { this(Runnable::run); }
		private Session(Executor background)
		{
			analysis = new BankAnalysis(Runnable::run, background, request::get, status::set,
				CompositeItemCatalog.DEFAULT, preset);
		}
		private void submit(BankAnalysisRequest next)
		{
			request.set(Optional.of(next));
			analysis.analyzeBank();
		}
		private BankOrganizationPreview preview()
		{
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			return status.get().organizationPreview().orElseThrow(AssertionError::new);
		}
		private BankOrganizationPreview proposal()
		{
			assertEquals(BankAnalysisStatus.Kind.SUCCESS, status.get().kind());
			return status.get().proposal().orElseThrow(AssertionError::new);
		}
	}
}
