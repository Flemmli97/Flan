package io.github.flemmli97.flan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Config config;
    public static LangConfig lang;
    private static Map<ResourceKey<Level>, Path> claimSavePath = new HashMap<>();
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

    public static Path getClaimSavePath(MinecraftServer server, ResourceKey<Level> reg) {
        return claimSavePath.computeIfAbsent(reg, r -> DimensionType.getStorageFolder(r, server.getWorldPath(LevelResource.ROOT)).resolve("data").resolve("claims"));
    }

    public static Path getPlayerSavePath(MinecraftServer server) {
        if (playerSavePath == null)
            playerSavePath = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("claimData");
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
