package io.github.flemmli97.flan.platform.integration.webmap;

import com.mojang.authlib.GameProfile;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BluemapIntegration {
    private static final String markerID = "flan.claims", markerLabel = "Claims";

    public static void reg(MinecraftServer server) {
        BlueMapAPI.onEnable(api -> {
            for (ServerLevel level : server.getAllLevels()) {
                api.getWorld(level).ifPresent(world -> {
                    world.getMaps().forEach(map -> {
                        MarkerSet markerSet = MarkerSet.builder().label(markerLabel).build();
                        map.getMarkerSets().put(markerID, markerSet);
                    });
                });
                processClaims(level);
            }
            WebmapCalls.bluemapLoaded = true;
        });
    }

    public static void processClaims(ServerLevel level) {
        ClaimStorage claimStorage = ClaimStorage.get(level);
        Map<UUID, Set<Claim>> claimMap = claimStorage.getClaims();
        claimMap.forEach((uuid, claims) -> {
            claims.forEach(BluemapIntegration::addClaimMarker);
        });
    }

    public static void addClaimMarker(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getWorld())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                MarkerSet markerSet = map.getMarkerSets().get(markerID);
                int[] dim = claim.getDimensions();
                ShapeMarker marker = ShapeMarker.builder()
                        .label(claimLabel(claim))
                        .depthTestEnabled(false)
                        .shape(Shape.createRect(dim[0], dim[2], dim[1], dim[3]), claim.getWorld().getSeaLevel())
                        .lineColor(new Color(lineColor(claim.isAdminClaim()), 0.8F))
                        .lineWidth(3)
                        .fillColor(new Color(fillColor(claim.isAdminClaim()), 0.2F))
                        .build();
                markerSet.put(claim.getClaimID().toString(), marker);
            }
        });
    }

    public static void removeMarker(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getWorld())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                MarkerSet markerSet = map.getMarkerSets().get(markerID);
                markerSet.remove(claim.getClaimID().toString());
            }
        });
    }

    public static void changeClaimName(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getWorld())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                MarkerSet markerSet = map.getMarkerSets().get(markerID);
                Marker marker = markerSet.get(claim.getClaimID().toString());
                marker.setLabel(claimLabel(claim));
            }
        });
    }

    public static void changeClaimOwner(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getWorld())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                MarkerSet markerSet = map.getMarkerSets().get(markerID);
                Marker marker = markerSet.get(claim.getClaimID().toString());
                marker.setLabel(claimLabel(claim));
            }
        });
    }

    private static int lineColor(boolean admin) {
        return admin ? 0xb50909 : 0xffa200;
    }

    private static int fillColor(boolean admin) {
        return admin ? 0xff0000 : 0xe0e01d;
    }

    private static String claimLabel(Claim claim) {
        String name = claim.getClaimName();
        if (claim.isAdminClaim()) {
            if (name == null || name.isEmpty()) {
                return "Admin Claim";
            } else {
                return name + " - " + "Admin Claim";
            }
        }
        Optional<GameProfile> prof = claim.getWorld().getServer().getProfileCache().get(claim.getOwner());
        if (name == null || name.isEmpty()) {
            return prof.map(GameProfile::getName).orElse("UNKNOWN") + "'s Claim";
        } else {
            return name + " - " + prof.map(GameProfile::getName).orElse("UNKNOWN") + "'s Claim";
        }
    }
}
