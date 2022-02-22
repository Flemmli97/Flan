package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExperienceOrb.class, priority = 100)
public abstract class XpEntityMixin {

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void collision(Player player, CallbackInfo info) {
        if (EntityInteractEvents.xpAbsorb(player)) {
            info.cancel();
        }
    }

}
