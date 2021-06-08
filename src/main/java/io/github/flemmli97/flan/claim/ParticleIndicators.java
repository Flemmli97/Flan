package io.github.flemmli97.flan.claim;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3f;

public class ParticleIndicators {

    public static final DustParticleEffect CLAIMCORNER = new DustParticleEffect(new Vec3f(194 / 255f, 130 / 255f, 4 / 255f), 3);
    public static final DustParticleEffect CLAIMMIDDLE = new DustParticleEffect(new Vec3f(237 / 255f, 187 / 255f, 38 / 255f), 3);

    public static final DustParticleEffect SUBCLAIMCORNER = new DustParticleEffect(new Vec3f(125 / 255f, 125 / 255f, 125 / 255f), 3);
    public static final DustParticleEffect SUBCLAIMMIDDLE = new DustParticleEffect(new Vec3f(194 / 255f, 194 / 255f, 194 / 255f), 3);

    public static final DustParticleEffect EDITCLAIMCORNER = new DustParticleEffect(new Vec3f(12 / 255f, 110 / 255f, 103 / 255f), 3);
    public static final DustParticleEffect EDITCLAIMMIDDLE = new DustParticleEffect(new Vec3f(20 / 255f, 186 / 255f, 175 / 255f), 3);

    public static final DustParticleEffect SETCORNER = new DustParticleEffect(new Vec3f(18 / 255f, 38 / 255f, 150 / 255f), 3);

    public static final DustParticleEffect OVERLAPCLAIM = new DustParticleEffect(new Vec3f(255 / 255f, 0 / 255f, 0 / 255f), 3);
}
