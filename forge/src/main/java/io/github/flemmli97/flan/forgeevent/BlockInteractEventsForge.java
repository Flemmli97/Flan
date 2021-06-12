package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class BlockInteractEventsForge {

    public static void breakBlocks(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof World))
            return;
        if (!BlockInteractEvents.breakBlocks((World) event.getWorld(), event.getPlayer(), event.getPos(), event.getState(), event.getWorld().getBlockEntity(event.getPos())))
            event.setCanceled(true);
    }

    //Right click block
    public static void useBlocks(PlayerInteractEvent.RightClickBlock event) {
        ActionResult result = BlockInteractEvents.useBlocks(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
        if (result != ActionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
