package com.tech_monkey.fishtraps.client;

import com.tech_monkey.fishtraps.registry.ModScreenHandlers;
import com.tech_monkey.fishtraps.screen.FishTrapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class FishTrapsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.FISH_TRAP, FishTrapScreen::new);
    }
}
