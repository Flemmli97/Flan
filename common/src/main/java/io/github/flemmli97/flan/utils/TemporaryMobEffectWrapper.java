package io.github.flemmli97.flan.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class TemporaryMobEffectWrapper extends MobEffectInstance {

    private final MobEffectInstance wrapped;

    public TemporaryMobEffectWrapper(MobEffect mobEffect, int duration, int amplifier, MobEffectInstance wrapped) {
        super(mobEffect, duration, amplifier, true, false, false, null);
        this.wrapped = wrapped;
    }

    public MobEffectInstance getWrapped() {
        return this.wrapped;
    }

    @Override
    public boolean tick(LivingEntity entity, Runnable onExpirationRunnable) {
        if (this.wrapped != null)
            this.wrapped.tick(entity, onExpirationRunnable);
        return super.tick(entity, () -> {
            onExpirationRunnable.run();
            if (this.wrapped != null && this.wrapped.getDuration() > 0)
                entity.addEffect(this.wrapped);
        });
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        return this.wrapped == null ? super.save(nbt) : this.wrapped.save(nbt);
    }
}
