package io.github.flemmli97.flan.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Config {

    private File config;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 600;
    public int minClaimsize = 100;
    public int defaultClaimDepth = 10;
    public boolean lenientBlockEntityCheck;
    public List<String> ignoredBlocks = new ArrayList<>();
    public List<String> blockEntityTagIgnore = Lists.newArrayList(
            "IsDeathChest" //vanilla death chest
    );
    public List<String> entityTagIgnore = Lists.newArrayList(
            "graves.marker" //vanilla tweaks
    );

    public String[] blacklistedWorlds = new String[0];
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;
    public int permissionLevel = 2;

    public int sellPrice = -1;
    public int buyPrice = -1;

    public boolean lockDrops = true;
    public int dropTicks = 6000;

    public int inactivityTime = 30;
    public int inactivityBlocksMax = 2000;

    public boolean log;

    public Map<String, Map<ClaimPermission, Boolean>> defaultGroups = createHashMap(map -> {
        map.put("Co-Owner", createLinkedHashMap(perms -> PermissionRegistry.getPerms().forEach(p -> perms.put(p, true))));
        map.put("Visitor", createLinkedHashMap(perms -> {
            perms.put(PermissionRegistry.BED, true);
            perms.put(PermissionRegistry.DOOR, true);
            perms.put(PermissionRegistry.FENCEGATE, true);
            perms.put(PermissionRegistry.TRAPDOOR, true);
            perms.put(PermissionRegistry.BUTTONLEVER, true);
            perms.put(PermissionRegistry.PRESSUREPLATE, true);
            perms.put(PermissionRegistry.ENDERCHEST, true);
            perms.put(PermissionRegistry.ENCHANTMENTTABLE, true);
            perms.put(PermissionRegistry.ITEMFRAMEROTATE, true);
            perms.put(PermissionRegistry.PORTAL, true);
            perms.put(PermissionRegistry.TRADING, true);
        }));
    });

    private final Map<String, Map<ClaimPermission, GlobalType>> globalDefaultPerms = createHashMap(map -> map.put("*", createHashMap(perms -> {
        perms.put(PermissionRegistry.FLIGHT, GlobalType.ALLTRUE);
        perms.put(PermissionRegistry.MOBSPAWN, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.TELEPORT, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.NOHUNGER, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.EDITPOTIONS, GlobalType.ALLFALSE);
    })));

    public Config(MinecraftServer server) {
        File configDir = CrossPlatformStuff.configPath().resolve("flan").toFile();
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
            this.ignoredBlocks.clear();
            ConfigHandler.arryFromJson(obj, "ignoredBlocks").forEach(e -> this.ignoredBlocks.add(e.getAsString()));
            this.blockEntityTagIgnore.clear();
            ConfigHandler.arryFromJson(obj, "blockEntityTagIgnore").forEach(e -> this.blockEntityTagIgnore.add(e.getAsString()));
            this.entityTagIgnore.clear();
            ConfigHandler.arryFromJson(obj, "entityTagIgnore").forEach(e -> this.entityTagIgnore.add(e.getAsString()));

            JsonArray arr = ConfigHandler.arryFromJson(obj, "blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = ConfigHandler.fromJson(obj, "worldWhitelist", this.worldWhitelist);
            if (obj.has("claimingItem"))
                this.claimingItem = Registry.ITEM.get(new Identifier((obj.get("claimingItem").getAsString())));
            if (obj.has("inspectionItem"))
                this.inspectionItem = Registry.ITEM.get(new Identifier((obj.get("inspectionItem").getAsString())));
            this.claimDisplayTime = ConfigHandler.fromJson(obj, "claimDisplayTime", this.claimDisplayTime);
            this.defaultGroups.clear();
            JsonObject defP = ConfigHandler.fromJson(obj, "defaultGroups");
            defP.entrySet().forEach(e -> {
                Map<ClaimPermission, Boolean> perms = new HashMap<>();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        try {
                            perms.put(PermissionRegistry.get(jperm.getKey()), jperm.getValue().getAsBoolean());
                        } catch (NullPointerException ex) {
                            Flan.log("No permission with name {}", jperm.getKey());
                        }
                    });
                }
                this.defaultGroups.put(e.getKey(), perms);
            });
            this.globalDefaultPerms.clear();
            JsonObject glob = ConfigHandler.fromJson(obj, "globalDefaultPerms");
            glob.entrySet().forEach(e -> {
                Map<ClaimPermission, GlobalType> perms = new HashMap<>();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        try {
                            if (jperm.getValue().isJsonPrimitive() && jperm.getValue().getAsJsonPrimitive().isBoolean())
                                perms.put(PermissionRegistry.get(jperm.getKey()), jperm.getValue().getAsBoolean() ? GlobalType.ALLTRUE : GlobalType.ALLFALSE);
                            else
                                perms.put(PermissionRegistry.get(jperm.getKey()), GlobalType.valueOf(jperm.getValue().getAsString()));
                        } catch (NullPointerException ex) {
                            Flan.log("No permission with name {}", jperm.getKey());
                        }
                    });
                }
                this.globalDefaultPerms.put(e.getKey(), perms);
            });
            this.log = ConfigHandler.fromJson(obj, "enableLogs", this.log);
            this.permissionLevel = ConfigHandler.fromJson(obj, "permissionLevel", this.permissionLevel);
            this.sellPrice = ConfigHandler.fromJson(obj, "sellPrice", this.sellPrice);
            this.buyPrice = ConfigHandler.fromJson(obj, "buyPrice", this.buyPrice);
            this.lockDrops = ConfigHandler.fromJson(obj, "lockDrops", this.lockDrops);
            this.dropTicks = ConfigHandler.fromJson(obj, "dropTicks", this.dropTicks);
            this.inactivityTime = ConfigHandler.fromJson(obj, "inactivityTimeDays", this.inactivityTime);
            this.inactivityBlocksMax = ConfigHandler.fromJson(obj, "inactivityBlocksMax", this.inactivityBlocksMax);
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
        JsonArray blocks = new JsonArray();
        this.ignoredBlocks.forEach(blocks::add);
        obj.add("ignoredBlocks", blocks);
        obj.addProperty("lenientBlockEntityCheck", this.lenientBlockEntityCheck);
        JsonArray blocksEntities = new JsonArray();
        this.blockEntityTagIgnore.forEach(blocksEntities::add);
        obj.add("blockEntityTagIgnore", blocksEntities);
        JsonArray entities = new JsonArray();
        this.entityTagIgnore.forEach(entities::add);
        obj.add("entityTagIgnore", entities);

        JsonArray arr = new JsonArray();
        for (String blacklistedWorld : this.blacklistedWorlds)
            arr.add(blacklistedWorld);
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);
        obj.addProperty("claimingItem", Registry.ITEM.getId(this.claimingItem).toString());
        obj.addProperty("inspectionItem", Registry.ITEM.getId(this.inspectionItem).toString());
        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        obj.addProperty("permissionLevel", this.permissionLevel);
        JsonObject defPerm = new JsonObject();
        this.defaultGroups.forEach((key, value) -> {
            JsonObject perm = new JsonObject();
            value.forEach((key1, value1) -> perm.addProperty(key1.id, value1));
            defPerm.add(key, perm);
        });
        obj.add("defaultGroups", defPerm);
        JsonObject global = new JsonObject();
        this.globalDefaultPerms.forEach((key, value) -> {
            JsonObject perm = new JsonObject();
            value.forEach((key1, value1) -> perm.addProperty(key1.id, value1.toString()));
            global.add(key, perm);
        });
        obj.add("globalDefaultPerms", global);
        obj.addProperty("enableLogs", this.log);
        obj.addProperty("sellPrice", this.sellPrice);
        obj.addProperty("buyPrice", this.buyPrice);
        obj.addProperty("lockDrops", this.lockDrops);
        obj.addProperty("dropTicks", this.dropTicks);
        obj.addProperty("inactivityTimeDays", this.inactivityTime);
        obj.addProperty("inactivityBlocksMax", this.inactivityBlocksMax);
        try {
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean globallyDefined(ServerWorld world, ClaimPermission perm) {
        return !getGlobal(world, perm).canModify();
    }

    public GlobalType getGlobal(ServerWorld world, ClaimPermission perm) {
        //Update permission map if not done already
        Map<ClaimPermission, GlobalType> allMap = ConfigHandler.config.globalDefaultPerms.get("*");
        if (allMap != null) {
            world.getServer().getWorlds().forEach(w -> {
                Map<ClaimPermission, GlobalType> wMap = ConfigHandler.config.globalDefaultPerms.getOrDefault(w.getRegistryKey().getValue().toString(), new HashMap<>());
                allMap.forEach((key, value) -> {
                    if (!wMap.containsKey(key))
                        wMap.put(key, value);
                });
                ConfigHandler.config.globalDefaultPerms.put(w.getRegistryKey().getValue().toString(), wMap);
            });
            ConfigHandler.config.globalDefaultPerms.remove("*");
        }

        Map<ClaimPermission, GlobalType> permMap = ConfigHandler.config.globalDefaultPerms.get(world.getRegistryKey().getValue().toString());
        return permMap == null ? GlobalType.NONE : permMap.getOrDefault(perm, GlobalType.NONE);
    }

    public static <V, K> Map<V, K> createHashMap(Consumer<Map<V, K>> cons) {
        Map<V, K> map = new HashMap<>();
        cons.accept(map);
        return map;
    }

    public static <V, K> Map<V, K> createLinkedHashMap(Consumer<Map<V, K>> cons) {
        Map<V, K> map = new LinkedHashMap<>();
        cons.accept(map);
        return map;
    }

    public enum GlobalType {

        ALLTRUE,
        ALLFALSE,
        TRUE,
        FALSE,
        NONE;

        public boolean getValue() {
            return this == ALLTRUE || this == TRUE;
        }

        public boolean canModify() {
            return this.ordinal() > 1;
        }
    }
}
