package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.*;
import java.util.function.*;
import net.runelite.client.config.ConfigManager;

/** Ironman retains the original keys; Main has its own local preferences. */
final class PresetSettings
{
	private final BiFunction<String, String, String> reader;
	private final BiConsumer<String, Object> writer;
	private final Supplier<BankPreset> preset;
	private String profile = "";

	PresetSettings(ConfigManager manager, Supplier<BankPreset> preset)
	{
		this((key, fallback) -> {
			String value = manager.getConfiguration(IronmanBankArchitectConfig.GROUP, key);
			return value == null ? fallback : value;
		}, (key, value) -> manager.setConfiguration(IronmanBankArchitectConfig.GROUP, key, value), preset);
	}

	PresetSettings(BiFunction<String, String, String> reader, BiConsumer<String, Object> writer,
		Supplier<BankPreset> preset)
	{
		this.reader = reader;
		this.writer = writer;
		this.preset = preset;
	}

	private String key(BankPreset base, String key) { return base.getType() == BankPresetType.MAIN ? "main." + key : key; }
	private String key(String key) { return key(preset.get(), profile + key); }
	String get(String key, String fallback) { return reader.apply(key(key), fallback); }
	void set(String key, Object value) { writer.accept(key(key), value); }
	String getFor(BankPreset base, String key, String fallback) { return reader.apply(key(base, key), fallback); }

	PresetSettings forProfile(String name)
	{
		PresetSettings copy = new PresetSettings(reader, writer, preset);
		copy.profile = "profile." + java.util.Base64.getUrlEncoder().withoutPadding()
			.encodeToString(name.getBytes(java.nio.charset.StandardCharsets.UTF_8)) + ".";
		return copy;
	}

	<T extends Enum<T>> T get(String key, T fallback)
	{
		try { return Enum.valueOf(fallback.getDeclaringClass(), get(key, fallback.name())); }
		catch (IllegalArgumentException invalid) { return fallback; }
	}

	boolean get(String key, boolean fallback) { return Boolean.parseBoolean(get(key, Boolean.toString(fallback))); }
}
