package com.flemmli97.flan.claim;

import com.flemmli97.flan.player.PlayerClaimData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Claim {

    private boolean dirty;
    private int minX, minZ, maxX, maxZ, minY;

    private UUID owner;
    private UUID claimID;
    private final EnumSet<EnumPermission> globalPerm = EnumSet.noneOf(EnumPermission.class);
    private final Map<String, EnumMap<EnumPermission, Boolean>> permissions = Maps.newHashMap();

    private final Map<UUID, String> playersGroups = Maps.newHashMap();

    private final List<Claim> subClaims = Lists.newArrayList();

    /**
     * Flag for players tracking this claim
     */
    private boolean removed;

    private Claim() {
    }

    public Claim(BlockPos pos1, BlockPos pos2, UUID creator) {
        this(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ(), Math.min(pos1.getY(), pos2.getY()), creator);
    }

    public Claim(int x1, int x2, int z1, int z2, int minY, UUID creator) {
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);
        this.minY = Math.max(0, minY);
        this.owner = creator;
    }

    public static Claim fromTag(CompoundTag tag) {
        Claim claim = new Claim();
        claim.read(tag);
        return claim;
    }

    public static Claim fromJson(JsonObject obj, UUID owner) {
        Claim claim = new Claim();
        claim.readJson(obj, owner);
        return claim;
    }

    public void setClaimID(UUID uuid) {
        this.claimID = uuid;
        this.setDirty();
    }

    public UUID getClaimID() {
        return this.claimID;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public int getPlane() {
        return (this.maxX - this.minX) * (this.maxZ - this.minZ);
    }

    /**
     * @return The claims dimension in order: x, X, z, Z, y
     */
    public int[] getDimensions() {
        return new int[]{this.minX, this.maxX, this.minZ, this.maxZ, this.minY};
    }

    public boolean insideClaim(BlockPos pos) {
        return this.minX <= pos.getX() && this.maxX >= pos.getX() && this.minZ <= pos.getZ() && this.maxZ >= pos.getZ() && this.minY <= pos.getY();
    }

    public boolean intersects(Claim other) {
        return this.minX < other.maxX && this.maxX > other.minX && this.minZ < other.maxZ && this.maxZ > other.minZ;
    }

    public boolean isCorner(BlockPos pos) {
        return (pos.getX() == this.minX && pos.getZ() == this.minZ) || (pos.getX() == this.minX && pos.getZ() == this.maxZ)
                || (pos.getX() == this.maxX && pos.getZ() == this.minZ) || (pos.getX() == this.maxX && pos.getZ() == this.maxZ);
    }

    public void remove() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public boolean canInteract(PlayerEntity player, EnumPermission perm, BlockPos pos) {
        if (perm == EnumPermission.EXPLOSIONS || perm == EnumPermission.WITHER) {
            for (Claim claim : this.subClaims) {
                if (claim.insideClaim(pos)) {
                    if (claim.canInteract(player, perm, pos))
                        return true;
                    break;
                }
            }
            return this.permEnabled(perm);
        }
        if (player.getUuid().equals(this.owner))
            return true;
        PlayerClaimData data = PlayerClaimData.get(player);
        if (player.hasPermissionLevel(2) && data.isAdminIgnoreClaim())
            return true;
        for (Claim claim : this.subClaims) {
            if (claim.insideClaim(pos)) {
                if (claim.canInteract(player, perm, pos))
                    return true;
                break;
            }
        }
        if (this.playersGroups.containsKey(player.getUuid())) {
            EnumMap<EnumPermission, Boolean> map = this.permissions.get(this.playersGroups.get(player.getUuid()));
            if (map != null && map.containsKey(perm))
                return map.get(perm);
        }
        return this.permEnabled(perm);
    }

    public boolean permEnabled(EnumPermission perm) {
        return this.globalPerm.contains(perm);
    }

    public boolean tryCreateSubClaim(BlockPos pos1, BlockPos pos2) {
        return false;
    }

    public Claim getSubClaim(BlockPos pos) {
        for (Claim claim : this.subClaims)
            if (claim.insideClaim(pos))
                return claim;
        return null;
    }

    public boolean setPlayerGroup(UUID player, String group, boolean force) {
        if (group == null) {
            this.playersGroups.remove(player);
            this.setDirty();
            return true;
        }
        if (!this.playersGroups.containsKey(player) || force) {
            this.playersGroups.put(player, group);
            this.setDirty();
            return true;
        }
        return false;
    }

    public List<String> playersFromGroup(MinecraftServer server, String group) {
        List<UUID> l = Lists.newArrayList();
        this.playersGroups.forEach((uuid, g) -> {
            if (g.equals(group))
                l.add(uuid);
        });
        List<String> names = Lists.newArrayList();
        l.forEach(uuid -> {
            GameProfile prof = server.getUserCache().getByUuid(uuid);
            if (prof != null)
                names.add(prof.getName());
        });
        names.sort(null);
        return names;
    }

    public void editGlobalPerms(EnumPermission toggle) {
        if (this.globalPerm.contains(toggle))
            this.globalPerm.remove(toggle);
        else
            this.globalPerm.add(toggle);
        this.setDirty();
    }

    /**
     * Edit the permissions for a group. If not defined for the group creates a new default permission map for that group
     *
     * @param mode -1 = makes it resort to the global perm, 0 = deny perm, 1 = allow perm
     * @return If editing was successful or not
     */
    public boolean editPerms(PlayerEntity player, String group, EnumPermission perm, int mode) {
        if (player.getUuid().equals(this.owner) || this.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos())) {
            if (mode > 1)
                mode = -1;
            boolean has = this.permissions.containsKey(group);
            EnumMap<EnumPermission, Boolean> perms = has ? this.permissions.get(group) : new EnumMap<>(EnumPermission.class);
            if (mode == -1)
                perms.remove(perm);
            else
                perms.put(perm, mode == 1);
            if (!has)
                this.permissions.put(group, perms);
            this.setDirty();
            return true;
        }
        return false;
    }

    public boolean removePermGroup(PlayerEntity player, String group) {
        if (player.getUuid().equals(this.owner) || this.canInteract(player, EnumPermission.EDITPERMS, player.getBlockPos())) {
            this.permissions.remove(group);
            List<UUID> toRemove = Lists.newArrayList();
            this.playersGroups.forEach((uuid, g) -> {
                if (g.equals(group))
                    toRemove.add(uuid);
            });
            toRemove.forEach(this.playersGroups::remove);
            this.setDirty();
            return true;
        }
        return false;
    }

    public int groupHasPerm(String rank, EnumPermission perm) {
        if (!this.permissions.containsKey(rank) || !this.permissions.get(rank).containsKey(perm))
            return -1;
        return this.permissions.get(rank).get(perm) ? 1 : 0;
    }

    public List<String> groups() {
        List<String> l = Lists.newArrayList(this.permissions.keySet());
        l.sort(null);
        return l;
    }

    public boolean addSubClaims(Claim claim) {
        return false;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void readJson(JsonObject obj, UUID uuid) {
        JsonArray pos = obj.getAsJsonArray("PosxXzZY");
        this.minX = pos.get(0).getAsInt();
        this.maxX = pos.get(1).getAsInt();
        this.minZ = pos.get(2).getAsInt();
        this.maxZ = pos.get(3).getAsInt();
        this.minY = pos.get(4).getAsInt();
        this.owner = uuid;
        this.claimID = UUID.fromString(obj.get("ID").getAsString());
        this.globalPerm.clear();
        this.permissions.clear();
        this.subClaims.clear();
        if (obj.has("GlobalPerms")) {
            obj.getAsJsonArray("GlobalPerms").forEach(perm -> this.globalPerm.add(EnumPermission.valueOf(perm.getAsString())));
        }
        if (obj.has("PermGroup")) {
            JsonObject perms = obj.getAsJsonObject("PermGroup");
            perms.entrySet().forEach(key -> {
                EnumMap<EnumPermission, Boolean> map = new EnumMap<>(EnumPermission.class);
                JsonObject group = key.getValue().getAsJsonObject();
                group.entrySet().forEach(gkey -> map.put(EnumPermission.valueOf(gkey.getKey()), gkey.getValue().getAsBoolean()));
                this.permissions.put(key.getKey(), map);
            });
        }
        if (obj.has("PlayerPerms")) {
            JsonObject pl = obj.getAsJsonObject("PlayerPerms");
            pl.entrySet().forEach(key -> this.playersGroups.put(UUID.fromString(key.getKey()), key.getValue().getAsString()));
        }
        if (obj.has("SubClaims")) {
            obj.getAsJsonArray("SubClaims").forEach(sub -> this.subClaims.add(Claim.fromJson(sub.getAsJsonObject(), this.owner)));
        }
    }

    public JsonObject toJson(JsonObject obj) {
        JsonArray pos = new JsonArray();
        pos.add(this.minX);
        pos.add(this.maxX);
        pos.add(this.minZ);
        pos.add(this.maxZ);
        pos.add(this.minY);
        obj.add("PosxXzZY", pos);

        obj.addProperty("ID", this.claimID.toString());
        if (!this.globalPerm.isEmpty()) {
            JsonArray gPerm = new JsonArray();
            this.globalPerm.forEach(p -> gPerm.add(p.toString()));
            obj.add("GlobalPerms", gPerm);
        }
        if (!this.permissions.isEmpty()) {
            JsonObject perms = new JsonObject();
            this.permissions.forEach((s, pmap) -> {
                JsonObject group = new JsonObject();
                pmap.forEach((perm, bool) -> group.addProperty(perm.toString(), bool));
                perms.add(s, group);
            });
            obj.add("PermGroup", perms);
        }
        if (!this.playersGroups.isEmpty()) {
            JsonObject pl = new JsonObject();
            this.playersGroups.forEach((uuid, s) -> pl.addProperty(uuid.toString(), s));
            obj.add("PlayerPerms", pl);
        }
        if (!this.subClaims.isEmpty()) {
            JsonArray list = new JsonArray();
            this.subClaims.forEach(p -> list.add(p.toJson(new JsonObject())));
            obj.add("SubClaims", list);
        }
        return obj;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putIntArray("PosxXzZY", new int[]{this.minX, this.maxX, this.minZ, this.maxZ, this.minY});
        tag.putUuid("Owner", this.owner);
        tag.putUuid("ID", this.claimID);
        if (!this.globalPerm.isEmpty()) {
            ListTag list = new ListTag();
            this.globalPerm.forEach(p -> list.add(StringTag.of(p.toString())));
            tag.put("GlobalPerms", list);
        }
        if (!this.permissions.isEmpty()) {
            CompoundTag perms = new CompoundTag();
            this.permissions.forEach((s, pmap) -> {
                CompoundTag group = new CompoundTag();
                pmap.forEach((perm, bool) -> group.putBoolean(perm.toString(), bool));
                perms.put(s, group);
            });
            tag.put("PermGroup", perms);
        }
        if (!this.playersGroups.isEmpty()) {
            CompoundTag pl = new CompoundTag();
            this.playersGroups.forEach((uuid, s) -> pl.putString(uuid.toString(), s));
            tag.put("PlayerPerms", pl);
        }
        if (!this.subClaims.isEmpty()) {
            ListTag list = new ListTag();
            this.subClaims.forEach(p -> list.add(p.save(new CompoundTag())));
            tag.put("SubClaims", list);
        }
        return tag;
    }

    public void read(CompoundTag tag) {
        int[] pos = tag.getIntArray("PosxXzZY");
        this.minY = pos[0];
        this.maxX = pos[1];
        this.minZ = pos[2];
        this.maxZ = pos[3];
        this.minY = pos[4];
        this.owner = tag.getUuid("Owner");
        this.claimID = tag.getUuid("ID");
        this.globalPerm.clear();
        this.permissions.clear();
        this.subClaims.clear();
        if (tag.contains("GlobalPerms")) {
            tag.getList("GlobalPerms", 8).forEach(perm -> this.globalPerm.add(EnumPermission.valueOf(perm.asString())));
        }
        if (tag.contains("PermGroup")) {
            CompoundTag perms = tag.getCompound("PermGroup");
            perms.getKeys().forEach(key -> {
                EnumMap<EnumPermission, Boolean> map = new EnumMap<>(EnumPermission.class);
                CompoundTag group = perms.getCompound(key);
                group.getKeys().forEach(gkey -> map.put(EnumPermission.valueOf(gkey), group.getBoolean(gkey)));
                this.permissions.put(key, map);
            });
        }
        if (tag.contains("PlayerPerms")) {
            CompoundTag pl = tag.getCompound("PlayerPerms");
            pl.getKeys().forEach(key -> this.playersGroups.put(UUID.fromString(key), pl.getString(key)));
        }
        if (tag.contains("SubClaims")) {
            tag.getList("SubClaims", 10).forEach(sub -> this.subClaims.add(Claim.fromTag((CompoundTag) sub)));
        }
    }

    @Override
    public int hashCode() {
        return this.claimID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Claim)
            return this.claimID.equals(((Claim) obj).claimID);
        return false;
    }

    @Override
    public String toString() {
        return String.format("Claim:[Owner:%s, from: x=%d; z=%d, to: x=%d, z=%d", this.owner.toString(), this.minX, this.minZ, this.maxX, this.maxZ);
    }

    public String formattedClaim() {
        return String.format("[x=%d,z=%d] to: [x=%d,z=%d]", this.minX, this.minZ, this.maxX, this.maxZ);
    }
}
