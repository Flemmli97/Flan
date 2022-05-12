package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class MossBlockCheckMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void check(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
        if (PlayerEvents.mossBonemeal(context)) {
            info.setReturnValue(InteractionResult.PASS);
            info.cancel();
        }
    }
}
