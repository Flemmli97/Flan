package io.github.flemmli97.flan.integration.playerability;

public class PlayerAbilityEvents {

    public static void register() {
        //PlayerAbilityEnableCallback.EVENT.register(PlayerAbilityEvents::checkAbility);
    }

    /*public static boolean checkAbility(Player player, PlayerAbility ability, AbilitySource abilitySource) {
        if (player.level.isClientSide)
            return true;
        BlockPos pos = player.blockPosition();
        ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract((ServerPlayer) player, PermissionRegistry.FLIGHT, pos, true);
    }*/
}
