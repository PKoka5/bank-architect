package com.pkoka5.ironmanbankarchitect.match;

import com.pkoka5.ironmanbankarchitect.bank.BankSnapshot;
import com.pkoka5.ironmanbankarchitect.blueprint.BlueprintSlot;
import com.pkoka5.ironmanbankarchitect.blueprint.SlotKind;
import com.pkoka5.ironmanbankarchitect.blueprint.VisualBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BankSlotMatcher
{
	private BankSlotMatcher()
	{
	}

	public static BlockMatchResult match(VisualBlock block, BankSnapshot snapshot)
	{
		if (block == null)
		{
			throw new IllegalArgumentException("block must not be null");
		}
		if (snapshot == null)
		{
			throw new IllegalArgumentException("snapshot must not be null");
		}

		List<SlotMatchResult> results = new ArrayList<>();
		for (BlueprintSlot slot : block.getSlots())
		{
			results.add(matchSlot(slot, snapshot));
		}

		return new BlockMatchResult(block.getKey(), results);
	}

	private static SlotMatchResult matchSlot(BlueprintSlot slot, BankSnapshot snapshot)
	{
		if (slot.getKind() == SlotKind.EMPTY)
		{
			return new SlotMatchResult(slot.getKey(), slot.getLabel(), SlotMatchState.RESERVED_EMPTY, 0, 0);
		}

		Optional<SlotItemMapping> mapping = HardcodedSlotItemMappings.forSlotKey(slot.getKey());
		if (!mapping.isPresent())
		{
			return new SlotMatchResult(slot.getKey(), slot.getLabel(), SlotMatchState.ROLE_ONLY, 0, 0);
		}

		SlotItemMapping mappedItem = mapping.get();
		int quantity = snapshot.getTotalQuantity(mappedItem.getItemId());
		SlotMatchState state = quantity > 0 ? SlotMatchState.OWNED : SlotMatchState.MISSING;
		return new SlotMatchResult(slot.getKey(), slot.getLabel(), state, mappedItem.getItemId(), quantity);
	}
}
