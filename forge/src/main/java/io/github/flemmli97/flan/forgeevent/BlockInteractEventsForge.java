package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

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
}
