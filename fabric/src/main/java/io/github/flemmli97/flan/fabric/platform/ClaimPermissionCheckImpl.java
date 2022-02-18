package io.github.flemmli97.flan.fabric.platform;

import io.github.flemmli97.flan.api.fabric.PermissionCheckEvent;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.platform.ClaimPermissionCheck;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class ClaimPermissionCheckImpl extends ClaimPermissionCheck {

    public static void init() {
        INSTANCE = new ClaimPermissionCheckImpl();
    }

    @Override
    public InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos) {
        return PermissionCheckEvent.CHECK.invoker().check(player, permission, pos);
    }
}
