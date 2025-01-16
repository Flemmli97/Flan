package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.ParticleIndicators;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum EnumDisplayType {

    MAIN(Blocks.GOLD_BLOCK.defaultBlockState(), ParticleIndicators.CLAIMCORNER, ParticleIndicators.CLAIMMIDDLE),
    SUB(Blocks.IRON_BLOCK.defaultBlockState(), ParticleIndicators.SUBCLAIMCORNER, ParticleIndicators.SUBCLAIMMIDDLE),
    CONFLICT(Blocks.REDSTONE_BLOCK.defaultBlockState(), ParticleIndicators.OVERLAPCLAIM, ParticleIndicators.OVERLAPCLAIM),
    EDIT(Blocks.LAPIS_BLOCK.defaultBlockState(), ParticleIndicators.EDITCLAIMCORNER, ParticleIndicators.EDITCLAIMMIDDLE);

    public final BlockState displayBlock;
    public final ParticleOptions cornerParticle, middleParticle;

    EnumDisplayType(BlockState displayBlock, ParticleOptions cornerParticle, ParticleOptions middleParticle) {
        this.displayBlock = displayBlock;
        this.cornerParticle = cornerParticle;
        this.middleParticle = middleParticle;
    }
}
