package com.flemmli97.flan.claim;

import com.flemmli97.flan.IClaimData;
import com.flemmli97.flan.config.Config;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.EnumEditMode;
import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimStorage {

    public final Long2ObjectArrayMap<List<Claim>> claims = new Long2ObjectArrayMap<>();
    public final Map<UUID, Claim> claimUUIDMap = Maps.newHashMap();
    public final Map<UUID, Set<Claim>> playerClaimMap = Maps.newHashMap();

    public static ClaimStorage get(ServerWorld world) {
        return (ClaimStorage) ((IClaimData) world).getClaimData();
    }

    public ClaimStorage(MinecraftServer server, ServerWorld world) {
        this.read(server, world);
    }

    public UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        if (this.claimUUIDMap.containsKey(uuid))
            return generateUUID();
        return uuid;
    }

    public boolean createClaim(BlockPos pos1, BlockPos pos2, ServerPlayerEntity player) {
        Claim claim = new Claim(pos1.down(ConfigHandler.config.defaultClaimDepth), pos2.down(ConfigHandler.config.defaultClaimDepth), player.getUuid(), player.getServerWorld());
        Set<Claim> conflicts = conflicts(claim, null);
        if (conflicts.isEmpty()) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if(claim.getPlane()<ConfigHandler.config.minClaimsize){
                player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.minClaimSize, ConfigHandler.config.minClaimsize), Formatting.RED), false);
                return false;
            }
            if (!data.canUseClaimBlocks(claim.getPlane())) {
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.notEnoughBlocks, Formatting.RED), false);
                return false;
            }
            claim.setClaimID(this.generateUUID());
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.claimCreateSuccess, Formatting.GOLD), false);
            return true;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        conflicts.forEach(conf->data.addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.getBlockPos().getY()));
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
        return false;
    }

    private Set<Claim> conflicts(Claim claim, Claim except) {
        Set<Claim> conflicted = Sets.newHashSet();
        int[] chunks = getChunkPos(claim);
        for (int x = chunks[0]; x <= chunks[1]; x++)
            for (int z = chunks[2]; z <= chunks[3]; z++) {
                List<Claim> claims = this.claims.get(new ChunkPos(x, z).toLong());
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
        if(mode==EnumEditMode.SUBCLAIM){
            if(claim.parentClaim()!=null)
                return claim.parentClaim().deleteSubClaim(claim);
            return false;
        }
        if(updateClaim)
            claim.remove();
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                this.claims.compute(chunkPos.toLong(), (key, val) -> {
                    if (val == null)
                        return null;
                    val.remove(claim);
                    return val.isEmpty() ? null : val;
                });
            }
        this.playerClaimMap.getOrDefault(claim.getOwner(), Sets.newHashSet()).remove(claim);
        return this.claimUUIDMap.remove(claim.getClaimID()) != null;
    }

    public boolean resizeClaim(Claim claim, BlockPos from, BlockPos to, ServerPlayerEntity player) {
        int[] dims = claim.getDimensions(); //BlockPos from, BlockPos to
        BlockPos opposite = new BlockPos(dims[0]==from.getX()?dims[1]:dims[0], dims[4], dims[2]==from.getZ()?dims[3]:dims[2]);
        Claim newClaim = new Claim(opposite, to, player.getUuid(), player.getServerWorld());
        Set<Claim> conflicts = conflicts(newClaim, claim);
        if(!conflicts.isEmpty()) {
            conflicts.forEach(conf->PlayerClaimData.get(player).addDisplayClaim(conf, EnumDisplayType.CONFLICT, player.getBlockPos().getY()));
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        int diff = newClaim.getPlane()-claim.getPlane();
        if(data.canUseClaimBlocks(diff)) {
            this.deleteClaim(claim, false, EnumEditMode.DEFAULT, player.getServerWorld());
            claim.copySizes(newClaim);
            this.addClaim(claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.resizeSuccess, Formatting.GOLD), false);
            return true;
        }
        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.notEnoughBlocks, Formatting.RED), false);
        return false;
    }

    public Claim getClaimAt(BlockPos pos) {
        long chunk = new ChunkPos(pos).toLong();
        if (this.claims.containsKey(chunk))
            for (Claim claim : this.claims.get(chunk)) {
                if (claim.insideClaim(pos))
                    return claim;
            }
        return null;
    }

    private void addClaim(Claim claim) {
        int[] pos = getChunkPos(claim);
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                this.claims.merge(chunkPos.toLong(), Lists.newArrayList(claim), (old, val) -> {
                    old.add(claim);
                    return old;
                });
            }
        this.claimUUIDMap.put(claim.getClaimID(), claim);
        this.playerClaimMap.merge(claim.getOwner(), Sets.newHashSet(claim), (old, val) -> {
            old.add(claim);
            return old;
        });
    }

    public Collection<Claim> allClaimsFromPlayer(UUID player) {
        return this.playerClaimMap.containsKey(player) ? ImmutableSet.copyOf(this.playerClaimMap.get(player)) : ImmutableSet.of();
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
        File dir = new File(DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT).toFile()), "/data/claims/");
        if (dir.exists()) {
            try {
                for (File file : dir.listFiles()) {
                    if (!file.getName().endsWith(".json"))
                        continue;
                    UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
                    FileReader reader = new FileReader(file);
                    JsonArray arr = ConfigHandler.GSON.fromJson(reader, JsonArray.class);
                    if (arr == null)
                        continue;
                    arr.forEach(el -> {
                        if (el.isJsonObject()) {
                            this.addClaim(Claim.fromJson((JsonObject) el, uuid, world));
                        }
                    });
                    reader.close();
                }
            } catch (IOException e) {

            }
        }
    }

    public void save(MinecraftServer server, RegistryKey<World> reg) {
        File dir = new File(DimensionType.getSaveDirectory(reg, server.getSavePath(WorldSavePath.ROOT).toFile()), "/data/claims/");
        if (!dir.exists())
            dir.mkdir();
        try {
            for (Map.Entry<UUID, Set<Claim>> e : this.playerClaimMap.entrySet()) {

                File file = new File(dir, e.getKey().toString() + ".json");
                boolean dirty = false;
                if (!file.exists()) {
                    if (e.getValue().isEmpty())
                        continue;
                    file.createNewFile();
                    dirty = true;
                }
                else {
                    for(Claim claim : e.getValue())
                        if(claim.isDirty()) {
                            dirty = true;
                            break;
                        }
                }
                if(dirty){
                    FileWriter writer = new FileWriter(file);
                    JsonArray arr = new JsonArray();
                    e.getValue().forEach(claim -> arr.add(claim.toJson(new JsonObject())));
                    ConfigHandler.GSON.toJson(arr, writer);
                    writer.close();
                }
            }
        } catch (IOException e) {

        }
    }

    public static void readGriefPreventionData(MinecraftServer server) {
        Yaml yml = new Yaml();
        File griefPrevention = server.getSavePath(WorldSavePath.ROOT).resolve("plugins/GriefPreventionData/ClaimData").toFile();
        if (!griefPrevention.exists())
            return;
        Map<File, List<File>> subClaimMap = Maps.newHashMap();
        Map<Integer, File> intFileMap = Maps.newHashMap();

        try {
            //Get all parent claims
            for (File f : griefPrevention.listFiles()) {
                if (f.getName().endsWith(".yml")) {
                    FileReader reader = new FileReader(f);
                    Map<String, Object> values = yml.load(reader);
                    if (values.get("Parent Claim ID").equals(-1)) {
                        intFileMap.put(Integer.valueOf(values.get("Parent Claim ID").toString()), f);
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
                                    val.add(f);
                                    return val;
                                });
                    }
                }
            }
            for (File parent : intFileMap.values()) {
                Pair<ServerWorld, Claim> parentClaim = parseFromYaml(parent, yml, server);
                List<File> childs = subClaimMap.get(parent);
                if (childs != null && !childs.isEmpty()) {
                    for (File childF : childs)
                        parentClaim.second.addSubClaimGriefprevention(parseFromYaml(childF, yml, server).second);
                }
                ClaimStorage.get(parentClaim.first).addClaim(parentClaim.second);
            }
        } catch (IOException e) {

        }
    }

    private static Pair<ServerWorld, Claim> parseFromYaml(File file, Yaml yml, MinecraftServer server) throws IOException {
        FileReader reader = new FileReader(file);
        Map<String, Object> values = yml.load(reader);
        reader.close();
        UUID owner = UUID.fromString(values.get("Owner").toString());
        String[] lesserCorner = values.get("Lesser Boundary Corner").toString().split(";");
        String[] greaterCorner = values.get("Greater Boundary Corner").toString().split(";");
        ServerWorld world = server.getWorld(worldRegFromString(lesserCorner[0]));
        Claim claim = new Claim(Integer.parseInt(lesserCorner[1]), Integer.parseInt(greaterCorner[1]),
                Integer.parseInt(lesserCorner[3]), Integer.parseInt(greaterCorner[3]),
                Integer.parseInt(lesserCorner[2]), owner, world);
        return Pair.of(world, claim);
    }

    public static RegistryKey<World> worldRegFromString(String spigot) {
        if (spigot.equals("world_the_end"))
            return World.END;
        if (spigot.equals("world_nether"))
            return World.NETHER;
        return World.OVERWORLD;
    }
}
