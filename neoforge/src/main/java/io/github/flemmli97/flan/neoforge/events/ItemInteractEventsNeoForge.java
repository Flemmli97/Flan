package io.github.flemmli97.flan.neoforge.events;

import io.github.flemmli97.flan.event.ItemInteractEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class ItemInteractEventsNeoForge {

    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResultHolder<ItemStack> result = ItemInteractEvents.useItem(event.getEntity(), event.getLevel(), event.getHand());
        if (result.getResult() != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result.getResult());
        }
    }
}
