package io.github.flemmli97.flan.fabric.platform.integration.playerability;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import io.github.ladysnake.pal.PlayerAbilityEnableCallback;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerAbilityEvents {

    public static final AbilitySource SOURCE = Pal.getAbilitySource(Flan.CLAIM_FLIGHT_ID);

    public static void register() {
        PlayerAbilityEnableCallback.EVENT.register(PlayerAbilityEvents::checkAbility);
    }

    public static boolean checkAbility(Player player, PlayerAbility ability, AbilitySource abilitySource) {
        if (player.level().isClientSide() || ability != VanillaAbilities.ALLOW_FLYING)
            return true;
        BlockPos pos = player.blockPosition();
        ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level());
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract((ServerPlayer) player, BuiltinPermission.ALLOW_FLIGHT, pos, true);
    }

    public static void toggleCreativeFlight(ServerPlayer player, boolean flag) {
        if (flag && !SOURCE.grants(player, VanillaAbilities.ALLOW_FLYING)) {
            Pal.grantAbility(player, VanillaAbilities.ALLOW_FLYING, SOURCE);
        } else if (!flag && SOURCE.grants(player, VanillaAbilities.ALLOW_FLYING)) {
            Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, SOURCE);
        }
    }
}
