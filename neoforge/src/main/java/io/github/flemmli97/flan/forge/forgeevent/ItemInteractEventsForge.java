package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.event.ItemInteractEvents;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ItemInteractEventsForge {

    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResult result = ItemInteractEvents.useItem(event.getEntity(), event.getLevel(), event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
