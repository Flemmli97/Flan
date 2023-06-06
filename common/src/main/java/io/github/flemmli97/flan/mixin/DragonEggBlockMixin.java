package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
    private Player flanTempPlayer;

    @Inject(method = "use", at = @At("HEAD"))
    private void onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> info) {
        this.flanTempPlayer = player;
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void onUseReturn(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> info) {
        this.flanTempPlayer = null;
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo info) {
        this.flanTempPlayer = player;
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void onAttackReturn(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo info) {
        this.flanTempPlayer = null;
    }

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void onTeleport(BlockState state, Level level, BlockPos pos, CallbackInfo info) {
        if (this.flanTempPlayer instanceof ServerPlayer player) {
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock((DragonEggBlock) (Object) this);
            if (perm == null)
                perm = PermissionRegistry.INTERACTBLOCK;
            if (!ClaimStorage.get(player.getLevel()).canInteract(pos, 16, player, perm, true))
                info.cancel();
        }
    }
}
