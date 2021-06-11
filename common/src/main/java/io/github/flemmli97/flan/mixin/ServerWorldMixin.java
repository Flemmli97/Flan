package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.IClaimStorage;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements IClaimStorage {
    @Unique
    private ClaimStorage claimData;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        ServerWorld world = ((ServerWorld) (Object) this);
        this.claimData = new ClaimStorage(world.getServer(), world);
    }

    @Inject(method = "saveLevel()V", at = @At("RETURN"))
    private void saveClaimData(CallbackInfo info) {
        ServerWorld world = ((ServerWorld) (Object) this);
        this.claimData.save(world.getServer(), world.getRegistryKey());
    }

    @Override
    public ClaimStorage get() {
        return this.claimData;
    }
}
