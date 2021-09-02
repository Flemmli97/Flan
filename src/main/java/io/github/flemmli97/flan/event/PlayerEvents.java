package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEvents {

    public static void saveClaimData(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity)
            PlayerClaimData.get((ServerPlayerEntity) player).save(player.getServer());
    }

    public static void readClaimData(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity)
            PlayerClaimData.get((ServerPlayerEntity) player).read(player.getServer());
    }

    public static void onLogout(PlayerEntity player) {
        if (player.getServer() != null)
            LogoutTracker.getInstance(player.getServer()).track(player.getUuid());
    }
}
