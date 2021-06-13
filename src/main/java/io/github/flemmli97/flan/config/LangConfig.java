package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import net.minecraft.server.MinecraftServer;

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
    public String onlyOnePlayer = "Only one player can be used as argument";
    public String ownerTransferSuccess = "New Claimowner now: %s";
    public String ownerTransferFail = "Only the owner may transfer claims";
    public String ownerTransferNoBlocks = "The new owner doesnt have enough claimblocks";
    public String ownerTransferNoBlocksAdmin = "You can ignore this by switching to admin mode";

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
    public String noSuchPerm = "No such Permission %s";
    public String editPerm = "%1$s now set to %2$s";
    public String editPermGroup = "%1$s for %2$s now set to %3$s";
    public String editPersonalGroup = "Default permission %1$s for group %2$s now set to %3$s";

    public String adminMode = "Adminmode (Ignore Claims) set to: %s";
    public String adminDeleteAll = "Deleted all claims for following players: %s";
    public String setAdminClaim = "Adminclaim of this claim now: %s";
    public String readGriefpreventionData = "Reading data from GriefPrevention";
    public String readGriefpreventionClaimDataSuccess = "Successfully read claim data";
    public String readGriefpreventionPlayerDataSuccess = "Successfully read player data";
    public String cantFindData = "No griefprevention data at %s";
    public String errorFile = "Error reading file %s";
    public String readConflict = "%1$s conflicts with existing claims. Not added to world! Conflicts:";
    public String giveClaimBlocks = "Gave following players %2$d claimblocks: %1$s";

    public String claimBasicInfo = "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]; Subclaim-amount: %6$d";
    public String claimBasicInfoNamed = "Claim: %7$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]; Subclaim-amount: %6$d";
    public String claimSubHeader = "==SubclaimInfo==";
    public String claimBasicInfoSub = "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]";
    public String claimBasicInfoSubNamed = "Claim: %6$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]";
    public String claimInfoPerms = "Permissions: %s";
    public String claimGroupInfoHeader = "Groups: ";
    public String claimGroupPerms = "    Permissions: %s";
    public String claimGroupPlayers = "    Players: %s";
    public String helpHeader = "Available subcommands are (page %d):";
    public String helpCmdHeader = "====================";
    public String helpCmdSyntax = "Syntax: %s";

    public String screenEnableText = "Enabled: %s";
    public String screenUneditable = "Non Editable!";
    public String screenClose = "Close";
    public String screenNext = "Next";
    public String screenPrevious = "Prev";
    public String screenAdd = "Add";
    public String screenBack = "Back";
    public String screenNoPerm = "No Permission";

    public String screenMenu = "Claim-Menu";
    public String screenMenuSub = "SubClaim-Menu";
    public String screenMenuGlobal = "Edit Global Permissions";
    public String screenMenuGroup = "Edit Permissiongroups";
    public String screenMenuPotion = "Edit Potioneffects";
    public String screenMenuDelete = "Delete Claim";
    public String screenConfirm = "Confirm";
    public String screenYes = "Yes";
    public String screenNo = "No";
    public String screenGroupPlayers = "%s-Players";
    public String screenRemoveMode = "Remove Mode: %s";
    public String screenGlobalPerms = "Global-Permissions";
    public String screenGroupPerms = "%s-Permissions";
    public String screenPersonalGroups = "Personal-Groups";
    public String screenPersonalPermissions = "Personal Permissions for %s";

    public String sellDisabled = "Claimblocks selling is disabled";
    public String buyDisabled = "Claimblocks purchasing is disabled";
    public String sellFail = "Not enough claimblocks to sell";
    public String buyFail = "Not enough money";
    public String sellSuccess = "Sold %1$s claimblocks for %2$s";
    public String buySuccess = "Bought %1$s claimblocks for %2$s";
    public String gunpowderMissing = "Missing gunpowder currency mod";

    public String trappedRescue = "Rescuing. Don't move for 5 seconds";
    public String trappedFail = "Rescue not necessary or already rescuing";
    public String trappedMove = "You moved. Aborting teleport";

    public String unlockDropsCmd = "Your deathitems are protected. Use %s to unlock them for other players";
    public String unlockDrops = "Your deathitems are now unlocked for %s ticks";
    public String unlockDropsMulti = "Unlocked drops for %s";

    public String claimNameSet = "Claims name set to %s";
    public String claimNameUsed = "The owner of the claim already has another claim with the same name";
    public String claimNameUsedSub = "One of the subclaim of this claim already has this name";

    public String setHome = "Claim home set to [x=%s,y=%s,z=%s]";
    public String teleportHome = "Teleporting to claim home. Don't move for 5 seconds";
    public String teleportHomeFail = "Teleport already happening";

    public LangCommands cmdLang = new LangCommands();

    public LangConfig(MinecraftServer server) {
        File configDir = CrossPlatformStuff.configPath().resolve("flan").toFile();
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
                if (field.getType().equals(String.class) && obj.has(field.getName())) {
                    field.set(this, obj.get(field.getName()).getAsString());
                }
            }
            for (ClaimPermission perm : PermissionRegistry.getPerms()) {
                if (obj.has(perm.id + ".desc")) {
                    JsonElement pe = obj.get(perm.id + ".desc");
                    if (pe.isJsonObject())
                        throw new JsonParseException("Lang cant be json objects");
                    if (pe.isJsonArray()) {
                        String[] l = new String[pe.getAsJsonArray().size()];
                        for (int i = 0; i < l.length; i++)
                            l[i] = pe.getAsJsonArray().get(i).getAsString();
                        perm.desc = l;
                    } else
                        perm.desc = new String[]{pe.getAsString()};
                }
            }
            JsonObject cmd = ConfigHandler.fromJson(obj, "commands");
            this.cmdLang.load(cmd);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
        this.save();
    }

    private void save() {
        JsonObject obj = new JsonObject();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.getType().equals(String.class)) {
                    obj.addProperty(field.getName(), (String) field.get(this));
                }
            }
            for (ClaimPermission perm : PermissionRegistry.getPerms()) {
                if (perm.desc.length == 1)
                    obj.addProperty(perm.id + ".desc", perm.desc[0]);
                else {
                    JsonArray arr = new JsonArray();
                    for (String s : perm.desc)
                        arr.add(s);
                    obj.add(perm.id + ".desc", arr);
                }
            }
            JsonObject cmd = new JsonObject();
            this.cmdLang.save(cmd);
            obj.add("commands", cmd);
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
