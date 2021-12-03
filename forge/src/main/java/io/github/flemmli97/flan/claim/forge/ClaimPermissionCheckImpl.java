package io.github.flemmli97.flan.claim.forge;

import io.github.flemmli97.flan.api.forge.PermissionCheckEvent;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;

public class ClaimPermissionCheckImpl {

    public static InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }
}
