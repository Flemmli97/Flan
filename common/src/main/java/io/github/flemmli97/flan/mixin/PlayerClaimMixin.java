package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IPlayerClaimImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerClaimMixin implements IPlayerClaimImpl {

    @Unique
    private PlayerClaimData flanClaimData;

    @Unique
    private Claim flanCurrentClaim;

    @Shadow
    private MinecraftServer server;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        this.flanClaimData = new PlayerClaimData((ServerPlayer) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickData(CallbackInfo info) {
        this.flanClaimData.tick(this.flanCurrentClaim, claim -> this.flanCurrentClaim = claim);
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void copyOld(ServerPlayer oldPlayer, boolean alive, CallbackInfo info) {
        this.flanClaimData.clone(PlayerClaimData.get(oldPlayer));
    }

    @Override
    public PlayerClaimData get() {
        return this.flanClaimData;
    }

    @Override
    public Claim getCurrentClaim() {
        return this.flanCurrentClaim;
    }
}
