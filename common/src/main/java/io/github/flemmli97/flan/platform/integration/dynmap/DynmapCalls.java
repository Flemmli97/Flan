package io.github.flemmli97.flan.platform.integration.dynmap;

import io.github.flemmli97.flan.claim.Claim;

public class DynmapCalls {

    public static boolean dynmapLoaded;

    public static void addClaimMarker(Claim claim) {
        if (dynmapLoaded)
            DynmapIntegration.addClaimMarker(claim);
    }

    public static void removeMarker(Claim claim) {
        if (dynmapLoaded)
            DynmapIntegration.removeMarker(claim);
    }

    public static void changeClaimName(Claim claim) {
        if (dynmapLoaded)
            DynmapIntegration.changeClaimName(claim);
    }

    public static void changeClaimOwner(Claim claim) {
        if (dynmapLoaded)
            DynmapIntegration.changeClaimOwner(claim);
    }
}
