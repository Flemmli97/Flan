package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class XpEntityMixin {

    @Inject(method = "onPlayerCollision", at = @At(value = "HEAD"), cancellable = true)
    private void collision(PlayerEntity player, CallbackInfo info) {
        if (EntityInteractEvents.xpAbsorb(player)) {
            info.cancel();
        }
    }

}
