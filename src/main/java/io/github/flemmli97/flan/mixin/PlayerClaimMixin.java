package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.player.IPlayerClaimImpl;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerClaimMixin implements IPlayerClaimImpl {

    @Unique
    private PlayerClaimData flanClaimData;

    @Unique
    private Claim flanCurrentClaim;

    @Shadow
    private MinecraftServer server;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        this.flanClaimData = new PlayerClaimData((ServerPlayerEntity) (Object) this);
    }

    /*@Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readData(NbtCompound tag, CallbackInfo info) {
        this.claimData.read(this.server);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeData(NbtCompound tag, CallbackInfo info) {
        this.claimData.save(this.server);
    }*/

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickData(CallbackInfo info) {
        this.flanClaimData.tick(this.flanCurrentClaim, claim -> this.flanCurrentClaim = claim);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void copyOld(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
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
