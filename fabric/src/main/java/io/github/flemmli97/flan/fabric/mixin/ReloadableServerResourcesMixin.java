package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onNewServerReload(LayeredRegistryAccess<RegistryLayer> registryAccess, HolderLookup.Provider provider, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List<Registry.PendingTags<?>> list, int functionCompilationLevel, CallbackInfo info) {
        PermissionManager.create(provider);
        InteractionOverrideManager.create(provider);
    }
}
