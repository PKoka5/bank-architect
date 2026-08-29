package com.pkoka5.ironmanbankarchitect.organize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The player's assignment of blueprint tags to bank destinations.
 *
 * <p>There are ten destinations: the bank's main section first, then the nine
 * tabs after it. A destination may hold any number of tags, so a player can put
 * runes and teleports on separate tabs or gather several bundles onto one, and
 * it may hold none, so the main section can be kept empty as a place to dump
 * loot.</p>
 *
 * <p>The plan is keyed by tag rather than by category so every part of a bundle
 * can be placed on its own. Items are still classified and laid out per
 * category, so splitting a bundle across tabs decides where things go without
 * changing what they are.</p>
 *
 * <p>A tag the player never places still has to put its items somewhere, or they
 * would silently vanish from the blueprint. Those tags join the destination
 * holding the fallback tag, which is where unsorted items already belong. If
 * that tag is itself unplaced, the last destination takes the remainder, so
 * there is always a defined home.</p>
 *
 * <p>Parsing is deliberately forgiving: an unrecognised key is dropped, a
 * repeated one keeps its first destination, and anything missing falls to the
 * fallback destination. Plans written before tags existed named categories, and
 * a category key is still read as all of its tags, so upgrading keeps the
 * arrangement the player already had.</p>
 */
public final class BankLayoutPlan
{
	/** The main section plus the nine tabs the bank can hold. */
	public static final int DESTINATION_COUNT = 10;

	/** The main section is not a tab, so it is always the first destination. */
	public static final int MAIN_DESTINATION_INDEX = 0;

	/** Tags the player did not place land with the unsorted items. */
	public static final String FALLBACK_TAG_KEY = "cleanup";

	private static final String DESTINATION_SEPARATOR = "|";
	private static final String TAG_SEPARATOR = "+";
	private static final String LEGACY_SEPARATOR = ",";

	private final List<List<String>> destinations;

	private BankLayoutPlan(List<List<String>> destinations)
	{
		List<List<String>> copy = new ArrayList<>(destinations.size());
		for (List<String> destination : destinations)
		{
			copy.add(Collections.unmodifiableList(new ArrayList<>(destination)));
		}

		this.destinations = Collections.unmodifiableList(copy);
	}

	/**
	 * The preset's own arrangement: each category's tags together, on the
	 * destination that category occupies in the preset.
	 */
	public static BankLayoutPlan defaultFor(BankPreset preset)
	{
		Objects.requireNonNull(preset, "preset");

		List<List<String>> destinations = emptyDestinations();
		List<BankCategory> categories = preset.getCategories();
		for (int index = 0; index < categories.size() && index < DESTINATION_COUNT; index++)
		{
			for (BankTag tag : BankTags.forCategory(categories.get(index).getKey()))
			{
				destinations.get(index).add(tag.getKey());
			}
		}

		return new BankLayoutPlan(destinations);
	}

	/** The stored plan, completed against the preset so every tag has a home. */
	public static BankLayoutPlan parse(BankPreset preset, String serialized)
	{
		Objects.requireNonNull(preset, "preset");

		if (serialized == null || serialized.trim().isEmpty())
		{
			return defaultFor(preset);
		}

		return serialized.contains(DESTINATION_SEPARATOR)
			? parsePlan(preset, serialized) : parseLegacyOrder(preset, serialized);
	}

	/** Every destination, in bank order, each holding its tag keys in layout order. */
	public List<List<String>> getDestinations()
	{
		return destinations;
	}

	/** The tag keys assigned to one destination, in layout order. */
	public List<String> getTagKeys(int destinationIndex)
	{
		return destinations.get(destinationIndex);
	}

	/** The destination holding a tag, or -1 when the key is not in the plan. */
	public int destinationOf(String tagKey)
	{
		for (int index = 0; index < destinations.size(); index++)
		{
			if (destinations.get(index).contains(tagKey))
			{
				return index;
			}
		}

		return -1;
	}

	/**
	 * The same plan with one tag moved to a destination, appended after whatever
	 * that destination already holds. Moving a tag to where it already is leaves
	 * the plan untouched, so a redundant click does not quietly reorder the
	 * destination the player was looking at.
	 */
	public BankLayoutPlan withTagAt(String tagKey, int destinationIndex)
	{
		Objects.requireNonNull(tagKey, "tagKey");
		requireDestinationIndex(destinationIndex);

		if (destinationOf(tagKey) == destinationIndex)
		{
			return this;
		}

		List<List<String>> updated = mutableCopy();
		for (List<String> destination : updated)
		{
			destination.remove(tagKey);
		}
		updated.get(destinationIndex).add(tagKey);
		return new BankLayoutPlan(updated);
	}

	/**
	 * The same plan with a tag shifted within its own destination, which decides
	 * the order the tags appear in on that tab. A shift past either end is
	 * ignored rather than rejected, so pressing the button at the top of a list
	 * does nothing instead of erroring.
	 */
	public BankLayoutPlan withTagShifted(String tagKey, int offset)
	{
		Objects.requireNonNull(tagKey, "tagKey");

		int destinationIndex = destinationOf(tagKey);
		if (destinationIndex < 0 || offset == 0)
		{
			return this;
		}

		List<List<String>> updated = mutableCopy();
		List<String> destination = updated.get(destinationIndex);
		int from = destination.indexOf(tagKey);
		int to = from + offset;
		if (to < 0 || to >= destination.size())
		{
			return this;
		}

		destination.remove(from);
		destination.add(to, tagKey);
		return new BankLayoutPlan(updated);
	}

	/** True when this plan is the preset's own arrangement. */
	public boolean isDefault(BankPreset preset)
	{
		return destinations.equals(defaultFor(preset).destinations);
	}

	/**
	 * The same plan with every known tag placed somewhere.
	 *
	 * <p>Editing can leave a tag unplaced, and a stored plan can predate a tag
	 * that has since been added. Either way its items still have to go somewhere,
	 * so the remainder joins the fallback destination on the same terms as when a
	 * plan is first read.</p>
	 */
	public BankLayoutPlan completedFor(BankPreset preset)
	{
		Objects.requireNonNull(preset, "preset");

		Set<String> placed = new LinkedHashSet<>();
		for (List<String> destination : destinations)
		{
			placed.addAll(destination);
		}

		List<List<String>> completed = withRemainder(preset, mutableCopy(), placed);
		return completed.equals(destinations) ? this : new BankLayoutPlan(completed);
	}

	public String serialize()
	{
		StringBuilder builder = new StringBuilder();
		// Separators are placed by position, never by whether anything has been
		// written yet: an empty leading destination has to leave its marker or
		// reading the plan back shifts every later destination one place up.
		for (int destinationIndex = 0; destinationIndex < destinations.size(); destinationIndex++)
		{
			if (destinationIndex > 0)
			{
				builder.append(DESTINATION_SEPARATOR);
			}
			List<String> destination = destinations.get(destinationIndex);
			for (int index = 0; index < destination.size(); index++)
			{
				if (index > 0)
				{
					builder.append(TAG_SEPARATOR);
				}
				builder.append(destination.get(index));
			}
		}

		return builder.toString();
	}

	private static BankLayoutPlan parsePlan(BankPreset preset, String serialized)
	{
		Set<String> placed = new LinkedHashSet<>();
		List<List<String>> destinations = emptyDestinations();

		String[] parts = serialized.split("\\" + DESTINATION_SEPARATOR, -1);
		for (int index = 0; index < parts.length && index < DESTINATION_COUNT; index++)
		{
			for (String key : parts[index].split("\\" + TAG_SEPARATOR))
			{
				for (String tagKey : tagKeysFor(key.trim()))
				{
					if (placed.add(tagKey))
					{
						destinations.get(index).add(tagKey);
					}
				}
			}
		}

		return new BankLayoutPlan(withRemainder(preset, destinations, placed));
	}

	/**
	 * Reads the older comma-separated order, where the arrangement was only a
	 * sequence and each category occupied a destination of its own.
	 */
	private static BankLayoutPlan parseLegacyOrder(BankPreset preset, String serialized)
	{
		Set<String> placed = new LinkedHashSet<>();
		List<List<String>> destinations = emptyDestinations();

		int destinationIndex = 0;
		for (String key : serialized.split(LEGACY_SEPARATOR))
		{
			if (destinationIndex >= DESTINATION_COUNT)
			{
				break;
			}
			boolean placedAny = false;
			for (String tagKey : tagKeysFor(key.trim()))
			{
				if (placed.add(tagKey))
				{
					destinations.get(destinationIndex).add(tagKey);
					placedAny = true;
				}
			}
			if (placedAny)
			{
				destinationIndex++;
			}
		}

		return new BankLayoutPlan(withRemainder(preset, destinations, placed));
	}

	/**
	 * The tags a stored key stands for. A tag key stands for itself; a category
	 * key from a plan written before tags existed stands for all of its tags, so
	 * an upgraded plan keeps every bundle where the player had put it.
	 */
	private static List<String> tagKeysFor(String key)
	{
		if (BankTags.isKnown(key))
		{
			return Collections.singletonList(key);
		}

		List<BankTag> tags = BankTags.forCategory(key);
		if (tags.isEmpty())
		{
			return Collections.emptyList();
		}

		List<String> keys = new ArrayList<>(tags.size());
		for (BankTag tag : tags)
		{
			keys.add(tag.getKey());
		}

		return keys;
	}

	/**
	 * Gives every tag the player did not place a defined home, so nothing drops
	 * out of the blueprint. Anything still loose joins the fallback tag's
	 * destination, or the last destination when the fallback tag is itself
	 * unplaced and so cannot point anywhere.
	 */
	private static List<List<String>> withRemainder(BankPreset preset,
		List<List<String>> destinations, Set<String> placed)
	{
		List<String> remainder = new ArrayList<>();
		for (BankCategory category : preset.getCategories())
		{
			for (BankTag tag : BankTags.forCategory(category.getKey()))
			{
				if (!placed.contains(tag.getKey()))
				{
					remainder.add(tag.getKey());
				}
			}
		}
		if (remainder.isEmpty())
		{
			return destinations;
		}

		int fallbackIndex = DESTINATION_COUNT - 1;
		for (int index = 0; index < destinations.size(); index++)
		{
			if (destinations.get(index).contains(FALLBACK_TAG_KEY))
			{
				fallbackIndex = index;
				break;
			}
		}

		destinations.get(fallbackIndex).addAll(remainder);
		return destinations;
	}

	private static List<List<String>> emptyDestinations()
	{
		List<List<String>> destinations = new ArrayList<>(DESTINATION_COUNT);
		for (int index = 0; index < DESTINATION_COUNT; index++)
		{
			destinations.add(new ArrayList<String>());
		}

		return destinations;
	}

	private List<List<String>> mutableCopy()
	{
		List<List<String>> copy = new ArrayList<>(destinations.size());
		for (List<String> destination : destinations)
		{
			copy.add(new ArrayList<>(destination));
		}

		return copy;
	}

	private static void requireDestinationIndex(int destinationIndex)
	{
		if (destinationIndex < 0 || destinationIndex >= DESTINATION_COUNT)
		{
			throw new IllegalArgumentException("destinationIndex out of range: " + destinationIndex);
		}
	}
}
