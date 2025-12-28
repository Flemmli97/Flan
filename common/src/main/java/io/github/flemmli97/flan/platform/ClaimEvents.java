package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public interface ClaimEvents {

    ClaimEvents INSTANCE = Flan.getPlatformInstance(ClaimEvents.class,
            "io.github.flemmli97.flan.fabric.platform.ClaimEventsImpl",
            "io.github.flemmli97.flan.neoforge.platform.ClaimEventsImpl");

    InteractionResult claimCheck(ServerPlayer player, Identifier permission, BlockPos pos);

    void borderCross(ServerPlayer player, @Nullable Claim enter, @Nullable Claim exit);
}
