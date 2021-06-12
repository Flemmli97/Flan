package io.github.flemmli97.flan.claim.fabric;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.fabric.PermissionCheckEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ClaimPermissionCheckImpl {

    public static ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        return PermissionCheckEvent.CHECK.invoker().check(player, permission, pos);
    }
}
