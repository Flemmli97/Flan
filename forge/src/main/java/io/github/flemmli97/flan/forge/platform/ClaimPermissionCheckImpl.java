package io.github.flemmli97.flan.forge.platform;

import io.github.flemmli97.flan.api.forge.PermissionCheckEvent;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.platform.ClaimPermissionCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;

public class ClaimPermissionCheckImpl extends ClaimPermissionCheck {

    public static void init() {
        INSTANCE = new ClaimPermissionCheckImpl();
    }

    @Override
    public InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }
}
