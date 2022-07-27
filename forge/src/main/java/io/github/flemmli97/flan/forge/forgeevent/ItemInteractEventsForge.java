package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.event.ItemInteractEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ItemInteractEventsForge {

    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        InteractionResultHolder<ItemStack> result = ItemInteractEvents.useItem(event.getEntity(), event.getLevel(), event.getHand());
        if (result.getResult() != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result.getResult());
        }
    }
}
