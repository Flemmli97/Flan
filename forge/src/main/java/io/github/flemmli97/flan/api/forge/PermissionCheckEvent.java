package io.github.flemmli97.flan.api.forge;

import io.github.flemmli97.flan.api.ClaimPermission;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public class PermissionCheckEvent extends Event {

    public final ServerPlayerEntity player;
    public final ClaimPermission permission;
    public final BlockPos pos;
    private ActionResult result = ActionResult.PASS;

    /**
     * Event for when permissions are checked
     *
     * @param player     The corresponding player. Can be null if the check is e.g. caused by tnt explosions
     * @param permission The permission to check
     * @param pos        The block pos where the action is occuring
     */
    public PermissionCheckEvent(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        this.player = player;
        this.permission = permission;
        this.pos = pos;
    }

    /**
     * @return ActionResult#PASS to do nothing. ActionResult#FAIL to prevent the action. Else to allow the action
     */
    public ActionResult getActionResult() {
        return this.result;
    }

    public void setResult(ActionResult result) {
        this.result = result;
    }
}
