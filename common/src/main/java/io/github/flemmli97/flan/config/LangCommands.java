package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;

public class LangCommands {

    private Map<String, String[]> commands = Config.createHashMap(map -> {
        map.put("help", new String[]{"help <page> | (cmd <command>)", "Shows all available commands or info for the given command."});
        map.put("menu", new String[]{"menu", "When standing in a claim you have permissions for opens the claim menu."});
        map.put("claimInfo", new String[]{"claimInfo", "Prints infos about the claim you're standing in."});
        map.put("delete", new String[]{"delete", "Deletes the current claim."});
        map.put("deleteAll", new String[]{"deleteAll", "Deletes all your claims (you need to double type to confirm it so no accidents)."});
        map.put("deleteSubClaim", new String[]{"deleteSubClaim", "Deletes the current subclaim."});
        map.put("deleteAllSubClaims", new String[]{"deleteAllSubClaims", "Deletes all subclaim of the current claim."});
        map.put("list", new String[]{"list <player>", "Lists all claims you have. If op also gives ability to list other players claims."});
        map.put("switchMode", new String[]{"switchMode", "Switch between normal and subclaim mode."});
        map.put("group", new String[]{"group (add | remove <name>) | (players add | remove <player> [overwrite])", "- Adds/removes the group with that name. Also editable via the claim menu.", "- Adds/remove a player to the group. If overwrite then will overwrite the players current group else does nothing. Also editable via the claim menu."});
        map.put("transferClaim", new String[]{"transferClaim <player>", "Gives ownership of the claim to the specified player. Only works if you're the claim owner."});
        map.put("addClaim", new String[]{"addClaim <x y z> <x y z>", "Creates a claim with the given positions. Same as using the claim tool."});
        map.put("permission", new String[]{"permission {global | (group <name>) | (personal <name>)} <permission> true | false | default", " Sets global/group/personal permissions. Also editable via the claim menu (for group perm right click on the group in the menu)."});
        map.put("personalGroups", new String[]{"personalGroups", "Opens the gui to edit personal groups."});
        map.put("sellBlocks", new String[]{"sellBlocks <amount>", "Sells <amount> claimblocks. Needs gunpowder currency installed."});
        map.put("buyBlocks", new String[]{"buyBlocks <amount>", "Buys <amount> claimblocks. Needs gunpowder currency installed."});
        map.put("trapped", new String[]{"trapped", "If in a claim not owned by the player attempts to teleport the player out of it after 5 seconds."});
        map.put("unlockDrops", new String[]{"unlockDrops <players>", "Unlocks dropped items from death so other players can pick them up too. Or all of the given players (needs OP)"});

        map.put("reload", new String[]{"reload", "Reloads the config ingame."});
        map.put("adminMode", new String[]{"adminMode", "Switches to admin mode ignoring all claims."});
        map.put("readGriefPrevention", new String[]{"readGriefPreventionData", "Parses data from the GriefPrevention plugin to io.github.flemmli97.flan.Flan."});
        map.put("setAdminClaim", new String[]{"setAdminClaim", "Sets a claim to an admin claim."});
        map.put("listAdminClaims", new String[]{"listAdminClaim", "Lists all admin claims in the current world."});
        map.put("adminDelete", new String[]{"adminDelete [all <player>]", "Force deletes the current claim or deletes all claims from the specified player."});
        map.put("giveClaimBlocks", new String[]{"giveClaimBlocks <amount>", "Gives a player additional claim blocks."});
    });

    public void load(JsonObject obj) throws IllegalAccessException {
        obj.entrySet().forEach(e -> {
            String[] val;
            if (e.getValue().isJsonArray()) {
                JsonArray arr = e.getValue().getAsJsonArray();
                val = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++)
                    val[i] = arr.get(i).getAsString();
            } else {
                val = new String[1];
                val[0] = e.getValue().getAsString();
            }
            this.commands.put(e.getKey(), val);
        });
    }

    public void save(JsonObject obj) throws IllegalAccessException {
        this.commands.forEach((cmd, val) -> {
            JsonArray arr = new JsonArray();
            if (val != null)
                for (String s : val)
                    arr.add(s);
            obj.add(cmd, arr);
        });
    }

    public String[] getCommandHelp(String command) {
        return this.commands.getOrDefault(command, new String[0]);
    }
}
