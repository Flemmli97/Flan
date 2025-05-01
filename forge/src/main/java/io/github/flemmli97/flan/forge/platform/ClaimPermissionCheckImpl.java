package io.github.flemmli97.flan.forge.platform;

import io.github.flemmli97.flan.api.forge.ClaimBorderCrossEvent;
import io.github.flemmli97.flan.api.forge.PermissionCheckEvent;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.platform.ClaimEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public class ClaimPermissionCheckImpl implements ClaimEvents {

    @Override
    public InteractionResult claimCheck(ServerPlayer player, ResourceLocation permission, BlockPos pos) {
        PermissionCheckEvent event = new PermissionCheckEvent(player, permission, pos);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getActionResult();
    }

    @Override
    public void borderCross(ServerPlayer player, @Nullable Claim enter, @Nullable Claim exit) {
        MinecraftForge.EVENT_BUS.post(new ClaimBorderCrossEvent(player, enter, exit));
    }
}
