package io.github.flemmli97.flan.neoforge.platform;

import io.github.flemmli97.flan.api.neoforge.ClaimBorderCrossEvent;
import io.github.flemmli97.flan.api.neoforge.PermissionCheckEvent;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.platform.ClaimEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class ClaimEventsImpl implements ClaimEvents {

    @Override
    public InteractionResult claimCheck(ServerPlayer player, Identifier permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        NeoForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }

    @Override
    public void borderCross(ServerPlayer player, @Nullable Claim enter, @Nullable Claim exit) {
        NeoForge.EVENT_BUS.post(new ClaimBorderCrossEvent(player, enter, exit));
    }
}
