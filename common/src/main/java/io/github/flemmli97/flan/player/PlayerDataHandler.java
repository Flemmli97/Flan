package io.github.flemmli97.flan.player;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataHandler {

    private static Map<UUID, OfflinePlayerData> inActivePlayerData = null;

    public static void initInactivePlayers(MinecraftServer server) {
        inActivePlayerData = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        Map<UUID, OfflinePlayerData> playerData = collectAllPlayerData(server);
        playerData.forEach((uuid, data) -> {
            if (data.isExpired(now))
                inActivePlayerData.put(uuid, data);
        });
        Flan.log("Collected player data to delete {}", inActivePlayerData);
    }

    public static Map<UUID, OfflinePlayerData> collectAllPlayerData(MinecraftServer server) {
        Path dir = ConfigHandler.getPlayerSavePath(server);
        Map<UUID, OfflinePlayerData> playerDatas = new HashMap<>();
        if (Files.exists(dir)) {
            for (String name : dir.toFile().list((d, name) -> name.endsWith(".json"))) {
                UUID uuid = UUID.fromString(name.replace(".json", ""));
                playerDatas.put(uuid, new OfflinePlayerData(server, uuid));
            }
        }
        return playerDatas;
    }

    public static void deleteUnusedClaims(MinecraftServer server, ClaimStorage storage, ServerLevel world) {
        if (inActivePlayerData == null)
            initInactivePlayers(server);
        inActivePlayerData.forEach((uuid, data) -> {
            Flan.log("{} Deleting all claims for inactive player {} last seen {}", world.dimension(), data.owner, data.lastOnline);
            storage.allClaimsFromPlayer(data.owner)
                    .forEach(claim -> storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, world));
        });
    }

    public static void deleteInactivePlayerData(MinecraftServer server) {
        if (ConfigHandler.config.deletePlayerFile && inActivePlayerData != null) {
            inActivePlayerData.forEach((uuid, data) -> data.deleteFile());
            inActivePlayerData = null;
        }
    }
}
