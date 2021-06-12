package io.github.flemmli97.flan.api.fabric;

import io.github.flemmli97.flan.api.ClaimPermission;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

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
        ActionResult check(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos);

    }

    public static Event<PermissionCheck> CHECK = EventFactory.createArrayBacked(PermissionCheck.class,
            (listeners) -> (player, permission, pos) -> {
                for (PermissionCheck event : listeners) {
                    ActionResult result = event.check(player, permission, pos);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );
}
