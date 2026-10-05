package com.tech_monkey.fishtraps.screen;

import com.tech_monkey.fishtraps.blockentity.FishTrapBlockEntity;
import com.tech_monkey.fishtraps.registry.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FishTrapScreenHandler extends AbstractContainerMenu {
    private static final int OUT_X = 26;
    private static final int OUT_Y = 17;
    private static final int ROD_X = 134;
    private static final int ROD_Y = 35;
    private static final int CONTAINER_SLOTS = FishTrapBlockEntity.INV_SIZE;

    private final Container container;
    private final ContainerData data;

    public FishTrapScreenHandler(int containerId, Inventory playerInventory, BlockPos ignoredPos) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SLOTS), new SimpleContainerData(3));
    }

    public FishTrapScreenHandler(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModScreenHandlers.FISH_TRAP, containerId);
        checkContainerSize(container, CONTAINER_SLOTS);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;

        this.addSlot(new RodOnlySlot(container, FishTrapBlockEntity.SLOT_ROD, ROD_X, ROD_Y));

        int index = FishTrapBlockEntity.OUTPUT_START;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(container, index++, OUT_X + col * 18, OUT_Y + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        int inventoryY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, inventoryY + row * 18));
            }
        }
        int hotbarY = inventoryY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }

        this.addDataSlots(data);
    }

    public boolean isOpenWater() {
        return this.data.get(0) == 1;
    }

    public int getCatchTicksRemaining() {
        return Math.max(0, this.data.get(1));
    }

    public int getCatchTicksTotal() {
        return Math.max(0, this.data.get(2));
    }

    public int getCatchPercent() {
        int total = getCatchTicksTotal();
        if (total <= 0) return 0;
        int done = Math.max(0, total - getCatchTicksRemaining());
        return Math.min(100, Math.round((done / (float) total) * 100.0F));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        int playerStart = CONTAINER_SLOTS;
        int playerEnd = playerStart + 36;

        if (index < CONTAINER_SLOTS) {
            if (!this.moveItemStackTo(original, playerStart, playerEnd, true)) return ItemStack.EMPTY;
        } else if (original.getItem() == Items.FISHING_ROD) {
            if (!this.moveItemStackTo(original, FishTrapBlockEntity.SLOT_ROD,
                    FishTrapBlockEntity.SLOT_ROD + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, copy);
        return copy;
    }
}
