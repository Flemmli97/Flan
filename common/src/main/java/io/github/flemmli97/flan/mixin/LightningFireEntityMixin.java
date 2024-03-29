package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningFireEntityMixin {

    @Inject(method = "spawnFire", at = @At(value = "HEAD"), cancellable = true)
    private void stopFire(int attempts, CallbackInfo info) {
        if (!WorldEvents.lightningFire((LightningBolt) (Object) this))
            info.cancel();
    }
}
