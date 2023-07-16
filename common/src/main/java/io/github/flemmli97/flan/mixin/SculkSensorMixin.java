package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlock.class)
public abstract class SculkSensorMixin {

    @Inject(method = "activate", at = @At("HEAD"), cancellable = true)
    private void playerPermCheck(@Nullable Entity entity, Level level, BlockPos pos, BlockState state, int power, int frequency, CallbackInfo info) {
        ServerPlayer player = null;
        if (entity instanceof ServerPlayer p)
            player = p;
        else if (entity instanceof Projectile proj && proj.getOwner() instanceof ServerPlayer p)
            player = p;
        else if (entity instanceof ItemEntity item && item.getOwner() instanceof ServerPlayer p)
            player = p;
        if (player != null && !PlayerEvents.canSculkTrigger(pos, player))
            info.cancel();
    }
}
