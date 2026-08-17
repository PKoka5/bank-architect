package com.pkoka5.ironmanbankarchitect.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import net.runelite.client.ui.FontManager;

/**
 * Text rendering shared by the bank overlays.
 *
 * <p>Overlay text uses the client's own RuneScape fonts rather than a derived
 * AWT default. Those fonts are pixel designs: rendering them with the
 * antialiasing the overlay renderer switches on for shapes leaves small labels
 * looking soft and smeared, so text antialiasing is turned off while they are
 * drawn. The one-pixel black drop shadow matches the rest of the client and
 * keeps labels readable over both bright items and dark backgrounds.</p>
 */
final class BankOverlayText
{
	private BankOverlayText()
	{
	}

	/** Regular game font, for status text and legends. */
	static Font regularFont()
	{
		return FontManager.getRunescapeFont();
	}

	/** Bold game font, for the guidance heading. */
	static Font boldFont()
	{
		return FontManager.getRunescapeBoldFont();
	}

	/** Narrower game font, for labels that must fit inside a bank slot. */
	static Font smallFont()
	{
		return FontManager.getRunescapeSmallFont();
	}

	/** Selects a game font and the hints it needs to stay crisp. */
	static void prepare(Graphics2D graphics, Font font)
	{
		graphics.setFont(font);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	/** Draws {@code text} with the client's usual one-pixel drop shadow. */
	static void draw(Graphics2D graphics, String text, int x, int y, Color color)
	{
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
