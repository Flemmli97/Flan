package io.github.flemmli97.flan.forge;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static StatusEffect effectFromString(String s) {
        return ForgeRegistries.POTIONS.getValue(new Identifier(s));
    }

    public static String stringFromEffect(StatusEffect s) {
        return s.getRegistryName().toString();
    }
}
