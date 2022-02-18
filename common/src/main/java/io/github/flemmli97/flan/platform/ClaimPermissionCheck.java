package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public abstract class ClaimPermissionCheck {

    protected static ClaimPermissionCheck INSTANCE;

    public static ClaimPermissionCheck instance() {
        return INSTANCE;
    }

    public abstract InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos);
}
