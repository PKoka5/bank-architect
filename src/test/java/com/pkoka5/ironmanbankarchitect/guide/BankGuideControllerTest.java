package com.pkoka5.ironmanbankarchitect.guide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import com.pkoka5.ironmanbankarchitect.preset.AllRoundIronmanPreset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class BankGuideControllerTest
{
	private BankGuideController controller;

	@Before
	public void setUp()
	{
		controller = new BankGuideController(AllRoundIronmanPreset.create());
	}

	@Test
	public void availableBlocksMatchPresetOrder()
	{
		List<VisualBlock> blocks = controller.getAvailableBlocks();

		assertEquals(2, blocks.size());
		assertEquals("melee-setup", blocks.get(0).getKey());
		assertEquals("Melee Setup", blocks.get(0).getName());
		assertEquals("irit-super-attack", blocks.get(1).getKey());
		assertEquals("Irit → Super attack", blocks.get(1).getName());
	}

	@Test
	public void defaultSelectedBlockIsFirstAvailableBlock()
	{
		assertEquals("melee-setup", controller.getSelectedBlock().getKey());
	}

	@Test
	public void defaultGuideIsDisabled()
	{
		assertFalse(controller.isGuideEnabled());
	}

	@Test
	public void selectBlockChangesSelectedBlock()
	{
		controller.selectBlock("irit-super-attack");

		assertEquals("irit-super-attack", controller.getSelectedBlock().getKey());
	}

	@Test
	public void selectBlockWithUnknownKeyThrows()
	{
		try
		{
			controller.selectBlock("does-not-exist");
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException expected)
		{
			// expected
		}
	}

	@Test
	public void toggleGuideFlipsEnabledState()
	{
		assertFalse(controller.isGuideEnabled());

		controller.toggleGuide();
		assertTrue(controller.isGuideEnabled());

		controller.toggleGuide();
		assertFalse(controller.isGuideEnabled());
	}

	@Test
	public void setGuideEnabledSetsExactState()
	{
		controller.setGuideEnabled(true);
		assertTrue(controller.isGuideEnabled());

		controller.setGuideEnabled(true);
		assertTrue(controller.isGuideEnabled());

		controller.setGuideEnabled(false);
		assertFalse(controller.isGuideEnabled());
	}

	@Test
	public void defaultBankOpenStateIsClosed()
	{
		assertFalse(controller.isBankOpen());
	}

	@Test
	public void setBankOpenUpdatesBankOpenState()
	{
		controller.setBankOpen(true);
		assertTrue(controller.isBankOpen());

		controller.setBankOpen(false);
		assertFalse(controller.isBankOpen());
	}

	@Test
	public void statusTextReflectsBankClosedRegardlessOfGuideState()
	{
		assertEquals(BankGuideController.BANK_CLOSED_STATUS, controller.getStatusText());

		controller.setGuideEnabled(true);
		assertEquals(BankGuideController.BANK_CLOSED_STATUS, controller.getStatusText());
	}

	@Test
	public void statusTextReflectsGuideEnabledWhenBankOpen()
	{
		controller.setBankOpen(true);
		controller.setGuideEnabled(true);
		assertEquals(BankGuideController.GUIDE_ACTIVE_STATUS, controller.getStatusText());
	}

	@Test
	public void statusTextReflectsGuideDisabledWhenBankOpen()
	{
		controller.setBankOpen(true);
		controller.setGuideEnabled(false);
		assertEquals(BankGuideController.GUIDE_DISABLED_STATUS, controller.getStatusText());
	}

	@Test
	public void statusTextExactStrings()
	{
		assertEquals("Open your bank to preview the guide.", BankGuideController.BANK_CLOSED_STATUS);
		assertEquals("Guide preview active.", BankGuideController.GUIDE_ACTIVE_STATUS);
		assertEquals("Guide preview off.", BankGuideController.GUIDE_DISABLED_STATUS);
	}
}
