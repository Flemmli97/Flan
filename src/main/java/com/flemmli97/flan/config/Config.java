package com.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

    public Config(MinecraftServer server) {
        File configDir = server.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
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
            this.startingBlocks = obj.get("startingBlocks").getAsInt();
            this.maxClaimBlocks = obj.get("maxClaimBlocks").getAsInt();
            this.ticksForNextBlock = obj.get("ticksForNextBlock").getAsInt();
            this.minClaimsize = obj.get("minClaimsize").getAsInt();
            this.defaultClaimDepth = obj.get("defaultClaimDepth").getAsInt();
            JsonArray arr = obj.getAsJsonArray("blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = obj.get("worldWhitelist").getAsBoolean();
            this.claimingItem = Registry.ITEM.get(new Identifier((obj.get("claimingItem").getAsString())));
            this.inspectionItem = Registry.ITEM.get(new Identifier((obj.get("inspectionItem").getAsString())));
            this.claimDisplayTime = obj.get("claimDisplayTime").getAsInt();
            if(obj.has("permissionLevel"))
                this.permissionLevel = obj.get("permissionLevel").getAsInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.save();
    }

    private void save() {
        JsonObject obj = new JsonObject();
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
        try {
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
