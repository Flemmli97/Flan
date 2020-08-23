package com.flemmli97.flan.config;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;

public class Config {

    private File configDir;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 1200;

    public String[] blacklistedWorlds;
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;

    public Config(MinecraftServer server) {
        this.configDir = server.getSavePath(WorldSavePath.ROOT).resolve("config/claimConfigs").toFile();
    }


    public void load() {

    }

    public void save() {

    }
}
