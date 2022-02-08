package io.github.flemmli97.flan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LangManager {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<String, String> defaultTranslation = new LinkedHashMap<>();
    private final Map<String, String[]> defaultTranslationArray = new LinkedHashMap<>();

    private void loadDefault() {
        this.defaultTranslation.put("noClaim", "There is no claim here.");
        this.defaultTranslation.put("inspectBlockOwner", "This is %1$s's claim");
        this.defaultTranslation.put("inspectNoClaim", "Nobody owns this block");
        this.defaultTranslation.put("claimBlocksFormat", "Claim Blocks: %1$d + (Bonus) %2$d); Used: %3$d");
        this.defaultTranslation.put("listClaims", "Listing all claims:");
        this.defaultTranslation.put("listAdminClaims", "Listing all admin-claims in %1$s:");
        this.defaultTranslation.put("onlyOnePlayer", "Only one player can be used as argument");
        this.defaultTranslation.put("ownerTransferSuccess", "New Claimowner now: %1$s");
        this.defaultTranslation.put("ownerTransferFail", "Only the owner may transfer claims");
        this.defaultTranslation.put("ownerTransferNoBlocks", "The new owner doesnt have enough claimblocks");
        this.defaultTranslation.put("ownerTransferNoBlocksAdmin", "You can ignore this by switching to admin mode");

        this.defaultTranslation.put("noPermission", "You don't have the required permissions to do that here!");
        this.defaultTranslation.put("noPermissionSimple", "Sorry you can't do that here!");

        this.defaultTranslation.put("configReload", "Configs reloaded");

        this.defaultTranslation.put("cantClaimHere", "Sorry you cant claim here");
        this.defaultTranslation.put("minClaimSize", "This is too small. Minimum claimsize is %d");
        this.defaultTranslation.put("maxClaims", "Maximum amount of claims reached");
        this.defaultTranslation.put("landClaimDisabledWorld", "Claiming is disabled in this world");
        this.defaultTranslation.put("editMode", "Editing mode set to %1$s");
        this.defaultTranslation.put("notEnoughBlocks", "Not enough claim blocks");
        this.defaultTranslation.put("conflictOther", "Claim would overlap other claims");
        this.defaultTranslation.put("wrongMode", "Wrong claim mode. You are in %1$s-mode");
        this.defaultTranslation.put("stringScreenReturn", "Click on paper to go back");

        this.defaultTranslation.put("groupAdd", "Added group %1$s");
        this.defaultTranslation.put("groupRemove", "Removed group %1$s");
        this.defaultTranslation.put("groupExist", "Group already exist");
        this.defaultTranslation.put("playerModify", "Modified permission group for following players to %1$s: %2$s");
        this.defaultTranslation.put("playerModifyNo", "Couldn't set permission group for the players. Probably cause they already belong to a group");
        this.defaultTranslation.put("playerGroupAddFail", "Couldn't add that player to the group either cause the player " +
                "is already in a group or no player matching the name was found");
        this.defaultTranslation.put("resizeClaim", "Resizing claim");
        this.defaultTranslation.put("resizeSuccess", "Resized Claims");
        this.defaultTranslation.put("claimCreateSuccess", "Created a new claim");
        this.defaultTranslation.put("subClaimCreateSuccess", "Created a new subclaim");
        this.defaultTranslation.put("deleteClaim", "Claim deleted");
        this.defaultTranslation.put("deleteAllClaimConfirm", "Are you sure you want to delete all claims? Type it again to confirm");
        this.defaultTranslation.put("deleteAllClaim", "All claims deleted");
        this.defaultTranslation.put("deleteClaimError", "You can't delete this claim here");
        this.defaultTranslation.put("deleteSubClaim", "Subclaim deleted");
        this.defaultTranslation.put("deleteSubClaimAll", "All Subclaims from this claim deleted");
        this.defaultTranslation.put("noSuchPerm", "No such Permission %1$s");
        this.defaultTranslation.put("editPerm", "%1$s now set to %2$s");
        this.defaultTranslation.put("editPermGroup", "%1$s for %2$s now set to %3$s");
        this.defaultTranslation.put("editPersonalGroup", "Default permission %1$s for group %2$s now set to %3$s");

        this.defaultTranslation.put("adminMode", "Adminmode (Ignore Claims) set to: %1$s");
        this.defaultTranslation.put("adminDeleteAll", "Deleted all claims for following players: %1$s");
        this.defaultTranslation.put("setAdminClaim", "Adminclaim of this claim now: %1$s");
        this.defaultTranslation.put("readGriefpreventionData", "Reading data from GriefPrevention");
        this.defaultTranslation.put("readGriefpreventionClaimDataSuccess", "Successfully read claim data");
        this.defaultTranslation.put("readGriefpreventionPlayerDataSuccess", "Successfully read player data");
        this.defaultTranslation.put("cantFindData", "No griefprevention data at %1$s");
        this.defaultTranslation.put("errorFile", "Error reading file %1$s");
        this.defaultTranslation.put("readConflict", "%1$s conflicts with existing claims. Not added to world! Conflicts:");
        this.defaultTranslation.put("giveClaimBlocks", "Gave following players %2$d claimblocks: %1$s");

        this.defaultTranslation.put("claimBasicInfo", "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]); Subclaim-amount: %6$d");
        this.defaultTranslation.put("claimBasicInfoNamed", "Claim: %7$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]); Subclaim-amount: %6$d");
        this.defaultTranslation.put("claimSubHeader", "==SubclaimInfo==");
        this.defaultTranslation.put("claimBasicInfoSub", "Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]");
        this.defaultTranslation.put("claimBasicInfoSubNamed", "Claim: %6$s, Owner: %1$s, from: [x=%2$d,z=%3$d] to [x=%4$d,z=%5$d]");
        this.defaultTranslation.put("claimInfoPerms", "Permissions: %1$s");
        this.defaultTranslation.put("claimGroupInfoHeader", "Groups: ");
        this.defaultTranslation.put("claimGroupPerms", "    Permissions: %1$s");
        this.defaultTranslation.put("claimGroupPlayers", "    Players: %1$s");
        this.defaultTranslation.put("helpHeader", "Available subcommands are (page %d):");
        this.defaultTranslation.put("helpCmdHeader", "====================");
        this.defaultTranslation.put("helpCmdSyntax", "Syntax: %1$s");

        this.defaultTranslation.put("screenEnableText", "Enabled: %1$s");
        this.defaultTranslation.put("screenUneditable", "Non Editable!");
        this.defaultTranslation.put("screenClose", "Close");
        this.defaultTranslation.put("screenNext", "Next");
        this.defaultTranslation.put("screenPrevious", "Prev");
        this.defaultTranslation.put("screenAdd", "Add");
        this.defaultTranslation.put("screenBack", "Back");
        this.defaultTranslation.put("screenNoPerm", "No Permission");
        this.defaultTranslation.put("screenFalse", "false");
        this.defaultTranslation.put("screenTrue", "true");
        this.defaultTranslation.put("screenDefault", "default");

        this.defaultTranslation.put("screenMenu", "Claim-Menu");
        this.defaultTranslation.put("screenMenuSub", "SubClaim-Menu");
        this.defaultTranslation.put("screenMenuGlobal", "Edit Global Permissions");
        this.defaultTranslation.put("screenMenuGroup", "Edit Permissiongroups");
        this.defaultTranslation.put("screenMenuPotion", "Edit Potioneffects");
        this.defaultTranslation.put("screenMenuClaimText", "Edit Enter/Leave Text");
        this.defaultTranslation.put("screenMenuDelete", "Delete Claim");
        this.defaultTranslation.put("screenConfirm", "Confirm");
        this.defaultTranslation.put("screenYes", "Yes");
        this.defaultTranslation.put("screenNo", "No");
        this.defaultTranslation.put("screenGroupPlayers", "%1$s-Players");
        this.defaultTranslation.put("screenRemoveMode", "Remove Mode: %1$s");
        this.defaultTranslation.put("screenGlobalPerms", "Global-Permissions");
        this.defaultTranslation.put("screenGroups", "Claim-Groups");
        this.defaultTranslation.put("screenGroupPerms", "%1$s-Permissions");
        this.defaultTranslation.put("screenPersonalGroups", "Personal-Groups");
        this.defaultTranslation.put("screenPersonalPermissions", "Personal Permissions for %1$s");
        this.defaultTranslation.put("screenPotions", "Claim Potions");
        this.defaultTranslation.put("screenTitleEditor", "Claim messages");
        this.defaultTranslation.put("screenTitleEditorSub", "Subclaim messages");
        this.defaultTranslation.put("screenEnterText", "Edit title text on enter. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslation.put("screenEnterSubText", "Edit subtitle text on enter. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslation.put("screenLeaveText", "Edit title text on leave. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslation.put("screenLeaveSubText", "Edit subtitle text on enter. (Right-Click to use JSON text. See MC Wiki for that)");
        this.defaultTranslation.put("screenDelete", "Use \"$empty\" to delete the message");

        this.defaultTranslation.put("chatClaimTextEdit", "[Click for command]");

        this.defaultTranslation.put("sellDisabled", "Claimblocks selling is disabled");
        this.defaultTranslation.put("buyDisabled", "Claimblocks purchasing is disabled");
        this.defaultTranslation.put("sellFail", "Not enough claimblocks to sell");
        this.defaultTranslation.put("buyFail", "Not enough money");
        this.defaultTranslation.put("buyFailItem", "Not enough items");
        this.defaultTranslation.put("buyFailXP", "Not enough experience points");
        this.defaultTranslation.put("sellSuccess", "Sold %1$s claimblocks for %2$s");
        this.defaultTranslation.put("sellSuccessItem", "Sold %1$s claimblocks for %3$s x%2$s");
        this.defaultTranslation.put("sellSuccessXP", "Sold %1$s claimblocks for %2$s experience points");
        this.defaultTranslation.put("buySuccess", "Bought %1$s claimblocks for %2$s");
        this.defaultTranslation.put("buySuccessItem", "Bought %1$s claimblocks with %2$s items");
        this.defaultTranslation.put("buySuccessXP", "Bought %1$s claimblocks with %2$s experience points");

        this.defaultTranslation.put("currencyMissing", "Missing a supported currency mod");

        this.defaultTranslation.put("trappedRescue", "Rescuing. Don't move for 5 seconds");
        this.defaultTranslation.put("trappedFail", "Rescue not necessary or already rescuing");
        this.defaultTranslation.put("trappedMove", "You moved. Aborting teleport");

        this.defaultTranslation.put("unlockDropsCmd", "Your deathitems are protected. Use %1$s to unlock them for other players");
        this.defaultTranslation.put("unlockDrops", "Your deathitems are now unlocked for %1$s ticks");
        this.defaultTranslation.put("unlockDropsMulti", "Unlocked drops for %1$s");

        this.defaultTranslation.put("claimNameSet", "Claims name set to %1$s");
        this.defaultTranslation.put("claimNameUsed", "The owner of the claim already has another claim with the same name");
        this.defaultTranslation.put("claimNameUsedSub", "One of the subclaim of this claim already has this name");

        this.defaultTranslation.put("setHome", "Claim home set to [x=%1$s,y=%2$s,z=%3$s]");
        this.defaultTranslation.put("teleportHome", "Teleporting to claim home. Don't move for 5 seconds");
        this.defaultTranslation.put("teleportHomeFail", "Teleport already happening");

        this.defaultTranslation.put("setEnterMessage", "Set enter title to %1$s");
        this.defaultTranslation.put("setEnterSubMessage", "Set enter subtitle to %1$s");
        this.defaultTranslation.put("setLeaveMessage", "Set leave title to %1$s");
        this.defaultTranslation.put("setLeaveSubMessage", "Set leave subtitle to %1$s");

        this.defaultTranslation.put("wiki", "For more info check out the wiki:");

        for (ClaimPermission perm : PermissionRegistry.getPerms()) {
            this.defaultTranslationArray.put(perm.id + ".desc", perm.desc);
        }
        this.defaultTranslationArray.put("command.help", new String[]{"help <page> | (cmd <command>)", "Shows all available commands or info for the given command."});
        this.defaultTranslationArray.put("command.menu", new String[]{"menu", "When standing in a claim you have permissions for opens the claim menu."});
        this.defaultTranslationArray.put("command.claimInfo", new String[]{"claimInfo", "Prints infos about the claim you're standing in."});
        this.defaultTranslationArray.put("command.delete", new String[]{"delete", "Deletes the current claim."});
        this.defaultTranslationArray.put("command.deleteAll", new String[]{"deleteAll", "Deletes all your claims (you need to double type to confirm it so no accidents)."});
        this.defaultTranslationArray.put("command.deleteSubClaim", new String[]{"deleteSubClaim", "Deletes the current subclaim."});
        this.defaultTranslationArray.put("command.deleteAllSubClaims", new String[]{"deleteAllSubClaims", "Deletes all subclaim of the current claim."});
        this.defaultTranslationArray.put("command.list", new String[]{"list <player>", "Lists all claims you have. If op also gives ability to list other players claims."});
        this.defaultTranslationArray.put("command.switchMode", new String[]{"switchMode", "Switch between normal and subclaim mode."});
        this.defaultTranslationArray.put("command.group", new String[]{"group (add | remove <name>) | (players add | remove <player> [overwrite])", "- Adds/removes the group with that name. Also editable via the claim menu.", "- Adds/remove a player to the group. If overwrite then will overwrite the players current group else does nothing. Also editable via the claim menu."});
        this.defaultTranslationArray.put("command.transferClaim", new String[]{"transferClaim <player>", "Gives ownership of the claim to the specified player. Only works if you're the claim owner."});
        this.defaultTranslationArray.put("command.addClaim", new String[]{"addClaim (<x y z> <x y z>) | all | (rect x z)", "Creates a claim with the given positions. Same as using the claim tool.", "<all> uses up all remaining blocks for a squared claim centered around the player", "<rect> creates a rectangular claim centered around the player"});
        this.defaultTranslationArray.put("command.permission", new String[]{"permission {global | (group <name>) | (personal <name>)} <permission> true | false | default", " Sets global/group/personal permissions. Also editable via the claim menu (for group perm right click on the group in the menu)."});
        this.defaultTranslationArray.put("command.personalGroups", new String[]{"personalGroups", "Opens the gui to edit personal groups."});
        this.defaultTranslationArray.put("command.sellBlocks", new String[]{"sellBlocks <amount>", "Sells <amount> claimblocks. Needs gunpowder currency installed."});
        this.defaultTranslationArray.put("command.buyBlocks", new String[]{"buyBlocks <amount>", "Buys <amount> claimblocks. Needs gunpowder currency installed."});
        this.defaultTranslationArray.put("command.trapped", new String[]{"trapped", "If in a claim not owned by the player attempts to teleport the player out of it after 5 seconds."});
        this.defaultTranslationArray.put("command.unlockDrops", new String[]{"unlockDrops <players>", "Unlocks dropped items from death so other players can pick them up too. Or all of the given players (needs OP)"});
        this.defaultTranslationArray.put("command.setHome", new String[]{"setHome", "Standing in a claim with sufficient permission sets that claims home to the players position"});
        this.defaultTranslationArray.put("command.teleport", new String[]{"teleport self | admin | (other <player>) (<claim name> | <claim uuid>)", "Teleport to the given claims home position. Use admin to teleport to admin claims"});
        this.defaultTranslationArray.put("command.name", new String[]{"name self <name>", "Sets the current claims name"});
        this.defaultTranslationArray.put("command.claimMessage", new String[]{"claimMessage (enter | leave) (title | subtitle) (string | text) <value>", "Sets the claims message. Use \"$empty\" to remove the message"});

        this.defaultTranslationArray.put("command.reload", new String[]{"reload", "Reloads the config ingame."});
        this.defaultTranslationArray.put("command.adminMode", new String[]{"adminMode", "Switches to admin mode ignoring all claims."});
        this.defaultTranslationArray.put("command.readGriefPrevention", new String[]{"readGriefPreventionData", "Parses data from the GriefPrevention plugin to Flan"});
        this.defaultTranslationArray.put("command.setAdminClaim", new String[]{"setAdminClaim", "Sets a claim to an admin claim."});
        this.defaultTranslationArray.put("command.listAdminClaims", new String[]{"listAdminClaim", "Lists all admin claims in the current world."});
        this.defaultTranslationArray.put("command.adminDelete", new String[]{"adminDelete [all <player>]", "Force deletes the current claim or deletes all claims from the specified player."});
        this.defaultTranslationArray.put("command.giveClaimBlocks", new String[]{"giveClaimBlocks <amount>", "Gives a player additional claim blocks."});
    }

    private final Map<String, String> translation = new HashMap<>();
    private final Map<String, String[]> translationArr = new HashMap<>();

    private final Path confDir;

    public LangManager() {
        this.loadDefault();
        Path configDir = CrossPlatformStuff.configPath().resolve("flan").resolve("lang");
        this.confDir = configDir;
        try {
            File dir = configDir.toFile();
            if (!dir.exists())
                dir.mkdirs();
            URL url = LangManager.class.getClassLoader().getResource("data/flan/lang");
            if (url != null) {
                URI uri = LangManager.class.getClassLoader().getResource("data/flan/lang").toURI();
                try {
                    FileSystems.newFileSystem(uri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException | IllegalArgumentException ignored) {
                }
                Files.walk(Paths.get(uri))
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                InputStream s = Files.newInputStream(p, StandardOpenOption.READ);
                                File target = configDir.resolve(p.getFileName().toString()).toFile();
                                if (!target.exists())
                                    target.createNewFile();
                                OutputStream o = new FileOutputStream(target);
                                int read;
                                for (byte[] buffer = new byte[8192]; (read = s.read(buffer, 0, 8192)) >= 0; ) {
                                    o.write(buffer, 0, read);
                                }
                                s.close();
                                o.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            File def = configDir.resolve("en_us.json").toFile();
            if (!def.exists()) {
                File legacy = CrossPlatformStuff.configPath().resolve("flan").resolve("flan_lang.json").toFile();
                Map<String, String> translation;
                Map<String, String[]> translationArr;
                if (legacy.exists()) {
                    FileReader reader = new FileReader(legacy);
                    JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                    reader.close();
                    Map<String, String> fromConf = new HashMap<>();
                    Map<String, String[]> fromConfArr = new HashMap<>();
                    obj.entrySet().forEach(e -> {
                        if (e.getKey().equals("commands")) {
                            JsonObject commands = e.getValue().getAsJsonObject();
                            commands.entrySet().forEach(c -> fromConfArr.put("command." + c.getKey(), GSON.fromJson(c.getValue(), String[].class)));
                        } else if (legacyPermissionGetter(e.getKey())) {
                            if (e.getValue().isJsonArray())
                                fromConfArr.put(e.getKey(), GSON.fromJson(e.getValue(), String[].class));
                            else
                                fromConfArr.put(e.getKey(), new String[]{e.getValue().getAsString()});
                        } else
                            fromConf.put(e.getKey(), e.getValue().getAsString());
                    });
                    //To preserve order
                    translation = new LinkedHashMap<>();
                    translationArr = new LinkedHashMap<>();
                    this.defaultTranslation.forEach((key, t) -> translation.put(key, fromConf.getOrDefault(key, t)));
                    this.defaultTranslationArray.forEach((key, t) -> translationArr.put(key, fromConfArr.getOrDefault(key, t)));
                } else {
                    translation = this.defaultTranslation;
                    translationArr = this.defaultTranslationArray;
                }
                def.createNewFile();
                saveTo(def, translation, translationArr);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        this.reload(ConfigHandler.config.lang);
    }

    private static boolean legacyPermissionGetter(String id) {
        try {
            PermissionRegistry.get(id.replace(".desc", ""));
            return true;
        } catch (NullPointerException ignored) {

        }
        return false;
    }

    public void reload(String lang) {
        try {
            FileReader reader = new FileReader(this.confDir.resolve(lang + ".json").toFile());
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);
            reader.close();
            obj.entrySet().forEach(e -> {
                if (e.getValue().isJsonArray()) {
                    JsonArray arr = e.getValue().getAsJsonArray();
                    this.translationArr.put(e.getKey(), GSON.fromJson(arr, String[].class));
                } else if (e.getValue().isJsonPrimitive())
                    this.translation.put(e.getKey(), e.getValue().getAsString());
            });
            //en_us is basically used as a default modifiable file
            if (lang.equals("en_us")) {
                //To preserve order
                Map<String, String> ordered = new LinkedHashMap<>();
                Map<String, String[]> orderedArr = new LinkedHashMap<>();
                this.defaultTranslation.forEach((key, t) -> ordered.put(key, this.translation.getOrDefault(key, t)));
                this.defaultTranslationArray.forEach((key, t) -> orderedArr.put(key, this.translationArr.getOrDefault(key, t)));
                saveTo(this.confDir.resolve("en_us.json").toFile(), ordered, orderedArr);
            }
        } catch (IOException e) {
            if (lang.equals("en_us"))
                e.printStackTrace();
            else
                this.reload("en_us");
        }
    }

    public String get(String key) {
        return this.translation.getOrDefault(key, key);
    }

    public String[] getArray(String key) {
        return this.translationArr.getOrDefault(key, new String[]{key});
    }

    private static void saveTo(File file, Map<String, String> translation, Map<String, String[]> translationArr) {
        try {
            JsonObject plain = GSON.toJsonTree(translation).getAsJsonObject();
            JsonObject arr = GSON.toJsonTree(translationArr).getAsJsonObject();
            arr.entrySet().forEach(e -> plain.add(e.getKey(), e.getValue()));
            FileWriter writer = new FileWriter(file);
            GSON.toJson(plain, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
