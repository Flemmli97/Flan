package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record GlobalClaim(ServerLevel world) implements IPermissionContainer {

    @Override
    public boolean canInteract(ServerPlayer player, ResourceLocation perm, BlockPos pos, boolean message) {
        message = message && player.getClass().equals(ServerPlayer.class); //dont send messages to fake players
        Config.GlobalType global = ConfigHandler.CONFIG.getGlobal(this.world, perm);
        if (global != Config.GlobalType.NONE && (player == null || !PlayerClaimData.get(player).isAdminIgnoreClaim())) {
            if (global.getValue())
                return true;
            if (message)
                player.displayClientMessage(PermHelper.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
            return false;
        }
        return !perm.equals(BuiltinPermission.MOBSPAWN) && !perm.equals(BuiltinPermission.ANIMALSPAWN);
    }
}
