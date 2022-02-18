package io.github.flemmli97.flan.fabric.platform.integration.playerability;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.PlayerAbility;
import io.github.ladysnake.pal.PlayerAbilityEnableCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerAbilityEvents {

    public static void register() {
        PlayerAbilityEnableCallback.EVENT.register(PlayerAbilityEvents::checkAbility);
    }

    public static boolean checkAbility(Player player, PlayerAbility ability, AbilitySource abilitySource) {
        if (player.level.isClientSide)
            return true;
        BlockPos pos = player.blockPosition();
        ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract((ServerPlayer) player, PermissionRegistry.FLIGHT, pos, true);
    }
}
