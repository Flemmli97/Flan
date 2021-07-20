package io.github.flemmli97.flan.api;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPermissionStorage;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class ClaimHandler {

    /**
     * Get the permission storage for the world to check for permissions.
     * You can then use IPermissionContainer#getForPermissionCheck
     * to return an {@link IPermissionContainer} for which you can then check permissions against
     */
    public static IPermissionStorage getPermissionStorage(ServerWorld world) {
        return ClaimStorage.get(world);
    }

    /**
     * Gets the claim data for the given player
     */
    public static IPlayerData getPlayerData(ServerPlayerEntity player) {
        return PlayerClaimData.get(player);
    }

    /**
     * Same as the above but with an uuid. Use this if the player is not online.
     */
    public static IPlayerData getPlayerData(MinecraftServer server, UUID uuid) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (player != null)
            return getPlayerData(player);
        return new OfflinePlayerData(server, uuid);
    }
}
