package com.pkoka5.ironmanbankarchitect.overlay;

import static com.pkoka5.ironmanbankarchitect.overlay.BankOverlayGeometry.isSafeGeometry;
import static com.pkoka5.ironmanbankarchitect.overlay.BankOverlayGeometry.itemViewportBounds;

import com.pkoka5.ironmanbankarchitect.IronmanBankArchitectConfig;
import com.pkoka5.ironmanbankarchitect.bank.BankItemIds;
import com.pkoka5.ironmanbankarchitect.guide.BankGuideController;
import com.pkoka5.ironmanbankarchitect.organize.BankOrganizationPreview;
import com.pkoka5.ironmanbankarchitect.organize.CategoryPalette;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Tints every bank item with the colour of the tab the blueprint sends it to.
 *
 * <p>This is the one piece of guidance that needs nothing but a scan: it makes
 * the plan visible in the bank itself before the player commits to moving
 * anything. It draws only, so unlike the move guide it does not care which bank
 * view, rearrange mode, or tag filter is active.</p>
 */
public final class BankCategoryOverlay extends Overlay
{
	private static final int LEGEND_PADDING = 6;
	private static final int LEGEND_SWATCH = 8;
	private static final int LEGEND_GAP = 8;
	private static final Color LEGEND_BACKGROUND = new Color(0, 0, 0, 205);
	private static final Color LEGEND_FOREGROUND = new Color(255, 255, 255, 235);

	private final Client client;
	private final BankGuideController guideController;
	private final IronmanBankArchitectConfig config;
	private final BankOverlayReservations reservations;
	private final Map<Integer, Integer> canonicalItemIdCache = new HashMap<>();
	private BankOrganizationPreview cachedPreview;
	private PlannedCategoryIndex cachedIndex;

	public BankCategoryOverlay(Plugin plugin, Client client, BankGuideController guideController,
		IronmanBankArchitectConfig config, BankOverlayReservations reservations)
	{
		super(plugin);
		this.client = client;
		this.guideController = guideController;
		this.config = config;
		this.reservations = reservations;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(false);
		setResizable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		reservations.clearLegend();
		if (!config.showCategoryOverlay())
		{
			return null;
		}

		Widget bankItems = client.getWidget(InterfaceID.Bankmain.ITEMS);
		if (bankItems == null || bankItems.isHidden())
		{
			return null;
		}

		BankOrganizationPreview preview = guideController.getLatestOrganizationPreview();
		if (preview == null)
		{
			return null;
		}

		Widget bankItemsContainer = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		Rectangle gridBounds = itemViewportBounds(bankItems.getBounds(),
			bankItemsContainer == null || bankItemsContainer.isHidden()
				? null : bankItemsContainer.getBounds());
		if (!isSafeGeometry(gridBounds))
		{
			return null;
		}

		refreshIndex(preview);
		int opacity = config.categoryOverlayOpacity();
		TreeSet<Integer> shownCategories = new TreeSet<>();

		Graphics2D bankGraphics = (Graphics2D) graphics.create();
		try
		{
			bankGraphics.clip(gridBounds);
			bankGraphics.setStroke(new BasicStroke(2f));
			for (Widget child : itemWidgets(bankItems))
			{
				if (child == null || child.isHidden() || child.getItemId() <= 0)
				{
					continue;
				}
				Rectangle bounds = child.getBounds();
				if (!isSafeGeometry(bounds) || !gridBounds.intersects(bounds))
				{
					continue;
				}
				int categoryIndex = cachedIndex.categoryIndexFor(canonicalItemId(child.getItemId()));
				if (categoryIndex < 0)
				{
					continue;
				}
				shownCategories.add(categoryIndex);
				bankGraphics.setColor(CategoryPalette.colorFor(cachedIndex.paletteIndex(categoryIndex), opacity));
				bankGraphics.fill(bounds);
				bankGraphics.setColor(CategoryPalette.colorFor(cachedIndex.paletteIndex(categoryIndex)));
				bankGraphics.draw(bounds);
			}
		}
		finally
		{
			bankGraphics.dispose();
		}

		if (!shownCategories.isEmpty())
		{
			drawLegend(graphics, gridBounds, shownCategories);
		}
		return null;
	}

	private void refreshIndex(BankOrganizationPreview preview)
	{
		if (cachedPreview != preview || cachedIndex == null)
		{
			cachedPreview = preview;
			cachedIndex = PlannedCategoryIndex.from(preview);
			canonicalItemIdCache.clear();
		}
	}

	private Widget[] itemWidgets(Widget container)
	{
		Widget[] children = container.getChildren();
		return children == null ? container.getDynamicChildren() : children;
	}

	private int canonicalItemId(int itemId)
	{
		Integer cached = canonicalItemIdCache.get(itemId);
		if (cached != null)
		{
			return cached;
		}
		ItemComposition composition = client.getItemDefinition(itemId);
		int canonical = BankItemIds.canonical(itemId,
			composition == null ? -1 : composition.getPlaceholderTemplateId(),
			composition == null ? -1 : composition.getPlaceholderId());
		canonicalItemIdCache.put(itemId, canonical);
		return canonical;
	}

	/**
	 * Colours mean nothing without names, so the legend lists only the
	 * destinations actually on screen. It is placed beside the bank when the
	 * canvas has room, because covering bank slots would defeat the overlay.
	 */
	private void drawLegend(Graphics2D graphics, Rectangle gridBounds,
		Iterable<Integer> categoryIndexes)
	{
		Graphics2D legendGraphics = (Graphics2D) graphics.create();
		try
		{
			BankOverlayText.prepare(legendGraphics, BankOverlayText.regularFont());
			FontMetrics metrics = legendGraphics.getFontMetrics();

			int lines = 0;
			int textWidth = 0;
			for (int categoryIndex : categoryIndexes)
			{
				lines++;
				textWidth = Math.max(textWidth,
					metrics.stringWidth(cachedIndex.categoryName(categoryIndex)));
			}

			// Line height follows the font so the legend keeps its shape if the
			// client's font ever changes size.
			int lineHeight = metrics.getHeight();
			int width = LEGEND_PADDING * 2 + LEGEND_SWATCH + LEGEND_GAP + textWidth;
			int height = LEGEND_PADDING * 2 + lines * lineHeight;
			Rectangle legend = legendBounds(gridBounds, width, height);
			reservations.reserveLegend(legend);

			legendGraphics.setColor(LEGEND_BACKGROUND);
			legendGraphics.fillRect(legend.x, legend.y, legend.width, legend.height);

			int y = legend.y + LEGEND_PADDING;
			for (int categoryIndex : categoryIndexes)
			{
				legendGraphics.setColor(CategoryPalette.colorFor(cachedIndex.paletteIndex(categoryIndex)));
				legendGraphics.fillRect(legend.x + LEGEND_PADDING,
					y + (lineHeight - LEGEND_SWATCH) / 2, LEGEND_SWATCH, LEGEND_SWATCH);
				BankOverlayText.draw(legendGraphics, cachedIndex.categoryName(categoryIndex),
					legend.x + LEGEND_PADDING + LEGEND_SWATCH + LEGEND_GAP,
					y + metrics.getAscent(), LEGEND_FOREGROUND);
				y += lineHeight;
			}
		}
		finally
		{
			legendGraphics.dispose();
		}
	}

	/**
	 * Prefers the free canvas to the right of the bank, then the left, and only
	 * falls back to overlapping the bank when neither side fits.
	 */
	Rectangle legendBounds(Rectangle gridBounds, int width, int height)
	{
		return legendBounds(gridBounds, width, height, client.getCanvasWidth(),
			client.getCanvasHeight());
	}

	static Rectangle legendBounds(Rectangle gridBounds, int width, int height,
		int canvasWidth, int canvasHeight)
	{
		int x;
		if (gridBounds.x + gridBounds.width + LEGEND_GAP + width <= canvasWidth)
		{
			x = gridBounds.x + gridBounds.width + LEGEND_GAP;
		}
		else if (gridBounds.x - LEGEND_GAP - width >= 0)
		{
			x = gridBounds.x - LEGEND_GAP - width;
		}
		else
		{
			x = Math.max(0, Math.min(gridBounds.x, canvasWidth - width));
		}

		int y = Math.max(0, Math.min(gridBounds.y, canvasHeight - height));
		return new Rectangle(x, y, width, height);
	}
}
