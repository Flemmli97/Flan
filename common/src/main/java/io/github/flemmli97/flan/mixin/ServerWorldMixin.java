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
    private ClaimStorage flan$ClaimData;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        ServerLevel level = ((ServerLevel) (Object) this);
        this.flan$ClaimData = new ClaimStorage(level.getServer(), level);
    }

    @Inject(method = "saveLevelData", at = @At("RETURN"))
    private void saveClaimData(CallbackInfo info) {
        ServerLevel level = ((ServerLevel) (Object) this);
        this.flan$ClaimData.save(level.getServer(), level.dimension());
    }

    @Override
    public ClaimStorage flan$get() {
        return this.flan$ClaimData;
    }
}
