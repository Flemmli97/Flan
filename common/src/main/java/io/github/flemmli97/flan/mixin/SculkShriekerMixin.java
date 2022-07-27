package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkShriekerBlockEntity.class)
public abstract class SculkShriekerMixin extends BlockEntity {

    private SculkShriekerMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "tryShriek", at = @At("HEAD"), cancellable = true)
    private void playerCheck(ServerLevel serverLevel, @Nullable ServerPlayer serverPlayer, CallbackInfo info) {
        if (serverPlayer != null && !PlayerEvents.canWardenSpawnTrigger(this.getBlockPos(), serverPlayer))
            info.cancel();
    }
}
