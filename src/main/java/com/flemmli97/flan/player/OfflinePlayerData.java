package com.flemmli97.flan.player;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.config.ConfigHandler;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class OfflinePlayerData {

    public final int claimBlocks, additionalClaimBlocks;
    public final UUID owner;

    public OfflinePlayerData(MinecraftServer server, UUID uuid) {
        File dir = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "/claimData/");
        int claim = ConfigHandler.config.startingBlocks;
        int add = 0;
        this.owner = uuid;
        if (dir.exists()) {
            try {
                File file = new File(dir, uuid + ".json");
                if (file.exists()) {
                    FileReader reader = new FileReader(file);
                    JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
                    claim = obj.get("ClaimBlocks").getAsInt();
                    add = obj.get("AdditionalBlocks").getAsInt();
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.claimBlocks = claim;
        this.additionalClaimBlocks = add;
    }

    public int getUsedClaimBlocks(MinecraftServer server) {
        int usedClaimsBlocks = 0;
        for (ServerWorld world : server.getWorlds()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.owner);
            if (claims != null)
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
        }
        return usedClaimsBlocks;
    }
}
