package com.tech_monkey.fishtraps.registry;

import com.tech_monkey.fishtraps.FishTraps;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModItemGroups {
    private ModItemGroups() {
    }

    public static final Identifier FISH_TRAPS_TAB_ID = Identifier.fromNamespaceAndPath(FishTraps.MOD_ID, "fish_traps");

    public static final CreativeModeTab FISH_TRAPS_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            FISH_TRAPS_TAB_ID,
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fishtraps.fish_traps"))
                    .icon(() -> new ItemStack(ModBlocks.FISH_TRAP_ITEM))
                    .displayItems((parameters, output) -> output.accept(ModBlocks.FISH_TRAP_ITEM))
                    .build()
    );

    public static void register() {
    }
}
