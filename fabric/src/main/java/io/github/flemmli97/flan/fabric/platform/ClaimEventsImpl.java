package io.github.flemmli97.flan.fabric.platform;

import io.github.flemmli97.flan.api.fabric.ClaimBorderCrossEvent;
import io.github.flemmli97.flan.api.fabric.PermissionCheckEvent;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.platform.ClaimEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class ClaimEventsImpl implements ClaimEvents {

    @Override
    public InteractionResult claimCheck(ServerPlayer player, Identifier permission, BlockPos pos) {
        return PermissionCheckEvent.CHECK.invoker().check(player, permission, pos);
    }

    @Override
    public void borderCross(ServerPlayer player, Claim enter, Claim exit) {
        ClaimBorderCrossEvent.EVENT.invoker().borderCross(player, enter, exit);
    }
}
