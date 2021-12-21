package io.github.flemmli97.flan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Config config;
    public static LangConfig lang;
    private static Map<RegistryKey<World>, Path> claimSavePath = new HashMap<>();
    private static Path playerSavePath;

    public static void serverLoad(MinecraftServer server) {
        config = new Config(server);
        lang = new LangConfig(server);
        reloadConfigs(server);
    }

    public static void reloadConfigs(MinecraftServer server) {
        config.load();
        lang.load();
        ObjectToPermissionMap.reload(server);
    }

    public static Path getClaimSavePath(MinecraftServer server, RegistryKey<World> reg) {
        return claimSavePath.computeIfAbsent(reg, r -> DimensionType.getSaveDirectory(r, server.getSavePath(WorldSavePath.ROOT).toFile()).toPath().resolve("data").resolve("claims"));
    }

    public static Path getPlayerSavePath(MinecraftServer server) {
        if (playerSavePath == null)
            playerSavePath = server.getSavePath(WorldSavePath.PLAYERDATA).resolve("claimData");
        return playerSavePath;
    }


    public static int fromJson(JsonObject obj, String key, int fallback) {
        try {
            return obj.get(key).getAsInt();
        } catch (NullPointerException | UnsupportedOperationException e) {
            return fallback;
        }
    }

    public static boolean fromJson(JsonObject obj, String key, boolean fallback) {
        try {
            return obj.get(key).getAsBoolean();
        } catch (NullPointerException | UnsupportedOperationException e) {
            return fallback;
        }
    }

    public static String fromJson(JsonObject obj, String key, String fallback) {
        try {
            return obj.get(key).getAsString();
        } catch (NullPointerException | UnsupportedOperationException e) {
            return fallback;
        }
    }

    public static JsonObject fromJson(JsonObject obj, String key) {
        try {
            return obj.get(key).getAsJsonObject();
        } catch (NullPointerException | UnsupportedOperationException e) {
            return new JsonObject();
        }
    }

    public static JsonArray arryFromJson(JsonObject obj, String key) {
        try {
            return obj.get(key).getAsJsonArray();
        } catch (NullPointerException | UnsupportedOperationException e) {
            return new JsonArray();
        }
    }
}
