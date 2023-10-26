package io.github.flemmli97.flan.platform.integration.webmap;

import com.mojang.authlib.GameProfile;
import io.github.flemmli97.flan.claim.Claim;
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
    private static final String markerID = "flan.claims", markerLabel = "Claims";

    public static void reg() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
                MarkerAPI markerAPI = dynmapCommonAPI.getMarkerAPI();
                markerSet = markerAPI.createMarkerSet(markerID, markerLabel, dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
                WebmapCalls.dynmapLoaded = true;
            }
        });
    }

    static void addClaimMarker(Claim claim) {
        if (markerSet == null)
            return;
        int[] dim = claim.getDimensions();
        AreaMarker marker = markerSet.createAreaMarker(claim.getClaimID().toString(), claimLabel(claim), true, getWorldName(claim.getWorld()), new double[]{dim[0], dim[1]}, new double[]{dim[2], dim[3]}, false);
        marker.setLineStyle(3, 0.8, lineColor(claim.isAdminClaim()));
        marker.setFillStyle(0.2, fillColor(claim.isAdminClaim()));
        marker.setRangeY(dim[4], claim.getMaxY());
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
        markerSet.findAreaMarker(claim.getClaimID().toString())
                .setLabel(claimLabel(claim));
    }

    static void changeClaimOwner(Claim claim) {
        if (markerSet == null)
            return;
        if (claim.getClaimName() == null || claim.getClaimName().isEmpty())
            markerSet.findAreaMarker(claim.getClaimID().toString())
                    .setLabel(claimLabel(claim));
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
            Optional<GameProfile> prof = claim.getWorld().getServer().getProfileCache().get(claim.getOwner());
            return prof.map(GameProfile::getName).orElse("UNKOWN") + "'s Claim";
        }
        return name;
    }
}