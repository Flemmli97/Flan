package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.animal.SnowGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin {

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getGameRules()Lnet/minecraft/world/level/GameRules;"), cancellable = true)
    private void checkSnow(CallbackInfo info) {
        if (!EntityInteractEvents.canSnowGolemInteract((SnowGolem) (Object) this)) {
            info.cancel();
        }
    }
}
