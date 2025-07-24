package io.github.flemmli97.flan.fabric.integration;

import crystalspider.harvestwithease.api.events.HarvestWithEaseEvents;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.server.level.ServerPlayer;

public class HarvestWithEase {

    public static void init() {
        HarvestWithEaseEvents.HARVEST_CHECK.register((level, blockState, blockPos, player, interactionHand, harvestCheckEvent) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                ClaimStorage storage = ClaimStorage.get(serverPlayer.level());
                if (!storage.getForPermissionCheck(blockPos)
                        .canInteract(serverPlayer, BuiltinPermission.BREAK, blockPos)) {
                    harvestCheckEvent.setCanceled(true);
                    return false;
                }
            }
            return true;
        });
    }
}
