package io.github.flemmli97.flan.claim;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPermissionStorage;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.dynmap.DynmapCalls;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.EnumDisplayType;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.PlayerDataHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimStorage implements IPermissionStorage {

    public static final String adminClaimString = "!AdminClaims";
    private final Long2ObjectMap<List<Claim>> claims = new Long2ObjectOpenHashMap<>();
    private final Map<UUID, Claim> claimUUIDMap = new HashMap<>();
    private final Map<UUID, Set<Claim>> playerClaimMap = new HashMap<>();
    private final Set<UUID> dirty = new HashSet<>();
    private final GlobalClaim globalClaim;

    public static ClaimStorage get(ServerLevel world) {
        return ((IClaimStorage) world).get();
    }

    public ClaimStorage(MinecraftServer server, ServerLevel world) {
        this.globalClaim = new GlobalClaim(world);
        this.read(server, world);
        PlayerDataHandler.deleteUnusedClaims(server, this, world);
    }

    public UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        if (this.claimUUIDMap.containsKey(uuid))
            return this.generateUUID();
        return uuid;
    }

    public boolean createClaim(BlockPos pos1, BlockPos pos2, ServerPlayer player) {
        Claim claim = new Claim(pos1.below(ConfigHandler.config.defaultClaimDepth), pos2.below(ConfigHandler.config.defaultClaimDepth), player);
        Set<Claim> conflicts = this.conflicts(claim, null);
        if (conflicts.isEmpty()) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (claim.getPlane() < ConfigHandler.config.minClaimsize) {
                player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("minClaimSize"), ConfigHandler.config.minClaimsize), ChatFormatting.RED), false);
                return false;
            }
            if (!data.isAdminIgnoreClaim() && ConfigHandler.config.maxClaims != -1 && !PermissionNodeHandler.INSTANCE.permBelowEqVal(player, PermissionNodeHandler.permMaxClaims, this.playerClaimMap.getOrDefault(player.getUUID(), Sets.newHashSet()).size() + 1, ConfigHandler.config.maxClaims)) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("maxClaims"), ChatFormatting.RED), false);
                return false;
            }
            if (!data.isAdminIgnoreClaim() && !data.canUseClaimBlocks(claim.getPlane())) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("notEnoughBlocks"), ChatFormatting.RED), false);
                return false;
            }
            claim.setClaimID(this.generateUUID());
            Flan.log("Creating new claim {}", claim);
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            data.updateScoreboard();
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimCreateSuccess"), ChatFormatting.GOLD), false);
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                    data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks()), ChatFormatting.GOLD), false);
            return true;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        conflicts.forEach(conf -> data.addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.blockPosition().getY()));
        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("conflictOther"), ChatFormatting.RED), false);
        return false;
    }

    private Set<Claim> conflicts(Claim claim, Claim except) {
        Set<Claim> conflicted = new HashSet<>();
        int[] chunks = getChunkPos(claim);
        for (int x = chunks[0]; x <= chunks[1]; x++)
            for (int z = chunks[2]; z <= chunks[3]; z++) {
                List<Claim> claims = this.claims.get(ChunkPos.asLong(x, z));
                if (claims != null)
                    for (Claim other : claims) {
                        if (claim.intersects(other) && !other.equals(except)) {
                            conflicted.add(other);
                        }
                    }
            }
        return conflicted;
    }

    public boolean deleteClaim(Claim claim, boolean updateClaim, EnumEditMode mode, ServerLevel world) {
        if (mode == EnumEditMode.SUBCLAIM) {
            if (claim.parentClaim() != null)
                return claim.parentClaim().deleteSubClaim(claim);
            return false;
        }
        Flan.log("Try deleting claim {}", claim);
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                this.claims.compute(ChunkPos.asLong(x, z), (key, val) -> {
                    if (val == null)
                        return null;
                    val.remove(claim);
                    return val.isEmpty() ? null : val;
                });
            }
        this.playerClaimMap.getOrDefault(claim.getOwner(), new HashSet<>()).remove(claim);
        this.dirty.add(claim.getOwner());
        if (updateClaim) {
            claim.remove();
            claim.getOwnerPlayer().ifPresent(o -> PlayerClaimData.get(o).updateScoreboard());
        }
        DynmapCalls.removeMarker(claim);
        return this.claimUUIDMap.remove(claim.getClaimID()) != null;
    }

    public void toggleAdminClaim(ServerPlayer player, Claim claim, boolean toggle) {
        Flan.log("Set claim {} to an admin claim", claim);
        this.deleteClaim(claim, false, EnumEditMode.DEFAULT, player.getLevel());
        if (toggle)
            claim.getOwnerPlayer().ifPresent(o -> PlayerClaimData.get(o).updateScoreboard());
        claim.toggleAdminClaim(player, toggle);
        if (!toggle)
            PlayerClaimData.get(player).updateScoreboard();
        this.addClaim(claim);
    }

    public boolean resizeClaim(Claim claim, BlockPos from, BlockPos to, ServerPlayer player) {
        int[] dims = claim.getDimensions();
        BlockPos opposite = new BlockPos(dims[0] == from.getX() ? dims[1] : dims[0], dims[4], dims[2] == from.getZ() ? dims[3] : dims[2]);
        Claim newClaim = new Claim(opposite, to, player.getUUID(), player.getLevel());
        if (newClaim.getPlane() < ConfigHandler.config.minClaimsize) {
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("minClaimSize"), ConfigHandler.config.minClaimsize), ChatFormatting.RED), false);
            return false;
        }
        Set<Claim> conflicts = this.conflicts(newClaim, claim);
        if (!conflicts.isEmpty()) {
            conflicts.forEach(conf -> PlayerClaimData.get(player).addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.blockPosition().getY()));
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("conflictOther"), ChatFormatting.RED), false);
            return false;
        }
        int diff = newClaim.getPlane() - claim.getPlane();
        PlayerClaimData data = PlayerClaimData.get(player);
        IPlayerData newData = claim.getOwnerPlayer().map(o -> {
            if (o == player || claim.isAdminClaim())
                return data;
            return (IPlayerData) PlayerClaimData.get(o);
        }).orElse(new OfflinePlayerData(player.getServer(), claim.getOwner()));
        boolean enoughBlocks = claim.isAdminClaim() || data.isAdminIgnoreClaim() || newData.canUseClaimBlocks(diff);
        if (enoughBlocks) {
            Flan.log("Resizing claim {}", claim);
            this.deleteClaim(claim, false, EnumEditMode.DEFAULT, player.getLevel());
            claim.copySizes(newClaim);
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            if (newData instanceof PlayerClaimData)
                ((PlayerClaimData) newData).updateScoreboard();
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("resizeSuccess"), ChatFormatting.GOLD), false);
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"),
                    newData.getClaimBlocks(), newData.getAdditionalClaims(), newData.usedClaimBlocks()), ChatFormatting.GOLD), false);
            return true;
        }
        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("notEnoughBlocks"), ChatFormatting.RED), false);
        return false;
    }

    public Claim getClaimAt(BlockPos pos) {
        long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        List<Claim> list = this.claims.get(chunk);
        if (list != null)
            for (Claim claim : list) {
                if (claim.insideClaim(pos))
                    return claim;
            }
        return null;
    }

    @Override
    public IPermissionContainer getForPermissionCheck(BlockPos pos) {
        Claim claim = this.getClaimAt(pos);
        if (claim != null)
            return claim;
        return this.globalClaim;
    }

    public boolean canInteract(BlockPos pos, int radius, ServerPlayer player, ClaimPermission perm, boolean message) {
        ChunkPos c = new ChunkPos(new BlockPos(pos.getX() - radius, pos.getY(), pos.getZ() - radius));
        List<Claim> affected = new ArrayList<>();
        for (int x = 0; SectionPos.sectionToBlockCoord(c.x + x) <= pos.getX() + radius; x++) {
            for (int z = 0; SectionPos.sectionToBlockCoord(c.z + z) <= pos.getZ() + radius; z++) {
                List<Claim> list = this.claims.get(ChunkPos.asLong(c.x + x, c.z + z));
                if (list != null)
                    affected.addAll(this.claims.get(ChunkPos.asLong(c.x + x, c.z + z)));
            }
        }
        Claim last = null;
        for (BlockPos ipos : BlockPos.betweenClosed(pos.getX() - radius, pos.getY(), pos.getZ() - radius,
                pos.getX() + radius, pos.getY(), pos.getZ() + radius)) {
            if (last != null) {
                if (last.insideClaim(ipos)) {
                    if (!last.canInteract(player, perm, ipos, message))
                        return false;
                    continue;
                } else last = null;
            }
            for (Claim claim : affected) {
                if (claim.insideClaim(ipos)) {
                    last = claim;
                    if (!claim.canInteract(player, perm, ipos, message))
                        return false;
                }
            }
        }
        return true;
    }

    public Claim getFromUUID(UUID uuid) {
        return this.claimUUIDMap.get(uuid);
    }

    private void addClaim(Claim claim) {
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                this.claims.merge(ChunkPos.asLong(x, z), Lists.newArrayList(claim), (old, val) -> {
                    old.add(claim);
                    return old;
                });
            }
        this.claimUUIDMap.put(claim.getClaimID(), claim);
        this.playerClaimMap.merge(claim.getOwner(), Sets.newHashSet(claim), (old, val) -> {
            old.add(claim);
            return old;
        });
        DynmapCalls.addClaimMarker(claim);
    }

    public boolean transferOwner(Claim claim, ServerPlayer player, UUID newOwner) {
        if (!player.getUUID().equals(claim.getOwner()))
            return false;
        this.playerClaimMap.merge(claim.getOwner(), new HashSet<>(), (old, val) -> {
            old.remove(claim);
            return old;
        });
        this.dirty.add(claim.getOwner());
        claim.getOwnerPlayer().ifPresent(o -> PlayerClaimData.get(o).updateScoreboard());
        claim.transferOwner(newOwner);
        this.playerClaimMap.merge(claim.getOwner(), Sets.newHashSet(claim), (old, val) -> {
            old.add(claim);
            return old;
        });
        this.dirty.add(claim.getOwner());
        DynmapCalls.changeClaimOwner(claim);
        return true;
    }

    public Collection<Claim> allClaimsFromPlayer(UUID player) {
        return this.playerClaimMap.containsKey(player) ? ImmutableSet.copyOf(this.playerClaimMap.get(player)) : ImmutableSet.of();
    }

    public Collection<Claim> getAdminClaims() {
        return ImmutableSet.copyOf(this.playerClaimMap.get(null));
    }

    public static int[] getChunkPos(Claim claim) {
        int[] dim = claim.getDimensions();
        int[] pos = new int[4];
        pos[0] = dim[0] >> 4;
        pos[1] = dim[1] >> 4;
        pos[2] = dim[2] >> 4;
        pos[3] = dim[3] >> 4;
        return pos;
    }

    public void read(MinecraftServer server, ServerLevel world) {
        Flan.log("Loading claim data for world {}", world.dimension());
        Path dir = ConfigHandler.getClaimSavePath(server, world.dimension());
        if (Files.exists(dir)) {
            try {
                for (Path file : Files.walk(dir).filter(Files::isRegularFile).collect(Collectors.toSet())) {
                    String name = file.toFile().getName();
                    if (!name.endsWith(".json"))
                        continue;
                    String realName = name.replace(".json", "");
                    UUID uuid = realName.equals(adminClaimString) ? null : UUID.fromString(realName);
                    JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
                    JsonArray arr = ConfigHandler.GSON.fromJson(reader, JsonArray.class);
                    reader.close();
                    if (arr == null)
                        continue;
                    Flan.debug("Reading claim data from json {} for player uuid {}", arr, uuid);
                    arr.forEach(el -> {
                        if (el.isJsonObject()) {
                            this.addClaim(Claim.fromJson((JsonObject) el, uuid, world));
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(MinecraftServer server, ResourceKey<Level> reg) {
        Flan.log("Saving claims for world {}", reg);
        Path dir = ConfigHandler.getClaimSavePath(server, reg);
        try {
            Files.createDirectories(dir);
            for (Map.Entry<UUID, Set<Claim>> e : this.playerClaimMap.entrySet()) {
                String owner = e.getKey() == null ? adminClaimString : e.getKey().toString();
                Path file = dir.resolve(owner + ".json");
                Flan.debug("Attempting saving claim data for player uuid {}", owner);
                boolean dirty = false;
                if (!Files.exists(file)) {
                    if (e.getValue().isEmpty())
                        continue;
                    Files.createFile(file);
                    dirty = true;
                } else {
                    if (e.getValue().isEmpty()) {
                        Files.delete(file);
                        continue;
                    }
                    if (this.dirty.remove(owner.equals(adminClaimString) ? null : e.getKey())) {
                        dirty = true;
                    } else {
                        for (Claim claim : e.getValue())
                            if (claim.isDirty()) {
                                dirty = true;
                                claim.setDirty(false);
                            }
                    }
                }
                if (dirty) {
                    JsonArray arr = new JsonArray();
                    e.getValue().forEach(claim -> arr.add(claim.toJson(new JsonObject())));
                    Flan.debug("Attempting saving changed claim data {} for player uuid {}", arr, owner);
                    JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
                    ConfigHandler.GSON.toJson(arr, jsonWriter);
                    jsonWriter.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readGriefPreventionData(MinecraftServer server, CommandSourceStack src) {
        Yaml yml = new Yaml();
        File griefPrevention = server.getWorldPath(LevelResource.ROOT).resolve("plugins/GriefPreventionData/ClaimData").toFile();
        if (!griefPrevention.exists()) {
            src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("cantFindData"), griefPrevention.getAbsolutePath()), ChatFormatting.DARK_RED), false);
            return false;
        }
        Map<File, List<File>> subClaimMap = new HashMap<>();
        Map<Integer, File> intFileMap = new HashMap<>();

        Set<ClaimPermission> managers = complementOf(PermissionRegistry.EDITCLAIM);
        Set<ClaimPermission> builders = complementOf(PermissionRegistry.EDITPERMS, PermissionRegistry.EDITCLAIM);
        Set<ClaimPermission> containers = complementOf(PermissionRegistry.EDITPERMS, PermissionRegistry.EDITCLAIM,
                PermissionRegistry.BREAK, PermissionRegistry.PLACE, PermissionRegistry.NOTEBLOCK, PermissionRegistry.REDSTONE, PermissionRegistry.JUKEBOX,
                PermissionRegistry.ITEMFRAMEROTATE, PermissionRegistry.LECTERNTAKE, PermissionRegistry.ENDCRYSTALPLACE, PermissionRegistry.PROJECTILES,
                PermissionRegistry.TRAMPLE, PermissionRegistry.RAID, PermissionRegistry.BUCKET, PermissionRegistry.ARMORSTAND, PermissionRegistry.BREAKNONLIVING);
        Set<ClaimPermission> accessors = complementOf(PermissionRegistry.EDITPERMS, PermissionRegistry.EDITCLAIM,
                PermissionRegistry.BREAK, PermissionRegistry.PLACE, PermissionRegistry.OPENCONTAINER, PermissionRegistry.ANVIL, PermissionRegistry.BEACON,
                PermissionRegistry.NOTEBLOCK, PermissionRegistry.REDSTONE, PermissionRegistry.JUKEBOX, PermissionRegistry.ITEMFRAMEROTATE,
                PermissionRegistry.LECTERNTAKE, PermissionRegistry.ENDCRYSTALPLACE, PermissionRegistry.PROJECTILES, PermissionRegistry.TRAMPLE, PermissionRegistry.RAID,
                PermissionRegistry.BUCKET, PermissionRegistry.ANIMALINTERACT, PermissionRegistry.HURTANIMAL, PermissionRegistry.TRADING, PermissionRegistry.ARMORSTAND,
                PermissionRegistry.BREAKNONLIVING);
        Map<String, Set<ClaimPermission>> perms = new HashMap<>();
        perms.put("managers", managers);
        perms.put("builders", builders);
        perms.put("containers", containers);
        perms.put("accessors", accessors);

        try {
            //Get all parent claims
            for (File f : griefPrevention.listFiles()) {
                if (f.getName().endsWith(".yml")) {
                    FileReader reader = new FileReader(f);
                    Map<String, Object> values = yml.load(reader);
                    if (values.get("Parent Claim ID").equals(-1)) {
                        try {
                            intFileMap.put(Integer.valueOf(f.getName().replace(".yml", "")), f);
                        } catch (NumberFormatException e) {
                            src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("errorFile"), f.getName(), ChatFormatting.RED)), false);
                        }
                    }
                }
            }
            //Map child to parent claims
            for (File f : griefPrevention.listFiles()) {
                if (f.getName().endsWith(".yml")) {
                    FileReader reader = new FileReader(f);
                    Map<String, Object> values = yml.load(reader);
                    if (!values.get("Parent Claim ID").equals(-1)) {
                        subClaimMap.merge(intFileMap.get(Integer.valueOf(values.get("Parent Claim ID").toString()))
                                , Lists.newArrayList(f), (key, val) -> {
                                    key.add(f);
                                    return key;
                                });
                    }
                }
            }
            for (File parent : intFileMap.values()) {
                try {
                    Tuple<ServerLevel, Claim> parentClaim = parseFromYaml(parent, yml, server, perms);
                    List<File> childs = subClaimMap.get(parent);
                    if (childs != null && !childs.isEmpty()) {
                        for (File childF : childs)
                            parentClaim.getB().addSubClaimGriefprevention(parseFromYaml(childF, yml, server, perms).getB());
                    }
                    ClaimStorage storage = ClaimStorage.get(parentClaim.getA());
                    Set<Claim> conflicts = storage.conflicts(parentClaim.getB(), null);
                    if (conflicts.isEmpty()) {
                        parentClaim.getB().setClaimID(storage.generateUUID());
                        storage.addClaim(parentClaim.getB());
                    } else {
                        src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("readConflict"), parent.getName(), conflicts), ChatFormatting.DARK_RED), false);
                        for (Claim claim : conflicts) {
                            int[] dim = claim.getDimensions();
                            MutableComponent text = PermHelper.simpleColoredText(String.format("@[x=%d;z=%d]", dim[0], dim[2]), ChatFormatting.RED);
                            text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + dim[0] + " ~ " + dim[2])).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip"))));
                            src.sendSuccess(text, false);
                        }
                    }
                } catch (Exception e) {
                    src.sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("errorFile"), parent.getName(), ChatFormatting.RED)), false);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static Set<ClaimPermission> complementOf(ClaimPermission... perms) {
        Set<ClaimPermission> set = Sets.newHashSet(PermissionRegistry.getPerms());
        for (ClaimPermission perm : perms)
            set.remove(perm);
        return set;
    }

    private static Tuple<ServerLevel, Claim> parseFromYaml(File file, Yaml yml, MinecraftServer server,
                                                           Map<String, Set<ClaimPermission>> perms) throws IOException {
        FileReader reader = new FileReader(file);
        Map<String, Object> values = yml.load(reader);
        reader.close();
        String ownerString = (String) values.get("Owner");

        UUID owner = ownerString.isEmpty() ? null : UUID.fromString(ownerString);
        List<String> builders = readList(values, "Builders");
        List<String> managers = readList(values, "Managers");
        List<String> containers = readList(values, "Containers");
        List<String> accessors = readList(values, "Accessors");
        String[] lesserCorner = values.get("Lesser Boundary Corner").toString().split(";");
        String[] greaterCorner = values.get("Greater Boundary Corner").toString().split(";");
        ServerLevel world = server.getLevel(worldRegFromString(lesserCorner[0]));
        Claim claim = new Claim(Integer.parseInt(lesserCorner[1]), Integer.parseInt(greaterCorner[1]),
                Integer.parseInt(lesserCorner[3]), Integer.parseInt(greaterCorner[3]), ConfigHandler.config.defaultClaimDepth == 255 ? 0 :
                Integer.parseInt(lesserCorner[2]), owner, world);
        if (!builders.isEmpty() && !builders.contains(ownerString)) {
            if (builders.contains("public")) {
                perms.get("builders").forEach(perm -> {
                    if (!PermissionRegistry.globalPerms().contains(perm))
                        claim.editGlobalPerms(null, perm, 1);
                });
            } else {
                perms.get("builders").forEach(perm -> claim.editPerms(null, "Builders", perm, 1, true));
                builders.forEach(s -> claim.setPlayerGroup(UUID.fromString(s), "Builders", true));
            }
        }
        if (!managers.isEmpty() && !managers.contains(ownerString)) {
            if (managers.contains("public")) {
                perms.get("managers").forEach(perm -> {
                    if (!PermissionRegistry.globalPerms().contains(perm))
                        claim.editGlobalPerms(null, perm, 1);
                });
            } else {
                perms.get("managers").forEach(perm -> claim.editPerms(null, "Managers", perm, 1, true));
                managers.forEach(s -> claim.setPlayerGroup(UUID.fromString(s), "Managers", true));
            }
        }
        if (!containers.isEmpty() && !containers.contains(ownerString)) {
            if (containers.contains("public")) {
                perms.get("containers").forEach(perm -> {
                    if (!PermissionRegistry.globalPerms().contains(perm))
                        claim.editGlobalPerms(null, perm, 1);
                });
            } else {
                perms.get("containers").forEach(perm -> claim.editPerms(null, "Containers", perm, 1, true));
                containers.forEach(s -> claim.setPlayerGroup(UUID.fromString(s), "Containers", true));
            }
        }
        if (!accessors.isEmpty() && !accessors.contains(ownerString)) {
            if (accessors.contains("public")) {
                perms.get("accessors").forEach(perm -> {
                    if (!PermissionRegistry.globalPerms().contains(perm))
                        claim.editGlobalPerms(null, perm, 1);
                });
            } else {
                perms.get("accessors").forEach(perm -> claim.editPerms(null, "Accessors", perm, 1, true));
                accessors.forEach(s -> claim.setPlayerGroup(UUID.fromString(s), "Accessors", true));
            }
        }
        return new Tuple<>(world, claim);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> readList(Map<String, Object> values, String key) {
        Object obj = values.get(key);
        if (obj instanceof List)
            return (List<T>) obj;
        return new ArrayList<>();
    }

    public static ResourceKey<Level> worldRegFromString(String spigot) {
        if (spigot.equals("world_the_end"))
            return Level.END;
        if (spigot.equals("world_nether"))
            return Level.NETHER;
        return Level.OVERWORLD;
    }
}
