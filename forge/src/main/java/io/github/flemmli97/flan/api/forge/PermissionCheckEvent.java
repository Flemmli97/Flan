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

    public PermissionCheckEvent(ServerPlayerEntity player, ClaimPermission permission, BlockPos pos) {
        this.player = player;
        this.permission = permission;
        this.pos = pos;
    }

    public ActionResult getActionResult() {
        return this.result;
    }

    public void setResult(ActionResult result) {
        this.result = result;
    }
}
