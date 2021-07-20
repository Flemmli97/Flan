package io.github.flemmli97.flan.integration.playerability;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.PlayerAbility;
import io.github.ladysnake.pal.PlayerAbilityEnableCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PlayerAbilityEvents {

    public static void register() {
        PlayerAbilityEnableCallback.EVENT.register(PlayerAbilityEvents::checkAbility);
    }

    public static boolean checkAbility(PlayerEntity player, PlayerAbility ability, AbilitySource abilitySource) {
        if (player.world.isClient)
            return true;
        BlockPos pos = player.getBlockPos();
        ClaimStorage storage = ClaimStorage.get((ServerWorld) player.world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract((ServerPlayerEntity) player, PermissionRegistry.FLIGHT, pos, true);
    }
}
