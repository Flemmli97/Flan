package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record GlobalClaim(ServerLevel level) implements IPermissionContainer {

    @Override
    public boolean canInteract(ServerPlayer player, ResourceLocation perm, BlockPos pos, boolean message) {
        ClaimPermission permission = PermissionManager.getInstance().get(perm);
        if (permission == null)
            return false;
        message = message && player.getClass().equals(ServerPlayer.class); //dont send messages to fake players
        Config.GlobalType global = ConfigHandler.CONFIG.getGlobal(this.level, perm);
        if (global != Config.GlobalType.NONE && (player == null || !this.isAdmin(player, permission))) {
            if (global.getValue())
                return true;
            if (message)
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
            return false;
        }
        return permission.globalVal;
    }

    private boolean isAdmin(ServerPlayer player, ClaimPermission permission) {
        if (permission.requireExplicitSet)
            return false;
        return PlayerClaimData.get(player).isAdminIgnoreClaim();
    }
}
