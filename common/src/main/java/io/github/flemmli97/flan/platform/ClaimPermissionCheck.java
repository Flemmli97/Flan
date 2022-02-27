package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public interface ClaimPermissionCheck {

    ClaimPermissionCheck INSTANCE = Flan.getPlatformInstance(ClaimPermissionCheck.class,
            "io.github.flemmli97.flan.fabric.platform.ClaimPermissionCheckImpl",
            "io.github.flemmli97.flan.forge.platform.ClaimPermissionCheckImpl");

    InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos);
}
