package com.flemmli97.flan.claim;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IPermissionContainer {

    default boolean canInteract(ServerPlayerEntity player, EnumPermission perm, BlockPos pos){
        return this.canInteract(player, perm, pos, false);
    }

    boolean canInteract(ServerPlayerEntity player, EnumPermission perm, BlockPos pos, boolean message);

}
