package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherMixin {

    @Shadow
    private int destroyBlocksTick;

    @Inject(method = "customServerAiStep", at = @At(value = "HEAD"))
    private void preventClaimDmg(CallbackInfo info) {
        if (this.destroyBlocksTick > 0 && !EntityInteractEvents.witherCanDestroy((WitherBoss) (Object) this))
            this.destroyBlocksTick = -1;
    }
}
