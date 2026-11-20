package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopperGolem.class)
public class CopperGolemMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickData(CallbackInfo info) {
        EntityInteractEvents.handleEntityPassBorder((Entity) (Object) this);
    }
}
