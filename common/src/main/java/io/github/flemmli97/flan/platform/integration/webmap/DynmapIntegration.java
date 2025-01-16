package io.github.flemmli97.flan.platform.integration.webmap;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.Optional;

public class DynmapIntegration {

    private static MarkerSet markerSet;
    private static final String MARKER_ID = "flan.claims", MARKER_LABEL = "Claims";

    public static void reg() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
                MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
                markerSet = markerAPI.createMarkerSet(MARKER_ID, MARKER_LABEL, dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
                WebmapCalls.dynmapLoaded = true;
            }
        });
    }

    static void addClaimMarker(Claim claim) {
        if (markerSet == null)
            return;
        ClaimBox dim = claim.getDimensions();
        AreaMarker marker = markerSet.createAreaMarker(claim.getClaimID().toString(), claimLabel(claim), true, getWorldName(claim.getLevel()),
                new double[]{dim.minX(), dim.maxX()}, new double[]{dim.minZ(), dim.maxZ()}, false);
        if (marker != null) {
            marker.setLineStyle(3, 0.8, lineColor(claim.isAdminClaim()));
            marker.setFillStyle(0.2, fillColor(claim.isAdminClaim()));
            marker.setRangeY(dim.minY(), dim.maxY());
        }
    }

    static void removeMarker(Claim claim) {
        if (markerSet == null)
            return;
        AreaMarker marker = markerSet.findAreaMarker(claim.getClaimID().toString());
        if (marker != null)
            marker.deleteMarker();
    }

    static void changeClaimName(Claim claim) {
        if (markerSet == null)
            return;
        AreaMarker marker = markerSet.findAreaMarker(claim.getClaimID().toString());
        if (marker != null)
            marker.setLabel(claimLabel(claim));
    }

    static void changeClaimOwner(Claim claim) {
        if (markerSet == null)
            return;
        if (claim.getClaimName() == null || claim.getClaimName().isEmpty()) {
            AreaMarker marker = markerSet.findAreaMarker(claim.getClaimID().toString());
            if (marker != null)
                marker.setLabel(claimLabel(claim));
        }
    }

    private static String getWorldName(Level level) {
        ResourceKey<Level> key = level.dimension();
        if (key == Level.OVERWORLD) {
            return level.getServer().getWorldData().getLevelName();
        } else if (key == Level.END) {
            return "DIM1";
        } else if (key == Level.NETHER) {
            return "DIM-1";
        }
        return key.location().getNamespace() + "_" + key.location().getPath();
    }

    private static int lineColor(boolean admin) {
        return admin ? 0xb50909 : 0xffa200;
    }

    private static int fillColor(boolean admin) {
        return admin ? 0xff0000 : 0xe0e01d;
    }

    private static String claimLabel(Claim claim) {
        String name = claim.getClaimName();
        if (name == null || name.isEmpty()) {
            if (claim.isAdminClaim())
                return "Admin Claim";
            Optional<String> prof = ClaimUtils.fetchUsername(claim.getOwner(), claim.getLevel().getServer());
            return prof.orElse("UNKOWN") + "'s Claim";
        }
        return name;
    }
}