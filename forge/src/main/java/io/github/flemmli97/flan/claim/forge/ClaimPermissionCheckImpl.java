package io.github.flemmli97.flan.claim.forge;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.forge.PermissionCheckEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

public class ClaimPermissionCheckImpl {

    public static ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }
}
