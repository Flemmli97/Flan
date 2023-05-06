package io.github.flemmli97.flan.claim;

import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

public class ParticleIndicators {

    public static final DustParticleOptions CLAIMCORNER = new DustParticleOptions(new Vector3f(194 / 255f, 130 / 255f, 4 / 255f), 3);
    public static final DustParticleOptions CLAIMMIDDLE = new DustParticleOptions(new Vector3f(237 / 255f, 187 / 255f, 38 / 255f), 3);

    public static final DustParticleOptions SUBCLAIMCORNER = new DustParticleOptions(new Vector3f(125 / 255f, 125 / 255f, 125 / 255f), 3);
    public static final DustParticleOptions SUBCLAIMMIDDLE = new DustParticleOptions(new Vector3f(194 / 255f, 194 / 255f, 194 / 255f), 3);

    public static final DustParticleOptions EDITCLAIMCORNER = new DustParticleOptions(new Vector3f(12 / 255f, 110 / 255f, 103 / 255f), 3);
    public static final DustParticleOptions EDITCLAIMMIDDLE = new DustParticleOptions(new Vector3f(20 / 255f, 186 / 255f, 175 / 255f), 3);

    public static final DustParticleOptions SETCORNER = new DustParticleOptions(new Vector3f(18 / 255f, 38 / 255f, 150 / 255f), 3);

    public static final DustParticleOptions OVERLAPCLAIM = new DustParticleOptions(new Vector3f(255 / 255f, 0 / 255f, 0 / 255f), 3);
}
