package io.github.flemmli97.flan.api;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPermissionStorage;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ClaimHandler {

    /**
     * Check if a player can do an interaction at the given position
     *
     * @param player     The player doing the interaction
     * @param pos        The position the player interacts with
     * @param permission The id of the permission to check. For default permissions check {@link BuiltinPermission}
     */
    public static boolean canInteract(ServerPlayer player, BlockPos pos, ResourceLocation permission) {
        return getPermissionStorage(player.level()).getForPermissionCheck(pos).canInteract(player, permission, pos);
    }

    /**
     * Get the permission storage for the world to check for permissions.
     * You can then use IPermissionContainer#getForPermissionCheck
     * to return an {@link IPermissionContainer} for which you can then check permissions against.
     * This can be the permissions for the world or a specific claim
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

    /**
     * Registers a mapping to migrate old permission keys to their new ids
     *
     * @param key   The old key of the permission
     * @param newId The new datapack permission id
     */
    public static void registerMapping(String key, ResourceLocation newId) {
        BuiltinPermission.registerMapping(key, newId);
    }
}
