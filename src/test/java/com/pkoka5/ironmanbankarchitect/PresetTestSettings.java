package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankPreset;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.client.config.ConfigItem;

/** Connects existing config proxies to the preset storage seam without a mocking dependency. */
final class PresetTestSettings
{
	private PresetTestSettings() {}

	static void attach(IronmanBankArchitectPlugin plugin, IronmanBankArchitectConfig config) throws Exception
	{
		attach(plugin, config, new LinkedHashMap<>());
	}

	static void attach(IronmanBankArchitectPlugin plugin, IronmanBankArchitectConfig config,
		Map<String, String> mainValues) throws Exception
	{
		Map<String, Method> readers = new LinkedHashMap<>();
		Map<String, Method> writers = new LinkedHashMap<>();
		for (Method method : IronmanBankArchitectConfig.class.getMethods())
		{
			ConfigItem item = method.getAnnotation(ConfigItem.class);
			if (item == null) continue;
			if (method.getParameterCount() == 0) readers.put(item.keyName(), method);
			else if (method.getParameterCount() == 1) writers.put(item.keyName(), method);
		}
		Method activePreset = IronmanBankArchitectPlugin.class.getDeclaredMethod("activePreset");
		activePreset.setAccessible(true);
		PresetSettings settings = new PresetSettings((key, fallback) -> {
			if (key.startsWith("main.")) return mainValues.getOrDefault(key, fallback);
			Method reader = readers.get(key);
			if (reader == null) return mainValues.getOrDefault(key, fallback);
			try
			{
				Object value = reader.invoke(config);
				return value == null ? fallback : serialized(value);
			}
			catch (InvocationTargetException failure)
			{
				// Older narrow proxies return null for unsupported primitive getters.
				if (failure.getCause() instanceof NullPointerException) return fallback;
				throw new AssertionError(failure.getCause());
			}
			catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
		}, (key, value) -> {
			if (key.startsWith("main."))
			{
				mainValues.put(key, serialized(value));
				return;
			}
			Method writer = writers.get(key);
			if (writer == null)
			{
				mainValues.put(key, serialized(value));
				return;
			}
			try { writer.invoke(config, value); }
			catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
		}, () -> {
			try { return (BankPreset) activePreset.invoke(plugin); }
			catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
		});
		Field field = IronmanBankArchitectPlugin.class.getDeclaredField("settings");
		field.setAccessible(true);
		field.set(plugin, settings);
	}

	private static String serialized(Object value)
	{
		return value instanceof Enum ? ((Enum<?>) value).name() : value.toString();
	}
}
