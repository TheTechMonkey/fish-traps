package com.tech_monkey.fishtraps.blockentity;

import com.tech_monkey.fishtraps.FishTraps;
import com.tech_monkey.fishtraps.block.FishTrapBlock;
import com.tech_monkey.fishtraps.registry.ModBlockEntities;
import com.tech_monkey.fishtraps.screen.FishTrapScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class FishTrapBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOT_ROD = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_SLOTS = 9;
    public static final int OUTPUT_END = OUTPUT_START + OUTPUT_SLOTS;
    public static final int INV_SIZE = OUTPUT_END;

    private static final ResourceKey<LootTable> LT_FISH = lootTable("gameplay/fishing/fish");
    private static final ResourceKey<LootTable> LT_JUNK = lootTable("gameplay/fishing/junk");
    private static final ResourceKey<LootTable> LT_TREASURE = lootTable("gameplay/fishing/treasure");
    private static final Component NAME = Component.translatable("container.fishtraps.fish_trap");

    private final NonNullList<ItemStack> items = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
    private int nextCatchTicks;
    private int nextCatchTotalTicks;
    private int bubbleCooldown;
    private boolean openWater;
    private int openWaterRecheckCooldown;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> openWater ? 1 : 0;
                case 1 -> Math.max(0, nextCatchTicks);
                case 2 -> Math.max(0, nextCatchTotalTicks);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> openWater = value != 0;
                case 1 -> nextCatchTicks = value;
                case 2 -> nextCatchTotalTicks = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public FishTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_TRAP, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FishTrapBlockEntity trap) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (trap.openWaterRecheckCooldown-- <= 0) {
            boolean newValue = trap.computeOpenWater(serverLevel, state);
            if (newValue != trap.openWater) {
                trap.openWater = newValue;
                trap.setChanged();
            }
            trap.openWaterRecheckCooldown = 20;
        }

        if (!trap.canRun(state)) {
            trap.nextCatchTicks = 0;
            trap.nextCatchTotalTicks = 0;
            trap.bubbleCooldown = 0;
            return;
        }

        if (trap.nextCatchTicks <= 0 || trap.nextCatchTotalTicks <= 0) {
            int total = trap.rollNextCatchTime(serverLevel);
            trap.nextCatchTotalTicks = total;
            trap.nextCatchTicks = total;
            trap.setChanged();
        }

        if (trap.bubbleCooldown-- <= 0) {
            trap.spawnBubbles(serverLevel);
            trap.bubbleCooldown = 40;
        }

        trap.nextCatchTicks -= trap.openWater ? 2 : 1;
        if (trap.nextCatchTicks <= 0) {
            trap.performCatch(serverLevel);
            trap.nextCatchTicks = 0;
            trap.nextCatchTotalTicks = 0;
            trap.setChanged();
        }
        level.blockEntityChanged(pos);
    }

    private boolean computeOpenWater(ServerLevel level, BlockState state) {
        return state.getValueOrElse(FishTrapBlock.WATERLOGGED, false)
                && OpenWaterUtil.isTrapOpenWater(level, this.worldPosition);
    }

    private boolean canRun(BlockState state) {
        return state.getValueOrElse(FishTrapBlock.WATERLOGGED, false)
                && isFishingRod(this.items.get(SLOT_ROD))
                && !isOutputFull();
    }

    public boolean isOutputFull() {
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            if (this.items.get(slot).isEmpty()) return false;
        }
        return true;
    }

    private int rollNextCatchTime(ServerLevel level) {
        int min = 20 * 60;
        int max = 20 * 120;
        int lure = getEnchantmentLevel(level, this.items.get(SLOT_ROD), Enchantments.LURE);
        float multiplier = Math.max(0.4F, 1.0F - 0.10F * lure);
        int base = min + level.getRandom().nextInt(max - min + 1);
        return Math.max(20, Math.round(base * multiplier));
    }

    private void performCatch(ServerLevel level) {
        if (isOutputFull()) return;
        ItemStack rod = this.items.get(SLOT_ROD);
        if (!isFishingRod(rod)) return;

        ResourceKey<LootTable> tableKey = chooseFishingSubtable(level, rod);
        LootTable table = level.getServer().reloadableRegistries().getLootTable(tableKey);
        if (table == LootTable.EMPTY) {
            FishTraps.LOGGER.warn("Missing fishing loot table {}", tableKey.identifier());
            return;
        }

        float luck = getEnchantmentLevel(level, rod, Enchantments.LUCK_OF_THE_SEA);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .withParameter(LootContextParams.TOOL, rod)
                .withLuck(luck)
                .create(LootContextParamSets.FISHING);

        boolean insertedAny = false;
        for (ItemStack drop : table.getRandomItems(params)) {
            if (!drop.isEmpty() && insertIntoOutputs(drop.copy())) insertedAny = true;
        }

        if (insertedAny) {
            int xp = tableKey.equals(LT_JUNK) ? 0 : 1 + level.getRandom().nextInt(6);
            damageRodAndApplyMending(level, xp);
        }
    }

    private ResourceKey<LootTable> chooseFishingSubtable(ServerLevel level, ItemStack rod) {
        int luck = getEnchantmentLevel(level, rod, Enchantments.LUCK_OF_THE_SEA);
        float treasureChance = this.openWater ? Math.min(0.05F + 0.02F * luck, 0.60F) : 0.0F;
        float junkChance = Math.min(Math.max(0.10F - 0.02F * luck, 0.0F), 0.60F);
        float roll = level.getRandom().nextFloat();
        if (roll < treasureChance) return LT_TREASURE;
        if (roll < treasureChance + junkChance) return LT_JUNK;
        return LT_FISH;
    }

    private boolean insertIntoOutputs(ItemStack stack) {
        for (int slot = OUTPUT_START; slot < OUTPUT_END && !stack.isEmpty(); slot++) {
            ItemStack existing = this.items.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int moved = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    stack.shrink(moved);
                }
            }
        }
        for (int slot = OUTPUT_START; slot < OUTPUT_END && !stack.isEmpty(); slot++) {
            if (this.items.get(slot).isEmpty()) {
                this.items.set(slot, stack.split(stack.getMaxStackSize()));
            }
        }
        this.setChanged();
        return stack.isEmpty();
    }

    private void damageRodAndApplyMending(ServerLevel level, int catchXp) {
        ItemStack rod = this.items.get(SLOT_ROD);
        if (!rod.isDamageableItem()) return;

        int unbreaking = getEnchantmentLevel(level, rod, Enchantments.UNBREAKING);
        if (unbreaking <= 0 || level.getRandom().nextInt(unbreaking + 1) == 0) {
            rod.setDamageValue(rod.getDamageValue() + 1);
            if (rod.isBroken()) {
                this.items.set(SLOT_ROD, ItemStack.EMPTY);
                this.setChanged();
                return;
            }
        }

        int mending = getEnchantmentLevel(level, rod, Enchantments.MENDING);
        if (mending > 0 && catchXp > 0 && rod.isDamaged()) {
            rod.setDamageValue(Math.max(0, rod.getDamageValue() - catchXp * 2));
        }
        this.setChanged();
    }

    private static int getEnchantmentLevel(ServerLevel level, ItemStack stack,
                                           ResourceKey<Enchantment> enchantmentKey) {
        Holder<Enchantment> enchantment = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(enchantmentKey);
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    private void spawnBubbles(ServerLevel level) {
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.9,
                this.worldPosition.getZ() + 0.5,
                3, 0.2, 0.0, 0.2, 0.0);
    }

    private static boolean isFishingRod(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.FISHING_ROD;
    }

    private static ResourceKey<LootTable> lootTable(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace(path));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.openWater = input.getBooleanOr("OpenWater", false);
        this.nextCatchTicks = input.getIntOr("NextCatchTicks", 0);
        this.nextCatchTotalTicks = input.getIntOr("WaitTicks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putBoolean("OpenWater", this.openWater);
        output.putInt("NextCatchTicks", this.nextCatchTicks);
        output.putInt("WaitTicks", this.nextCatchTotalTicks);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) Containers.dropContents(this.level, pos, this);
    }

    @Override
    public Component getDisplayName() {
        return NAME;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FishTrapScreenHandler(containerId, inventory, this, this.data);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            int[] slots = new int[OUTPUT_SLOTS];
            for (int index = 0; index < OUTPUT_SLOTS; index++) slots[index] = OUTPUT_START + index;
            return slots;
        }
        return new int[]{SLOT_ROD};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot >= OUTPUT_START;
    }
}
