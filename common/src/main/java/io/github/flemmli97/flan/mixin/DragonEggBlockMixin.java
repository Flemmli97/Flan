package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin {

    @Unique
    private Player flan$TempPlayer;

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> info) {
        this.flan$TempPlayer = player;
    }

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void onUseReturn(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> info) {
        this.flan$TempPlayer = null;
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo info) {
        this.flan$TempPlayer = player;
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackReturn(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo info) {
        this.flan$TempPlayer = null;
    }

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void onTeleport(BlockState state, Level level, BlockPos pos, CallbackInfo info) {
        if (this.flan$TempPlayer instanceof ServerPlayer player) {
            ResourceLocation perm = InteractionOverrideManager.getInstance().getBlockInteract((DragonEggBlock) (Object) this);
            if (perm == null)
                perm = BuiltinPermission.INTERACTBLOCK;
            if (!ClaimStorage.get(player.serverLevel()).canInteract(pos, 16, player, perm, true))
                info.cancel();
        }
    }
}
