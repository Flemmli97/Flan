package com.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private final File configDir;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 1200;
    public int minClaimsize = 100;
    public int defaultClaimDepth = 255;

    public String[] blacklistedWorlds = new String[] {"minecraft:the_nether"};
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;

    public Config(MinecraftServer server) {
        this.configDir = server.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
        try {
            if(!this.configDir.exists())
                this.configDir.mkdirs();
            File file = new File(this.configDir, "flan_config.json");
            if(!file.exists())
                file.createNewFile();
            this.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void load() {

    }

    private void save(File file) {
        JsonObject obj = new JsonObject();
        obj.addProperty("startingBlocks", this.startingBlocks);
        obj.addProperty("maxClaimBlocks", this.maxClaimBlocks);
        obj.addProperty("ticksForNextBlock", this.ticksForNextBlock);
        JsonArray arr = new JsonArray();
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);
        obj.addProperty("claimingItem", Registry.ITEM.getId(this.claimingItem).toString());
        obj.addProperty("inspectionItem", Registry.ITEM.getId(this.inspectionItem).toString());
        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        try {
            FileWriter writer = new FileWriter(file);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
