package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

public class BlockInteractEventsForge {

    public static void startBreakBlocks(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getWorld() instanceof ServerLevel))
            return;
        if (BlockInteractEvents.startBreakBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos(), event.getFace()) == InteractionResult.FAIL)
            event.setCanceled(true);
    }

    public static void breakBlocks(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof ServerLevel))
            return;
        if (!BlockInteractEvents.breakBlocks((Level) event.getWorld(), event.getPlayer(), event.getPos(), event.getState(), event.getWorld().getBlockEntity(event.getPos())))
            event.setCanceled(true);
    }

    public static void useBlocks(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult res = BlockInteractEvents.useBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
        if (res == InteractionResult.SUCCESS) {
            event.setCancellationResult(res);
            event.setCanceled(true);
            return;
        }
        if (res == InteractionResult.FAIL)
            event.setUseBlock(Event.Result.DENY);
        res = ItemInteractEvents.onItemUseBlock(new UseOnContext(event.getPlayer(), event.getHand(), event.getHitVec()));
        if (res == InteractionResult.FAIL)
            event.setUseItem(Event.Result.DENY);
    }

    /**
     * This is in most cases a double check but since its not in all cases we need to do this
     */
    public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
        event.setCanceled(forgePlaceBlocks(event.getEntity(), event.getPos(), event.getPlacedBlock()));
    }

    /**
     * This is in most cases a double check but since its not in all cases we need to do this
     */
    public static void placeBlocks(BlockEvent.EntityMultiPlaceEvent event) {
        event.setCanceled(forgePlaceBlocks(event.getEntity(), event.getPos(), event.getPlacedBlock()));
    }

    private static boolean forgePlaceBlocks(Entity entity, BlockPos placePos, BlockState placedBlock) {
        if (!(entity instanceof ServerPlayer player))
            return false;
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        IPermissionContainer claim = storage.getForPermissionCheck(placePos);
        if (claim == null)
            return false;
        ClaimPermission perm = ObjectToPermissionMap.getFromBlock(placedBlock.getBlock());
        if (perm != null) {
            if (!claim.canInteract(player, perm, placePos, false)) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), true);
                return true;
            }
        }
        if (!claim.canInteract(player, PermissionRegistry.PLACE, placePos, false)) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), true);
            PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, player.getInventory().getSelected()));
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, 40, player.getInventory().getItem(40)));
            return true;
        }
        return false;
    }
}
