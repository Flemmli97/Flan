package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric APIs UseEntityCallback is at Entity#interactAt and thats only used for armor stand. Why its only there idk...
 */
@Mixin(Player.class)
public abstract class PlayerInteractEntityMixin {

    @Inject(method = "interactOn", at = @At(value = "HEAD"), cancellable = true)
    private void interactOnEntity(Entity entity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> info) {
        if (entity == null || !(entity.level() instanceof ServerLevel))
            return;
        InteractionResult result = EntityInteractEvents.useEntity((Player) (Object) this, entity.level(), interactionHand, entity);
        if (result != InteractionResult.PASS) {
            info.setReturnValue(InteractionResult.PASS);
            info.cancel();
        }
    }
}
