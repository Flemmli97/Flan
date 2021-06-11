package io.github.flemmli97.flan.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigPathImpl {

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
