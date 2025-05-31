package io.github.flemmli97.flan.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
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
    public boolean noSpawnClaim;
    public int claimingCooldown;

    public String[] blacklistedWorlds = new String[0];
    public boolean worldWhitelist;

    public Item claimingItem = Items.GOLDEN_HOE;
    public CompoundTag claimingNBT = new CompoundTag();
    public Item inspectionItem = Items.STICK;
    public CompoundTag inspectionNBT = new CompoundTag();
    public boolean main3dClaims = true;
    public int minHeight3d = 10;
    public int nearbyClaimsToolDisplay = 24;

    public int claimDisplayTime = 600;
    public boolean particleDisplay = false;
    public boolean claimDisplayActionBar = false;
    public int permissionLevel = 2;

    public boolean autoClaimStructures;

    public boolean ftbChunksCheck = true;
    public boolean gomlReservedCheck = true;
    public boolean mineColoniesCheck = true;

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

    public List<String> breakBlockEntityTagBlacklist = Lists.newArrayList(
    );
    public List<String> interactBlockEntityTagBlacklist = Lists.newArrayList(
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

    private List<String> legacyOverrides = Lists.newArrayList(
            "@c:wrenches-flan:interact_block",
            "appliedenergistics2:nether_quartz_wrench-flan:interact_block",
            "appliedenergistics2:certus_quartz_wrench-flan:interact_block"
    );

    public int dropTicks = 6000;

    public int inactivityTime = -1;
    public int inactivityBlocksMax = 2000;
    public boolean deletePlayerFile = false;
    public int bannedDeletionTime = 30;

    public int offlineProtectActivation = -1;

    public boolean log;

    public int configVersion = 6;
    public int preConfigVersion;

    public Map<String, Map<ResourceLocation, Boolean>> defaultGroups = createHashMap(map -> {
        map.put("Co-Owner", createLinkedHashMap(perms -> PermissionManager.getInstance().getAll().forEach(p -> perms.put(p.getId(), true))));
        map.put("Visitor", createLinkedHashMap(perms -> {
            perms.put(BuiltinPermission.BED, true);
            perms.put(BuiltinPermission.DOOR, true);
            perms.put(BuiltinPermission.FENCEGATE, true);
            perms.put(BuiltinPermission.TRAPDOOR, true);
            perms.put(BuiltinPermission.BUTTONLEVER, true);
            perms.put(BuiltinPermission.PRESSUREPLATE, true);
            perms.put(BuiltinPermission.ENDERCHEST, true);
            perms.put(BuiltinPermission.ENCHANTMENTTABLE, true);
            perms.put(BuiltinPermission.ITEMFRAMEROTATE, true);
            perms.put(BuiltinPermission.PORTAL, true);
            perms.put(BuiltinPermission.TRADING, true);
        }));
    });

    protected final Map<String, Map<ResourceLocation, GlobalType>> globalDefaultPerms = createHashMap(map -> map.put("*", createHashMap(perms -> {
        perms.put(BuiltinPermission.ALLOW_FLIGHT, GlobalType.ALLTRUE);
        perms.put(BuiltinPermission.MAY_FLIGHT, GlobalType.ALLFALSE);
        perms.put(BuiltinPermission.MOBSPAWN, GlobalType.ALLFALSE);
        perms.put(BuiltinPermission.TELEPORT, GlobalType.ALLFALSE);
        perms.put(BuiltinPermission.NOHUNGER, GlobalType.ALLFALSE);
        perms.put(BuiltinPermission.EDITPOTIONS, GlobalType.ALLFALSE);
        perms.put(BuiltinPermission.LOCKITEMS, GlobalType.ALLTRUE);
    })));

    public Config() {
        File configDir = CrossPlatformStuff.INSTANCE.configPath().resolve("flan").toFile();
        if (!configDir.exists())
            configDir.mkdirs();
        this.config = new File(configDir, "flan_config.json");
    }

    public void load(MinecraftServer server) {
        if (!this.config.exists()) {
            try {
                this.config.createNewFile();
            } catch (IOException e) {
                Flan.LOGGER.error(e);
            }
            this.save(server);
        }
        try {
            FileReader reader = new FileReader(this.config);
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            this.preConfigVersion = ConfigHandler.fromJson(obj, "configVersion", 0);
            obj = ConfigUpdater.updateConfig(this.preConfigVersion, obj);
            this.startingBlocks = ConfigHandler.fromJson(obj, "startingBlocks", this.startingBlocks);
            this.maxClaimBlocks = ConfigHandler.fromJson(obj, "maxClaimBlocks", this.maxClaimBlocks);
            this.ticksForNextBlock = ConfigHandler.fromJson(obj, "ticksForNextBlock", this.ticksForNextBlock);
            this.minClaimsize = ConfigHandler.fromJson(obj, "minClaimsize", this.minClaimsize);
            this.defaultClaimDepth = ConfigHandler.fromJson(obj, "defaultClaimDepth", this.defaultClaimDepth);
            this.maxClaims = ConfigHandler.fromJson(obj, "maxClaims", this.maxClaims);
            this.defaultClaimName = ConfigHandler.fromJson(obj, "defaultClaimName", this.defaultClaimName);
            this.defaultEnterMessage = ConfigHandler.fromJson(obj, "defaultEnterMessage", this.defaultEnterMessage);
            this.defaultLeaveMessage = ConfigHandler.fromJson(obj, "defaultLeaveMessage", this.defaultLeaveMessage);
            this.noSpawnClaim = ConfigHandler.fromJson(obj, "noSpawnClaim", this.noSpawnClaim);
            this.claimingCooldown = ConfigHandler.fromJson(obj, "claimingCooldown", this.claimingCooldown);

            JsonArray arr = ConfigHandler.arryFromJson(obj, "blacklistedWorlds");
            this.blacklistedWorlds = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++)
                this.blacklistedWorlds[i] = arr.get(i).getAsString();
            this.worldWhitelist = ConfigHandler.fromJson(obj, "worldWhitelist", this.worldWhitelist);

            if (obj.has("claimingItem"))
                this.claimingItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse((obj.get("claimingItem").getAsString())));
            this.claimingNBT = CompoundTag.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(obj, "claimingNBT", new JsonObject()))
                    .getOrThrow();
            if (obj.has("inspectionItem"))
                this.inspectionItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse((obj.get("inspectionItem").getAsString())));
            this.inspectionNBT = CompoundTag.CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(obj, "inspectionNBT", new JsonObject()))
                    .getOrThrow();
            this.main3dClaims = ConfigHandler.fromJson(obj, "main3dClaims", this.main3dClaims);
            this.minHeight3d = ConfigHandler.fromJson(obj, "minHeight3d", this.minHeight3d);
            this.nearbyClaimsToolDisplay = ConfigHandler.fromJson(obj, "nearbyClaimsToolDisplay", this.nearbyClaimsToolDisplay);

            this.claimDisplayTime = ConfigHandler.fromJson(obj, "claimDisplayTime", this.claimDisplayTime);
            this.particleDisplay = ConfigHandler.fromJson(obj, "particleDisplay", this.particleDisplay);
            this.claimDisplayActionBar = ConfigHandler.fromJson(obj, "claimDisplayActionBar", this.claimDisplayActionBar);
            this.permissionLevel = ConfigHandler.fromJson(obj, "permissionLevel", this.permissionLevel);

            this.autoClaimStructures = ConfigHandler.fromJson(obj, "autoClaimStructures", this.autoClaimStructures);

            this.ftbChunksCheck = ConfigHandler.fromJson(obj, "ftbChunksCheck", this.ftbChunksCheck);
            this.gomlReservedCheck = ConfigHandler.fromJson(obj, "gomlReservedCheck", this.gomlReservedCheck);
            this.mineColoniesCheck = ConfigHandler.fromJson(obj, "mineColoniesCheck", this.mineColoniesCheck);

            this.buySellHandler.fromJson(ConfigHandler.fromJson(obj, "buySellHandler"), server);
            this.maxBuyBlocks = ConfigHandler.fromJson(obj, "maxBuyBlocks", this.maxBuyBlocks);

            this.lenientBlockEntityCheck = ConfigHandler.fromJson(obj, "lenientBlockEntityCheck", this.lenientBlockEntityCheck);
            this.breakBlockBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "breakBlockBlacklist").forEach(e -> this.breakBlockBlacklist.add(e.getAsString()));
            this.interactBlockBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "interactBlockBlacklist").forEach(e -> this.interactBlockBlacklist.add(e.getAsString()));
            this.breakBlockEntityTagBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "breakBlockEntityTagBlacklist").forEach(e -> this.breakBlockEntityTagBlacklist.add(e.getAsString()));
            this.interactBlockEntityTagBlacklist.clear();
            ConfigHandler.arryFromJson(obj, "interactBlockEntityTagBlacklist").forEach(e -> this.interactBlockEntityTagBlacklist.add(e.getAsString()));
            this.ignoredEntityTypes.clear();
            ConfigHandler.arryFromJson(obj, "ignoredEntities").forEach(e -> this.ignoredEntityTypes.add(e.getAsString()));
            this.entityTagIgnore.clear();
            ConfigHandler.arryFromJson(obj, "entityTagIgnore").forEach(e -> this.entityTagIgnore.add(e.getAsString()));

            this.legacyOverrides.clear();
            ConfigHandler.arryFromJson(obj, "legacyOverrides").forEach(e -> this.legacyOverrides.add(e.getAsString()));
            ConfigHandler.arryFromJson(obj, "customItemPermission").forEach(e -> this.legacyOverrides.add(e.getAsString()));
            ConfigHandler.arryFromJson(obj, "customBlockPermission").forEach(e -> this.legacyOverrides.add(e.getAsString()));
            ConfigHandler.arryFromJson(obj, "customEntityPermission").forEach(e -> this.legacyOverrides.add(e.getAsString()));
            ConfigHandler.arryFromJson(obj, "leftClickBlockPermission").forEach(e -> this.legacyOverrides.add(e.getAsString()));

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
                Map<ResourceLocation, Boolean> perms = new HashMap<>();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        ResourceLocation id = BuiltinPermission.tryLegacy(jperm.getKey());
                        ClaimPermission perm = PermissionManager.getInstance().get(id);
                        if (perm == null)
                            Flan.error("Default groups: No such permission for {}", jperm.getKey());
                        else
                            perms.put(id, jperm.getValue().getAsBoolean());
                    });
                }
                this.defaultGroups.put(e.getKey(), perms);
            });
            this.globalDefaultPerms.clear();
            JsonObject glob = ConfigHandler.fromJson(obj, "globalDefaultPerms");
            glob.entrySet().forEach(e -> {
                Map<ResourceLocation, GlobalType> perms = new HashMap<>();
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(jperm -> {
                        ResourceLocation id = BuiltinPermission.tryLegacy(jperm.getKey());
                        ClaimPermission perm = PermissionManager.getInstance().get(id);
                        if (perm == null)
                            Flan.error("Global Perms: No such permission for {}", jperm.getKey());
                        if (jperm.getValue().isJsonPrimitive() && jperm.getValue().getAsJsonPrimitive().isBoolean())
                            perms.put(id, jperm.getValue().getAsBoolean() ? GlobalType.ALLTRUE : GlobalType.ALLFALSE);
                        else
                            perms.put(id, GlobalType.valueOf(jperm.getValue().getAsString()));
                    });
                }
                this.globalDefaultPerms.put(e.getKey(), perms);
            });
            ConfigUpdater.postUpdateConfig(this.preConfigVersion, this);
        } catch (IOException e) {
            Flan.LOGGER.error(e);
        }
        this.save(server);
    }

    private void save(MinecraftServer server) {
        JsonObject obj = new JsonObject();
        obj.addProperty("__comment", "For help with the config refer to https://github.com/Flemmli97/Flan/wiki/Config");
        obj.addProperty("configVersion", this.configVersion);
        obj.addProperty("startingBlocks", this.startingBlocks);
        obj.addProperty("maxClaimBlocks", this.maxClaimBlocks);
        obj.addProperty("ticksForNextBlock", this.ticksForNextBlock);
        obj.addProperty("minClaimsize", this.minClaimsize);
        obj.addProperty("defaultClaimDepth", this.defaultClaimDepth);
        obj.addProperty("maxClaims", this.maxClaims);
        obj.addProperty("defaultClaimName", this.defaultClaimName);
        obj.addProperty("defaultEnterMessage", this.defaultEnterMessage);
        obj.addProperty("defaultLeaveMessage", this.defaultLeaveMessage);
        obj.addProperty("noSpawnClaim", this.noSpawnClaim);
        obj.addProperty("claimingCooldown", this.claimingCooldown);

        JsonArray arr = new JsonArray();
        for (String blacklistedWorld : this.blacklistedWorlds)
            arr.add(blacklistedWorld);
        obj.add("blacklistedWorlds", arr);
        obj.addProperty("worldWhitelist", this.worldWhitelist);

        obj.addProperty("claimingItem", BuiltInRegistries.ITEM.getKey(this.claimingItem).toString());
        obj.add("claimingNBT", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, this.claimingNBT)
                .getOrThrow());
        obj.addProperty("inspectionItem", BuiltInRegistries.ITEM.getKey(this.inspectionItem).toString());
        obj.add("inspectionNBT", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, this.inspectionNBT)
                .getOrThrow());
        obj.addProperty("main3dClaims", this.main3dClaims);
        obj.addProperty("minHeight3d", this.minHeight3d);
        obj.addProperty("nearbyClaimsToolDisplay", this.nearbyClaimsToolDisplay);

        obj.addProperty("claimDisplayTime", this.claimDisplayTime);
        obj.addProperty("particleDisplay", this.particleDisplay);
        obj.addProperty("claimDisplayActionBar", this.claimDisplayActionBar);
        obj.addProperty("permissionLevel", this.permissionLevel);

        obj.addProperty("autoClaimStructures", this.autoClaimStructures);

        obj.addProperty("ftbChunksCheck", this.ftbChunksCheck);
        obj.addProperty("gomlReservedCheck", this.gomlReservedCheck);
        obj.addProperty("mineColoniesCheck", this.mineColoniesCheck);

        obj.add("buySellHandler", this.buySellHandler.toJson(server));
        obj.addProperty("maxBuyBlocks", this.maxBuyBlocks);

        obj.addProperty("lenientBlockEntityCheck", this.lenientBlockEntityCheck);
        JsonArray blocksBreak = new JsonArray();
        this.breakBlockBlacklist.forEach(blocksBreak::add);
        obj.add("breakBlockBlacklist", blocksBreak);
        JsonArray blocksInteract = new JsonArray();
        this.interactBlockBlacklist.forEach(blocksInteract::add);
        obj.add("interactBlockBlacklist", blocksInteract);
        JsonArray blocksEntities = new JsonArray();
        this.breakBlockEntityTagBlacklist.forEach(blocksEntities::add);
        obj.add("breakBlockEntityTagBlacklist", blocksEntities);
        JsonArray blocksEntitiesInteract = new JsonArray();
        this.interactBlockEntityTagBlacklist.forEach(blocksEntitiesInteract::add);
        obj.add("interactBlockEntityTagBlacklist", blocksEntitiesInteract);
        JsonArray entities = new JsonArray();
        this.ignoredEntityTypes.forEach(entities::add);
        obj.add("ignoredEntities", entities);
        JsonArray entitiesTags = new JsonArray();
        this.entityTagIgnore.forEach(entitiesTags::add);
        obj.add("entityTagIgnore", entitiesTags);

        JsonArray overrides = new JsonArray();
        this.legacyOverrides.forEach(overrides::add);
        obj.add("legacyOverrides", overrides);

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
            value.forEach((key1, value1) -> perm.addProperty(key1.toString(), value1));
            defPerm.add(key, perm);
        });
        obj.add("defaultGroups", defPerm);
        JsonObject global = new JsonObject();
        this.globalDefaultPerms.forEach((key, value) -> {
            JsonObject perm = new JsonObject();
            value.forEach((key1, value1) -> perm.addProperty(key1.toString(), value1.toString()));
            global.add(key, perm);
        });
        obj.add("globalDefaultPerms", global);
        try {
            FileWriter writer = new FileWriter(this.config);
            ConfigHandler.GSON.toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            Flan.LOGGER.error(e);
        }
    }

    public boolean globallyDefined(ServerLevel world, ResourceLocation perm) {
        return !this.getGlobal(world, perm).canModify();
    }

    public GlobalType getGlobal(ServerLevel world, ResourceLocation perm) {
        //Update permission map if not done already
        Map<ResourceLocation, GlobalType> allMap = ConfigHandler.CONFIG.globalDefaultPerms.get("*");
        if (allMap != null) {
            world.getServer().getAllLevels().forEach(w -> {
                Map<ResourceLocation, GlobalType> wMap = ConfigHandler.CONFIG.globalDefaultPerms.getOrDefault(w.dimension().location().toString(), new HashMap<>());
                allMap.forEach((key, value) -> {
                    if (!wMap.containsKey(key))
                        wMap.put(key, value);
                });
                ConfigHandler.CONFIG.globalDefaultPerms.put(w.dimension().location().toString(), wMap);
            });
            ConfigHandler.CONFIG.globalDefaultPerms.remove("*");
        }

        Map<ResourceLocation, GlobalType> permMap = ConfigHandler.CONFIG.globalDefaultPerms.get(world.dimension().location().toString());
        return permMap == null ? GlobalType.NONE : permMap.getOrDefault(perm, GlobalType.NONE);
    }

    public Stream<Map.Entry<ResourceLocation, GlobalType>> getGloballyDefinedVals(ServerLevel world) {
        Map<ResourceLocation, GlobalType> allMap = ConfigHandler.CONFIG.globalDefaultPerms.get("*");
        if (allMap != null) {
            world.getServer().getAllLevels().forEach(w -> {
                Map<ResourceLocation, GlobalType> wMap = ConfigHandler.CONFIG.globalDefaultPerms.getOrDefault(w.dimension().location().toString(), new HashMap<>());
                allMap.forEach((key, value) -> {
                    if (!wMap.containsKey(key))
                        wMap.put(key, value);
                });
                ConfigHandler.CONFIG.globalDefaultPerms.put(w.dimension().location().toString(), wMap);
            });
            ConfigHandler.CONFIG.globalDefaultPerms.remove("*");
        }
        Map<ResourceLocation, GlobalType> permMap = ConfigHandler.CONFIG.globalDefaultPerms.get(world.dimension().location().toString());
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
