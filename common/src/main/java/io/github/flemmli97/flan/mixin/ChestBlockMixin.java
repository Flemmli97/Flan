package io.github.flemmli97.flan.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin {

    @ModifyVariable(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ChestBlock;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private ChestType modifyChestType(ChestType type, BlockPlaceContext context, @Local(ordinal = 0) Direction placeDirection) {
        if (type == ChestType.SINGLE) {
            return type;
        }

        Direction direction = type == ChestType.LEFT ? placeDirection.getClockWise() : placeDirection.getCounterClockWise();
        BlockPos pos = context.getClickedPos().relative(direction);

        if (BlockInteractEvents.useBlocks(context.getPlayer(), context.getLevel(), context.getHand(), new BlockHitResult(context.getClickLocation(), direction, pos, false)) == InteractionResult.FAIL) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                player.level().getServer().schedule(new TickTask(1, () -> player.connection.send(new ClientboundBlockUpdatePacket(pos, context.getLevel().getBlockState(pos)))));
            }
            return ChestType.SINGLE;
        }

        return type;
    }
}
