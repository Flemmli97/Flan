package io.github.flemmli97.flan;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.nio.file.Path;

public class CrossPlatformStuff {

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static StatusEffect effectFromString(String s) {
        return Registry.STATUS_EFFECT.get(new Identifier(s));
    }

    public static String stringFromEffect(StatusEffect s) {
        return Registry.STATUS_EFFECT.getId(s).toString();
    }
}
