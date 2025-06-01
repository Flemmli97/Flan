package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @ModifyVariable(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;hurtEntities()V"))
    private List<BlockPos> explosionHook(List<BlockPos> original) {
        WorldEvents.modifyExplosion(original, this.level);
        return original;
    }
}
