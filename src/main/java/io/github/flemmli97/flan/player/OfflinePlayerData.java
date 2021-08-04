package io.github.flemmli97.flan.player;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.BannedEntryAccessor;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.UUID;

public class OfflinePlayerData implements IPlayerData {

    public final int claimBlocks;
    private int additionalClaimBlocks;
    public final LocalDateTime lastOnline;
    public final UUID owner;
    public final MinecraftServer server;
    public final Path save;

    public OfflinePlayerData(MinecraftServer server, UUID uuid) {
        this.save = ConfigHandler.getPlayerSavePath(server).resolve(uuid + ".json");
        int claim = ConfigHandler.config.startingBlocks;
        int add = 0;
        this.owner = uuid;
        LocalDateTime last = LocalDateTime.now();
        if (Files.exists(this.save)) {
            try {
                JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(this.save, StandardCharsets.UTF_8));
                JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
                reader.close();
                if (obj == null) {
                    obj = new JsonObject();
                    Flan.error("Malformed json {}, using default values", uuid);
                }
                claim = ConfigHandler.fromJson(obj, "ClaimBlocks", claim);
                add = ConfigHandler.fromJson(obj, "AdditionalBlocks", add);
                if (obj.has("LastSeen"))
                    last = LocalDateTime.parse(obj.get("LastSeen").getAsString(), Flan.onlineTimeFormatter);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                Flan.error("Error parsing time for {}, ignoring", uuid);
            }
        }
        this.claimBlocks = claim;
        this.additionalClaimBlocks = add;
        this.lastOnline = last;
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
            Files.createDirectories(this.save.getParent());
            if (!Files.exists(this.save))
                Files.createFile(this.save);
            JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(this.save, StandardCharsets.UTF_8));
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
            Flan.debug("Attempting to write following json data {} to file {}", obj, this.save.getFileName());
            JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(this.save, StandardCharsets.UTF_8));
            ConfigHandler.GSON.toJson(obj, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            Flan.error("Error adding additional claimblocks to offline player {}", this.owner);
        }
    }

    public boolean isExpired(LocalDateTime now) {
        BannedPlayerEntry entry = this.server.getUserCache().getByUuid(this.owner).map(this.server.getPlayerManager().getUserBanList()::get).orElse(null);
        boolean banned = entry != null && entry.getExpiryDate() != null;
        if (banned) {
            LocalDateTime bannedTime = LocalDateTime.ofInstant(((BannedEntryAccessor) entry).getCreationDate().toInstant(), ZoneId.systemDefault());
            return ConfigHandler.config.bannedDeletionTime != -1 && now.isAfter(bannedTime.plusDays(ConfigHandler.config.bannedDeletionTime));
        }
        return ConfigHandler.config.inactivityTime != -1 && now.isAfter(this.lastOnline.plusDays(ConfigHandler.config.inactivityTime)) && this.claimBlocks + this.additionalClaimBlocks < ConfigHandler.config.inactivityBlocksMax;
    }

    public void deleteFile() {
        try {
            Files.delete(this.save);
        } catch (IOException e) {
            Flan.error("Couldn't delete file player data of {}", this.owner);
            e.printStackTrace();
        }
    }
}
