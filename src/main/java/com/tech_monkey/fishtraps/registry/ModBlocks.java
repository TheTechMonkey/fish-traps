package com.tech_monkey.fishtraps.registry;

import com.tech_monkey.fishtraps.FishTraps;
import com.tech_monkey.fishtraps.block.FishTrapBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    private ModBlocks() {
    }

    public static final Identifier FISH_TRAP_ID = Identifier.fromNamespaceAndPath(FishTraps.MOD_ID, "fish_trap");
    public static final ResourceKey<Block> FISH_TRAP_KEY = ResourceKey.create(Registries.BLOCK, FISH_TRAP_ID);
    public static final ResourceKey<Item> FISH_TRAP_ITEM_KEY = ResourceKey.create(Registries.ITEM, FISH_TRAP_ID);

    public static final FishTrapBlock FISH_TRAP = Registry.register(
            BuiltInRegistries.BLOCK,
            FISH_TRAP_KEY,
            new FishTrapBlock(BlockBehaviour.Properties.of()
                    .setId(FISH_TRAP_KEY)
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final BlockItem FISH_TRAP_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            FISH_TRAP_ITEM_KEY,
            new BlockItem(FISH_TRAP, new Item.Properties()
                    .setId(FISH_TRAP_ITEM_KEY)
                    .useBlockDescriptionPrefix())
    );

    public static void register() {
    }
}
