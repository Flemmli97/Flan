package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherEntity.class)
public abstract class WitherMixin {

    @Shadow
    private int field_7082;

    @Inject(method = "mobTick", at = @At(value = "HEAD"))
    public void preventClaimDmg(CallbackInfo info) {
        if (!EntityInteractEvents.witherCanDestroy((WitherEntity) (Object) this))
            this.field_7082 = -1;
    }
}
