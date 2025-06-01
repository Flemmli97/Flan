package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.flemmli97.flan.api.fabric.ItemUseBlockFlags;
import io.github.flemmli97.flan.utils.BlockBreakAttemptHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ItemUseBlockFlags {

    @Unique
    private boolean flan_stopInteractBlock;
    @Unique
    private boolean flan_stopInteractItemBlock;

    @ModifyVariable(method = "useItemOn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"))
    private boolean stopBlockUse(boolean orig) {
        if (this.flan_stopInteractBlock)
            return true;
        return orig;
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasInfiniteMaterials()Z"), cancellable = true)
    private void stopItemOnBlock(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
        if (this.flan_stopInteractItemBlock) {
            info.setReturnValue(InteractionResult.PASS);
            info.cancel();
        }
    }

    @Override
    public void flan$stopCanUseBlocks(boolean flag) {
        this.flan_stopInteractBlock = flag;
    }

    @Override
    public void flan$stopCanUseItems(boolean flag) {
        this.flan_stopInteractItemBlock = flag;
    }

    @Override
    public boolean flan$allowUseBlocks() {
        return !this.flan_stopInteractBlock;
    }

    @Override
    public boolean flan$allowUseItems() {
        return !this.flan_stopInteractItemBlock;
    }

    /**
     * Disable mismatched block warning if the cause was due to claim prevention
     */
    @WrapWithCondition(
            method = "handleBlockBreakAction",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false)
    )
    private boolean shouldWarn(Logger logger, String warn, Object obj, Object obj2) {
        return !((BlockBreakAttemptHandler) this).flan$wasInstabreak() || ((BlockBreakAttemptHandler) this).flan$failedPos() == null;
    }
}
