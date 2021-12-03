package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerEvents {

    public static void saveClaimData(Player player) {
        if (player instanceof ServerPlayer)
            PlayerClaimData.get((ServerPlayer) player).save(player.getServer());
    }

    public static void readClaimData(Player player) {
        if (player instanceof ServerPlayer)
            PlayerClaimData.get((ServerPlayer) player).read(player.getServer());
    }

    public static void onLogout(Player player) {
        if (player.getServer() != null)
            LogoutTracker.getInstance(player.getServer()).track(player.getUUID());
    }
}
