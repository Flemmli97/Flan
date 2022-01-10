package io.github.flemmli97.flan.claim;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ClaimPermissionCheck {

    @ExpectPlatform
    public static ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        throw new AssertionError();
    }
}
