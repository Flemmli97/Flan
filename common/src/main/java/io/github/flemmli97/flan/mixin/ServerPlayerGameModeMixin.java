package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.BlockBreakAttemptHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements BlockBreakAttemptHandler {

    @Final
    @Shadow
    protected ServerPlayer player;
    @Unique
    private BlockPos flan_blockBreakFail;
    @Unique
    private boolean flan_was_insta_break;

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(CallbackInfo info) {
        if (this.flan_blockBreakFail != null) {
            // All hail 1.20.5 with mining speed attribute!
            if (!this.flan_was_insta_break) {
                this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                        .addOrUpdateTransientModifier(new AttributeModifier(PlayerClaimData.MINING_SPEED_MOD, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void onBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo info) {
        if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK && pos.equals(this.flan_blockBreakFail)) {
            this.flan_blockBreakFail = null;
            this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                    .removeModifier(PlayerClaimData.MINING_SPEED_MOD);
            info.cancel();
        }
    }

    @Override
    public void flan$setBlockBreakAttemptFail(BlockPos pos, boolean instaBreak) {
        this.flan_was_insta_break = instaBreak;
        this.flan_blockBreakFail = pos;
        if (this.flan_blockBreakFail == null) {
            this.player.getAttribute(Attributes.BLOCK_BREAK_SPEED)
                    .removeModifier(PlayerClaimData.MINING_SPEED_MOD);
        }
    }

    @Override
    public BlockPos flan$failedPos() {
        return this.flan_blockBreakFail;
    }

    @Override
    public boolean flan$wasInstabreak() {
        return this.flan_was_insta_break;
    }
}
