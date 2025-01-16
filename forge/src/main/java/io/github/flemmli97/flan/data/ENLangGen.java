package io.github.flemmli97.flan.data;

import com.google.common.collect.ImmutableSet;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.gui.CustomInteractListScreenHandler;
import io.github.flemmli97.linguabib.api.ServerLangGen;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ENLangGen extends ServerLangGen {

    private final Set<String> keys = new HashSet<>();
    private final PermissionGen permissionGen;

    public ENLangGen(PackOutput output, PermissionGen permissionGen) {
        super(output, Flan.MODID, "en_us");
        this.permissionGen = permissionGen;
    }

    @Override
    protected void addTranslations() {
        this.add("flan.noClaim", "There is no claim here.");
        this.add("flan.inspectBlockOwner", "This is %1$s's claim");
        this.add("flan.inspectNoClaim", "Nobody owns this block");
        this.add("flan.claimBlocksFormat", "Claim Blocks: %1$s + (Bonus) %2$s; Used: %3$s; Remaining: %4$s");
        this.add("flan.listClaims", "Listing all claims:");
        this.add("flan.listAdminClaims", "Listing all admin-claims in %1$s:");
        this.add("flan.onlyOnePlayer", "Only one player can be used as argument");
        this.add("flan.ownerTransferSuccess", "New Claimowner now: %1$s");
        this.add("flan.ownerTransferFail", "Only the owner may transfer claims");
        this.add("flan.ownerTransferNoBlocks", "The new owner doesnt have enough claimblocks");
        this.add("flan.ownerTransferNoBlocksAdmin", "You can ignore this by switching to admin mode");
        this.add("flan.noSuchLevel", "Dimension with id %s doesn't exist");
        this.add("flan.noSuchPlayer", "Player with uuid/name %s doesn't exist");
        this.add("flan.claimCreationFailCommand", "Couldn't create claim at this position");

        this.add("flan.noPermission", "You don't have the required permissions to do that here!");
        this.add("flan.noPermissionSimple", "Sorry you can't do that here!");
        this.add("flan.noPermissionTooClose", "Sorry you are too close to a claim to do that here!");
        this.add("flan.tooCloseClaim", "You are too close to a protected area to do that!");

        this.add("flan.configReload", "Configs reloaded");

        this.add("flan.cantClaimHere", "Sorry you cant claim here");
        this.add("flan.minClaimSize", "This is too small. Minimum claimsize is %d");
        this.add("flan.maxClaims", "Maximum amount of claims reached");
        this.add("flan.landClaimDisabledWorld", "Claiming is disabled in this world");
        this.add("flan.claimingMode", "Claiming mode set to %1$s");
        this.add("flan.minClaimHeight", "Minimum required height is %d");
        this.add("flan.editMode", "Editing mode set to %1$s");
        this.add("flan.notEnoughBlocks", "Not enough claim blocks. Required: %s, Current: %s");
        this.add("flan.conflictOther", "Claim would overlap other claims");
        this.add("flan.wrongMode", "Wrong claim mode. You are in %1$s-mode");
        this.add("flan.stringScreenReturn", "Click on paper to go back");
        this.add("flan.conflictSpawn", "Claim would spawn protection area");
        this.add("flan.claimCooldown", "You need to wait %s ticks before creating another claim");

        this.add("flan.groupAdd", "Added group %1$s");
        this.add("flan.groupRemove", "Removed group %1$s");
        this.add("flan.groupExist", "Group already exist");
        this.add("flan.uuidFakeAdd", "Added fakeplayer with uuid %1$s to the claim");
        this.add("flan.uuidFakeRemove", "Removed fakeplayer with uuid %1$s from the claim");
        this.add("flan.uuidFakeModifyNo", "Fakeplayer with given uuid is already added");
        this.add("flan.playerModify", "Modified permission group for following players to %1$s: %2$s");
        this.add("flan.playerModifyNo", "Couldn't set permission group for the players. Probably cause they already belong to a group");
        this.add("flan.playerGroupAddFail", "Couldn't add that player to the group either cause the player " +
                "is already in a group or no player matching the name was found");
        this.add("flan.resizeClaim", "Resizing claim");
        this.add("flan.resizeSuccess", "Resized Claims");
        this.add("flan.claimCreateSuccess", "Created a new claim");
        this.add("flan.subClaimCreateSuccess", "Created a new subclaim");
        this.add("flan.deleteClaim", "Claim deleted");
        this.add("flan.deleteAllClaimConfirm", "Are you sure you want to delete all claims? Type it again to confirm");
        this.add("flan.deleteAllClaim", "All claims deleted");
        this.add("flan.deleteClaimError", "You can't delete this claim here");
        this.add("flan.deleteSubClaim", "Subclaim deleted");
        this.add("flan.deleteSubClaimError", "There is no subclaim here. Switch your claim mode to delete the main claim.");
        this.add("flan.deleteSubClaimAll", "All Subclaims from this claim deleted");
        this.add("flan.noSuchPerm", "No such Permission %1$s");
        this.add("flan.editPerm", "%1$s now set to %2$s");
        this.add("flan.editPermGroup", "%1$s for %2$s now set to %3$s");
        this.add("flan.editPersonalGroup", "Default permission %1$s for group %2$s now set to %3$s");
        this.add("flan.nonGlobalOnly", "Cannot edit %1$s here since its a global permission");

        this.add("flan.adminMode", "Adminmode (Ignore Claims) set to: %1$s");
        this.add("flan.adminDeleteAll", "Deleted all claims for following players: %1$s");
        this.add("flan.setAdminClaim", "Adminclaim of this claim now: %1$s");
        this.add("flan.readGriefpreventionData", "Reading data from GriefPrevention");
        this.add("flan.readGriefpreventionClaimDataSuccess", "Successfully read claim data");
        this.add("flan.readGriefpreventionPlayerDataSuccess", "Successfully read player data");
        this.add("flan.cantFindData", "No griefprevention data at %1$s");
        this.add("flan.errorFile", "Error reading file %1$s");
        this.add("flan.readConflict", "%1$s conflicts with existing claims. Not added to world! Conflicts:");
        this.add("flan.giveClaimBlocks", "Gave following players %2$s claimblocks: %1$s");
        this.add("flan.giveClaimBlocksBonus", "Gave following players %2$s bonus claimblocks: %1$s");

        this.add("flan.fakePlayerNotification1", "A fakeplayer tried to interact with your claim at %1$s in %2$s.");
        this.add("flan.fakePlayerNotification2", "Click %s while standing in your claim to add the fakeplayer to the claim.");
        this.add("flan.clickableComponent", "here");
        this.add("flan.fakePlayerNotification3", "Click %s to disable this notification.");
        this.add("flan.fakePlayerNotification", "FakePlayer notification set to %s");

        this.add("flan.claimBasicInfo", "Owner: %1$s, from: [x=%2$s,z=%3$s] to [x=%4$s,z=%5$s]); Subclaim-amount: %6$s");
        this.add("flan.claimBasicInfoNamed", "Claim: %7$s, Owner: %1$s, from: [x=%2$s,z=%3$s] to [x=%4$s,z=%5$s]); Subclaim-amount: %6$s");
        this.add("flan.claimSubHeader", "==SubclaimInfo==");
        this.add("flan.claimBasicInfoSub", "Owner: %1$s, from: [x=%2$s,z=%3$s] to [x=%4$s,z=%5$s]");
        this.add("flan.claimBasicInfoSubNamed", "Claim: %6$s, Owner: %1$s, from: [x=%2$s,z=%3$s] to [x=%4$s,z=%5$s]");
        this.add("flan.claimInfoPerms", "Permissions: %1$s");
        this.add("flan.claimGroupInfoHeader", "Groups: ");
        this.add("flan.claimGroupPerms", "    Permissions: %1$s");
        this.add("flan.claimGroupPlayers", "    Players: %1$s");
        this.add("flan.helpHeader", "Available subcommands are (page %d):");
        this.add("flan.helpCmdHeader", "====================");
        this.add("flan.helpCmdSyntax", "Syntax: %1$s");

        this.add("flan.screenEnableText", "Enabled: %1$s");
        this.add("flan.screenUneditable", "Non Editable!");
        this.add("flan.screenClose", "Close");
        this.add("flan.screenNext", "Next");
        this.add("flan.screenPrevious", "Prev");
        this.add("flan.screenAdd", "Add");
        this.add("flan.screenBack", "Back");
        this.add("flan.screenNoPerm", "No Permission");
        this.add("flan.screenFalse", "false");
        this.add("flan.screenTrue", "true");
        this.add("flan.screenDefault", "default");

        this.add("flan.screenMenu", "Claim-Menu");
        this.add("flan.screenMenuSub", "SubClaim-Menu");
        this.add("flan.screenMenuGlobal", "Edit Global Permissions");
        this.add("flan.screenMenuGroup", "Edit Permissiongroups");
        this.add("flan.screenMenuPotion", "Edit Potioneffects");
        this.add("flan.screenMenuClaimText", "Edit Enter/Leave Text");
        this.add("flan.screenMenuFakePlayers", "Fake Players");
        this.add("flan.screenMenuDelete", "Delete Claim");
        this.add("flan.screenConfirm", "Confirm");
        this.add("flan.screenYes", "Yes");
        this.add("flan.screenNo", "No");
        this.add("flan.screenGroupPlayers", "%1$s-Players");
        this.add("flan.screenRemoveMode", "Remove Mode: %1$s");
        this.add("flan.screenGlobalPerms", "Global-Permissions");
        this.add("flan.screenGroupName", "%s");
        this.add("flan.screenGroups", "Claim-Groups");
        this.add("flan.screenGroupPerms", "%1$s-Permissions");
        this.add("flan.screenPersonalGroups", "Personal-Groups");
        this.add("flan.screenPersonalPermissions", "Personal Permissions for %1$s");
        this.add("flan.screenFakePlayerNameUUID", "%s");
        this.add("flan.screenPotions", "Claim Potions");
        this.add("flan.screenPotionText", "%s");
        this.add("flan.screenTitleEditor", "Claim messages");
        this.add("flan.screenTitleEditorSub", "Subclaim messages");
        this.add("flan.screenTextJson", "Right-Click to use JSON text. See MC Wiki for that.");
        this.add("flan.screenEnterText", "Edit title text on enter.");
        this.add("flan.screenEnterSubText", "Edit subtitle text on enter.");
        this.add("flan.screenLeaveText", "Edit title text on leave.");
        this.add("flan.screenLeaveSubText", "Edit subtitle text on leave.");
        this.add("flan.screenDelete", "Use \"$empty\" to delete the message");
        this.add(CustomInteractListScreenHandler.Type.ITEM.translationKey, "Allowed item use");
        this.add(CustomInteractListScreenHandler.Type.BLOCKBREAK.translationKey, "Allowed block break");
        this.add(CustomInteractListScreenHandler.Type.BLOCKUSE.translationKey, "Allowed block use");
        this.add(CustomInteractListScreenHandler.Type.ENTITYATTACK.translationKey, "Allowed entities to attack");
        this.add(CustomInteractListScreenHandler.Type.ENTITYUSE.translationKey, "Allowed entity interactions");
        this.add("flan.allowListEmptyTag", "Empty Tag");

        this.add("flan.chatClaimTextEdit", "[Click for command]");

        this.add("flan.sellDisabled", "Claimblocks selling is disabled");
        this.add("flan.buyDisabled", "Claimblocks purchasing is disabled");
        this.add("flan.sellFail", "Not enough claimblocks to sell");
        this.add("flan.buyFail", "Not enough money");
        this.add("flan.buyFailItem", "Not enough items");
        this.add("flan.buyFailXP", "Not enough experience points");
        this.add("flan.sellSuccess", "Sold %1$s claimblocks for %2$s");
        this.add("flan.sellSuccessItem", "Sold %1$s claimblocks for %3$s x%2$s");
        this.add("flan.sellSuccessXP", "Sold %1$s claimblocks for %2$s experience points");
        this.add("flan.buySuccess", "Bought %1$s claimblocks for %2$s");
        this.add("flan.buySuccessItem", "Bought %1$s claimblocks with %2$s items");
        this.add("flan.buySuccessXP", "Bought %1$s claimblocks with %2$s experience points");

        this.add("flan.currencyMissing", "Missing a supported currency mod");

        this.add("flan.trappedRescue", "Rescuing. Don't move for 5 seconds");
        this.add("flan.trappedFail", "Rescue not necessary or already rescuing");
        this.add("flan.trappedMove", "You moved. Aborting teleport");

        this.add("flan.unlockDropsCmd", "Your deathitems are protected. Use %1$s to unlock them for other players");
        this.add("flan.unlockDrops", "Your deathitems are now unlocked for %1$s ticks");
        this.add("flan.unlockDropsMulti", "Unlocked drops for %1$s");

        this.add("flan.claimNameSet", "Claims name set to %1$s");
        this.add("flan.claimNameUsed", "The owner of the claim already has another claim with the same name");
        this.add("flan.claimNameUsedSub", "One of the subclaim of this claim already has this name");

        this.add("flan.setHome", "Claim home set to [x=%1$s,y=%2$s,z=%3$s]");
        this.add("flan.teleportNoClaim", "No such claim to teleport to");
        this.add("flan.teleportHome", "Teleporting to claim home. Don't move for 5 seconds");
        this.add("flan.teleportHomeFail", "Teleport already happening");

        this.add("flan.setEnterMessage", "Set enter title to %1$s");
        this.add("flan.setEnterSubMessage", "Set enter subtitle to %1$s");
        this.add("flan.setLeaveMessage", "Set leave title to %1$s");
        this.add("flan.setLeaveSubMessage", "Set leave subtitle to %1$s");

        this.add("flan.addIgnoreEntry", "Added %1$s to the claims ignore list %2$s");
        this.add("flan.removeIgnoreEntry", "Removed %1$s from the claims ignore list %2$s");

        this.add("flan.wiki", "For more info check out the wiki:");

        for (Map.Entry<ResourceLocation, ClaimPermission.Builder> entry : this.permissionGen.getData().entrySet()) {
            ClaimPermission perm = entry.getValue().build(entry.getKey());
            this.add(perm.translationKey(), this.capitalize(perm.getId().getPath()));
            this.add(perm.translationKeyDescription(), perm.desc.toArray(String[]::new));
        }

        this.add("flan.command.help", "help <page> | (cmd <command>)", "Shows all available commands or info for the given command.");
        this.add("flan.command.menu", "menu", "When standing in a claim you have permissions for opens the claim menu.");
        this.add("flan.command.claimInfo", "claimInfo", "Prints infos about the claim you're standing in.");
        this.add("flan.command.delete", "delete", "Deletes the current claim.");
        this.add("flan.command.deleteAll", "deleteAll", "Deletes all your claims (you need to double type to confirm it so no accidents).");
        this.add("flan.command.deleteSubClaim", "deleteSubClaim", "Deletes the current subclaim.");
        this.add("flan.command.deleteAllSubClaims", "deleteAllSubClaims", "Deletes all subclaim of the current claim.");
        this.add("flan.command.list", "list <player>", "Lists all claims you have. If op also gives ability to list other players claims.");
        this.add("flan.command.switchMode", "switchMode", "Switch between normal and subclaim mode.");
        this.add("flan.command.group", "group (add | remove <name>) | (players add | remove <player> [overwrite])", "- Adds/removes the group with that name. Also editable via the claim menu.", "- Adds/remove a player to the group. If overwrite then will overwrite the players current group else does nothing. Also editable via the claim menu.");
        this.add("flan.command.transferClaim", "transferClaim <player>", "Gives ownership of the claim to the specified player. Only works if you're the claim owner.");
        this.add("flan.command.addClaim", "addClaim (<x y z> <x y z>) | all | (rect x z)", "Creates a claim with the given positions. Same as using the claim tool.", "<all> uses up all remaining blocks for a squared claim centered around the player", "<rect> creates a rectangular claim centered around the player");
        this.add("flan.command.permission", "permission {global | (group <name>) | (personal <name>)} <permission> true | false | default", " Sets global/group/personal permissions. Also editable via the claim menu (for group perm right click on the group in the menu).");
        this.add("flan.command.personalGroups", "personalGroups", "Opens the gui to edit personal groups.");
        this.add("flan.command.sellBlocks", "sellBlocks <amount>", "Sells <amount> claimblocks. Needs gunpowder currency installed.");
        this.add("flan.command.buyBlocks", "buyBlocks <amount>", "Buys <amount> claimblocks. Needs gunpowder currency installed.");
        this.add("flan.command.trapped", "trapped", "If in a claim not owned by the player attempts to teleport the player out of it after 5 seconds.");
        this.add("flan.command.unlockDrops", "unlockDrops <players>", "Unlocks dropped items from death so other players can pick them up too. Or all of the given players (needs OP)");
        this.add("flan.command.setHome", "setHome", "Standing in a claim with sufficient permission sets that claims home to the players position");
        this.add("flan.command.teleport", "teleport self | admin | (other <player>) (<claim name> | <claim uuid>)", "Teleport to the given claims home position. Use admin to teleport to admin claims");
        this.add("flan.command.name", "name self <name>", "Sets the current claims name");
        this.add("flan.command.claimMessage", "claimMessage (enter | leave) (title | subtitle) (string | text) <value>", "Sets the claims message. Use \"$empty\" to remove the message");
        this.add("flan.command.ignoreList", "ignoreList (add | remove) <type> <value>", "Adds/Removes an entry to the claim specific ignore list");

        this.add("flan.command.reload", "reload", "Reloads the config ingame.");
        this.add("flan.command.adminMode", "adminMode", "Switches to admin mode ignoring all claims.");
        this.add("flan.command.readGriefPrevention", "readGriefPreventionData", "Parses data from the GriefPrevention plugin to Flan");
        this.add("flan.command.setAdminClaim", "setAdminClaim", "Sets a claim to an admin claim.");
        this.add("flan.command.listAdminClaims", "listAdminClaim", "Lists all admin claims in the current world.");
        this.add("flan.command.adminDelete", "adminDelete [all <player>]", "Force deletes the current claim or deletes all claims from the specified player.");
        this.add("flan.command.giveClaimBlocks", "giveClaimBlocks <amount>", "Gives a player additional claim blocks.");
    }

    @Override
    public void add(String key, String value) {
        super.add(key, value);
        this.keys.add(key);
    }

    @Override
    public void add(String key, String... lines) {
        super.add(key, lines);
        this.keys.add(key);
    }

    public Set<String> allKeys() {
        return ImmutableSet.copyOf(this.keys);
    }

    private String capitalize(String s) {
        return Stream.of(s.trim().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
