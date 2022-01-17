package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ServerLangManager {

    private final Map<String, String> defaultTranslations = new HashMap<>();

    private File config;

    private final Map<String, String> translations = new HashMap<>();

    public LangCommands cmdLang = new LangCommands();

    public ServerLangManager(MinecraftServer server) {
        this.createDefaultTranslations();
        this.loadDefault();
        File configDir = CrossPlatformStuff.configPath().resolve("flan").toFile();
        try {
            if (!configDir.exists())
                configDir.mkdirs();
            this.config = new File(configDir, "flan_lang.json");
            if (!this.config.exists()) {
                this.config.createNewFile();
                this.saveDefault();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload(String lang) {
        this.translations.clear();
        if (!lang.equals("default")) {
            InputStream input = ServerLangManager.class.getResourceAsStream("data/flan/lang" + lang + ".json");
            if (input != null) {
                try {
                    InputStreamReader reader = new InputStreamReader(input);
                    JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
                    reader.close();
                    this.readFromJson(obj);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Flan.log("Couldn't process lang file of language " + lang + ". Reverting to default");
        }
        this.loadDefault();
        this.translations.putAll(this.defaultTranslations);
    }

    public String getTranslation(String key) {
        return this.translations.getOrDefault(key, key);
    }

    private void loadDefault() {
        try {
            FileReader reader = new FileReader(this.config);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            this.readFromJson(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.saveDefault();
    }

    private void readFromJson(JsonObject obj) {
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
    }

    private void saveDefault() {
        JsonObject obj = new JsonObject();
        try {
            this.defaultTranslations.forEach(obj::addProperty);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultTranslations() {
        this.defaultTranslations.put("noClaim", "There is no claim here.");
        this.defaultTranslations.put("inspectBlockOwner", "This is %1$s's claim");
        this.defaultTranslations.put("inspectNoClaim", "Nobody owns this block");
        this.defaultTranslations.put("claimBlocksFormat", "Claim Blocks: %1$d + (Bonus) %2$d); Used: %3$d");
        this.defaultTranslations.put("listClaims", "Listing all claims:");
        this.defaultTranslations.put("listAdminClaims", "Listing all admin-claims in %1$s:");
        this.defaultTranslations.put("onlyOnePlayer", "Only one player can be used as argument");
        this.defaultTranslations.put("ownerTransferSuccess", "New Claimowner now: %1$s");
        this.defaultTranslations.put("ownerTransferFail", "Only the owner may transfer claims");
        this.defaultTranslations.put("ownerTransferNoBlocks", "The new owner doesnt have enough claimblocks");
        this.defaultTranslations.put("ownerTransferNoBlocksAdmin", "You can ignore this by switching to admin mode");

        this.defaultTranslations.put("noPermission", "You don't have the required permissions to do that here!");
        this.defaultTranslations.put("noPermissionSimple", "Sorry you can't do that here!");

        this.defaultTranslations.put("configReload", "Configs reloaded");

        this.defaultTranslations.put("cantClaimHere", "Sorry you cant claim here");
        this.defaultTranslations.put("minClaimSize", "This is too small. Minimum claimsize is %d");
        this.defaultTranslations.put("maxClaims", "Maximum amount of claims reached");
        this.defaultTranslations.put("landClaimDisabledWorld", "Claiming is disabled in this world");
        this.defaultTranslations.put("editMode", "Editing mode set to %1$s");
        this.defaultTranslations.put("notEnoughBlocks", "Not enough claim blocks");
        this.defaultTranslations.put("conflictOther", "Claim would overlap other claims");
        this.defaultTranslations.put("wrongMode", "Wrong claim mode. You are in %1$s-mode");
        this.defaultTranslations.put("stringScreenReturn", "Click on paper to go back");

        this.defaultTranslations.put("groupAdd", "Added group %1$s");
        this.defaultTranslations.put("groupRemove", "Removed group %1$s");
        this.defaultTranslations.put("groupExist", "Group already exist");
        this.defaultTranslations.put("playerModify", "Modified permission group for following players to %1$s: %2$s");
        this.defaultTranslations.put("playerModifyNo", "Couldn't set permission group for the players. Probably cause they already belong to a group");
        this.defaultTranslations.put("playerGroupAddFail", "Couldn't add that player to the group either cause the player " +
                "is already in a group or no player matching the name was found");
        this.defaultTranslations.put("resizeClaim", "Resizing claim");
        this.defaultTranslations.put("resizeSuccess", "Resized Claims");
        this.defaultTranslations.put("claimCreateSuccess", "Created a new claim");
        this.defaultTranslations.put("subClaimCreateSuccess", "Created a new subclaim");
        this.defaultTranslations.put("deleteClaim", "Claim deleted");
        this.defaultTranslations.put("deleteAllClaimConfirm", "Are you sure you want to delete all claims? Type it again to confirm");
        this.defaultTranslations.put("deleteAllClaim", "All claims deleted");
        this.defaultTranslations.put("deleteClaimError", "You can't delete this claim here");
        this.defaultTranslations.put("deleteSubClaim", "Subclaim deleted");
        this.defaultTranslations.put("deleteSubClaimAll", "All Subclaims from this claim deleted");
        this.defaultTranslations.put("noSuchPerm", "No such Permission %1$s");
        this.defaultTranslations.put("editPerm", "%1$s now set to %2$s");
        this.defaultTranslations.put("editPermGroup", "%1$s for %2$s now set to %3$s");
        this.defaultTranslations.put("editPersonalGroup", "Default permission %1$s for group %2$s now set to %3$s");

        this.defaultTranslations.put("adminMode", "Adminmode (Ignore Claims) set to: %1$s");
        this.defaultTranslations.put("adminDeleteAll", "Deleted all claims for following players: %1$s");
        this.defaultTranslations.put("setAdminClaim", "Adminclaim of this claim now: %1$s");
        this.defaultTranslations.put("readGriefpreventionData", "Reading data from GriefPrevention");
        this.defaultTranslations.put("readGriefpreventionClaimDataSuccess", "Successfully read claim data");
        this.defaultTranslations.put("readGriefpreventionPlayerDataSuccess", "Successfully read player data");
        this.defaultTranslations.put("cantFindData", "No griefprevention data at %1$s");
        this.defaultTranslations.put("errorFile", "Error reading file %1$s");
        this.defaultTranslations.put("readConflict", "%1$s conflicts with existing claims. Not added to world! Conflicts:");
        this.defaultTranslations.put("giveClaimBlocks", "Gave following players %2$d claimblocks: %1$s");

        this.defaultTranslations.put("claimBasicInfo", "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]); Subclaim-amount: %6$d");
        this.defaultTranslations.put("claimBasicInfoNamed", "Claim: %7$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]); Subclaim-amount: %6$d");
        this.defaultTranslations.put("claimSubHeader", "==SubclaimInfo==");
        this.defaultTranslations.put("claimBasicInfoSub", "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]");
        this.defaultTranslations.put("claimBasicInfoSubNamed", "Claim: %6$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]");
        this.defaultTranslations.put("claimInfoPerms", "Permissions: %1$s");
        this.defaultTranslations.put("claimGroupInfoHeader", "Groups: ");
        this.defaultTranslations.put("claimGroupPerms", "    Permissions: %1$s");
        this.defaultTranslations.put("claimGroupPlayers", "    Players: %1$s");
        this.defaultTranslations.put("helpHeader", "Available subcommands are (page %d):");
        this.defaultTranslations.put("helpCmdHeader", "====================");
        this.defaultTranslations.put("helpCmdSyntax", "Syntax: %1$s");

        this.defaultTranslations.put("screenEnableText", "Enabled: %1$s");
        this.defaultTranslations.put("screenUneditable", "Non Editable!");
        this.defaultTranslations.put("screenClose", "Close");
        this.defaultTranslations.put("screenNext", "Next");
        this.defaultTranslations.put("screenPrevious", "Prev");
        this.defaultTranslations.put("screenAdd", "Add");
        this.defaultTranslations.put("screenBack", "Back");
        this.defaultTranslations.put("screenNoPerm", "No Permission");
        this.defaultTranslations.put("screenFalse", "false");
        this.defaultTranslations.put("screenTrue", "true");
        this.defaultTranslations.put("screenDefault", "default");

        this.defaultTranslations.put("screenMenu", "Claim-Menu");
        this.defaultTranslations.put("screenMenuSub", "SubClaim-Menu");
        this.defaultTranslations.put("screenMenuGlobal", "Edit Global Permissions");
        this.defaultTranslations.put("screenMenuGroup", "Edit Permissiongroups");
        this.defaultTranslations.put("screenMenuPotion", "Edit Potioneffects");
        this.defaultTranslations.put("screenMenuClaimText", "Edit Enter/Leave Text");
        this.defaultTranslations.put("screenMenuDelete", "Delete Claim");
        this.defaultTranslations.put("screenConfirm", "Confirm");
        this.defaultTranslations.put("screenYes", "Yes");
        this.defaultTranslations.put("screenNo", "No");
        this.defaultTranslations.put("screenGroupPlayers", "%1$s-Players");
        this.defaultTranslations.put("screenRemoveMode", "Remove Mode: %1$s");
        this.defaultTranslations.put("screenGlobalPerms", "Global-Permissions");
        this.defaultTranslations.put("screenGroups", "Claim-Groups");
        this.defaultTranslations.put("screenGroupPerms", "%1$s-Permissions");
        this.defaultTranslations.put("screenPersonalGroups", "Personal-Groups");
        this.defaultTranslations.put("screenPersonalPermissions", "Personal Permissions for %1$s");
        this.defaultTranslations.put("screenPotions", "Claim Potions");
        this.defaultTranslations.put("screenTitleEditor", "Claim messages");
        this.defaultTranslations.put("screenTitleEditorSub", "Subclaim messages");
        this.defaultTranslations.put("screenEnterText", "Edit title text on enter. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslations.put("screenEnterSubText", "Edit subtitle text on enter. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslations.put("screenLeaveText", "Edit title text on leave. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslations.put("screenLeaveSubText", "Edit subtitle text on enter. (Right-Click to use JSON text. See MC Wiki for that)");

        this.defaultTranslations.put("chatClaimTextEdit", "[Click for command]");

        this.defaultTranslations.put("sellDisabled", "Claimblocks selling is disabled");
        this.defaultTranslations.put("buyDisabled", "Claimblocks purchasing is disabled");
        this.defaultTranslations.put("sellFail", "Not enough claimblocks to sell");
        this.defaultTranslations.put("buyFail", "Not enough money");
        this.defaultTranslations.put("sellSuccess", "Sold %1$s claimblocks for %2$s");
        this.defaultTranslations.put("buySuccess", "Bought %1$s claimblocks for %2$s");
        this.defaultTranslations.put("currencyMissing", "Missing a supported currency mod");

        this.defaultTranslations.put("trappedRescue", "Rescuing. Don't move for 5 seconds");
        this.defaultTranslations.put("trappedFail", "Rescue not necessary or already rescuing");
        this.defaultTranslations.put("trappedMove", "You moved. Aborting teleport");

        this.defaultTranslations.put("unlockDropsCmd", "Your deathitems are protected. Use %1$s to unlock them for other players");
        this.defaultTranslations.put("unlockDrops", "Your deathitems are now unlocked for %1$s ticks");
        this.defaultTranslations.put("unlockDropsMulti", "Unlocked drops for %1$s");

        this.defaultTranslations.put("claimNameSet", "Claims name set to %1$s");
        this.defaultTranslations.put("claimNameUsed", "The owner of the claim already has another claim with the same name");
        this.defaultTranslations.put("claimNameUsedSub", "One of the subclaim of this claim already has this name");

        this.defaultTranslations.put("setHome", "Claim home set to [x=%1$s,y=%2$s,z=%3$s]");
        this.defaultTranslations.put("teleportHome", "Teleporting to claim home. Don't move for 5 seconds");
        this.defaultTranslations.put("teleportHomeFail", "Teleport already happening");

        this.defaultTranslations.put("setEnterMessage", "Set enter title to %1$s");
        this.defaultTranslations.put("setEnterSubMessage", "Set enter subtitle to %1$s");
        this.defaultTranslations.put("setLeaveMessage", "Set leave title to %1$s");
        this.defaultTranslations.put("setLeaveSubMessage", "Set leave subtitle to %1$s");

        this.defaultTranslations.put("wiki", "For more info check out the wiki:");
    }
}
