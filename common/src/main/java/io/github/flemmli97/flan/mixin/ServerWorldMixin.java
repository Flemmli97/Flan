package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.IClaimStorage;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements IClaimStorage {
    @Unique
    private ClaimStorage flanClaimData;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        ServerLevel world = ((ServerLevel) (Object) this);
        this.flanClaimData = new ClaimStorage(world.getServer(), world);
    }

    @Inject(method = "saveLevelData", at = @At("RETURN"))
    private void saveClaimData(CallbackInfo info) {
        ServerLevel world = ((ServerLevel) (Object) this);
        this.flanClaimData.save(world.getServer(), world.dimension());
    }

    @Override
    public ClaimStorage get() {
        return this.flanClaimData;
    }
}
