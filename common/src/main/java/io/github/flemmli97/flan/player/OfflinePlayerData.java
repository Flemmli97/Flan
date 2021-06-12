package io.github.flemmli97.flan.player;

import com.google.gson.JsonObject;
import io.github.flemmli97.flan.api.IPlayerData;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class OfflinePlayerData implements IPlayerData {

    public final int claimBlocks, additionalClaimBlocks;
    public final UUID owner;
    public final MinecraftServer server;

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
        this.server = server;
    }

    public int getUsedClaimBlocks() {
        int usedClaimsBlocks = 0;
        for (ServerWorld world : this.server.getWorlds()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.owner);
            if (claims != null)
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
        }
        return usedClaimsBlocks;
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
        return this.usedClaimBlocks();
    }
}
