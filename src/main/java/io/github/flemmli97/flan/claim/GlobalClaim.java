package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public record GlobalClaim(ServerWorld world) implements IPermissionContainer {

    @Override
    public boolean canInteract(ServerPlayerEntity player, ClaimPermission perm, BlockPos pos, boolean message) {
        Config.GlobalType global = ConfigHandler.config.getGlobal(this.world, perm);
        if (global != Config.GlobalType.NONE && (player == null || !PlayerClaimData.get(player).isAdminIgnoreClaim())) {
            if (global.getValue())
                return true;
            if (message)
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
            return false;
        }
        return perm != PermissionRegistry.MOBSPAWN && perm != PermissionRegistry.ANIMALSPAWN;
    }
}
