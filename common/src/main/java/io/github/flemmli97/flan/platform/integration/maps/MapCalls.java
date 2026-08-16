package io.github.flemmli97.flan.platform.integration.maps;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class MapCalls {

    public static boolean dynmapLoaded;
    public static boolean bluemapLoaded;
    public static boolean journeymapLoaded;

    public static void addClaimMarker(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.addClaimMarker(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.addClaimMarker(claim);
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration)
            JourneymapIntegration.addClaimMarker(claim);
    }

    public static void removeMarker(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.removeMarker(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.removeMarker(claim);
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration)
            JourneymapIntegration.addClaimMarker(claim);
    }

    public static void changeClaimName(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.changeClaimName(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.changeClaimName(claim);
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration)
            JourneymapIntegration.addClaimMarker(claim);
    }

    public static void changeClaimOwner(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.changeClaimOwner(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.changeClaimOwner(claim);
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration)
            JourneymapIntegration.addClaimMarker(claim);
    }

    public static void onExtendDownwards(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration) {
            DynmapIntegration.removeMarker(claim);
            DynmapIntegration.addClaimMarker(claim);
        }
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.addClaimMarker(claim);
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration)
            JourneymapIntegration.addClaimMarker(claim);
    }

    public static void triggerStylesChange(MinecraftServer server) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration) {
            for (ServerLevel level : server.getAllLevels()) {
                DynmapIntegration.updateStyles(ClaimStorage.get(level));
            }
        }
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration) {
            for (ServerLevel level : server.getAllLevels()) {
                BluemapIntegration.updateStyles(ClaimStorage.get(level));
            }
        }
        if (journeymapLoaded && ConfigHandler.CONFIG.journeymapIntegration) {
            for (ServerLevel level : server.getAllLevels()) {
                JourneymapIntegration.updateStyles(ClaimStorage.get(level));
            }
        }
    }
}
