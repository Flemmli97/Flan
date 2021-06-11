package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.ClaimPermission;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IPermissionContainer {

    default boolean canInteract(ServerPlayerEntity player, ClaimPermission perm, BlockPos pos) {
        return this.canInteract(player, perm, pos, false);
    }

    boolean canInteract(ServerPlayerEntity player, ClaimPermission perm, BlockPos pos, boolean message);

}
