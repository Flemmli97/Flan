package io.github.flemmli97.flan.api.data;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface IPermissionContainer {

    default boolean canInteract(ServerPlayer player, ResourceLocation perm, BlockPos pos) {
        return this.canInteract(player, perm, pos, false);
    }

    /**
     * Return true of the action for the given ClamPermission is allowed here at the BlockPos
     *
     * @param player The player doing the action. Can be null
     */
    boolean canInteract(ServerPlayer player, ResourceLocation perm, BlockPos pos, boolean message);
}
