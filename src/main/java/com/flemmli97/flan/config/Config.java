package com.flemmli97.flan.config;

import com.flemmli97.flan.Flan;
import com.flemmli97.flan.api.ClaimPermission;
import com.flemmli97.flan.api.PermissionRegistry;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Config {

    private File config;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 600;
    public int minClaimsize = 100;
    public int defaultClaimDepth = 10;
    public boolean lenientBlockEntityCheck;

    public String[] blacklistedWorlds = new String[0];
    public boolean worldWhitelist;
    public boolean allowMobSpawnToggle;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;
    public int permissionLevel = 2;

    public boolean log;

    private final Map<String, Map<ClaimPermission, Boolean>> globalDefaultPerms = Maps.newHashMap();

    public Config(MinecraftServer server) {
        File configDir = FabricLoader.getInstance().getConfigDir().resolve("flan").toFile();
        //.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
        try {
            if (!configDir.exists())
                configDir.mkdirs();
            this.config = new File(configDir, "flan_config.json");
            if (!this.config.exists()) {
                this.config.createNewFile();
                this.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileReader reader = new FileReader(this.config);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            this.startingBlocks = ConfigHandler.fromJson(obj, "startingBlocks", this.startingBlocks);
            this.maxClaimBlocks = ConfigHandler.fromJson(obj, "maxClaimBlocks", this.maxClaimBlocks);
            this.ticksForNextBlock = ConfigHandler.fromJson(obj, "ticksForNextBlock", this.ticksForNextBlock);
            this.minClaimsize = ConfigHandler.fromJson(obj, "minClaimsize", this.minClaimsize);
            this.defaultClaimDepth = ConfigHandler.fromJson(obj, "defaultClaimDepth", this.defaultClaimDepth);
            this.lenientBlockEntityCheck = ConfigHandler.fromJson(obj, "lenientBlockEntityCheck", this.lenientBlockEntityCheck);
            JsonArray arr = ConfigHandler.arryFromJson(obj, "blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = ConfigHandler.fromJson(obj, "worldWhitelist", this.worldWhitelist);
            this.allowMobSpawnToggle = ConfigHandler.fromJson(obj, "allowMobSpawnToggle", false);
            if (obj.has("claimingItem"))
                this.claimingItem = Registry.ITEM.get(new Identifier((obj.get("claimingItem").getAsString())));
            if (obj.has("inspectionItem"))
                this.inspectionItem = Registry.ITEM.get(new Identifier((obj.get("inspectionItem").getAsString())));
            this.claimDisplayTime = ConfigHandler.fromJson(obj, "claimDisplayTime", this.claimDisplayTime);
            this.globalDefaultPerms.clear();
            JsonObject glob = ConfigHandler.fromJson(obj, "globalDefaultPerms");
            glob.entrySet().forEach(e -> {
                Map<ClaimPermission, Boolean> perms = Maps.newHashMap();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        try {
                            perms.put(PermissionRegistry.get(jperm.getKey()), jperm.getValue().getAsBoolean());
                        } catch (NullPointerException ex) {
                            Flan.log("No permission with name {}", jperm.getKey());
                        }
                    });
                }
                this.globalDefaultPerms.put(e.getKey(), perms);
            });
            this.log = ConfigHandler.fromJson(obj, "enableLogs", this.log);
            this.permissionLevel = ConfigHandler.fromJson(obj, "permissionLevel", this.permissionLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.save();
    }

    private void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("__comment", "For help with the config refer to https://github.com/Flemmli97/Flan/wiki/Config");
        obj.addProperty("startingBlocks", this.startingBlocks);
        obj.addProperty("maxClaimBlocks", this.maxClaimBlocks);
        obj.addProperty("ticksForNextBlock", this.ticksForNextBlock);
        obj.addProperty("minClaimsize", this.minClaimsize);
        obj.addProperty("defaultClaimDepth", this.defaultClaimDepth);
        obj.addProperty("lenientBlockEntityCheck", this.lenientBlockEntityCheck);
        JsonArray arr = new JsonArray();
        for (int i = 0; i < this.blacklistedWorlds.length; i++)
            arr.add(this.blacklistedWorlds[i]);
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);
        obj.addProperty("allowMobSpawnToggle", this.allowMobSpawnToggle);
        obj.addProperty("claimingItem", Registry.ITEM.getId(this.claimingItem).toString());
        obj.addProperty("inspectionItem", Registry.ITEM.getId(this.inspectionItem).toString());
        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        obj.addProperty("permissionLevel", this.permissionLevel);
        JsonObject global = new JsonObject();
        this.globalDefaultPerms.forEach((key, value) -> {
            JsonObject perm = new JsonObject();
            value.forEach((key1, value1) -> perm.addProperty(key1.id, value1));
            global.add(key, perm);
        });
        obj.add("globalDefaultPerms", global);
        obj.addProperty("enableLogs", this.log);
        try {
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean globallyDefined(ServerWorld world, ClaimPermission perm) {
        return getGlobal(world, perm) != null;
    }

    public Boolean getGlobal(ServerWorld world, ClaimPermission perm) {
        if (perm == PermissionRegistry.MOBSPAWN && !this.allowMobSpawnToggle)
            return Boolean.FALSE;
        Map<ClaimPermission, Boolean> permMap = ConfigHandler.config.globalDefaultPerms.get(world.getRegistryKey().getValue().toString());
        return permMap == null ? null : permMap.getOrDefault(perm, null);
    }
}
