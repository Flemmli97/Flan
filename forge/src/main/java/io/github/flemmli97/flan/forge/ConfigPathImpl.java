package io.github.flemmli97.flan.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ConfigPathImpl {

    public static Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
