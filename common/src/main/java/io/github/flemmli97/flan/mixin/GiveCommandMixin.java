package io.github.flemmli97.flan.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;

/**
 * @author Carlos Varas Alonso - 21/10/2025 8:22
 */
@Mixin(GiveCommand.class)
public abstract class GiveCommandMixin {
    /**
     * @author zonary123
     * @reason This is a fix for give command in drop
     */
    @Overwrite(remap = true)
    private static int giveItem(CommandSourceStack source, ItemInput item, Collection<ServerPlayer> targets, int count) throws CommandSyntaxException {
        ItemStack templateStack = item.createItemStack(1, false);
        int maxStack = templateStack.getMaxStackSize();
        int maxTotal = maxStack * 100;

        if (count > maxTotal) {
            source.sendFailure(Component.translatable("commands.give.failed.toomanyitems", maxTotal, templateStack.getDisplayName()));
            return 0;
        }

        for (ServerPlayer serverPlayer : targets) {
            int remaining = count;

            while (remaining > 0) {
                int stackSize = Math.min(maxStack, remaining);
                remaining -= stackSize;

                ItemStack stackToGive = item.createItemStack(stackSize, false);
                boolean added = serverPlayer.getInventory().add(stackToGive);

                if (!added) {
                    // If inventory is full, drop the remaining stack
                    ItemEntity dropped = serverPlayer.drop(stackToGive, false);
                    if (dropped != null) {
                        dropped.setNoPickUpDelay();
                        dropped.setTarget(serverPlayer.getUUID());
                    }
                } else {
                    serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                        ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    serverPlayer.containerMenu.broadcastChanges();
                }
            }
        }

        // Send success message
        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable("commands.give.success.single", count, templateStack.getDisplayName(), target.getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.give.success.single", count, templateStack.getDisplayName(), targets.size()), true);
        }

        return targets.size();
    }

}
