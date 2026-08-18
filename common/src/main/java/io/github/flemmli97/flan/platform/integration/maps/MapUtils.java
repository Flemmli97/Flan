package io.github.flemmli97.flan.platform.integration.maps;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;

import java.util.Optional;

public class MapUtils {

    public static int lineColor(boolean admin) {
        return admin ? ConfigHandler.CONFIG.adminMapBorderColor : ConfigHandler.CONFIG.mapBorderColor;
    }

    public static int fillColor(boolean admin) {
        return admin ? ConfigHandler.CONFIG.adminMapFillColor : ConfigHandler.CONFIG.mapFillColor;
    }

    public static String claimLabel(Claim claim) {
        String name = claim.getClaimName();
        if (claim.isAdminClaim()) {
            if (name == null || name.isEmpty()) {
                return "Admin Claim";
            } else {
                return name + " - " + "Admin Claim";
            }
        }
        Optional<String> prof = ClaimUtils.fetchUsername(claim.getOwner(), claim.getLevel().getServer());
        if (name == null || name.isEmpty()) {
            return prof.orElse("UNKNOWN") + "'s Claim";
        } else {
            return name + " - " + prof.orElse("UNKNOWN") + "'s Claim";
        }
    }
}
