package com.flemmli97.flan.claim;

import com.flemmli97.flan.IClaimData;
import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimStorage {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public final Long2ObjectArrayMap<List<Claim>> claims = new Long2ObjectArrayMap();
    public final Map<UUID, Claim> claimUUIDMap = Maps.newHashMap();
    public final Map<UUID, Set<Claim>> playerClaimMap = Maps.newHashMap();

    public static ClaimStorage get(ServerWorld world) {
        return (ClaimStorage) ((IClaimData) world).getClaimData();
    }

    public ClaimStorage(MinecraftServer server, RegistryKey<World> key) {
        this.read(server, key);
    }

    public UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        if (this.claimUUIDMap.containsKey(uuid))
            return generateUUID();
        return uuid;
    }

    public boolean createClaim(BlockPos pos1, BlockPos pos2, PlayerEntity player) {
        Claim claim = new Claim(pos1.down(5), pos2.down(5), player.getUuid());
        boolean conflicts = conflicts(claim, null);
        if (!conflicts) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (!data.useClaimBlocks(claim.getPlane()))
                return false;
            claim.setClaimID(this.generateUUID());
            this.addClaim(claim);
            data.addDisplayClaim(claim);
            return true;
        }
        player.sendMessage(Text.of("Error creating claim"), false);
        return false;
    }

    private boolean conflicts(Claim claim, Claim except) {
        int[] chunks = getChunkPos(claim);
        for (int x = chunks[0]; x <= chunks[1]; x++)
            for (int z = chunks[2]; z <= chunks[3]; z++) {
                List<Claim> claims = this.claims.get(new ChunkPos(x, z).toLong());
                if (claims != null)
                    for (Claim other : claims) {
                        if (claim.intersects(other) && !claim.equals(except)) {
                            return true;
                        }
                    }
            }
        return false;
    }

    public boolean deleteClaim(Claim claim) {
        System.out.println("claim " + claim);
        System.out.println("claimmap " + this.claims);

        claim.remove();
        int[] pos = getChunkPos(claim);
        System.out.println("" + Arrays.toString(pos));
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
        System.out.println(this.claims);
        this.playerClaimMap.getOrDefault(claim.getOwner(), Sets.newHashSet()).remove(claim);
        return this.claimUUIDMap.remove(claim.getClaimID()) != null;
    }

    public boolean resizeClaim(Claim claim, BlockPos pos) {
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
        System.out.println("adding claim " + claim);
        int[] pos = getChunkPos(claim);
        System.out.println("" + Arrays.toString(pos));
        for (int x = pos[0]; x <= pos[1]; x++)
            for (int z = pos[2]; z <= pos[3]; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                this.claims.merge(chunkPos.toLong(), Lists.newArrayList(claim), (key, val) -> {
                    val.add(claim);
                    return val;
                });
            }
        System.out.println("claimmap " + this.claims);
        this.claimUUIDMap.put(claim.getClaimID(), claim);
        this.playerClaimMap.merge(claim.getOwner(), Sets.newHashSet(claim), (key, val) -> {
            val.add(claim);
            return val;
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

    public void fromTag(CompoundTag compoundTag) {
        ListTag list = compoundTag.getList("Claims", 10);
        list.forEach(tag -> {
            Claim claim = Claim.fromTag((CompoundTag) tag);
            this.addClaim(claim);
        });
    }

    public CompoundTag toTag(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        this.claims.forEach((l, cList) ->
                cList.forEach(claim -> list.add(claim.save(new CompoundTag()))));
        compoundTag.put("Claims", list);
        return compoundTag;
    }

    public void read(MinecraftServer server, RegistryKey<World> reg) {
        File dir = new File(DimensionType.getSaveDirectory(reg, server.getSavePath(WorldSavePath.ROOT).toFile()), "/data/claims/");
        if (dir.exists()) {
            try {
                for (File file : dir.listFiles()) {
                    if (!file.getName().endsWith(".json"))
                        continue;
                    UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
                    FileReader reader = new FileReader(file);
                    JsonArray arr = GSON.fromJson(reader, JsonArray.class);
                    if (arr == null)
                        continue;
                    arr.forEach(el -> {
                        if (el.isJsonObject()) {
                            this.addClaim(Claim.fromJson((JsonObject) el, uuid));
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
                if (!file.exists()) {
                    if (e.getValue().isEmpty())
                        continue;
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file);
                JsonArray arr = new JsonArray();
                e.getValue().forEach(claim -> arr.add(claim.toJson(new JsonObject())));
                GSON.toJson(arr, writer);
                writer.close();
            }
        } catch (IOException e) {

        }
    }

    public static void readGriefPreventionData(MinecraftServer server) {
        Yaml yml = new Yaml();
        File griefPrevention = server.getSavePath(WorldSavePath.ROOT).resolve("GriefPreventionData/ClaimData").toFile();
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
                    if (values.get("Parent Claim ID").equals(Integer.valueOf(-1))) {
                        intFileMap.put(Integer.valueOf(values.get("Parent Claim ID").toString()), f);
                    }
                }
            }
            //Map child to parent claims
            for (File f : griefPrevention.listFiles()) {
                if (f.getName().endsWith(".yml")) {
                    FileReader reader = new FileReader(f);
                    Map<String, Object> values = yml.load(reader);
                    if (!values.get("Parent Claim ID").equals(Integer.valueOf(-1))) {
                        subClaimMap.merge(intFileMap.get(Integer.valueOf(values.get("Parent Claim ID").toString()))
                                , Lists.newArrayList(f), (key, val) -> {
                                    val.add(f);
                                    return val;
                                });
                    }
                }
            }

            for (File parent : intFileMap.values()) {
                Pair<String, Claim> parentClaim = parseFromYaml(parent, yml);
                List<File> childs = subClaimMap.get(parent);
                if (childs != null && !childs.isEmpty()) {
                    for (File childF : childs)
                        parentClaim.second.addSubClaims(parseFromYaml(childF, yml).second);
                }
                ClaimStorage.get(server.getWorld(worldRegFromString(parentClaim.first))).addClaim(parentClaim.second);
            }
        } catch (IOException e) {

        }
    }

    private static Pair<String, Claim> parseFromYaml(File file, Yaml yml) throws IOException {
        FileReader reader = new FileReader(file);
        Map<String, Object> values = yml.load(reader);
        reader.close();
        UUID owner = UUID.fromString(values.get("Owner").toString());
        String[] lesserCorner = values.get("Lesser Boundary Corner").toString().split(";");
        String[] greaterCorner = values.get("Greater Boundary Corner").toString().split(";");
        Claim claim = new Claim(Integer.parseInt(lesserCorner[1]), Integer.parseInt(greaterCorner[1]),
                Integer.parseInt(lesserCorner[3]), Integer.parseInt(greaterCorner[3]),
                Integer.parseInt(lesserCorner[2]), owner);


        return Pair.of(lesserCorner[0], claim);
    }

    public static RegistryKey<World> worldRegFromString(String spigot) {
        if (spigot.equals("world_the_end"))
            return World.END;
        if (spigot.equals("world_nether"))
            return World.NETHER;
        return World.OVERWORLD;
    }
}
