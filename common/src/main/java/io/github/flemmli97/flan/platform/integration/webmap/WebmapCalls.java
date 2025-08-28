package io.github.flemmli97.flan.platform.integration.webmap;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.config.ConfigHandler;

public class WebmapCalls {

    public static boolean dynmapLoaded;
    public static boolean bluemapLoaded;

    public static void addClaimMarker(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.addClaimMarker(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.addClaimMarker(claim);
    }

    public static void removeMarker(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.removeMarker(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.removeMarker(claim);
    }

    public static void changeClaimName(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.changeClaimName(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.changeClaimName(claim);
    }

    public static void changeClaimOwner(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration)
            DynmapIntegration.changeClaimOwner(claim);
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.changeClaimOwner(claim);
    }

    public static void onExtendDownwards(Claim claim) {
        if (dynmapLoaded && ConfigHandler.CONFIG.dynmapIntegration) {
            DynmapIntegration.removeMarker(claim);
            DynmapIntegration.addClaimMarker(claim);
        }
        if (bluemapLoaded && ConfigHandler.CONFIG.bluemapIntegration)
            BluemapIntegration.addClaimMarker(claim);
    }
}
