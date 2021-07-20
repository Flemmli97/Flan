package io.github.flemmli97.flan.player;

import com.google.gson.JsonObject;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPlayerData;
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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflinePlayerData implements IPlayerData {

    public final int claimBlocks;
    private int additionalClaimBlocks;
    public final LocalDateTime lastOnline;
    public final UUID owner;
    public final MinecraftServer server;
    public final File saveFile;

    public OfflinePlayerData(MinecraftServer server, UUID uuid) {
        File dir = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "/claimData/");
        this.saveFile = new File(dir, uuid + ".json");
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
                    if (obj.has("LastSeen")) {
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
        this.saveFile = dataFile;
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

    @Override
    public void setAdditionalClaims(int amount) {
        this.additionalClaimBlocks = amount;
        try {
            if (!this.saveFile.getParentFile().exists()) {
                this.saveFile.getParentFile().mkdirs();
                this.saveFile.createNewFile();
            } else if (!this.saveFile.exists())
                this.saveFile.createNewFile();
            FileReader reader = new FileReader(this.saveFile);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            if (obj == null) {
                obj = new JsonObject();
                obj.addProperty("ClaimBlocks", this.claimBlocks);
                obj.addProperty("AdditionalBlocks", this.additionalClaimBlocks);
                obj.addProperty("LastSeen", this.lastOnline.format(Flan.onlineTimeFormatter));
                JsonObject defPerm = new JsonObject();
                obj.add("DefaultGroups", defPerm);
            } else
                obj.addProperty("AdditionalBlocks", this.additionalClaimBlocks);
            Flan.debug("Attempting to write following json data {} to file {}", obj, this.saveFile.getName());
            FileWriter writer = new FileWriter(this.saveFile);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            Flan.log("Error adding additional claimblocks to offline player {}", this.owner);
        }
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
