package com.flemmli97.flan.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PersistentProjectileEntity.class)
public interface IPersistentProjectileVars {

    @Accessor("inBlockState")
    void setInBlockState(BlockState state);

    @Accessor("inGround")
    void setInGround(boolean flag);

    @Invoker("getSound")
    SoundEvent getSoundEvent();

    @Invoker("clearPiercingStatus")
    void resetPiercingStatus();
}
