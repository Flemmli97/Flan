package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.api.fabric.ItemUseBlockFlags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ItemUseBlockFlags {

    @Unique
    private boolean stopInteractBlock;
    @Unique
    private boolean stopInteractItemBlock;

    @ModifyVariable(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), ordinal = 1)
    private boolean stopBlockUse(boolean orig) {
        if (this.stopInteractBlock)
            return true;
        return orig;
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"), cancellable = true)
    private void stopItemOnBlock(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
        if (this.stopInteractItemBlock) {
            info.setReturnValue(InteractionResult.PASS);
            info.cancel();
        }
    }

    @Override
    public void stopCanUseBlocks(boolean flag) {
        this.stopInteractBlock = flag;
    }

    @Override
    public void stopCanUseItems(boolean flag) {
        this.stopInteractItemBlock = flag;
    }

    @Override
    public boolean allowUseBlocks() {
        return !this.stopInteractBlock;
    }

    @Override
    public boolean allowUseItems() {
        return !this.stopInteractItemBlock;
    }
}
