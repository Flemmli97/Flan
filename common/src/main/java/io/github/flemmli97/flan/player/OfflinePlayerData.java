package io.github.flemmli97.flan.player;

import com.google.gson.JsonObject;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.IPlayerData;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflinePlayerData implements IPlayerData {

    public final int claimBlocks, additionalClaimBlocks;
    public final LocalDateTime lastOnline;
    public final UUID owner;
    public final MinecraftServer server;

    public OfflinePlayerData(MinecraftServer server, UUID uuid) {
        File dir = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "/claimData/");
        int claim = ConfigHandler.config.startingBlocks;
        int add = 0;
        this.owner = uuid;
        LocalDateTime last = LocalDateTime.now();
        if (dir.exists()) {
            try {
                File file = new File(dir, uuid + ".json");
                if (file.exists()) {
                    FileReader reader = new FileReader(file);
                    JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
                    reader.close();
                    claim = ConfigHandler.fromJson(obj, "ClaimBlocks", claim);
                    add = ConfigHandler.fromJson(obj, "AdditionalBlocks", add);
                    if(obj.has("LastSeen")) {
                        try {
                            last = LocalDateTime.parse(obj.get("LastSeen").getAsString(), Flan.onlineTimeFormatter);
                        } catch (RuntimeException e) {
                            Flan.log("Error parsing time for {}, ignoring", uuid);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.claimBlocks = claim;
        this.additionalClaimBlocks = add;
        this.lastOnline = last;
        this.server = server;
    }

    private OfflinePlayerData(MinecraftServer server, File dataFile, UUID uuid) {
        int claim = ConfigHandler.config.startingBlocks;
        int add = 0;
        LocalDateTime last = LocalDateTime.now();
        try {
            FileReader reader = new FileReader(dataFile);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();

            claim = ConfigHandler.fromJson(obj, "ClaimBlocks", claim);
            add = ConfigHandler.fromJson(obj, "AdditionalBlocks", add);
            if (obj.has("LastSeen")) {
                try {
                    last = LocalDateTime.parse(obj.get("LastSeen").getAsString(), Flan.onlineTimeFormatter);
                } catch (RuntimeException e) {
                    Flan.log("Error parsing time for {}, ignoring", uuid);
                }
            } else {
                obj.addProperty("LastSeen", last.format(Flan.onlineTimeFormatter));
                FileWriter write = new FileWriter(dataFile);
                ConfigHandler.GSON.toJson(obj, write);
                write.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.claimBlocks = claim;
        this.additionalClaimBlocks = add;
        this.lastOnline = last;
        this.owner = uuid;
        this.server = server;
    }

    @Override
    public int getClaimBlocks() {
        return this.claimBlocks;
    }

    @Override
    public int getAdditionalClaims() {
        return this.additionalClaimBlocks;
    }

    @Override
    public int usedClaimBlocks() {
        int usedClaimsBlocks = 0;
        for (ServerWorld world : this.server.getWorlds()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.owner);
            if (claims != null)
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
        }
        return usedClaimsBlocks;
    }

    public static Map<UUID, OfflinePlayerData> collectAllPlayerData(MinecraftServer server) {
        File dir = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "/claimData/");
        Map<UUID, OfflinePlayerData> playerDatas = new HashMap<>();
        if (dir.exists()) {
            for (File data : dir.listFiles()) {
                if (data.getName().endsWith(".json")) {
                    UUID uuid = UUID.fromString(data.getName().replace(".json", ""));
                    playerDatas.put(uuid, new OfflinePlayerData(server, data, uuid));
                }
            }
        }
        return playerDatas;
    }

    public static void deleteUnusedClaims(MinecraftServer server, ClaimStorage storage, ServerWorld world) {
        if (ConfigHandler.config.inactivityTime == -1)
            return;
        Map<UUID, OfflinePlayerData> playerData = collectAllPlayerData(server);
        LocalDateTime now = LocalDateTime.now();
        playerData.forEach((uuid, data) -> {
            if (now.isAfter(data.lastOnline.plusDays(ConfigHandler.config.inactivityTime))
                    && data.claimBlocks + data.additionalClaimBlocks < ConfigHandler.config.inactivityBlocksMax) {
                Flan.log("{} Deleting all claims for inactive player {} last seen {}", world.getRegistryKey(), data.owner, data.lastOnline);
                storage.allClaimsFromPlayer(data.owner)
                        .forEach(claim -> storage.deleteClaim(claim, true, EnumEditMode.DEFAULT, world));
            }
        });
    }
}
