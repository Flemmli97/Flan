package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.flan.api.fabric.ItemUseBlockFlags;
import io.github.flemmli97.flan.utils.BlockBreakAttemptHandler;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerGameMode.class, priority = 999)
public abstract class ServerPlayerGameModeMixin implements ItemUseBlockFlags {

    @Unique
    private boolean flan_stopInteractBlock;
    @Unique
    private boolean flan_stopInteractItemBlock;

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;"))
    private ItemInteractionResult stopBlockUse(BlockState state, ItemStack stack, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, Operation<ItemInteractionResult> original) {
        if (this.flan_stopInteractBlock)
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return original.call(state, stack, level, player, interactionHand, blockHitResult);
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult stopItemOnBlock(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        if (this.flan_stopInteractItemBlock) {
            context.getPlayer().setItemInHand(context.getHand(), stack);
            return InteractionResult.PASS;
        }
        return original.call(stack, context);
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
