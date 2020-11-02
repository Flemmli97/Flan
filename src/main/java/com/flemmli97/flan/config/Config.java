package com.flemmli97.flan.config;

import com.flemmli97.flan.Flan;
import com.flemmli97.flan.claim.EnumPermission;
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
import java.util.EnumMap;
import java.util.Map;

public class Config {

    private File config;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 1200;
    public int minClaimsize = 100;
    public int defaultClaimDepth = 10;

    public String[] blacklistedWorlds = new String[0];
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;
    public int permissionLevel = 2;

    public boolean log;

    public final Map<String,EnumMap<EnumPermission,Boolean>> globalDefaultPerms = Maps.newHashMap();

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
            JsonArray arr = ConfigHandler.arryFromJson(obj, "blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = ConfigHandler.fromJson(obj, "worldWhitelist", this.worldWhitelist);
            if(obj.has("claimingItem"))
                this.claimingItem = Registry.ITEM.get(new Identifier((obj.get("claimingItem").getAsString())));
            if(obj.has("inspectionItem"))
                this.inspectionItem = Registry.ITEM.get(new Identifier((obj.get("inspectionItem").getAsString())));
            this.claimDisplayTime = ConfigHandler.fromJson(obj, "claimDisplayTime", this.claimDisplayTime);
            this.globalDefaultPerms.clear();
            JsonObject glob = ConfigHandler.fromJson(obj, "globalDefaultPerms");
            glob.entrySet().forEach(e->{
                EnumMap<EnumPermission, Boolean> perms = new EnumMap<>(EnumPermission.class);
                if(e.getValue().isJsonObject()){
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm->{
                        try{
                            perms.put(EnumPermission.valueOf(jperm.getKey()), jperm.getValue().getAsBoolean());
                        }
                        catch (IllegalArgumentException ex){
                            Flan.log("No permmission with name {}", jperm.getKey());
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
        JsonArray arr = new JsonArray();
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);
        obj.addProperty("claimingItem", Registry.ITEM.getId(this.claimingItem).toString());
        obj.addProperty("inspectionItem", Registry.ITEM.getId(this.inspectionItem).toString());
        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        obj.addProperty("permissionLevel", this.permissionLevel);
        JsonObject global = new JsonObject();
        this.globalDefaultPerms.forEach((key, value) -> {
            JsonObject perm = new JsonObject();
            value.forEach((key1, value1) -> perm.addProperty(key1.toString(), value1));
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

    public boolean globallyDefined(ServerWorld world, EnumPermission perm){
        EnumMap<EnumPermission,Boolean> global = ConfigHandler.config.globalDefaultPerms.get(world.getRegistryKey().getValue().toString());
        return global != null && global.containsKey(perm);
    }}
