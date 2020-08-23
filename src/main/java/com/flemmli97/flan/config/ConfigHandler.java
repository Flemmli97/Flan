package com.flemmli97.flan.config;

import net.minecraft.server.MinecraftServer;

public class ConfigHandler {

    public static Config config;
    public static LangConfig lang;

    public static void serverLoad(MinecraftServer server) {
        config = new Config(server);
        config.load();
        lang = new LangConfig(server);
        lang.load();
    }
}
