package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.utils.BlockBreakAttemptHandler;
import io.github.flemmli97.flan.utils.TemporaryMobEffectWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin implements BlockBreakAttemptHandler {

    @Shadow
    private ServerPlayer player;
    @Unique
    private BlockPos flan_blockBreakFail;
    @Unique
    private MobEffectInstance flan_mining_fatigue_old;
    @Unique
    private boolean flan_was_insta_break;

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTick(CallbackInfo info) {
        if (this.flan_blockBreakFail != null) {
            // Use mining fatigue for blocking block destroy progress
            if (!this.flan_was_insta_break) {
                this.player.addEffect(new TemporaryMobEffectWrapper(MobEffects.DIG_SLOWDOWN, 20, -1, this.flan_mining_fatigue_old));
            }
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void onBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo info) {
        if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK && pos.equals(this.flan_blockBreakFail)) {
            this.flan_blockBreakFail = null;
            this.flan_mining_fatigue_old = null;
            MobEffectInstance current = this.player.getEffect(MobEffects.DIG_SLOWDOWN);
            // Reapply mining fatigue if player had it previously
            if (current instanceof TemporaryMobEffectWrapper temp) {
                this.player.removeEffect(MobEffects.DIG_SLOWDOWN);
                if (temp.getWrapped() != null)
                    this.player.addEffect(temp.getWrapped());
            }
            info.cancel();
        }
    }

    @Override
    public void setBlockBreakAttemptFail(BlockPos pos, boolean instaBreak) {
        this.flan_was_insta_break = instaBreak;
        if (pos == null) {
            this.flan_blockBreakFail = null;
            this.flan_mining_fatigue_old = null;
            MobEffectInstance current = this.player.getEffect(MobEffects.DIG_SLOWDOWN);
            // Reapply mining fatigue if player had it previously
            if (current instanceof TemporaryMobEffectWrapper temp) {
                this.player.removeEffect(MobEffects.DIG_SLOWDOWN);
                if (temp.getWrapped() != null)
                    this.player.addEffect(temp.getWrapped());
            }
            return;
        }
        this.flan_blockBreakFail = pos;
        this.flan_mining_fatigue_old = this.player.getEffect(MobEffects.DIG_SLOWDOWN);
        this.player.removeEffect(MobEffects.DIG_SLOWDOWN);
    }
}
