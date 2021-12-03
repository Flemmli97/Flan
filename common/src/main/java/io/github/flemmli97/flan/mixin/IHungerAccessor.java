package io.github.flemmli97.flan.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface IHungerAccessor {

    @Accessor("saturationLevel")
    void setSaturation(float saturation);
}
