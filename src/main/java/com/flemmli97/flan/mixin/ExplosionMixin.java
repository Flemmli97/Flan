package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.WorldEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    private List<BlockPos> affectedBlocks;
    @Shadow
    private World world;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "RETURN"))
    public void collision(CallbackInfo info) {
        WorldEvents.modifyExplosion(this.affectedBlocks, this.world);
    }
}
