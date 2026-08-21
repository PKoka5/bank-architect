package com.pkoka5.ironmanbankarchitect;

import com.pkoka5.ironmanbankarchitect.organize.BankCategory;
import com.pkoka5.ironmanbankarchitect.organize.BankPresets;
import java.util.List;

/**
 * Reads and stores the player's blueprint tab order for the sidebar.
 *
 * <p>Kept behind an interface so the panel can be built and tested without a
 * live config, in the same way the panel takes its analyze and reset actions as
 * callbacks.</p>
 */
interface TabOrderModel
{
	/** The preset's own order, with saving ignored. Used when no config is wired up. */
	TabOrderModel DEFAULT = new TabOrderModel()
	{
		@Override
		public List<BankCategory> categories()
		{
			return BankPresets.IRONMAN.getCategories();
		}

		@Override
		public void save(List<String> keys)
		{
		}
	};

	/** Every destination, in the order the player arranged them. */
	List<BankCategory> categories();

	/** Stores a new order and rebuilds the blueprint from it. */
	void save(List<String> keys);
}
