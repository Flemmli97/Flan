package io.github.flemmli97.flan;

import me.shedaniel.architectury.annotations.ExpectPlatform;

import java.nio.file.Path;

public class ConfigPath {

    @ExpectPlatform
    public static Path configPath() {
        throw new AssertionError();
    }
}
