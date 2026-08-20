package io.github.flemmli97.flan.platform.integration.maps;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.claim.ClaimStorage;
import journeymap.api.v2.client.display.Context;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.server.IServerAPI;
import journeymap.api.v2.server.IServerPlugin;
import journeymap.api.v2.server.overlay.OverlayPoints;
import journeymap.api.v2.server.overlay.OverlayPolygon;
import journeymap.api.v2.server.overlay.OverlayShapeProps;
import journeymap.api.v2.server.overlay.ServerPolygon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JourneyMapPlugin(apiVersion = "2.0.0")
public class JourneymapIntegration implements IServerPlugin {

    private static IServerAPI API;

    @Override
    public String getModId() {
        return Flan.MODID;
    }

    @Override
    public void initialize(IServerAPI jmServerApi) {
        API = jmServerApi;
        MapCalls.journeymapLoaded = true;
    }

    public static void onJoinWorld(ServerPlayer player) {
        ServerLevel level = player.level();
        ClaimStorage claimStorage = ClaimStorage.get(level);
        Map<UUID, Set<Claim>> claimMap = claimStorage.getClaims();
        claimMap.forEach((uuid, claims) -> claims.forEach((claim) -> showClaimTo(claim, player)));
    }

    public static void addClaimMarker(Claim claim) {
        claim.getLevel().players().forEach(player -> showClaimTo(claim, player));
    }

    private static void showClaimTo(Claim claim, ServerPlayer player) {
        API.getOverlayApi().show(player, Flan.MODID, createClaimPolygon(claim));
    }

    public static void removeMarker(Claim claim) {
        claim.getLevel().players().forEach(player -> API.getOverlayApi().remove(player, Flan.MODID, claim.getClaimID().toString()));
    }

    public static void changeClaimName(Claim claim) {
        claim.getLevel().players().forEach(player -> showClaimTo(claim, player));
    }

    public static void changeClaimOwner(Claim claim) {
        claim.getLevel().players().forEach(player -> showClaimTo(claim, player));
    }

    public static void updateStyles(ClaimStorage storage) {
        storage.getLevel().players().forEach(player ->
                storage.getClaims().forEach((id, claims) ->
                        claims.forEach(claim -> showClaimTo(claim, player))));
    }

    private static ServerPolygon createClaimPolygon(Claim claim) {
        ClaimBox dimensions = claim.getDimensions();
        return new ServerPolygon(claim.getClaimID().toString(), claim.getLevel().dimension(),
                List.of(new OverlayPolygon(new OverlayPoints(List.of(
                        BlockPos.asLong(dimensions.minX(), dimensions.minY(), dimensions.minZ()),
                        BlockPos.asLong(dimensions.maxX() + 1, dimensions.minY(), dimensions.minZ()),
                        BlockPos.asLong(dimensions.maxX() + 1, dimensions.minY(), dimensions.maxZ() + 1),
                        BlockPos.asLong(dimensions.minX(), dimensions.minY(), dimensions.maxZ() + 1)
                )), null)), new OverlayShapeProps(
                MapUtils.fillColor(claim.isAdminClaim()),
                0.2F, MapUtils.lineColor(claim.isAdminClaim()), 3, 0.8F, 1000, Integer.MIN_VALUE, Integer.MAX_VALUE,
                Set.of(Context.UI.all()), Set.of(Context.MapType.all()), null, MapUtils.claimLabel(claim)
        ));
    }
}
