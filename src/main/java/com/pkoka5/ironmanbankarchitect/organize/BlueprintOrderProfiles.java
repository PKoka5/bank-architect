package com.pkoka5.ironmanbankarchitect.organize;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Independent local orders per saved profile; encoded names cannot collide with separators. */
public final class BlueprintOrderProfiles
{
	private final Map<String, String> values = new LinkedHashMap<>();

	public static BlueprintOrderProfiles parse(String value)
	{
		BlueprintOrderProfiles result = new BlueprintOrderProfiles();
		if (value == null) return result;
		for (String part : value.split(";"))
		{
			String[] entry = part.split("~", 2);
			if (entry.length != 2) continue;
			try
			{
				String name = new String(Base64.getUrlDecoder().decode(entry[0]), StandardCharsets.UTF_8);
				result.values.put(name, entry[1]);
			}
			catch (IllegalArgumentException malformed) { /* Keep the other profiles. */ }
		}
		return result;
	}

	public BlueprintItemOrders forProfile(String name)
	{
		return BlueprintItemOrders.parse(values.get(name));
	}

	public void put(String name, BlueprintItemOrders orders)
	{
		if (orders.serialize().isEmpty()) values.remove(name);
		else values.put(name, orders.serialize());
	}

	public void remove(String name) { values.remove(name); }

	public String serialize()
	{
		StringBuilder out = new StringBuilder();
		values.forEach((name, value) -> {
			if (out.length() > 0) out.append(';');
			out.append(Base64.getUrlEncoder().withoutPadding().encodeToString(name.getBytes(StandardCharsets.UTF_8)))
				.append('~').append(value);
		});
		return out.toString();
	}
}
