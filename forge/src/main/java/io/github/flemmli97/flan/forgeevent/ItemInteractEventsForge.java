package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.ItemInteractEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ItemInteractEventsForge {

    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        TypedActionResult<ItemStack> result = ItemInteractEvents.useItem(event.getPlayer(), event.getWorld(), event.getHand());
        if (result.getResult() != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result.getResult());
        }
    }
}
