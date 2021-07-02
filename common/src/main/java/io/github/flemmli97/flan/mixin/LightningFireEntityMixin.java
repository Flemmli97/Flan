package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
public class LightningFireEntityMixin {

    @Inject(method = "spawnFire", at = @At(value = "HEAD"), cancellable = true)
    private void stopFire(int attempts, CallbackInfo info) {
        if (!WorldEvents.lightningFire((LightningEntity) (Object) this))
            info.cancel();
    }
}
