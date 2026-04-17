package io.github.flemmli97.flan.mixin;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Migrates claim storage from old structure to post-26.1 structure. Player claim storage is already handled by vanilla logic.
 */
@Mixin(DimensionStorageFileFix.class)
public abstract class ClaimStorageMigrationMixin extends FileFix {

	public ClaimStorageMigrationMixin(Schema schema) {
		super(schema);
	}

	@Inject(method = "makeFixer", at = @At(value = "HEAD"))
	private void addClaimStorageFixer(CallbackInfo info) {
		this.addFileFixOperation(FileFixOperations.move("data/claims", "dimensions/minecraft/overworld/data/claims"));
		this.addFileFixOperation(FileFixOperations.move("DIM-1/data/claims", "dimensions/minecraft/the_nether/data/claims"));
		this.addFileFixOperation(FileFixOperations.move("DIM1/data/claims", "dimensions/minecraft/the_end/data/claims"));
	}

}
