package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.ItemUseBlockFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin implements ItemUseBlockFlags {

    @Unique
    private boolean stopInteractBlock;
    @Unique
    private boolean stopInteractItemBlock;

    @ModifyVariable(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), ordinal = 1)
    private boolean stopBlockUse(boolean orig) {
        if (this.stopInteractBlock)
            return true;
        return orig;
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"), cancellable = true)
    private void stopItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info) {
        if (this.stopInteractItemBlock) {
            info.setReturnValue(ActionResult.PASS);
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
}
