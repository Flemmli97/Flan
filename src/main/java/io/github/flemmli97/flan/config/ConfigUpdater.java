package io.github.flemmli97.flan.config;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;

import java.util.Map;

public class ConfigUpdater {

    private static final Map<Integer, Updater> updater = Config.createHashMap(map -> {
        map.put(2, () -> {
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
    });

    public static void updateConfig(int preVersion) {
        updater.entrySet().stream().filter(e -> e.getKey() > preVersion).map(Map.Entry::getValue)
                .forEach(Updater::configUpdater);
    }

    interface Updater {

        void configUpdater();

    }
}
