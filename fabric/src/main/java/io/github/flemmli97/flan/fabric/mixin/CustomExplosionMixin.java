package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "ladysnake.blast.common.world.CustomExplosion")
public abstract class CustomExplosionMixin {
    @Shadow
    @Final
    private Level world;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "explode", at = @At("TAIL"))
    private void explosionHook(CallbackInfo info) {
        if (this.world instanceof ServerLevel world) {
            WorldEvents.modifyExplosion((Explosion) (Object) this, world);
        }
    }
}
