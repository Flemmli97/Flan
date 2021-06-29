package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;

public class BlockInteractEventsForge {

    public static void startBreakBlocks(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getWorld() instanceof ServerWorld))
            return;
        if (BlockInteractEvents.startBreakBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos(), event.getFace()) == ActionResult.FAIL)
            event.setCanceled(true);
    }

    public static void breakBlocks(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof ServerWorld))
            return;
        if (!BlockInteractEvents.breakBlocks((World) event.getWorld(), event.getPlayer(), event.getPos(), event.getState(), event.getWorld().getBlockEntity(event.getPos())))
            event.setCanceled(true);
    }

    //Right click block
    public static void useBlocks(PlayerInteractEvent.RightClickBlock event) {
        ActionResult result = BlockInteractEvents.useBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec(), BlockInteractEventsForge::isContainer);
        if (result != ActionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static boolean isContainer(BlockEntity blockEntity) {
        return blockEntity instanceof Inventory || blockEntity instanceof InventoryProvider || blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
    }
}
