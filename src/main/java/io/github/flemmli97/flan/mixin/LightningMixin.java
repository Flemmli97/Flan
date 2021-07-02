package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LightningEntity.class)
public class LightningMixin {

    @Inject(method = "spawnFire", at = @At(value = "HEAD"), cancellable = true)
    private void stopFire(int attempts, CallbackInfo info) {
        if (!WorldEvents.lightningFire((LightningEntity) (Object) this))
            info.cancel();
    }

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), require = 1, ordinal = 1)
    private List<Entity> affectedEntities(List<Entity> list) {
        list.removeIf(EntityInteractEvents::preventLightningConvert);
        return list;
    }
}
