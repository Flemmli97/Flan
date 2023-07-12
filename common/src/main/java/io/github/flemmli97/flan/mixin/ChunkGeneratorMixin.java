package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @ModifyVariable(method = "tryGenerateStructure", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/StructureFeatureManager;setStartForFeature(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/feature/ConfiguredStructureFeature;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/FeatureAccess;)V"))
    private StructureStart onStructureGenerate(StructureStart origin, StructureSet.StructureSelectionEntry structureSelectionEntry, StructureFeatureManager structureManager) {
        WorldEvents.onStructureGen(origin, structureManager);
        return origin;
    }
}
