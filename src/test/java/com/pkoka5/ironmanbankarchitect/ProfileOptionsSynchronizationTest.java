package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileOptionsSynchronizationTest
{
	@Test public void ownSaveEventsAndExplicitAnalyzeRequestsProduceOneLatestCapture() throws Exception
	{
		Fixture f=new Fixture();
		f.model.saveProfile("Ready",f.model.plan());
		Deque<Runnable> client=new ArrayDeque<>(),background=new ArrayDeque<>();
		List<com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest> captures=new ArrayList<>();
		BankGuideController controller=new BankGuideController();
		f.set("guideController",controller);
		f.set("clientThread",new net.runelite.client.callback.ClientThread() {
			@Override public void invoke(Runnable task) { client.addLast(task); }
			@Override public void invokeLater(Runnable task) { client.addLast(task); }
		});
		com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis analysis=new com.pkoka5.ironmanbankarchitect.analysis.BankAnalysis(
			client::addLast,background::addLast,()-> {
				var request=new com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisRequest(
					new com.pkoka5.ironmanbankarchitect.bank.BankSnapshot(Collections.singletonList(
						new com.pkoka5.ironmanbankarchitect.bank.BankItemSnapshot(995,1,0))),
					Collections.emptyMap(),Collections.emptyMap(),Collections.emptyMap(),f.model.plan(),f.model.options());
				captures.add(request);return Optional.of(request);
			},controller::publishBankAnalysis,com.pkoka5.ironmanbankarchitect.catalog.CompositeItemCatalog.DEFAULT,BankPresets.IRONMAN);
		f.set("bankAnalysis",analysis);
		f.model.save(f.model.plan().withTagAt("gear",9));
		f.model.saveOptions(f.model.options().withAlchMode(AlchMode.FIXED));
		assertEquals("Writes and explicit analysis share one refresh",1,client.size());
		while(!client.isEmpty()) client.removeFirst().run();
		assertEquals(1,captures.size());assertEquals(1,background.size());
		assertEquals(9,captures.get(0).layoutPlan().destinationOf("gear"));
		assertEquals(AlchMode.FIXED,captures.get(0).layoutOptions().alchMode());
		background.removeFirst().run();
		assertEquals(com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus.Kind.SUCCESS,controller.bankAnalysisStatus().kind());
		analysis.analyzeBank();client.removeFirst().run();
		f.model.saveOptions(f.model.options().withAlchMode(AlchMode.FLEXIBLE));
		background.removeFirst().run();
		assertEquals("Old work cannot publish during a queued settings refresh",
			com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus.Kind.NOT_STARTED,controller.bankAnalysisStatus().kind());
		while(!client.isEmpty()) client.removeFirst().run();
		assertEquals(3,captures.size());assertEquals(1,background.size());background.removeFirst().run();
		assertEquals(AlchMode.FLEXIBLE,captures.get(2).layoutOptions().alchMode());
		assertEquals(com.pkoka5.ironmanbankarchitect.analysis.BankAnalysisStatus.Kind.SUCCESS,controller.bankAnalysisStatus().kind());
	}
	@Test public void incomingProfileIsDisplayedAndAnActualGlobalEditUpdatesOnlyThatProfile() throws Exception
	{
		Fixture f=new Fixture();
		f.model.saveProfile("A",f.model.plan());
		f.model.saveOptions(f.model.options().withAlchMode(AlchMode.FIXED));
		f.model.saveProfile("B",f.model.plan());
		f.model.saveOptions(f.model.options().withAlchMode(AlchMode.FLEXIBLE));
		List<String> profiles=new ArrayList<>(f.model.profileNames());
		f.model.selectProfile("A");
		assertEquals("FIXED",f.values.get("alchMode"));
		f.model.selectProfile("B");
		assertEquals("FLEXIBLE",f.values.get("alchMode"));
		f.write("alchMode",AlchMode.FIXED);
		assertEquals(AlchMode.FIXED,f.model.options().alchMode());
		assertEquals("B",f.model.matchingProfile());
		assertEquals(profiles,f.model.profileNames());
		f.model.selectProfile("A");
		assertEquals(AlchMode.FIXED,f.model.options().alchMode());
		f.model.selectProfile("B");
		assertEquals(AlchMode.FIXED,f.model.options().alchMode());
	}

	@Test public void builtinsKeepIndependentModesWithoutCreatingCustomsAndSynchronizeOtherOptions() throws Exception
	{
		Fixture f=new Fixture();
		f.model.saveOptions(f.model.options().withAlchMode(AlchMode.FIXED));
		assertEquals("FIXED",f.values.get("alchMode"));
		f.model.selectPreset(BankPresetType.MAIN);
		assertEquals("FLEXIBLE",f.values.get("alchMode"));
		assertEquals("false",f.values.get("gatherFrequentlyUsed"));
		assertEquals("Main",f.model.selectedPresetChoice());
		f.model.selectPreset(BankPresetType.IRONMAN);
		assertEquals("FIXED",f.values.get("alchMode"));
		assertEquals("true",f.values.get("gatherFrequentlyUsed"));
		assertEquals("Ironman",f.model.selectedPresetChoice());
	}

	private static final class Fixture
	{
		final Map<String,String> values=new LinkedHashMap<>();
		final IronmanBankArchitectPlugin plugin=new IronmanBankArchitectPlugin();
		final BankLayoutModel model;
		Fixture() throws Exception
		{
			values.put("customPresetsReady","true"); values.put("main.customPresetsReady","true");
			IronmanBankArchitectConfig config=(IronmanBankArchitectConfig)Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[]{IronmanBankArchitectConfig.class},(proxy,method,args)-> {
					if(method.getName().equals("bankPreset"))return values.getOrDefault("bankPreset","IRONMAN");
					if(method.getName().equals("setBankPreset")){write("bankPreset",args[0]);return null;}
					return method.isDefault()?MethodHandles.privateLookupIn(method.getDeclaringClass(),MethodHandles.lookup())
						.unreflectSpecial(method,method.getDeclaringClass()).bindTo(proxy).invokeWithArguments():null;
				});
			set("config",config);
			set("settings",new PresetSettings((key,fallback)->values.getOrDefault(key,fallback),this::write,
				()->"MAIN".equals(values.get("bankPreset"))?BankPresets.MAIN:BankPresets.IRONMAN));
			set("guideController",new BankGuideController());
			Method factory=IronmanBankArchitectPlugin.class.getDeclaredMethod("bankLayoutModel");
			factory.setAccessible(true);model=(BankLayoutModel)factory.invoke(plugin);
		}
		void write(String key,Object value)
		{
			String text=value instanceof Enum?((Enum<?>)value).name():value.toString();
			String previous=values.put(key,text);
			if(Objects.equals(previous,text))return;
			ConfigChanged event=new ConfigChanged();event.setGroup(IronmanBankArchitectConfig.GROUP);
			event.setKey(key);event.setOldValue(previous);event.setNewValue(text);plugin.onConfigChanged(event);
		}
		void set(String key,Object value)throws Exception {Field field=IronmanBankArchitectPlugin.class.getDeclaredField(key);field.setAccessible(true);field.set(plugin,value);}
	}
}
