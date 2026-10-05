package com.tech_monkey.fishtraps.registry;

import com.tech_monkey.fishtraps.FishTraps;
import com.tech_monkey.fishtraps.blockentity.FishTrapBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static final Identifier FISH_TRAP_ID = Identifier.fromNamespaceAndPath(FishTraps.MOD_ID, "fish_trap");

    public static final BlockEntityType<FishTrapBlockEntity> FISH_TRAP = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            FISH_TRAP_ID,
            FabricBlockEntityTypeBuilder.create(FishTrapBlockEntity::new, ModBlocks.FISH_TRAP).build()
    );

    public static void register() {
    }
}
