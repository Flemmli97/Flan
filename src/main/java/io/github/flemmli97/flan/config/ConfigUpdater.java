package io.github.flemmli97.flan.config;

import io.github.flemmli97.flan.claim.Claim;

import java.util.Map;

public class ConfigUpdater {

    private static final Map<Integer, Updater> updater = Config.createHashMap(map -> {

    });

    public static void updateConfig(int preVersion) {
        updater.entrySet().stream().filter(e -> e.getKey() > preVersion).map(Map.Entry::getValue)
                .forEach(Updater::configUpdater);
    }

    public static void updateClaim(Claim claim) {
        updater.entrySet().stream().filter(e -> e.getKey() > ConfigHandler.config.preConfigVersion).map(Map.Entry::getValue)
                .forEach(up -> up.claimUpdater(claim));
    }

    interface Updater {

        void configUpdater();

        void claimUpdater(Claim claim);

    }
}
