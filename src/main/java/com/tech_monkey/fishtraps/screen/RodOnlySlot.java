package com.tech_monkey.fishtraps.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RodOnlySlot extends Slot {
    public RodOnlySlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() == Items.FISHING_ROD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
