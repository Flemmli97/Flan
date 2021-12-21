package io.github.flemmli97.flan.config;

import com.google.gson.JsonObject;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;

import java.util.Map;

public class ConfigUpdater {

    private static final Map<Integer, Updater> updater = Config.createHashMap(map -> {
        map.put(2, old -> {
            Flan.debug("Updating config to version 2");
            ConfigHandler.config.globalDefaultPerms.compute("*", (k, v) -> {
                if (v == null) {
                    return Config.createHashMap(map1 -> map1.put(PermissionRegistry.LOCKITEMS, Config.GlobalType.ALLTRUE));
                } else {
                    v.put(PermissionRegistry.LOCKITEMS, Config.GlobalType.ALLTRUE);
                    return v;
                }
            });
        });
        map.put(3, old -> {
            Flan.debug("Updating config to version 3");
            ConfigHandler.arryFromJson(old, "ignoredBlocks").forEach(e -> {
                if (!ConfigHandler.config.breakBlockBlacklist.contains(e.getAsString()))
                    ConfigHandler.config.breakBlockBlacklist.add(e.getAsString());
            });
            ConfigHandler.arryFromJson(old, "ignoredBlocks").forEach(e -> {
                if (!ConfigHandler.config.interactBlockBlacklist.contains(e.getAsString()))
                    ConfigHandler.config.interactBlockBlacklist.add(e.getAsString());
            });
            ConfigHandler.arryFromJson(old, "blockEntityTagIgnore").forEach(e -> {
                if (!ConfigHandler.config.interactBETagBlacklist.contains(e.getAsString()))
                    ConfigHandler.config.interactBETagBlacklist.add(e.getAsString());
            });
        });
        map.put(4, old -> {
            Flan.debug("Updating config to version 4");
            ConfigHandler.config.itemPermission.add("@c:wrenches-INTERACTBLOCK");
        });
    });

    public static void updateConfig(int preVersion, JsonObject oldVals) {
        updater.entrySet().stream().filter(e -> e.getKey() > preVersion).map(Map.Entry::getValue)
                .forEach(u -> u.configUpdater(oldVals));
    }

    interface Updater {

        void configUpdater(JsonObject oldVals);

    }
}