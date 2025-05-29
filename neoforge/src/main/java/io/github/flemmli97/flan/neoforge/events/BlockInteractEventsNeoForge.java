package io.github.flemmli97.flan.neoforge.events;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class BlockInteractEventsNeoForge {

    public static void startBreakBlocks(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel))
            return;
        if (BlockInteractEvents.startBreakBlocks(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace()) == InteractionResult.FAIL)
            event.setCanceled(true);
    }

    public static void breakBlocks(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel))
            return;
        if (!BlockInteractEvents.breakBlocks((Level) event.getLevel(), event.getPlayer(), event.getPos(), event.getState(), event.getLevel().getBlockEntity(event.getPos())))
            event.setCanceled(true);
    }

    public static void useBlocks(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult res = BlockInteractEvents.useBlocks(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (res == InteractionResult.SUCCESS) {
            event.setCancellationResult(res);
            event.setCanceled(true);
            return;
        }
        if (res == InteractionResult.FAIL)
            event.setUseBlock(TriState.FALSE);
        res = ItemInteractEvents.onItemUseBlock(new UseOnContext(event.getEntity(), event.getHand(), event.getHitVec()));
        if (res == InteractionResult.FAIL)
            event.setUseItem(TriState.FALSE);
    }

    /**
     * This is in most cases a double check but since its not in all cases we need to do this
     */
    public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        event.setCanceled(placeBlocksHandler(event.getEntity(), event.getPos(), event.getPlacedBlock()));
    }

    /**
     * This is in most cases a double check but since its not in all cases we need to do this
     */
    public static void placeBlocks(BlockEvent.EntityMultiPlaceEvent event) {
        event.setCanceled(placeBlocksHandler(event.getEntity(), event.getPos(), event.getPlacedBlock()));
    }

    private static boolean placeBlocksHandler(Entity entity, BlockPos placePos, BlockState placedBlock) {
        if (!(entity instanceof ServerPlayer player))
            return false;
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        IPermissionContainer claim = storage.getForPermissionCheck(placePos);
        if (claim == null)
            return false;
        ResourceLocation perm = InteractionOverrideManager.INSTANCE.getBlockInteract(placedBlock.getBlock());
        if (perm != null) {
            if (!claim.canInteract(player, perm, placePos, false)) {
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
                return true;
            }
        }
        if (!claim.canInteract(player, BuiltinPermission.PLACE, placePos, false)) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
            PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, player.getInventory().getSelected()));
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, 40, player.getInventory().getItem(40)));
            return true;
        }
        return false;
    }
}
