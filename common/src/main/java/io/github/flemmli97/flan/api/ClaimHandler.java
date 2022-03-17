package io.github.flemmli97.flan.api;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPermissionStorage;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ClaimHandler {

    /**
     * Check if a player do an interaction at the given position
     */
    public static boolean canInteract(ServerPlayer player, BlockPos pos, ClaimPermission permission) {
        return ClaimStorage.get(player.getLevel()).getClaimAt(pos).canInteract(player, permission, pos);
    }

    /**
     * Get the permission storage for the world to check for permissions.
     * You can then use IPermissionContainer#getForPermissionCheck
     * to return an {@link IPermissionContainer} for which you can then check permissions against
     */
    public static IPermissionStorage getPermissionStorage(ServerLevel world) {
        return ClaimStorage.get(world);
    }

    /**
     * Gets the claim data for the given player
     */
    public static IPlayerData getPlayerData(ServerPlayer player) {
        return PlayerClaimData.get(player);
    }

    /**
     * Same as the above but with an uuid. Use this if the player is not online.
     */
    public static IPlayerData getPlayerData(MinecraftServer server, UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null)
            return getPlayerData(player);
        return new OfflinePlayerData(server, uuid);
    }
}
