package io.github.flemmli97.flan.api.fabric;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class PermissionCheckEvent {

    public interface PermissionCheck {

        /**
         * Callback for when permissions are checked
         *
         * @param player     The corresponding player. Can be null if the check is e.g. caused by tnt explosions
         * @param permission The permission to check
         * @param pos        The block pos where the action is occuring
         * @return ActionResult#PASS to do nothing. ActionResult#FAIL to prevent the action. Else to allow the action
         */
        InteractionResult check(ServerPlayer player, ClaimPermission permission, BlockPos pos);

    }

    public static Event<PermissionCheck> CHECK = EventFactory.createArrayBacked(PermissionCheck.class,
            (listeners) -> (player, permission, pos) -> {
                for (PermissionCheck event : listeners) {
                    InteractionResult result = event.check(player, permission, pos);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            }
    );
}
