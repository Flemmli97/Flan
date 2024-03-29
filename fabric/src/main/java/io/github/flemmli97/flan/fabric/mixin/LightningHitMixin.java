package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(LightningBolt.class)
public abstract class LightningHitMixin {

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), require = 1)
    private List<Entity> affectedEntities(List<Entity> list) {
        list.removeIf(EntityInteractEvents::preventLightningConvert);
        return list;
    }
}
