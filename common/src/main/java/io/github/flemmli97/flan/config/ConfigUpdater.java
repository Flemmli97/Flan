package io.github.flemmli97.flan.config;

import com.google.gson.JsonObject;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;

import java.util.Map;

public class ConfigUpdater {

    private static final Map<Integer, Updater> UPDATER = Config.createHashMap(map -> {
        map.put(2, old -> {
            Flan.debug("Updating config to version 2");
            ConfigHandler.CONFIG.globalDefaultPerms.compute("*", (k, v) -> {
                if (v == null) {
                    return Config.createHashMap(map1 -> map1.put(BuiltinPermission.LOCKITEMS, Config.GlobalType.ALLTRUE));
                } else {
                    v.put(BuiltinPermission.LOCKITEMS, Config.GlobalType.ALLTRUE);
                    return v;
                }
            });
        });
        map.put(3, old -> {
            Flan.debug("Updating config to version 3");
            ConfigHandler.arryFromJson(old, "ignoredBlocks").forEach(e -> {
                if (!ConfigHandler.CONFIG.breakBlockBlacklist.contains(e.getAsString()))
                    ConfigHandler.CONFIG.breakBlockBlacklist.add(e.getAsString());
            });
            ConfigHandler.arryFromJson(old, "ignoredBlocks").forEach(e -> {
                if (!ConfigHandler.CONFIG.interactBlockBlacklist.contains(e.getAsString()))
                    ConfigHandler.CONFIG.interactBlockBlacklist.add(e.getAsString());
            });
            ConfigHandler.arryFromJson(old, "blockEntityTagIgnore").forEach(e -> {
                if (!ConfigHandler.CONFIG.interactBETagBlacklist.contains(e.getAsString()))
                    ConfigHandler.CONFIG.interactBETagBlacklist.add(e.getAsString());
            });
        });
    });

    public static void updateConfig(int preVersion, JsonObject oldVals) {
        UPDATER.entrySet().stream().filter(e -> e.getKey() > preVersion).map(Map.Entry::getValue)
                .forEach(u -> u.configUpdater(oldVals));
    }

    interface Updater {

        void configUpdater(JsonObject oldVals);

    }
}