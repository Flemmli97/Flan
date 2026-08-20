package io.github.flemmli97.flan.platform.integration.maps;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class BluemapIntegration {

    private static final String MARKER_3D = "flan.claims", MARKER_2D = "flan.claims.2d";

    private static boolean lastEnabled;

    public static void reg(MinecraftServer server) {
        if (ConfigHandler.CONFIG.bluemapIntegration) {
            BlueMapAPI.onEnable(api -> {
                initMaps(server, api);
                WebmapCalls.bluemapLoaded = true;
                lastEnabled = true;
            });
        }
    }

    public static void updateBluemapState(MinecraftServer server) {
        if (!WebmapCalls.bluemapLoaded)
            return;
        if (lastEnabled == ConfigHandler.CONFIG.bluemapIntegration)
            return;
        BlueMapAPI.getInstance().ifPresent(api -> {
            if (ConfigHandler.CONFIG.bluemapIntegration) {
                initMaps(server, api);
            } else {
                for (ServerLevel level : server.getAllLevels()) {
                    api.getWorld(level).ifPresent(world -> world.getMaps().forEach(map -> {
                        map.getMarkerSets().remove(MARKER_3D);
                        map.getMarkerSets().remove(MARKER_2D);
                    }));
                }
            }
        });
        lastEnabled = ConfigHandler.CONFIG.bluemapIntegration;
    }

    private static void initMaps(MinecraftServer server, BlueMapAPI api) {
        for (ServerLevel level : server.getAllLevels()) {
            api.getWorld(level).ifPresent(world -> world.getMaps().forEach(map -> {
                MarkerSet markerSet = MarkerSet.builder().label("Claims (3D)")
                        .defaultHidden(true).build();
                MarkerSet markerSet2 = MarkerSet.builder().label("Claims (2D)").build();
                map.getMarkerSets().put(MARKER_3D, markerSet);
                map.getMarkerSets().put(MARKER_2D, markerSet2);
            }));
            processClaims(level);
        }
    }

    public static void processClaims(ServerLevel level) {
        ClaimStorage claimStorage = ClaimStorage.get(level);
        Map<UUID, Set<Claim>> claimMap = claimStorage.getClaims();
        claimMap.forEach((uuid, claims) -> claims.forEach(BluemapIntegration::addClaimMarker));
    }

    public static void addClaimMarker(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getLevel())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                MarkerSet markerSet = map.getMarkerSets().get(MARKER_3D);
                ClaimBox dim = claim.getDimensions();
                ExtrudeMarker marker = ExtrudeMarker.builder()
                        .label(claimLabel(claim))
                        .depthTestEnabled(false)
                        // Seems you need the offset
                        .shape(Shape.createRect(dim.minX(), dim.minZ(), dim.maxX() + 1, dim.maxZ() + 1), dim.minY(), dim.maxY())
                        .lineColor(new Color(lineColor(claim.isAdminClaim()), 0.8F))
                        .lineWidth(3)
                        .fillColor(new Color(fillColor(claim.isAdminClaim()), 0.2F))
                        .build();
                markerSet.put(claim.getClaimID().toString(), marker);
                MarkerSet markerSet2 = map.getMarkerSets().get(MARKER_2D);
                ShapeMarker shapeMarker = ShapeMarker.builder()
                        .label(claimLabel(claim))
                        .depthTestEnabled(false)
                        // Seems you need the offset
                        .shape(Shape.createRect(dim.minX(), dim.minZ(), dim.maxX() + 1, dim.maxZ() + 1), dim.minY())
                        .lineColor(new Color(lineColor(claim.isAdminClaim()), 0.8F))
                        .lineWidth(3)
                        .fillColor(new Color(fillColor(claim.isAdminClaim()), 0.2F))
                        .build();
                markerSet2.put(claim.getClaimID().toString(), shapeMarker);
            }
        });
    }

    public static void removeMarker(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getLevel())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                updateMarkers(map, markerSet -> markerSet.remove(claim.getClaimID().toString()));
            }
        });
    }

    public static void changeClaimName(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getLevel())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                updateMarkers(map, markerSet -> {
                    Marker marker = markerSet.get(claim.getClaimID().toString());
                    marker.setLabel(claimLabel(claim));
                });
            }
        });
    }

    public static void changeClaimOwner(Claim claim) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(claim.getLevel())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                updateMarkers(map, markerSet -> {
                    Marker marker = markerSet.get(claim.getClaimID().toString());
                    marker.setLabel(claimLabel(claim));
                });
            }
        });
    }

    private static int lineColor(boolean admin) {
        return admin ? ConfigHandler.CONFIG.adminMapBorderColor : ConfigHandler.CONFIG.mapBorderColor;
    }

    private static int fillColor(boolean admin) {
        return admin ? ConfigHandler.CONFIG.adminMapFillColor : ConfigHandler.CONFIG.mapFillColor;
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
        Optional<String> prof = ClaimUtils.fetchUsername(claim.getOwner(), claim.getLevel().getServer());
        if (name == null || name.isEmpty()) {
            return prof.orElse("UNKNOWN") + "'s Claim";
        } else {
            return name + " - " + prof.orElse("UNKNOWN") + "'s Claim";
        }
    }

    private static void updateMarkers(BlueMapMap map, Consumer<MarkerSet> cons) {
        cons.accept(map.getMarkerSets().get(MARKER_3D));
        cons.accept(map.getMarkerSets().get(MARKER_2D));
    }

    public static void updateStyles(ClaimStorage storage) {
        BlueMapAPI.getInstance().flatMap(api -> api.getWorld(storage.getLevel())).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                updateMarkers(map, markerSet -> {
                    markerSet.getMarkers().forEach((id, marker) -> {
                        Claim claim = storage.getFromUUID(UUID.fromString(id));
                        if (claim != null && marker instanceof ShapeMarker shapeMarker) {
                            shapeMarker.setLineColor(new Color(lineColor(claim.isAdminClaim()), 0.8F));
                            shapeMarker.setFillColor(new Color(fillColor(claim.isAdminClaim()), 0.2F));
                        }
                    });
                });
            }
        });
    }
}
