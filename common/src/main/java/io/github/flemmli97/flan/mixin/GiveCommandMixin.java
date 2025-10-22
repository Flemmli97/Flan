package io.github.flemmli97.flan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.flemmli97.flan.utils.PlayerDropHandler;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    @WrapOperation(method = "giveItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static ItemEntity handleDrop(ServerPlayer player, ItemStack stack, boolean thrower, Operation<ItemEntity> original) {
        ((PlayerDropHandler) player).flan$setForcedDrop(true);
        ItemEntity entity = original.call(player, stack, thrower);
        ((PlayerDropHandler) player).flan$setForcedDrop(false);
        return entity;
    }
}
