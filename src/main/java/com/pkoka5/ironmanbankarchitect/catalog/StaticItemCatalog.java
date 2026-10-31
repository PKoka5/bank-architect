package com.pkoka5.ironmanbankarchitect.catalog;

import java.util.*;

/** Verified workflow seeds and supplemental identities, ahead of the generated registry. */
public final class StaticItemCatalog implements ItemCatalog
{
	public static final StaticItemCatalog INSTANCE = new StaticItemCatalog();
	private final RequiredResource<Map<Integer,CatalogItem>> items = new RequiredResource<>("seed item catalog",
		() -> ResourceItemRegistry.loadItems(StaticItemCatalog.class.getResourceAsStream("seed-items.tsv"),true));

	private StaticItemCatalog() { }
	@Override public void requireAvailable() { items.get(); }
	@Override public Optional<CatalogItem> findById(int id) { return Optional.ofNullable(items.get().get(id)); }
	public boolean containsId(int id) { return items.get().containsKey(id); }
	public int size() { return items.get().size(); }
}
