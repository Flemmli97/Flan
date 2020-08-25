package com.flemmli97.flan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

public class ConfigHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Config config;
    public static LangConfig lang;

    public static void serverLoad(MinecraftServer server) {
        config = new Config(server);
        lang = new LangConfig(server);
        reloadConfigs();
    }

    public static void reloadConfigs() {
        config.load();
        lang.load();
    }
}
