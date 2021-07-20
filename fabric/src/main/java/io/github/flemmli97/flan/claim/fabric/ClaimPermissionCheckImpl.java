package io.github.flemmli97.flan.claim.fabric;

import io.github.flemmli97.flan.api.fabric.PermissionCheckEvent;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ClaimPermissionCheckImpl {

    public static ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        return PermissionCheckEvent.CHECK.invoker().check(player, permission, pos);
    }
}
