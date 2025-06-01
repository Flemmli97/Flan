package io.github.flemmli97.flan.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.BonemealableFeaturePlacerBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BonemealableFeaturePlacerBlock.class)
public interface BonemealableBlockAccess {

    @Accessor("feature")
    ResourceKey<ConfiguredFeature<?, ?>> getFeature();

}
