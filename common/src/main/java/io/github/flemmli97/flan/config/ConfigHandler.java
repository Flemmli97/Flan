package io.github.flemmli97.flan.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Objects;

public class ConfigHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final Config CONFIG = new Config();

    public static void reloadConfigs(MinecraftServer server) {
        CONFIG.load(server);
    }

    public static Path getClaimSavePath(MinecraftServer server, ResourceKey<Level> reg) {
        return DimensionType.getStorageFolder(reg, server.getWorldPath(LevelResource.ROOT)).resolve("data").resolve("claims");
    }

    public static Path getPlayerSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("claimData");
    }

    public static boolean isClaimingTool(ItemStack stack) {
        return stack.getItem() == ConfigHandler.CONFIG.claimingItem && partialyMatchNBT(ConfigHandler.CONFIG.claimingNBT, stack);
    }

    public static boolean isInspectionTool(ItemStack stack) {

        return stack.getItem() == ConfigHandler.CONFIG.inspectionItem && partialyMatchNBT(ConfigHandler.CONFIG.inspectionNBT, stack);
    }

    private static boolean partialyMatchNBT(CompoundTag config, ItemStack stack) {
        if (config == null)
            return true;
        CompoundTag second = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (second.isEmpty())
            return config.isEmpty();
        return config.entrySet().stream().allMatch(e -> Objects.equals(e.getValue(), second.get(e.getKey())));
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
