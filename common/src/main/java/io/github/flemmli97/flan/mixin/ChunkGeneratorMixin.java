package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @ModifyVariable(method = "tryGenerateStructure", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/StructureManager;setStartForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/StructureAccess;)V"))
    private StructureStart onStructureGenerate(StructureStart origin, StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager) {
        WorldEvents.onStructureGen(origin, structureManager);
        return origin;
    }
}
