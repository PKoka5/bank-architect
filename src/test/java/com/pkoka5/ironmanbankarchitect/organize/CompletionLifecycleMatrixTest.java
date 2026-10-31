package com.pkoka5.ironmanbankarchitect.organize;

import static org.junit.Assert.*;

import com.pkoka5.ironmanbankarchitect.analysis.*;
import com.pkoka5.ironmanbankarchitect.bank.*;
import com.pkoka5.ironmanbankarchitect.catalog.*;
import com.pkoka5.ironmanbankarchitect.guide.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Domain lifecycle matrix; real plugin persistence/events are covered by AcceptedBlueprintLifecycleTest. */
@RunWith(Parameterized.class)
public class CompletionLifecycleMatrixTest
{
    enum Mode { AUTOMATIC, ACCEPTED, CAPTURED }
    enum Event { REPEAT, ADD, PLACEHOLDER, REMOVE_RETURN, MOVE_TAB, RELOAD_PROFILE }
    private final Fixture fixture;
    private final BankPreset preset;
    private final Mode mode;
    private final Event event;

    @Parameterized.Parameters(name="{0}/{1}/{2}/{3}")
    public static Collection<Object[]> cases() throws Exception
    {
        List<Fixture> banks = new ArrayList<>();
        for (String file : Arrays.asList("real-bank-fixture-2026-07-19.tsv", "reported-midgame-combat-85.tsv",
            "reported-void-combat-94.tsv", "main-bank-combat-2026-10-12.tsv")) banks.add(Fixture.load(file));
        Fixture beginner = new Fixture("beginner");
        // Reviewed ordinary early-game gear/tools; bundled catalog supplies names and classifications.
        for (int id : new int[]{1153,1115,1067,1323,1191,1351,2347,1755,315,556,995})
        {
            assertTrue(CompositeItemCatalog.DEFAULT.findById(id).isPresent());
            beginner.items.add(new BankItemSnapshot(id,id == 995 ? 1500 : id == 556 ? 50 : 1,beginner.items.size()));
        }
        banks.add(beginner);
        List<Object[]> result = new ArrayList<>();
        for (Fixture bank : banks) for (BankPreset preset : Arrays.asList(BankPresets.IRONMAN,BankPresets.MAIN))
            for (Mode mode : Mode.values()) for (Event event : Event.values()) result.add(new Object[]{bank,preset,mode,event});
        assertEquals(180,result.size());
        return result;
    }

    public CompletionLifecycleMatrixTest(Fixture fixture, BankPreset preset, Mode mode, Event event)
    { this.fixture=fixture; this.preset=preset; this.mode=mode; this.event=event; }

    @Test public void lifecyclePreservesBankFactsAndTheSelectedLayoutContract()
    {
        Harness h = new Harness();
        BankOrganizationPreview baseline = h.analyze(fixture.items);
        if (mode == Mode.ACCEPTED) h.accepted = AcceptedBlueprint.capture(baseline,h.context());
        if (mode == Mode.CAPTURED)
        {
            Map<Integer,List<Integer>> physical = new LinkedHashMap<>();
            for (int tab=0;tab<10;tab++)
            {
                List<Integer> ids = ids(baseline,tab); Collections.reverse(ids);
                physical.put((tab+1)%10,ids);
            }
            h.orders = BlueprintItemOrders.capture(physical,baseline,BlueprintItemOrders.EMPTY);
        }
        baseline = h.analyze(fixture.items);
        String originalAccepted=h.accepted, originalOrders=h.orders.serialize();
        int changed = fixture.items.stream().filter(i -> i.getQuantity()>0)
            .filter(i -> fixture.catalog().describeOrUnknown(i.getItemId()).getCategory()==ItemCategory.GEAR)
            .findFirst().orElse(fixture.items.get(0)).getItemId();
        switch (event)
        {
            case REPEAT:
                assertEquals(signature(baseline),signature(h.analyze(fixture.items)));
                break;
            case ADD:
                Set<Integer> owned=fixture.items.stream().map(BankItemSnapshot::getItemId).collect(Collectors.toSet());
                int known=Arrays.stream(new int[]{34024,34401,233,1963,379,385,526,1511,561,560})
                    .filter(id -> !owned.contains(id)).findFirst().orElseThrow(AssertionError::new);
                assertTrue(fixture.catalog().findById(known).isPresent());
                int unknown=987654321;
                assertFalse(fixture.catalog().findById(unknown).isPresent());
                for (int id : new int[]{known,unknown})
                {
                    List<BankItemSnapshot> added=new ArrayList<>(fixture.items);
                    added.add(new BankItemSnapshot(id,7,added.size()));
                    BankOrganizationPreview next=h.analyze(added);
                    assertKeptOrder(baseline,next);
                    if (mode!=Mode.AUTOMATIC)
                    {
                        int tab=destination(h.status.get().proposal().get(),id);
                        assertEquals(tab,destination(next,id));
                        assertEquals(Integer.valueOf(id),ids(next,tab).get(ids(next,tab).size()-1));
                    }
                    assertEquals(signature(next),signature(h.analyze(added)));
                }
                break;
            case PLACEHOLDER:
                BankOrganizationPreview withdrawn=h.analyze(changed(changed,true));
                assertKeptOrder(baseline,withdrawn);
                if (mode!=Mode.AUTOMATIC) assertEquals(signature(baseline),signature(withdrawn));
                assertKeptOrder(baseline,h.analyze(fixture.items));
                break;
            case REMOVE_RETURN:
                BankOrganizationPreview absent=h.analyze(changed(changed,false));
                assertEquals(-1,destination(absent,changed));
                assertKeptOrder(baseline,absent);
                BankOrganizationPreview returned=h.analyze(fixture.items);
                assertKeptOrder(baseline,returned);
                if (mode!=Mode.AUTOMATIC) assertEquals(signature(baseline),signature(returned));
                break;
            case MOVE_TAB:
                String previous=h.context();
                h.plan=h.plan.withDestinationMoved(1,3,preset);
                h.orders=h.orders.withDestinationMoved(1,3);
                h.accepted=AcceptedBlueprint.withDestinationMoved(h.accepted,previous,h.context(),1,3);
                BankOrganizationPreview moved=h.analyze(fixture.items);
                for (int tab=0;tab<10;tab++)
                {
                    int target=BankLayoutPlan.destinationAfterMove(tab,1,3);
                    if (mode==Mode.AUTOMATIC) assertEquals(new HashSet<>(ids(baseline,tab)),new HashSet<>(ids(moved,target)));
                    else assertEquals(ids(baseline,tab),ids(moved,target));
                }
                break;
            case RELOAD_PROFILE:
                String name="Working", other="Other";
                BankLayoutProfiles profiles=BankLayoutProfiles.parse("",name,BankLayoutProfiles.defaultName(preset))
                    .withProfile(name,h.plan.serialize()).withProfile(other,h.plan.withTagAt("tools",9).serialize());
                BlueprintOrderProfiles storedOrders=BlueprintOrderProfiles.parse("");
                storedOrders.put(name,h.orders);
                String serializedOrders=storedOrders.serialize();
                Map<String,String> snapshots=new LinkedHashMap<>();
                snapshots.put(name,h.accepted); snapshots.put(other,"");
                h.plan=BankLayoutPlan.parse(preset,profiles.activePlan());
                h.orders=BlueprintOrderProfiles.parse(serializedOrders).forProfile(other); h.accepted=snapshots.get(other);
                h.restart(); h.analyze(fixture.items);
                profiles=BankLayoutProfiles.parse(profiles.serialize(),name,BankLayoutProfiles.defaultName(preset));
                h.plan=BankLayoutPlan.parse(preset,profiles.activePlan());
                h.orders=BlueprintOrderProfiles.parse(serializedOrders).forProfile(name); h.accepted=snapshots.get(name);
                h.restart();
                assertEquals(signature(baseline),signature(h.analyze(fixture.items)));
                assertEquals(originalAccepted,h.accepted); assertEquals(originalOrders,h.orders.serialize());
                break;
            default: throw new AssertionError(event);
        }
        if (event!=Event.MOVE_TAB)
        {
            assertEquals("Bank changes must not rewrite saved targets",originalAccepted,h.accepted);
            assertEquals(originalOrders,h.orders.serialize());
        }
        h.analysis.close();
    }

    private List<BankItemSnapshot> changed(int id, boolean placeholder)
    {
        List<BankItemSnapshot> result=new ArrayList<>();
        for (BankItemSnapshot item : fixture.items)
            if (item.getItemId()!=id) result.add(item);
            else if (placeholder) result.add(new BankItemSnapshot(id,0,item.getSlotIndex(),true));
        return result;
    }

    private void assertKeptOrder(BankOrganizationPreview before, BankOrganizationPreview after)
    {
        if (mode==Mode.AUTOMATIC) return;
        Set<Integer> present=after.getPlannedItems().stream().filter(i -> !i.isBlank())
            .map(BankPreviewItem::getItemId).collect(Collectors.toSet());
        Set<Integer> old=before.getPlannedItems().stream().filter(i -> !i.isBlank())
            .map(BankPreviewItem::getItemId).collect(Collectors.toSet());
        for (int tab=0;tab<10;tab++) assertEquals(ids(before,tab).stream().filter(present::contains).collect(Collectors.toList()),
            ids(after,tab).stream().filter(old::contains).collect(Collectors.toList()));
    }

    private static List<Integer> ids(BankOrganizationPreview preview,int tab)
    { return preview.getCategories().get(tab).getItems().stream().filter(i -> !i.isBlank()).map(BankPreviewItem::getItemId).collect(Collectors.toList()); }
    private static List<List<Integer>> signature(BankOrganizationPreview preview)
    { List<List<Integer>> out=new ArrayList<>(); for(int tab=0;tab<10;tab++) out.add(ids(preview,tab)); return out; }
    private static int destination(BankOrganizationPreview preview,int id)
    { for(int tab=0;tab<10;tab++) if(ids(preview,tab).contains(id)) return tab; return -1; }
    private static void assertCompleteGuide(BankOrganizationPreview preview)
    {
        BankTabPlan plan=BankTabPlan.fromPreview(preview);
        int[] actual=plan.getFlattenedItems().stream().mapToInt(BankPreviewItem::getItemId).toArray();
        int[] counts=new int[9];
        for(BankTabPlan.TargetTab tab:plan.getNumberedTabs()) counts[tab.getBankTabNumber()-1]=tab.getItems().size();
        for(RearrangeMode mode:RearrangeMode.values())
            assertEquals(TabRouteAdvisor.Status.COMPLETE,TabRouteAdvisor.assess(actual,plan,counts,0,mode).getStatus());
    }

    private final class Harness
    {
        BankLayoutPlan plan=BankLayoutPlan.defaultFor(preset);
        BlueprintItemOrders orders=BlueprintItemOrders.EMPTY;
        String accepted="";
        final AtomicReference<Optional<BankAnalysisRequest>> request=new AtomicReference<>();
        final AtomicReference<BankAnalysisStatus> status=new AtomicReference<>();
        BankAnalysis analysis;
        Harness() { restart(); }
        String context() { return preset.getType()+"|"+plan.serialize(); }
        void restart()
        {
            if(analysis!=null) analysis.close();
            analysis=new BankAnalysis(Runnable::run,Runnable::run,request::get,status::set,fixture.catalog(),preset);
        }
        BankOrganizationPreview analyze(List<BankItemSnapshot> items)
        {
            request.set(Optional.of(new BankAnalysisRequest(new BankSnapshot(items),fixture.stats,Collections.emptyMap(),
                Collections.emptyMap(),plan,BankLayoutOptions.defaultFor(preset).withItemOrders(orders),preset,accepted,context())));
            analysis.analyzeBank();
            assertEquals(status.get().organizationPreviewText(),BankAnalysisStatus.Kind.SUCCESS,status.get().kind());
            BankOrganizationPreview preview=status.get().organizationPreview().get();
            Map<Integer,List<Integer>> expected=new TreeMap<>(), actual=new TreeMap<>();
            for(BankItemSnapshot item:items) expected.computeIfAbsent(item.getItemId(),k->new ArrayList<>()).add(item.getQuantity());
            for(BankPreviewItem item:preview.getPlannedItems()) if(!item.isBlank())
            {
                actual.computeIfAbsent(item.getItemId(),k->new ArrayList<>()).add(item.getQuantity());
                assertEquals(item.getQuantity()==0,item.isPlaceholder());
            }
            expected.values().forEach(Collections::sort); actual.values().forEach(Collections::sort);
            assertEquals("Item quantities and occurrences are conserved",expected,actual);
            assertCompleteGuide(preview);
            return preview;
        }
    }

    private static final class Fixture
    {
        final String name;
        final List<BankItemSnapshot> items=new ArrayList<>();
        final Map<Integer,CatalogItem> definitions=new LinkedHashMap<>();
        final Map<Integer,GearStats> stats=new LinkedHashMap<>();
        Fixture(String name) { this.name=name; }
        ItemCatalog catalog() { return id -> definitions.containsKey(id) ? Optional.of(definitions.get(id)) : CompositeItemCatalog.DEFAULT.findById(id); }
        @Override public String toString() { return name; }
        static Fixture load(String file) throws Exception
        {
            Fixture fixture=new Fixture(file);
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(
                CompletionLifecycleMatrixTest.class.getResourceAsStream(file),StandardCharsets.UTF_8)))
            {
                for(String line=reader.readLine();line!=null;line=reader.readLine())
                {
                    if(line.isEmpty() || line.startsWith("#") || line.startsWith("itemId")) continue;
                    String[] row=line.split("\t",-1); int id=Integer.parseInt(row[0]);
                    int quantity=1;
                    if(file.startsWith("reported-midgame"))
                    {
                        quantity=Integer.parseInt(row[4]);
                        fixture.definitions.put(id,new CatalogItem(id,row[1],ItemCategory.valueOf(row[2]),row[3],Collections.emptySet(),null));
                        int[] v=Arrays.stream(row[5].split(",")).mapToInt(Integer::parseInt).toArray();
                        assertEquals(16,v.length);
                        fixture.stats.put(id,new GearStats(GearSlot.fromRuneLiteSlot(v[0]),v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13],v[14],v[15]));
                    }
                    else if(file.startsWith("main-bank"))
                    {
                        quantity=Boolean.parseBoolean(row[1])?0:1;
                        fixture.definitions.put(id,new CatalogItem(id,row[4],ItemCategory.valueOf(row[2]),row[3],Collections.emptySet(),null));
                    }
                    else if(file.startsWith("reported-void")) quantity=Integer.parseInt(row[1]);
                    assertFalse("fixture IDs must be unique",fixture.items.stream().anyMatch(i -> i.getItemId()==id));
                    fixture.items.add(new BankItemSnapshot(id,quantity,fixture.items.size(),quantity==0));
                }
            }
            return fixture;
        }
    }
}
