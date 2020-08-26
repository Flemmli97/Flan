package com.flemmli97.flan.config;

import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public class LangConfig {

    private File config;

    public String noClaim = "There is no claim here.";
    public String inspectBlockOwner = "This is %1$s's claim";
    public String inspectNoClaim = "Nobody owns this block";
    public String claimBlocksFormat = "Claim Blocks: %1$d + (Bonus) %2$d; Used: %3$d";
    public String listClaims = "Listing all claims:";
    public String listAdminClaims = "Listing all admin-claims in %1:";

    public String noPermission = "You don't have the required permissions to do that here!";
    public String noPermissionSimple = "Sorry you can't do that here!";

    public String configReload = "Configs reloaded";

    public String cantClaimHere = "Sorry you cant claim here";
    public String minClaimSize = "This is too small. Minimum claimsize is %d";
    public String landClaimDisabledWorld = "Claiming is disabled in this world";
    public String editMode = "Editing mode set to %s";
    public String notEnoughBlocks = "Not enough claim blocks";
    public String conflictOther = "Claim would overlap other claims";
    public String wrongMode = "Wrong claim mode. You are in %s-mode";
    public String stringScreenReturn = "Click on paper to go back";

    public String groupAdd = "Added group %s";
    public String groupRemove = "Removed group %s";
    public String groupExist = "Group already exist";
    public String playerModify = "Modified permission group for following players to %1$s: %2$s";
    public String playerModifyNo = "Couldn't set permission group for the players. Probably cause they already belong to a group";
    public String playerGroupAddFail = "Couldn't add that player to the group either cause the player " +
            "is already in a group or no player matching the name was found";
    public String resizeClaim = "Resizing claim";
    public String resizeSuccess = "Resized Claims";
    public String claimCreateSuccess = "Created a new claim";
    public String subClaimCreateSuccess = "Created a new subclaim";
    public String deleteClaim = "Claim deleted";
    public String deleteAllClaimConfirm = "Are you sure you want to delete all claims? Type it again to confirm";
    public String deleteAllClaim = "All claims deleted";
    public String deleteClaimError = "You can't delete this claim here";
    public String deleteSubClaim = "Subclaim deleted";
    public String deleteSubClaimAll = "All Subclaims from this claim deleted";

    public String adminMode = "Adminmode (Ignore Claims) set to: %s";
    public String adminDeleteAll = "Deleted all claims for following players: %s";
    public String setAdminClaim = "Adminclaim of this claim now: %s";
    public String readGriefpreventionData = "Reading data from GriefPrevention";
    public String readGriefpreventionDataSuccess = "Successfully read data";
    public String readGriefpreventionDataFail = "Failed reading data for following claim files (Check the logs!): %s";
    public String giveClaimBlocks = "Gave following players %2$d claimblocks: %1$s";

    public String claimBasicInfo = "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]; Subclaim-amount: %6$d";
    public String claimBasicInfoSub = "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]";
    public String claimInfoPerms = "Permissions: %s";
    public String claimGroupInfoHeader = "Groups: ";
    public String claimGroupPerms = "    Permissions: %s";
    public String claimGroupPlayers = "    Players: %s";

    public LangConfig(MinecraftServer server) {
        File configDir = server.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
        try {
            if (!configDir.exists())
                configDir.mkdirs();
            this.config = new File(configDir, "flan_lang.json");
            if (!this.config.exists()) {
                this.config.createNewFile();
                this.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileReader reader = new FileReader(this.config);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getDeclaringClass().equals(String.class)) {
                    field.set(this, obj.get(field.getName()).getAsString());
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        JsonObject obj = new JsonObject();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getType().equals(String.class)) {
                    obj.addProperty(field.getName(), (String) field.get(this));
                }
            }
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
