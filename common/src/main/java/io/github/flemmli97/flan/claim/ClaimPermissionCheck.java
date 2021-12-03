package io.github.flemmli97.flan.claim;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class ClaimPermissionCheck {

    @ExpectPlatform
    public static InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos) {
        throw new AssertionError();
    }
}
