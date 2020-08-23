package com.flemmli97.flan.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;

public class LangConfig {

    private final File configDir;

    public String inspectBlockOwner = "This is %1$s's claim";
    public String inspectNoClaim = "Nobody owns this block";
    public String cantClaimHere = "Sorry you cant claim here";
    public String listClaims = "Listing all claims:";

    public String deleteClaim = "Claim deleted";
    public String deleteAllClaimConfirm = "Are you sure you want to delete all claims? Type it again to confirm";
    public String deleteAllClaim = "All claims deleted";
    public String deleteClaimError = "No claim for you to delete here";
    public String adminDeleteAll = "Deleted all claims for following players: %s";

    public String giveClaimBlocks = "Gave following players %2$d claimblocks: %1$s";
    public String adminMode = "Adminmode set to: ";
    public String editMode = "Editing mode set to %s";

    public String stringScreenReturn = "Click on paper to go back";

    public String playerGroupAddFail = "Couldn't add that player to the group either cause the player " +
            "is already in a group or no player matching the name was found";

    public LangConfig(MinecraftServer server) {
        this.configDir = server.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
    }

    public void load() {

    }
}
