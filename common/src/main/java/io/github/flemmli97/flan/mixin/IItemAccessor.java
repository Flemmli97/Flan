package io.github.flemmli97.flan.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.class)
public interface IItemAccessor {

    @Invoker("getPlayerPOVHitResult")
    static BlockHitResult getRaycast(Level world, Player player, ClipContext.Fluid fluidHandling) {
        throw new IllegalStateException();
    }
}
