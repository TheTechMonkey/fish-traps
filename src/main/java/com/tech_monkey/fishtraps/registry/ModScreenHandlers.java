package com.tech_monkey.fishtraps.registry;

import com.tech_monkey.fishtraps.FishTraps;
import com.tech_monkey.fishtraps.screen.FishTrapScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class ModScreenHandlers {
    private ModScreenHandlers() {
    }

    public static final MenuType<FishTrapScreenHandler> FISH_TRAP = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(FishTraps.MOD_ID, "fish_trap"),
            new ExtendedMenuType<>(FishTrapScreenHandler::new, BlockPos.STREAM_CODEC)
    );

    public static void register() {
    }
}
