package com.flemmli97.flan.claim;

import com.flemmli97.flan.Flan;
import com.flemmli97.flan.IClaimData;
import com.flemmli97.flan.api.ClaimPermission;
import com.flemmli97.flan.api.PermissionRegistry;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.EnumEditMode;
import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimStorage {

    public static final String adminClaimString = "!AdminClaims";
    private final Long2ObjectArrayMap<List<Claim>> claims = new Long2ObjectArrayMap<>();
    private final Map<UUID, Claim> claimUUIDMap = new HashMap<>();
    private final Map<UUID, Set<Claim>> playerClaimMap = new HashMap<>();
    private final Set<UUID> dirty = new HashSet<>();
    private final GlobalClaim globalClaim;

    public static ClaimStorage get(ServerWorld world) {
        return ((IClaimData<ClaimStorage>) world).getClaimData();
    }

    public ClaimStorage(MinecraftServer server, ServerWorld world) {
        this.globalClaim = new GlobalClaim(world);
        this.read(server, world);
    }

    public UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        if (this.claimUUIDMap.containsKey(uuid))
            return this.generateUUID();
        return uuid;
    }

    public boolean createClaim(BlockPos pos1, BlockPos pos2, ServerPlayerEntity player) {
        Claim claim = new Claim(pos1.down(ConfigHandler.config.defaultClaimDepth), pos2.down(ConfigHandler.config.defaultClaimDepth), player.getUuid(), player.getServerWorld());
        Set<Claim> conflicts = this.conflicts(claim, null);
        if (conflicts.isEmpty()) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (claim.getPlane() < ConfigHandler.config.minClaimsize) {
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.minClaimSize, ConfigHandler.config.minClaimsize), Formatting.RED), false);
                return false;
            }
            if (!data.canUseClaimBlocks(claim.getPlane())) {
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.notEnoughBlocks, Formatting.RED), false);
                return false;
            }
            claim.setClaimID(this.generateUUID());
            Flan.log("Creating new claim {}", claim);
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.claimCreateSuccess, Formatting.GOLD), false);
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBlocksFormat,
                    data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks()), Formatting.GOLD), false);
            return true;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        conflicts.forEach(conf -> data.addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.getBlockPos().getY()));
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
        return false;
    }

    private Set<Claim> conflicts(Claim claim, Claim except) {
        Set<Claim> conflicted = new HashSet<>();
        int[] chunks = getChunkPos(claim);
        for (int x = chunks[0]; x <= chunks[1]; x++)
            for (int z = chunks[2]; z <= chunks[3]; z++) {
                List<Claim> claims = this.claims.get(ChunkPos.toLong(x, z));
                if (claims != null)
                    for (Claim other : claims) {
                        if (claim.intersects(other) && !other.equals(except)) {
                            conflicted.add(other);
                        }
                    }
            }
        return conflicted;
    }

    public boolean deleteClaim(Claim claim, boolean updateClaim, EnumEditMode mode, ServerWorld world) {
        if (mode == EnumEditMode.SUBCLAIM) {
            if (claim.parentClaim() != null)
                return claim.parentClaim().deleteSubClaim(claim);
            return false;
        }
        Flan.log("Try deleting claim {}", claim);
        if (updateClaim)
            claim.remove();
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                this.claims.compute(ChunkPos.toLong(x, z), (key, val) -> {
                    if (val == null)
                        return null;
                    val.remove(claim);
                    return val.isEmpty() ? null : val;
                });
            }
        this.playerClaimMap.getOrDefault(claim.getOwner(), new HashSet<>()).remove(claim);
        this.dirty.add(claim.getOwner());
        return this.claimUUIDMap.remove(claim.getClaimID()) != null;
    }

    public void toggleAdminClaim(ServerPlayerEntity player, Claim claim, boolean toggle) {
        Flan.log("Set claim {} to an admin claim", claim);
        this.deleteClaim(claim, false, EnumEditMode.DEFAULT, player.getServerWorld());
        claim.toggleAdminClaim(player, toggle);
        this.addClaim(claim);
    }

    public boolean resizeClaim(Claim claim, BlockPos from, BlockPos to, ServerPlayerEntity player) {
        int[] dims = claim.getDimensions();
        BlockPos opposite = new BlockPos(dims[0] == from.getX() ? dims[1] : dims[0], dims[4], dims[2] == from.getZ() ? dims[3] : dims[2]);
        Claim newClaim = new Claim(opposite, to, player.getUuid(), player.getServerWorld());
        if (newClaim.getPlane() < ConfigHandler.config.minClaimsize) {
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.minClaimSize, ConfigHandler.config.minClaimsize), Formatting.RED), false);
            return false;
        }
        Set<Claim> conflicts = this.conflicts(newClaim, claim);
        if (!conflicts.isEmpty()) {
            conflicts.forEach(conf -> PlayerClaimData.get(player).addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.getBlockPos().getY()));
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        int diff = newClaim.getPlane() - claim.getPlane();
        if (data.canUseClaimBlocks(diff)) {
            Flan.log("Resizing claim {}", claim);
            this.deleteClaim(claim, false, EnumEditMode.DEFAULT, player.getServerWorld());
            claim.copySizes(newClaim);
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.resizeSuccess, Formatting.GOLD), false);
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBlocksFormat,
                    data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks()), Formatting.GOLD), false);
            return true;
        }
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.notEnoughBlocks, Formatting.RED), false);
        return false;
    }

    public Claim getClaimAt(BlockPos pos) {
        long chunk = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
        if (this.claims.containsKey(chunk))
            for (Claim claim : this.claims.get(chunk)) {
                if (claim.insideClaim(pos))
                    return claim;
            }
        return null;
    }

    public IPermissionContainer getForPermissionCheck(BlockPos pos) {
        Claim claim = this.getClaimAt(pos);
        if (claim != null)
            return claim;
        return this.globalClaim;
    }

    public Claim getFromUUID(UUID uuid) {
        return this.claimUUIDMap.get(uuid);
    }

    private void addClaim(Claim claim) {
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                this.claims.merge(ChunkPos.toLong(x, z), new ArrayList<>(claim), (old, val) -> {
                    old.add(claim);
                    return old;
                });
            }
        this.claimUUIDMap.put(claim.getClaimID(), claim);
        this.playerClaimMap.merge(claim.getOwner(), new HashSet<>((claim), (old, val) -> {
            old.add(claim);
            return old;
        });
    }

    public boolean transferOwner(Claim claim, ServerPlayerEntity player, UUID newOwner) {
        if (!player.getUuid().equals(claim.getOwner()))
            return false;
        this.playerClaimMap.merge(claim.getOwner(), new HashSet<>(), (old, val) -> {
            old.remove(claim);
            return old;
        });
        this.dirty.add(claim.getOwner());
        claim.transferOwner(newOwner);
        this.playerClaimMap.merge(claim.getOwner(), new HashSet<>((claim), (old, val) -> {
            old.add(claim);
            return old;
        });
        this.dirty.add(claim.getOwner());
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

    public void read(MinecraftServer server, ServerWorld world) {
        Flan.log("Loading claim data for world {}", world.getRegistryKey());
        File dir = new File(DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT).toFile()), "/data/claims/");
        if (dir.exists()) {
            try {
                for (File file : dir.listFiles()) {
                    if (!file.getName().endsWith(".json"))
                        continue;
                    String realName = file.getName().replace(".json", "");
                    UUID uuid = realName.equals(adminClaimString) ? null : UUID.fromString(file.getName().replace(".json", ""));
                    FileReader reader = new FileReader(file);
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

    public void save(MinecraftServer server, RegistryKey<World> reg) {
        Flan.log("Saving claims for world {}", reg);
        File dir = new File(DimensionType.getSaveDirectory(reg, server.getSavePath(WorldSavePath.ROOT).toFile()), "/data/claims/");
        if (!dir.exists())
            dir.mkdir();
        try {
            for (Map.Entry<UUID, Set<Claim>> e : this.playerClaimMap.entrySet()) {
                String owner = e.getKey() == null ? adminClaimString : e.getKey().toString();
                File file = new File(dir, owner + ".json");
                Flan.debug("Attempting saving claim data for player uuid {}", owner);
                boolean dirty = false;
                if (!file.exists()) {
                    if (e.getValue().isEmpty())
                        continue;
                    file.createNewFile();
                    dirty = true;
                } else {
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
                    FileWriter writer = new FileWriter(file);
                    ConfigHandler.GSON.toJson(arr, writer);
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readGriefPreventionData(MinecraftServer server, ServerCommandSource src) {
        Yaml yml = new Yaml();
        File griefPrevention = server.getSavePath(WorldSavePath.ROOT).resolve("plugins/GriefPreventionData/ClaimData").toFile();
        if (!griefPrevention.exists())
            return;
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
                            src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.errorFile, f.getName(), Formatting.RED)), false);
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
                                , new ArrayList<>(f), (key, val) -> {
                                    key.add(f);
                                    return key;
                                });
                    }
                }
            }
            for (File parent : intFileMap.values()) {
                try {
                    Pair<ServerWorld, Claim> parentClaim = parseFromYaml(parent, yml, server, perms);
                    List<File> childs = subClaimMap.get(parent);
                    if (childs != null && !childs.isEmpty()) {
                        for (File childF : childs)
                            parentClaim.getRight().addSubClaimGriefprevention(parseFromYaml(childF, yml, server, perms).getRight());
                    }
                    ClaimStorage storage = ClaimStorage.get(parentClaim.getLeft());
                    Set<Claim> conflicts = storage.conflicts(parentClaim.getRight(), null);
                    if (conflicts.isEmpty()) {
                        parentClaim.getRight().setClaimID(storage.generateUUID());
                        storage.addClaim(parentClaim.getRight());
                    } else {
                        src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.readConflict, parent.getName(), conflicts), Formatting.DARK_RED), false);
                        for (Claim claim : conflicts) {
                            int[] dim = claim.getDimensions();
                            MutableText text = PermHelper.simpleColoredText(String.format("@[x=%d;z=%d]", dim[0], dim[2]), Formatting.RED);
                            text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + dim[0] + " ~ " + dim[2])).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip"))));
                            src.sendFeedback(text, false);
                        }
                    }
                } catch (Exception e) {
                    src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.errorFile, parent.getName(), Formatting.RED)), false);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<ClaimPermission> complementOf(ClaimPermission... perms) {
        Set<ClaimPermission> set = new HashSet<>((PermissionRegistry.getPerms());
        for (ClaimPermission perm : perms)
            set.remove(perm);
        return set;
    }

    private static Pair<ServerWorld, Claim> parseFromYaml(File file, Yaml yml, MinecraftServer server,
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
        ServerWorld world = server.getWorld(worldRegFromString(lesserCorner[0]));
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
        return new Pair<>(world, claim);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> readList(Map<String, Object> values, String key) {
        Object obj = values.get(key);
        if (obj instanceof List)
            return (List<T>) obj;
        return new ArrayList<>();
    }

    public static RegistryKey<World> worldRegFromString(String spigot) {
        if (spigot.equals("world_the_end"))
            return World.END;
        if (spigot.equals("world_nether"))
            return World.NETHER;
        return World.OVERWORLD;
    }
}
