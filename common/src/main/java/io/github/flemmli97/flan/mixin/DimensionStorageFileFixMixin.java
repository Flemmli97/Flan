package io.github.flemmli97.flan.mixin;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(DimensionStorageFileFix.class)
public abstract class DimensionStorageFileFixMixin extends FileFix {

    private DimensionStorageFileFixMixin(Schema schema) {
        super(schema);
    }

    @Inject(method = "makeFixer", at = @At(value = "HEAD"))
    private void addClaimStorageFixer(CallbackInfo info) {
        this.addFileFixOperation(FileFixOperations.groupMove(Map.of(
                        "data", "dimensions/minecraft/overworld",
                        "DIM-1/data", "dimensions/minecraft/the_nether",
                        "DIM1/data", "dimensions/minecraft/the_end"),
                List.of(FileFixOperations.moveSimple("claims"))));
    }

}