package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class GlobalClaim implements IPermissionContainer {

    private final ServerLevel world;

    public GlobalClaim(ServerLevel world) {
        this.world = world;
    }

    @Override
    public boolean canInteract(ServerPlayer player, ClaimPermission perm, BlockPos pos, boolean message) {
        Config.GlobalType global = ConfigHandler.config.getGlobal(this.world, perm);
        if (global != Config.GlobalType.NONE && (player == null || !PlayerClaimData.get(player).isAdminIgnoreClaim())) {
            if (global.getValue())
                return true;
            if (message)
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, ChatFormatting.DARK_RED), true);
            return false;
        }
        return perm != PermissionRegistry.MOBSPAWN && perm != PermissionRegistry.ANIMALSPAWN;
    }
}
