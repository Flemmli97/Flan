package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.gui.StringResultScreenHandler;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void screenResultHandler(CallbackInfo info) {
        if ((Object) this instanceof StringResultScreenHandler h) {
            h.createResultInternal();
            info.cancel();
        }
    }
}
