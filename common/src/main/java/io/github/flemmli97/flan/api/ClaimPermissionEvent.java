package io.github.flemmli97.flan.api;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ClaimPermissionEvent {

    @ExpectPlatform
    public static ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        throw new AssertionError();
    }
}
