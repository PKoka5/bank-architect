package com.pkoka5.ironmanbankarchitect.organize;

import com.pkoka5.ironmanbankarchitect.catalog.ItemCategory;

final class ReviewItemSorter
{
	private ReviewItemSorter()
	{
	}

	static int rank(BankPreviewItem item)
	{
		if (item.getItemCategory() == ItemCategory.UNKNOWN)
		{
			return 90;
		}

		String name = normalizedName(item.getDisplayName());
		return rankByName(name,
			group(0, "seed vault", "costume room", "poh", "stash", "armour case", "magic wardrobe",
				"treasure chest", "toy box", "cape rack"),
			group(10, "clue", "casket"),
			group(20, "graceful", "pyromancer", "prospector", "angler", "rogue", "lumberjack",
				"farmer's", "carpenter", "costume", "cosmetic", "mask", "gown", "robe top",
				"robe bottom"),
			group(30, "quest", "key", "notes", "letter", "book", "scroll", "manual", "certificate",
				"sample", "specimen", "plaque", "report", "tome", "binding book"),
			group(40, "christmas", "easter", "halloween", "holiday", "santa", "partyhat", "pumpkin",
				"snow", "event"),
			group(50, "burnt", "beer glass", "junk", "old boot", "buttons", "broken", "empty"),
			group(60, "bronze", "iron", "steel", "black", "mithril", "adamant", "rune"));
	}

	static String label(BankPreviewItem item)
	{
		int rank = rank(item);
		if (rank == 0)
		{
			return "External Storage";
		}
		if (rank == 10)
		{
			return "Clue & STASH";
		}
		if (rank == 20)
		{
			return "Cosmetics & Collection";
		}
		if (rank == 30)
		{
			return "Quest Leftovers";
		}
		if (rank == 40)
		{
			return "Holiday & Event";
		}
		if (rank == 50)
		{
			return "Burnt & Junk";
		}
		if (rank == 60)
		{
			return "Redundant Gear Review";
		}
		if (rank == 90)
		{
			return "Unknown Safe Review";
		}

		return "General Review";
	}

	private static int rankByName(String name, Group... groups)
	{
		for (Group group : groups)
		{
			for (String needle : group.needles)
			{
				if (name.contains(needle))
				{
					return group.rank;
				}
			}
		}

		return 50;
	}

	private static Group group(int rank, String... needles)
	{
		return new Group(rank, needles);
	}

	private static String normalizedName(String value)
	{
		return value == null ? "" : value.toLowerCase();
	}

	private static final class Group
	{
		private final int rank;
		private final String[] needles;

		private Group(int rank, String[] needles)
		{
			this.rank = rank;
			this.needles = needles;
		}
	}
}
