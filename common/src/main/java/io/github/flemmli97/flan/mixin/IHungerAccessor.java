package io.github.flemmli97.flan.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HungerManager.class)
public interface IHungerAccessor {

    @Accessor("foodSaturationLevel")
    void setSaturation(float saturation);
}
