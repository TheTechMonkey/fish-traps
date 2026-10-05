package com.tech_monkey.fishtraps.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;

public final class OpenWaterUtil {
    private OpenWaterUtil() {
    }

    public static boolean isTrapOpenWater(ServerLevel level, BlockPos trapPos) {
        if (!isWater(level, trapPos.above())) return false;
        if (!isWater(level, trapPos.north()) || !isWater(level, trapPos.north(2))) return false;
        if (!isWater(level, trapPos.south()) || !isWater(level, trapPos.south(2))) return false;
        if (!isWater(level, trapPos.east()) || !isWater(level, trapPos.east(2))) return false;
        return isWater(level, trapPos.west()) && isWater(level, trapPos.west(2));
    }

    private static boolean isWater(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }
}
