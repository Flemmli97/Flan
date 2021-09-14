package io.github.flemmli97.flan.claim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Claim implements IPermissionContainer {

    private boolean dirty;
    private int minX, minZ, maxX, maxZ, minY;

    private UUID owner;

    private UUID claimID;
    private String claimName = "";
    private BlockPos homePos;
    private final Map<ClaimPermission, Boolean> globalPerm = new HashMap<>();
    private final Map<String, Map<ClaimPermission, Boolean>> permissions = new HashMap<>();

    private final Map<UUID, String> playersGroups = new HashMap<>();

    private final List<Claim> subClaims = new ArrayList<>();

    private UUID parent;
    private Claim parentClaim;

    /**
     * Flag for players tracking this claim
     */
    private boolean removed;

    private final ServerWorld world;

    private final Map<StatusEffect, Integer> potions = new HashMap<>();

    public Text enterTitle, enterSubtitle, leaveTitle, leaveSubtitle;

    private Claim(ServerWorld world) {
        this.world = world;
    }

    public Claim(BlockPos pos1, BlockPos pos2, ServerPlayerEntity creator) {
        this(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ(), Math.min(pos1.getY(), pos2.getY()), creator.getUuid(), creator.getServerWorld(), PlayerClaimData.get(creator).playerDefaultGroups().isEmpty());
        PlayerClaimData.get(creator).playerDefaultGroups().forEach((s, m) -> m.forEach((perm, bool) -> this.editPerms(null, s, perm, bool ? 1 : 0, true)));
    }

    public Claim(BlockPos pos1, BlockPos pos2, UUID creator, ServerWorld world) {
        this(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ(), Math.min(pos1.getY(), pos2.getY()), creator, world);
    }

    public Claim(int x1, int x2, int z1, int z2, int minY, UUID creator, ServerWorld world) {
        this(x1, x2, z1, z2, minY, creator, world, true);
    }

    public Claim(int x1, int x2, int z1, int z2, int minY, UUID creator, ServerWorld world, boolean setDefaultGroups) {
        this.minX = Math.min(x1, x2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxZ = Math.max(z1, z2);
        this.minY = Math.max(0, minY);
        this.owner = creator;
        this.world = world;
        this.homePos = this.getInitCenterPos();
        this.setDirty(true);
        PermissionRegistry.getPerms().stream().filter(perm -> perm.defaultVal).forEach(perm -> this.globalPerm.put(perm, true));
        if (setDefaultGroups)
            ConfigHandler.config.defaultGroups.forEach((s, m) -> m.forEach((perm, bool) -> this.editPerms(null, s, perm, bool ? 1 : 0, true)));
    }

    public static Claim fromJson(JsonObject obj, UUID owner, ServerWorld world) {
        Claim claim = new Claim(world);
        claim.readJson(obj, owner);
        ClaimUpdater.updateClaim(claim);
        return claim;
    }

    private BlockPos getInitCenterPos() {
        BlockPos center = new BlockPos(this.minX + (this.maxX - this.minX) * 0.5, 0, this.minZ + (this.maxZ - this.minZ) * 0.5);
        int y = this.world.getChunk(center.getX() >> 4, center.getZ() >> 4, ChunkStatus.HEIGHTMAPS).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, center.getX() & 15, center.getZ() & 15);
        return new BlockPos(center.getX(), y + 1, center.getZ());
    }

    private BlockPos getDefaultCenterPos() {
        BlockPos center = new BlockPos(this.minX + (this.maxX - this.minX) * 0.5, 0, this.minZ + (this.maxZ - this.minZ) * 0.5);
        return new BlockPos(center.getX(), 255, center.getZ());
    }

    public void setClaimID(UUID uuid) {
        this.claimID = uuid;
        this.setDirty(true);
    }

    public void extendDownwards(BlockPos pos) {
        this.minY = pos.getY();
        this.setDirty(true);
    }

    public UUID getClaimID() {
        return this.claimID;
    }

    public String getClaimName() {
        return this.claimName;
    }

    public void setClaimName(String name) {
        this.claimName = name;
        this.setDirty(true);
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Optional<ServerPlayerEntity> getOwnerPlayer() {
        if (this.getOwner() != null)
            return Optional.ofNullable(this.world.getServer().getPlayerManager().getPlayer(this.getOwner()));
        return Optional.empty();
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public Claim parentClaim() {
        if (this.parent == null)
            return null;
        if (this.parentClaim == null) {
            ClaimStorage storage = ClaimStorage.get(this.world);
            this.parentClaim = storage.getFromUUID(this.parent);
        }
        return this.parentClaim;
    }

    public void copySizes(Claim claim) {
        this.minX = claim.minX;
        this.maxX = claim.maxX;
        this.minZ = claim.minZ;
        this.maxZ = claim.maxZ;
        this.minY = claim.minY;
        this.removed = false;
        this.setDirty(true);
    }

    public void toggleAdminClaim(ServerPlayerEntity player, boolean flag) {
        if (!flag)
            this.transferOwner(player.getUuid());
        else {
            this.owner = null;
            this.subClaims.forEach(claim -> claim.owner = null);
        }
        this.setDirty(true);
    }

    public boolean isAdminClaim() {
        return this.owner == null;
    }

    public void transferOwner(UUID player) {
        this.owner = player;
        this.subClaims.forEach(claim -> claim.owner = player);
        this.setDirty(true);
    }

    public int getPlane() {
        return (this.maxX - this.minX + 1) * (this.maxZ - this.minZ + 1);
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
        return this.minX <= other.maxX && this.maxX >= other.minX && this.minZ <= other.maxZ && this.maxZ >= other.minZ;
    }

    public boolean intersects(Box box) {
        return this.minX < box.maxX && this.maxX + 1 > box.minX && this.minZ < box.maxZ && this.maxZ + 1 > box.minZ && box.maxY >= this.minY;
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

    @Override
    public boolean canInteract(ServerPlayerEntity player, ClaimPermission perm, BlockPos pos, boolean message) {
        ActionResult res = ClaimPermissionCheck.check(player, perm, pos);
        if (res != ActionResult.PASS)
            return res != ActionResult.FAIL;
        if (perm != null) {
            ClaimPermission.PermissionFlag flag = perm.test.test(this, player, pos);
            if (flag != ClaimPermission.PermissionFlag.PASS) {
                if (flag == ClaimPermission.PermissionFlag.NO) {
                    if (message)
                        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
                    return false;
                }
                return true;
            }
        }
        if (!this.isAdminClaim()) {
            Config.GlobalType global = ConfigHandler.config.getGlobal(this.world, perm);
            if (!global.canModify()) {
                if (global.getValue() || (player != null && this.isAdminIgnore(player)))
                    return true;
                if (message)
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
                return false;
            }
            if (ConfigHandler.config.offlineProtectActivation != -1 && (LogoutTracker.getInstance(this.world.getServer()).justLoggedOut(this.getOwner()) || this.getOwnerPlayer().isPresent())) {
                return global == Config.GlobalType.NONE || global.getValue();
            }
        }
        if (PermissionRegistry.globalPerms().contains(perm)) {
            for (Claim claim : this.subClaims) {
                if (claim.insideClaim(pos)) {
                    return claim.canInteract(player, perm, pos, message);
                }
            }
            if (this.hasPerm(perm))
                return true;
            if (message)
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
            return false;
        }
        if (this.isAdminIgnore(player) || player.getUuid().equals(this.owner))
            return true;
        if (perm != PermissionRegistry.EDITCLAIM && perm != PermissionRegistry.EDITPERMS)
            for (Claim claim : this.subClaims) {
                if (claim.insideClaim(pos)) {
                    return claim.canInteract(player, perm, pos, message);
                }
            }
        if (this.playersGroups.containsKey(player.getUuid())) {
            Map<ClaimPermission, Boolean> map = this.permissions.get(this.playersGroups.get(player.getUuid()));
            if (map != null && map.containsKey(perm)) {
                if (map.get(perm))
                    return true;
                if (message)
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
                return false;
            }
        }
        if (this.hasPerm(perm))
            return true;
        if (message)
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
        return false;
    }

    private boolean isAdminIgnore(ServerPlayerEntity player) {
        return player == null || ((this.isAdminClaim() && player.hasPermissionLevel(2)) || PlayerClaimData.get(player).isAdminIgnoreClaim());
    }

    /**
     * @return -1 for default, 0 for false, 1 for true
     */
    public int permEnabled(ClaimPermission perm) {
        return !this.globalPerm.containsKey(perm) ? -1 : this.globalPerm.get(perm) ? 1 : 0;
    }

    private boolean hasPerm(ClaimPermission perm) {
        if (this.parentClaim() == null)
            return this.permEnabled(perm) == 1;
        if (this.permEnabled(perm) == -1)
            return this.parentClaim().permEnabled(perm) == 1;
        return this.permEnabled(perm) == 1;
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();
        for (Claim claim : this.subClaims)
            if (claim.claimID.equals(uuid)) {
                return this.generateUUID();
            }
        return uuid;
    }

    public Set<Claim> tryCreateSubClaim(BlockPos pos1, BlockPos pos2) {
        Claim sub = new Claim(pos1, new BlockPos(pos2.getX(), 0, pos2.getZ()), this.owner, this.world);
        sub.setClaimID(this.generateUUID());
        Set<Claim> conflicts = new HashSet<>();
        for (Claim other : this.subClaims)
            if (sub.intersects(other)) {
                conflicts.add(other);
            }
        if (conflicts.isEmpty()) {
            sub.parent = this.claimID;
            sub.parentClaim = this;
            this.subClaims.add(sub);
            this.setDirty(true);
        }
        return conflicts;
    }

    public void addSubClaimGriefprevention(Claim claim) {
        claim.setClaimID(this.generateUUID());
        claim.parent = this.claimID;
        claim.parentClaim = this;
        this.subClaims.add(claim);
        this.setDirty(true);
    }

    public Claim getSubClaim(BlockPos pos) {
        for (Claim claim : this.subClaims)
            if (claim.insideClaim(pos))
                return claim;
        return null;
    }

    public boolean deleteSubClaim(Claim claim) {
        claim.remove();
        this.setDirty(true);
        return this.subClaims.remove(claim);
    }

    public List<Claim> getAllSubclaims() {
        return ImmutableList.copyOf(this.subClaims);
    }

    public Set<Claim> resizeSubclaim(Claim claim, BlockPos from, BlockPos to) {
        int[] dims = claim.getDimensions();
        BlockPos opposite = new BlockPos(dims[0] == from.getX() ? dims[1] : dims[0], dims[4], dims[2] == from.getZ() ? dims[3] : dims[2]);
        Claim newClaim = new Claim(opposite, to, claim.claimID, this.world);
        Set<Claim> conflicts = new HashSet<>();
        for (Claim other : this.subClaims)
            if (!claim.equals(other) && newClaim.intersects(other))
                conflicts.add(other);
        if (conflicts.isEmpty()) {
            claim.copySizes(newClaim);
            this.setDirty(true);
        }
        return conflicts;
    }

    public boolean setPlayerGroup(UUID player, String group, boolean force) {
        if (player.equals(this.owner))
            return false;
        if (group == null) {
            this.playersGroups.remove(player);
            this.setDirty(true);
            return true;
        }
        if (!this.playersGroups.containsKey(player) || force) {
            this.playersGroups.put(player, group);
            this.setDirty(true);
            return true;
        }
        return false;
    }

    public List<String> playersFromGroup(MinecraftServer server, String group) {
        List<UUID> l = new ArrayList<>();
        this.playersGroups.forEach((uuid, g) -> {
            if (g.equals(group))
                l.add(uuid);
        });
        List<String> names = new ArrayList<>();
        l.forEach(uuid -> {
            GameProfile prof = server.getUserCache().getByUuid(uuid);
            if (prof != null)
                names.add(prof.getName());
        });
        names.sort(null);
        return names;
    }

    public boolean editGlobalPerms(ServerPlayerEntity player, ClaimPermission toggle, int mode) {
        if ((player != null && !this.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) || (!this.isAdminClaim() && ConfigHandler.config.globallyDefined(this.world, toggle)))
            return false;
        if (mode > 1)
            mode = -1;
        if (mode == -1)
            this.globalPerm.remove(toggle);
        else
            this.globalPerm.put(toggle, mode == 1);
        this.setDirty(true);
        return true;
    }

    public boolean editPerms(ServerPlayerEntity player, String group, ClaimPermission perm, int mode) {
        return this.editPerms(player, group, perm, mode, false);
    }

    /**
     * Edit the permissions for a group. If not defined for the group creates a new default permission map for that group
     *
     * @param mode -1 = makes it resort to the global perm, 0 = deny perm, 1 = allow perm
     * @return If editing was successful or not
     */
    public boolean editPerms(ServerPlayerEntity player, String group, ClaimPermission perm, int mode, boolean alwaysCan) {
        if (PermissionRegistry.globalPerms().contains(perm) || (!this.isAdminClaim() && ConfigHandler.config.globallyDefined(this.world, perm)))
            return false;
        if (alwaysCan || this.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) {
            if (mode > 1)
                mode = -1;
            boolean has = this.permissions.containsKey(group);
            Map<ClaimPermission, Boolean> perms = has ? this.permissions.get(group) : new HashMap<>();
            if (mode == -1)
                perms.remove(perm);
            else
                perms.put(perm, mode == 1);
            if (!has)
                this.permissions.put(group, perms);
            this.setDirty(true);
            return true;
        }
        return false;
    }

    public boolean removePermGroup(ServerPlayerEntity player, String group) {
        if (this.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos())) {
            this.permissions.remove(group);
            List<UUID> toRemove = new ArrayList<>();
            this.playersGroups.forEach((uuid, g) -> {
                if (g.equals(group))
                    toRemove.add(uuid);
            });
            toRemove.forEach(this.playersGroups::remove);
            this.setDirty(true);
            return true;
        }
        return false;
    }

    public int groupHasPerm(String rank, ClaimPermission perm) {
        if (!this.permissions.containsKey(rank) || !this.permissions.get(rank).containsKey(perm))
            return -1;
        return this.permissions.get(rank).get(perm) ? 1 : 0;
    }

    public List<String> groups() {
        List<String> l = new ArrayList<>(this.permissions.keySet());
        l.sort(null);
        return l;
    }

    public boolean setHomePos(BlockPos homePos) {
        if (this.insideClaim(homePos)) {
            this.homePos = homePos;
            this.setDirty(true);
            return true;
        }
        return false;
    }

    public void addPotion(StatusEffect effect, int amplifier) {
        this.potions.put(effect, amplifier);
        this.setDirty(true);
    }

    public void removePotion(StatusEffect effect) {
        this.potions.remove(effect);
        this.setDirty(true);
    }

    public Map<StatusEffect, Integer> getPotions() {
        return this.potions;
    }

    public void applyEffects(ServerPlayerEntity player) {
        if (player.world.getTime() % 80 == 0)
            this.potions.forEach((effect, amp) -> player.applyStatusEffect(new StatusEffectInstance(effect, 200, amp - 1, true, false)));
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    public void setEnterTitle(Text title, Text sub) {
        this.enterTitle = title;
        this.enterSubtitle = sub;
        this.setDirty(true);
    }

    public void setLeaveTitle(Text title, Text sub) {
        this.leaveTitle = title;
        this.leaveSubtitle = sub;
        this.setDirty(true);
    }

    public void displayEnterTitle(ServerPlayerEntity player) {
        if (this.enterTitle != null) {
            player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, this.enterTitle));
            if (this.enterSubtitle != null)
                player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, this.enterSubtitle));
        }
    }

    public void displayLeaveTitle(ServerPlayerEntity player) {
        if (this.leaveTitle != null) {
            player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, this.leaveTitle));
            if (this.leaveSubtitle != null)
                player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, this.leaveSubtitle));
        }
    }

    /**
     * Only marks non sub claims
     */
    public void setDirty(boolean flag) {
        if (this.parentClaim() != null)
            this.parentClaim().setDirty(flag);
        else
            this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void readJson(JsonObject obj, UUID uuid) {
        try {
            this.claimID = UUID.fromString(obj.get("ID").getAsString());
            this.claimName = ConfigHandler.fromJson(obj, "Name", "");
            JsonArray pos = obj.getAsJsonArray("PosxXzZY");
            this.minX = pos.get(0).getAsInt();
            this.maxX = pos.get(1).getAsInt();
            this.minZ = pos.get(2).getAsInt();
            this.maxZ = pos.get(3).getAsInt();
            this.minY = pos.get(4).getAsInt();
            JsonArray home = ConfigHandler.arryFromJson(obj, "Home");
            if (home.size() != 3)
                this.homePos = this.getDefaultCenterPos();
            else {
                this.homePos = new BlockPos(home.get(0).getAsInt(), home.get(1).getAsInt(), home.get(2).getAsInt());
            }
            JsonObject potion = ConfigHandler.fromJson(obj, "Potions");
            potion.entrySet().forEach(e -> this.potions.put(CrossPlatformStuff.registryStatusEffects().getFromId(new Identifier(e.getKey())), e.getValue().getAsInt()));
            if (ConfigHandler.fromJson(obj, "AdminClaim", false))
                this.owner = null;
            else
                this.owner = uuid;
            this.globalPerm.clear();
            this.permissions.clear();
            this.subClaims.clear();
            if (obj.has("Parent"))
                this.parent = UUID.fromString(obj.get("Parent").getAsString());
            if (obj.has("GlobalPerms")) {
                if (this.parent == null) {
                    obj.getAsJsonArray("GlobalPerms").forEach(perm -> {
                        try {
                            this.globalPerm.put(PermissionRegistry.get(perm.getAsString()), true);
                        } catch (NullPointerException e) {
                            Flan.logger.error("Error reading permission {} from json for claim {} belonging to {}. No such permission exist", perm.getAsString(), this.claimID, this.owner);
                        }
                    });
                } else {
                    obj.getAsJsonObject("GlobalPerms").entrySet().forEach(entry -> {
                        try {
                            this.globalPerm.put(PermissionRegistry.get(entry.getKey()), entry.getValue().getAsBoolean());
                        } catch (NullPointerException e) {
                            Flan.logger.error("Error reading permission {} from json for claim {} belonging to {}. No such permission exist", entry.getKey(), this.claimID, this.owner);
                        }
                    });
                }
            }
            ConfigHandler.fromJson(obj, "PermGroup").entrySet().forEach(key -> {
                Map<ClaimPermission, Boolean> map = new HashMap<>();
                JsonObject group = key.getValue().getAsJsonObject();
                group.entrySet().forEach(gkey -> {
                    try {
                        map.put(PermissionRegistry.get(gkey.getKey()), gkey.getValue().getAsBoolean());
                    } catch (NullPointerException e) {
                        Flan.logger.error("Error reading permission {} from json for claim {} belonging to {}. No such permission exist", gkey.getKey(), this.claimID, this.owner);
                    }
                });
                this.permissions.put(key.getKey(), map);
            });
            ConfigHandler.fromJson(obj, "PlayerPerms").entrySet()
                    .forEach(key -> this.playersGroups.put(UUID.fromString(key.getKey()), key.getValue().getAsString()));
            ConfigHandler.arryFromJson(obj, "SubClaims")
                    .forEach(sub -> this.subClaims.add(Claim.fromJson(sub.getAsJsonObject(), this.owner, this.world)));
        } catch (Exception e) {
            throw new IllegalStateException("Error reading claim data for claim " + uuid);
        }
    }

    public JsonObject toJson(JsonObject obj) {
        obj.addProperty("ID", this.claimID.toString());
        obj.addProperty("Name", this.claimName);
        JsonArray pos = new JsonArray();
        pos.add(this.minX);
        pos.add(this.maxX);
        pos.add(this.minZ);
        pos.add(this.maxZ);
        pos.add(this.minY);
        obj.add("PosxXzZY", pos);
        JsonArray home = new JsonArray();
        home.add(this.homePos.getX());
        home.add(this.homePos.getY());
        home.add(this.homePos.getZ());
        obj.add("Home", home);
        JsonObject potions = new JsonObject();
        this.potions.forEach((effect, amp) -> potions.addProperty(CrossPlatformStuff.registryStatusEffects().getIDFrom(effect).toString(), amp));
        obj.add("Potions", potions);
        if (this.parent != null)
            obj.addProperty("Parent", this.parent.toString());
        if (!this.globalPerm.isEmpty()) {
            JsonElement gPerm;
            if (this.parent == null) {
                gPerm = new JsonArray();
                this.globalPerm.forEach((perm, bool) -> {
                    if (bool)
                        ((JsonArray) gPerm).add(perm.id);
                });
            } else {
                gPerm = new JsonObject();
                this.globalPerm.forEach((perm, bool) -> ((JsonObject) gPerm).addProperty(perm.id, bool));
            }
            obj.add("GlobalPerms", gPerm);
        }
        if (!this.permissions.isEmpty()) {
            JsonObject perms = new JsonObject();
            this.permissions.forEach((s, pmap) -> {
                JsonObject group = new JsonObject();
                pmap.forEach((perm, bool) -> group.addProperty(perm.id, bool));
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

    @Override
    public int hashCode() {
        return this.claimID == null ? Arrays.hashCode(this.getDimensions()) : this.claimID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Claim) {
            Claim other = (Claim) obj;
            if (this.claimID == null && other.claimID == null)
                return Arrays.equals(this.getDimensions(), ((Claim) obj).getDimensions());
            if (this.claimID != null)
                return this.claimID.equals(((Claim) obj).claimID);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Claim:[ID=%s, Owner=%s, from: [x=%d,z=%d], to: [x=%d,z=%d]", this.claimID != null ? this.claimID.toString() : "null", this.owner != null ? this.owner.toString() : "Admin", this.minX, this.minZ, this.maxX, this.maxZ);
    }

    public String formattedClaim() {
        if (this.claimName.isEmpty())
            return String.format("[x=%d,z=%d] - [x=%d,z=%d]", this.minX, this.minZ, this.maxX, this.maxZ);
        return String.format("%s:[x=%d,z=%d] - [x=%d,z=%d]", this.claimName, this.minX, this.minZ, this.maxX, this.maxZ);
    }

    public List<Text> infoString(ServerPlayerEntity player, InfoType infoType) {
        boolean perms = this.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos());
        List<Text> l = new ArrayList<>();
        l.add(PermHelper.simpleColoredText("=============================================", Formatting.GREEN));
        GameProfile prof = this.owner != null ? player.getServer().getUserCache().getByUuid(this.owner) : null;
        String ownerName = this.isAdminClaim() ? "Admin" : prof != null ? prof.getName() : "<UNKNOWN>";
        if (this.parent == null) {
            if (this.claimName.isEmpty())
                l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBasicInfo, ownerName, this.minX, this.minZ, this.maxX, this.maxZ, this.subClaims.size()), Formatting.GOLD));
            else
                l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBasicInfoNamed, ownerName, this.minX, this.minZ, this.maxX, this.maxZ, this.subClaims.size(), this.claimName), Formatting.GOLD));
        } else {
            if (this.claimName.isEmpty())
                l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBasicInfoSub, ownerName, this.minX, this.minZ, this.maxX, this.maxZ), Formatting.GOLD));
            else
                l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBasicInfoSubNamed, ownerName, this.minX, this.minZ, this.maxX, this.maxZ, this.claimName), Formatting.GOLD));
        }
        if (perms) {
            if (infoType == InfoType.ALL || infoType == InfoType.GLOBAL)
                l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimInfoPerms, this.globalPerm), Formatting.RED));
            if (infoType == InfoType.ALL || infoType == InfoType.GROUP) {
                l.add(PermHelper.simpleColoredText(ConfigHandler.lang.claimGroupInfoHeader, Formatting.RED));
                Map<String, List<String>> nameToGroup = new HashMap<>();
                for (Map.Entry<UUID, String> e : this.playersGroups.entrySet()) {
                    GameProfile pgroup = player.getServer().getUserCache().getByUuid(e.getKey());
                    if (prof != null) {
                        nameToGroup.merge(e.getValue(), Lists.newArrayList(pgroup.getName()), (old, val) -> {
                            old.add(pgroup.getName());
                            return old;
                        });
                    }
                }
                for (Map.Entry<String, Map<ClaimPermission, Boolean>> e : this.permissions.entrySet()) {
                    l.add(PermHelper.simpleColoredText(String.format("  %s:", e.getKey()), Formatting.DARK_RED));
                    l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimGroupPerms, e.getValue()), Formatting.RED));
                    l.add(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimGroupPlayers, nameToGroup.getOrDefault(e.getKey(), new ArrayList<>())), Formatting.RED));
                }
            }
        }
        l.add(PermHelper.simpleColoredText("=============================================", Formatting.GREEN));
        return l;
    }

    public enum InfoType {
        ALL,
        SIMPLE,
        GLOBAL,
        GROUP
    }

    interface ClaimUpdater {

        Map<Integer, ClaimUpdater> updater = Config.createHashMap(map -> {
            map.put(2, claim -> claim.globalPerm.put(PermissionRegistry.LOCKITEMS, true));
        });

        static void updateClaim(Claim claim) {
            updater.entrySet().stream().filter(e -> e.getKey() > ConfigHandler.config.preConfigVersion).map(Map.Entry::getValue)
                    .forEach(up -> up.update(claim));
        }

        void update(Claim claim);
    }
}
