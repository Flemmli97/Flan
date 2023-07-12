package io.github.flemmli97.flan.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Config {

    private File config;

    public int startingBlocks = 500;
    public int maxClaimBlocks = 5000;
    public int ticksForNextBlock = 600;
    public int minClaimsize = 100;
    public int defaultClaimDepth = 10;
    public int maxClaims = -1;
    public String defaultClaimName = "";
    public String defaultEnterMessage = "";
    public String defaultLeaveMessage = "";

    public String[] blacklistedWorlds = new String[0];
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public Item inspectionItem = Items.STICK;

    public int claimDisplayTime = 1000;
    public boolean claimDisplayActionBar = false;
    public int permissionLevel = 2;

    public boolean autoClaimStructures;

    public BuySellHandler buySellHandler = new BuySellHandler();
    public int maxBuyBlocks = -1;

    public boolean lenientBlockEntityCheck;
    public List<String> breakBlockBlacklist = Lists.newArrayList(
            "universal_graves:grave",
            "yigd:grave"
    );
    public List<String> interactBlockBlacklist = Lists.newArrayList(
            "universal_graves:grave",
            "yigd:grave",
            "waystones",
            "universal_shops:trade_block"
    );

    public List<String> breakBETagBlacklist = Lists.newArrayList(
    );
    public List<String> interactBETagBlacklist = Lists.newArrayList(
            "IsDeathChest", //vanilla death chest
            "gunpowder.owner", //gunpowder
            "shop-activated" //dicemc-money
    );

    public List<String> ignoredEntityTypes = Lists.newArrayList(
            "corpse:corpse"
    );
    public List<String> entityTagIgnore = Lists.newArrayList(
            "graves.marker" //vanilla tweaks
    );

    public List<String> itemPermission = Lists.newArrayList(
            "@c:wrenches-INTERACTBLOCK",
            "appliedenergistics2:nether_quartz_wrench-INTERACTBLOCK",
            "appliedenergistics2:certus_quartz_wrench-INTERACTBLOCK"
    );
    public List<String> blockPermission = Lists.newArrayList(
    );
    public List<String> entityPermission = Lists.newArrayList(
            "taterzen:npc-TRADING"
    );

    public List<String> leftClickBlockPermission = Lists.newArrayList(
            "@storagedrawers:drawers-OPENCONTAINER",
            "mekanism:basic_bin-OPENCONTAINER",
            "mekanism:advanced_bin-OPENCONTAINER",
            "mekanism:ultimate_bin-OPENCONTAINER",
            "mekanism:creative_bin-OPENCONTAINER"
    );

    public int dropTicks = 6000;

    public int inactivityTime = 30;
    public int inactivityBlocksMax = 2000;
    public boolean deletePlayerFile = false;
    public int bannedDeletionTime = 30;

    public int offlineProtectActivation = -1;

    public boolean log;

    public String lang = "en_us";

    public int configVersion = 4;
    public int preConfigVersion;

    public boolean ftbChunksCheck = true;
    public boolean gomlReservedCheck = true;
    public boolean mineColoniesCheck = true;

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

    protected final Map<String, Map<ClaimPermission, GlobalType>> globalDefaultPerms = createHashMap(map -> map.put("*", createHashMap(perms -> {
        perms.put(PermissionRegistry.FLIGHT, GlobalType.ALLTRUE);
        perms.put(PermissionRegistry.MOBSPAWN, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.TELEPORT, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.NOHUNGER, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.EDITPOTIONS, GlobalType.ALLFALSE);
        perms.put(PermissionRegistry.LOCKITEMS, GlobalType.ALLTRUE);
    })));

    public Config(MinecraftServer server) {
        File configDir = CrossPlatformStuff.INSTANCE.configPath().resolve("flan").toFile();
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
            this.preConfigVersion = ConfigHandler.fromJson(obj, "configVersion", 0);
            this.lang = ConfigHandler.fromJson(obj, "lang", this.lang);
            this.startingBlocks = ConfigHandler.fromJson(obj, "startingBlocks", this.startingBlocks);
            this.maxClaimBlocks = ConfigHandler.fromJson(obj, "maxClaimBlocks", this.maxClaimBlocks);
            this.ticksForNextBlock = ConfigHandler.fromJson(obj, "ticksForNextBlock", this.ticksForNextBlock);
            this.minClaimsize = ConfigHandler.fromJson(obj, "minClaimsize", this.minClaimsize);
            this.defaultClaimDepth = ConfigHandler.fromJson(obj, "defaultClaimDepth", this.defaultClaimDepth);
            this.maxClaims = ConfigHandler.fromJson(obj, "maxClaims", this.maxClaims);
            this.defaultClaimName = ConfigHandler.fromJson(obj, "defaultClaimName", this.defaultClaimName);
            this.defaultEnterMessage = ConfigHandler.fromJson(obj, "defaultEnterMessage", this.defaultEnterMessage);
            this.defaultLeaveMessage = ConfigHandler.fromJson(obj, "defaultLeaveMessage", this.defaultLeaveMessage);

            JsonArray arr = ConfigHandler.arryFromJson(obj, "blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = ConfigHandler.fromJson(obj, "worldWhitelist", this.worldWhitelist);

            if (obj.has("claimingItem"))
                this.claimingItem = CrossPlatformStuff.INSTANCE.registryItems().getFromId(new ResourceLocation((obj.get("claimingItem").getAsString())));
            if (obj.has("inspectionItem"))
                this.inspectionItem = CrossPlatformStuff.INSTANCE.registryItems().getFromId(new ResourceLocation((obj.get("inspectionItem").getAsString())));
            this.claimDisplayTime = ConfigHandler.fromJson(obj, "claimDisplayTime", this.claimDisplayTime);
            this.claimDisplayActionBar = ConfigHandler.fromJson(obj, "claimDisplayActionBar", this.claimDisplayActionBar);
            this.permissionLevel = ConfigHandler.fromJson(obj, "permissionLevel", this.permissionLevel);

            this.autoClaimStructures = ConfigHandler.fromJson(obj, "autoClaimStructures", this.autoClaimStructures);

            this.ftbChunksCheck = ConfigHandler.fromJson(obj, "ftbChunksCheck", this.ftbChunksCheck);
            this.gomlReservedCheck = ConfigHandler.fromJson(obj, "gomlReservedCheck", this.gomlReservedCheck);
            this.mineColoniesCheck = ConfigHandler.fromJson(obj, "mineColoniesCheck", this.mineColoniesCheck);

            this.buySellHandler.fromJson(ConfigHandler.fromJson(obj, "buySellHandler"));
            this.maxBuyBlocks = ConfigHandler.fromJson(obj, "maxBuyBlocks", this.maxBuyBlocks);

            this.lenientBlockEntityCheck = ConfigHandler.fromJson(obj, "lenientBlockEntityCheck", this.lenientBlockEntityCheck);
            this.breakBlockBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "breakBlockBlacklist").forEach(e -> this.breakBlockBlacklist.add(e.getAsString()));
            this.interactBlockBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "interactBlockBlacklist").forEach(e -> this.interactBlockBlacklist.add(e.getAsString()));
            this.breakBETagBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "breakBlockEntityTagBlacklist").forEach(e -> this.breakBETagBlacklist.add(e.getAsString()));
            this.interactBETagBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "interactBlockEntityTagBlacklist").forEach(e -> this.interactBETagBlacklist.add(e.getAsString()));
            this.ignoredEntityTypes.clear();
            ConfigHandler.arryFromJson(obj, "ignoredEntities").forEach(e -> this.ignoredEntityTypes.add(e.getAsString()));
            this.entityTagIgnore.clear();
            ConfigHandler.arryFromJson(obj, "entityTagIgnore").forEach(e -> this.entityTagIgnore.add(e.getAsString()));

            this.itemPermission.clear();
            ConfigHandler.arryFromJson(obj, "customItemPermission").forEach(e -> this.itemPermission.add(e.getAsString()));
            this.blockPermission.clear();
            ConfigHandler.arryFromJson(obj, "customBlockPermission").forEach(e -> this.blockPermission.add(e.getAsString()));
            this.entityPermission.clear();
            ConfigHandler.arryFromJson(obj, "customEntityPermission").forEach(e -> this.entityPermission.add(e.getAsString()));
            this.leftClickBlockPermission.clear();
            ConfigHandler.arryFromJson(obj, "leftClickBlockPermission").forEach(e -> this.leftClickBlockPermission.add(e.getAsString()));

            this.dropTicks = ConfigHandler.fromJson(obj, "dropTicks", this.dropTicks);
            this.inactivityTime = ConfigHandler.fromJson(obj, "inactivityTimeDays", this.inactivityTime);
            this.inactivityBlocksMax = ConfigHandler.fromJson(obj, "inactivityBlocksMax", this.inactivityBlocksMax);
            this.deletePlayerFile = ConfigHandler.fromJson(obj, "deletePlayerFile", this.deletePlayerFile);
            this.bannedDeletionTime = ConfigHandler.fromJson(obj, "bannedDeletionTime", this.bannedDeletionTime);
            this.offlineProtectActivation = ConfigHandler.fromJson(obj, "offlineProtectActivation", this.offlineProtectActivation);
            this.log = ConfigHandler.fromJson(obj, "enableLogs", this.log);

            this.defaultGroups.clear();
            JsonObject defP = ConfigHandler.fromJson(obj, "defaultGroups");
            defP.entrySet().forEach(e -> {
                Map<ClaimPermission, Boolean> perms = new HashMap<>();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        try {
                            perms.put(PermissionRegistry.get(jperm.getKey()), jperm.getValue().getAsBoolean());
                        } catch (NullPointerException ex) {
                            Flan.error("No permission with name {}", jperm.getKey());
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
                            Flan.error("No permission with name {}", jperm.getKey());
                        }
                    });
                }
                this.globalDefaultPerms.put(e.getKey(), perms);
            });
            ConfigUpdater.updateConfig(this.preConfigVersion, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.save();
    }

    private void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("__comment", "For help with the config refer to https://github.com/Flemmli97/Flan/wiki/Config");
        obj.addProperty("configVersion", this.configVersion);
        obj.addProperty("lang", this.lang);
        obj.addProperty("startingBlocks", this.startingBlocks);
        obj.addProperty("maxClaimBlocks", this.maxClaimBlocks);
        obj.addProperty("ticksForNextBlock", this.ticksForNextBlock);
        obj.addProperty("minClaimsize", this.minClaimsize);
        obj.addProperty("defaultClaimDepth", this.defaultClaimDepth);
        obj.addProperty("maxClaims", this.maxClaims);
        obj.addProperty("defaultClaimName", this.defaultClaimName);
        obj.addProperty("defaultEnterMessage", this.defaultEnterMessage);
        obj.addProperty("defaultLeaveMessage", this.defaultLeaveMessage);

        JsonArray arr = new JsonArray();
        for (String blacklistedWorld : this.blacklistedWorlds)
            arr.add(blacklistedWorld);
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);

        obj.addProperty("claimingItem", CrossPlatformStuff.INSTANCE.registryItems().getIDFrom(this.claimingItem).toString());
        obj.addProperty("inspectionItem", CrossPlatformStuff.INSTANCE.registryItems().getIDFrom(this.inspectionItem).toString());
        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        obj.addProperty("claimDisplayActionBar", this.claimDisplayActionBar);
        obj.addProperty("permissionLevel", this.permissionLevel);

        obj.addProperty("autoClaimStructures", this.autoClaimStructures);

        obj.addProperty("ftbChunksCheck", this.ftbChunksCheck);
        obj.addProperty("gomlReservedCheck", this.gomlReservedCheck);
        obj.addProperty("mineColoniesCheck", this.mineColoniesCheck);

        obj.add("buySellHandler", this.buySellHandler.toJson());
        obj.addProperty("maxBuyBlocks", this.maxBuyBlocks);

        obj.addProperty("lenientBlockEntityCheck", this.lenientBlockEntityCheck);
        JsonArray blocksBreak = new JsonArray();
        this.breakBlockBlacklist.forEach(blocksBreak::add);
        obj.add("breakBlockBlacklist", blocksBreak);
        JsonArray blocksInteract = new JsonArray();
        this.interactBlockBlacklist.forEach(blocksInteract::add);
        obj.add("interactBlockBlacklist", blocksInteract);
        JsonArray blocksEntities = new JsonArray();
        this.breakBETagBlacklist.forEach(blocksEntities::add);
        obj.add("breakBlockEntityTagBlacklist", blocksEntities);
        JsonArray blocksEntitiesInteract = new JsonArray();
        this.interactBETagBlacklist.forEach(blocksEntitiesInteract::add);
        obj.add("interactBlockEntityTagBlacklist", blocksEntitiesInteract);
        JsonArray entities = new JsonArray();
        this.ignoredEntityTypes.forEach(entities::add);
        obj.add("ignoredEntities", entities);
        JsonArray entitiesTags = new JsonArray();
        this.entityTagIgnore.forEach(entitiesTags::add);
        obj.add("entityTagIgnore", entitiesTags);

        JsonArray itemPerms = new JsonArray();
        this.itemPermission.forEach(itemPerms::add);
        obj.add("customItemPermission", itemPerms);
        JsonArray blockPerms = new JsonArray();
        this.blockPermission.forEach(blockPerms::add);
        obj.add("customBlockPermission", blockPerms);
        this.entityPermission.forEach(blockPerms::add);
        obj.add("customEntityPermission", blockPerms);
        JsonArray leftIgnore = new JsonArray();
        this.leftClickBlockPermission.forEach(leftIgnore::add);
        obj.add("leftClickBlockPermission", leftIgnore);

        obj.addProperty("dropTicks", this.dropTicks);
        obj.addProperty("inactivityTimeDays", this.inactivityTime);
        obj.addProperty("inactivityBlocksMax", this.inactivityBlocksMax);
        obj.addProperty("deletePlayerFile", this.deletePlayerFile);
        obj.addProperty("bannedDeletionTime", this.bannedDeletionTime);
        obj.addProperty("offlineProtectActivation", this.offlineProtectActivation);
        obj.addProperty("enableLogs", this.log);

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
        try {
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean globallyDefined(ServerLevel world, ClaimPermission perm) {
        return !this.getGlobal(world, perm).canModify();
    }

    public GlobalType getGlobal(ServerLevel world, ClaimPermission perm) {
        //Update permission map if not done already
        Map<ClaimPermission, GlobalType> allMap = ConfigHandler.config.globalDefaultPerms.get("*");
        if (allMap != null) {
            world.getServer().getAllLevels().forEach(w -> {
                Map<ClaimPermission, GlobalType> wMap = ConfigHandler.config.globalDefaultPerms.getOrDefault(w.dimension().location().toString(), new HashMap<>());
                allMap.forEach((key, value) -> {
                    if (!wMap.containsKey(key))
                        wMap.put(key, value);
                });
                ConfigHandler.config.globalDefaultPerms.put(w.dimension().location().toString(), wMap);
            });
            ConfigHandler.config.globalDefaultPerms.remove("*");
        }

        Map<ClaimPermission, GlobalType> permMap = ConfigHandler.config.globalDefaultPerms.get(world.dimension().location().toString());
        return permMap == null ? GlobalType.NONE : permMap.getOrDefault(perm, GlobalType.NONE);
    }

    public Stream<Map.Entry<ClaimPermission, GlobalType>> getGloballyDefinedVals(ServerLevel world) {
        Map<ClaimPermission, GlobalType> allMap = ConfigHandler.config.globalDefaultPerms.get("*");
        if (allMap != null) {
            world.getServer().getAllLevels().forEach(w -> {
                Map<ClaimPermission, GlobalType> wMap = ConfigHandler.config.globalDefaultPerms.getOrDefault(w.dimension().location().toString(), new HashMap<>());
                allMap.forEach((key, value) -> {
                    if (!wMap.containsKey(key))
                        wMap.put(key, value);
                });
                ConfigHandler.config.globalDefaultPerms.put(w.dimension().location().toString(), wMap);
            });
            ConfigHandler.config.globalDefaultPerms.remove("*");
        }
        Map<ClaimPermission, GlobalType> permMap = ConfigHandler.config.globalDefaultPerms.get(world.dimension().location().toString());
        return permMap == null ? Stream.empty() : permMap.entrySet().stream().filter(e -> e.getValue().canModify());
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
