package io.github.flemmli97.flan.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface IPersistentProjectileVars {

    @Accessor("lastState")
    void setInBlockState(BlockState state);

    @Accessor("inGround")
    void setInGround(boolean flag);

    @Invoker("getHitGroundSoundEvent")
    SoundEvent getSoundEvent();

    @Invoker("resetPiercedEntities")
    void resetPiercingStatus();

    @Accessor("piercingIgnoreEntityIds")
    IntOpenHashSet getPiercedEntities();

    @Accessor("piercingIgnoreEntityIds")
    void setPiercedEntities(IntOpenHashSet set);
}
