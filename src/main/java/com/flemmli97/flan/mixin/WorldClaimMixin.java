package com.flemmli97.flan.mixin;

import com.flemmli97.flan.IClaimData;
import com.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class WorldClaimMixin extends World implements IClaimData {
    @Unique
    private ClaimStorage claimData;

    protected WorldClaimMixin(MutableWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(properties, registryKey, dimensionType, supplier, bl, bl2, l);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        this.claimData = new ClaimStorage(this.getServer(), this.getRegistryKey());
    }

    @Inject(method = "saveLevel()V", at = @At("RETURN"))
    private void saveClaimData(CallbackInfo info) {
        this.claimData.save(this.getServer(), this.getRegistryKey());
    }

    @Override
    public ClaimStorage getClaimData() {
        return this.claimData;
    }
}
