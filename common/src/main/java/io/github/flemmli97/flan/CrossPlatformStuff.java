package io.github.flemmli97.flan;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.entity.effect.StatusEffect;

import java.nio.file.Path;

public class CrossPlatformStuff {

    @ExpectPlatform
    public static Path configPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static StatusEffect effectFromString(String s) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String stringFromEffect(StatusEffect s) {
        throw new AssertionError();
    }

}
