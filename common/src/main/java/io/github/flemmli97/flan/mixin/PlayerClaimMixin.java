package io.github.flemmli97.flan.mixin;

import com.mojang.authlib.GameProfile;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.platform.ClaimEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IPlayerClaimImpl;
import io.github.flemmli97.flan.utils.VanillaFlightStateTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerClaimMixin extends Player implements IPlayerClaimImpl, VanillaFlightStateTracker {

    @Shadow
    protected abstract void nextContainerCounter();

    @Unique
    private PlayerClaimData flan$ClaimData;

    @Unique
    private Claim flan$CurrentClaim;

    @Unique
    private boolean flan$claimFlight, flan$togglingFlight, flan$otherFlightState;

    private PlayerClaimMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        this.flan$ClaimData = new PlayerClaimData((ServerPlayer) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickData(CallbackInfo info) {
        Claim newClaim = EntityInteractEvents.currentClaimTick((ServerPlayer) (Object) this, this.flan$CurrentClaim);
        if (this.flan$CurrentClaim != newClaim)
            ClaimEvents.INSTANCE.borderCross((ServerPlayer) (Object) this, newClaim, this.flan$CurrentClaim);
        this.flan$CurrentClaim = newClaim;
        this.flan$ClaimData.tick(this.flan$CurrentClaim);
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void copyOld(ServerPlayer oldPlayer, boolean alive, CallbackInfo info) {
        this.flan$ClaimData.clone(PlayerClaimData.get(oldPlayer));
    }

    @Inject(method = "onUpdateAbilities", at = @At("HEAD"))
    private void abilityUpdate(CallbackInfo info) {
        // Track changes
        if (!this.flan$togglingFlight) {
            this.flan$otherFlightState = this.getAbilities().mayfly;
        }
    }

    @Override
    public PlayerClaimData get() {
        return this.flan$ClaimData;
    }

    @Override
    public Claim getCurrentClaim() {
        return this.flan$CurrentClaim;
    }

    @Override
    public void toggleFlight(boolean mayFly) {
        boolean changed = false;
        if (this.flan$claimFlight != mayFly) {
            if (mayFly) {
                this.flan$otherFlightState = this.getAbilities().mayfly;
                this.getAbilities().mayfly = true;
            } else {
                this.getAbilities().mayfly = this.flan$otherFlightState;
            }
            changed = true;
        }
        this.flan$claimFlight = mayFly;
        if (this.flan$claimFlight && !this.getAbilities().mayfly) {
            this.getAbilities().mayfly = true;
            changed = true;
        }
        if (!changed)
            return;
        if (!this.getAbilities().mayfly && this.getAbilities().flying)
            this.getAbilities().flying = false;

        this.flan$togglingFlight = true;
        this.onUpdateAbilities();
        this.flan$togglingFlight = false;
    }
}
