package io.github.flemmli97.flan.player;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ParticleIndicators;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerClaimData implements IPlayerData {

    private int claimBlocks, additionalClaimBlocks, confirmTick, actionCooldown;
    //Scoreboard tracking
    private int usedBlocks;

    private int lastBlockTick, trappedTick = -1, deathPickupTick;
    private Vec3d trappedPos;
    private BlockPos tpPos;
    private EnumEditMode mode = EnumEditMode.DEFAULT;
    private Claim editingClaim;
    private ClaimDisplay displayEditing;

    private BlockPos firstCorner;
    private int[] cornerRenderPos;

    private final Set<ClaimDisplay> claimDisplayList = new HashSet<>();
    private final Set<ClaimDisplay> displayToAdd = new HashSet<>();

    private final ServerPlayerEntity player;

    private boolean confirmDeleteAll, adminIgnoreClaim, claimBlockMessage;

    private final Map<String, Map<ClaimPermission, Boolean>> defaultGroups = new HashMap<>();

    private boolean shouldProtectDrop, calculateShouldDrop = true;

    public PlayerClaimData(ServerPlayerEntity player) {
        this.player = player;
        this.claimBlocks = ConfigHandler.config.startingBlocks;
    }

    public static PlayerClaimData get(ServerPlayerEntity player) {
        return ((IPlayerClaimImpl) player).get();
    }

    @Override
    public int getClaimBlocks() {
        return this.claimBlocks;
    }

    public void setClaimBlocks(int amount) {
        this.claimBlocks = amount;
        updateScoreFor(this.player, ClaimCriterias.AMOUNT, this.claimBlocks + this.additionalClaimBlocks);
        updateScoreFor(this.player, ClaimCriterias.FREE, this.claimBlocks + this.additionalClaimBlocks - this.usedBlocks);
    }

    public boolean addClaimBlocks(int amount) {
        if (this.canIncrease(this.claimBlocks + amount)) {
            this.setClaimBlocks(this.claimBlocks + amount);
            return true;
        }
        return false;
    }

    private boolean canIncrease(int blocks) {
        return PermissionNodeHandler.permBelowEqVal(this.player, PermissionNodeHandler.permClaimBlocks, blocks, ConfigHandler.config.maxClaimBlocks);
    }

    @Override
    public int getAdditionalClaims() {
        return this.additionalClaimBlocks;
    }

    @Override
    public void setAdditionalClaims(int amount) {
        this.additionalClaimBlocks = Math.max(0, amount);
        updateScoreFor(this.player, ClaimCriterias.AMOUNT, this.claimBlocks + this.additionalClaimBlocks);
        updateScoreFor(this.player, ClaimCriterias.FREE, this.claimBlocks + this.additionalClaimBlocks - this.usedBlocks);
    }

    @Override
    public boolean canUseClaimBlocks(int amount) {
        if (ConfigHandler.config.maxClaimBlocks == -1)
            return true;
        int usedClaimsBlocks = this.usedClaimBlocks();
        return usedClaimsBlocks + amount <= this.claimBlocks + this.additionalClaimBlocks;
    }

    @Override
    public int usedClaimBlocks() {
        return this.calculateUsedClaimBlocks();
    }

    /**
     * To prevent double processing. most notably when right clicking on a block and the block doesnt do anything ->
     * block onUse -> item use. Might be a better way but for now this. But also handles having
     * same items on both hands triggering
     */
    public void setClaimActionCooldown() {
        this.actionCooldown = 10;
    }

    public boolean claimCooldown() {
        return this.actionCooldown > 0;
    }

    public Claim currentEdit() {
        return this.editingClaim;
    }

    public void setEditClaim(Claim claim, int height) {
        if (claim != null)
            this.displayEditing = new ClaimDisplay(claim, EnumDisplayType.EDIT, height);
        else
            this.displayEditing = null;
        this.editingClaim = claim;
    }

    public void addDisplayClaim(IPermissionContainer cont, EnumDisplayType type, int height) {
        if (cont instanceof Claim) {
            Claim claim = (Claim) cont;
            this.displayToAdd.add(new ClaimDisplay(claim, type, height));
            if (type == EnumDisplayType.MAIN)
                for (Claim sub : claim.getAllSubclaims())
                    this.displayToAdd.add(new ClaimDisplay(sub, EnumDisplayType.SUB, height));
        }
    }

    public EnumEditMode getEditMode() {
        return this.mode;
    }

    public void setEditMode(EnumEditMode mode) {
        this.mode = mode;
        this.setEditClaim(null, 0);
        this.setEditingCorner(null);
    }

    public BlockPos editingCorner() {
        return this.firstCorner;
    }

    public void setEditingCorner(BlockPos pos) {
        if (pos != null) {
            BlockState state = this.player.world.getBlockState(pos);
            while (state.isAir() || state.getMaterial().isReplaceable()) {
                pos = pos.down();
                state = this.player.world.getBlockState(pos);
            }
            this.cornerRenderPos = ClaimDisplay.getPosFrom(this.player.getServerWorld(), pos.getX(), pos.getZ(), pos.getY());
        } else
            this.cornerRenderPos = null;
        this.firstCorner = pos;
    }

    public boolean confirmedDeleteAll() {
        return this.confirmDeleteAll;
    }

    public void setConfirmDeleteAll(boolean flag) {
        this.confirmDeleteAll = flag;
        this.confirmTick = 400;
    }

    public void setAdminIgnoreClaim(boolean flag) {
        this.adminIgnoreClaim = flag;
    }

    public boolean isAdminIgnoreClaim() {
        return this.adminIgnoreClaim;
    }

    public Map<String, Map<ClaimPermission, Boolean>> playerDefaultGroups() {
        return this.defaultGroups;
    }

    public boolean editDefaultPerms(String group, ClaimPermission perm, int mode) {
        if (PermissionRegistry.globalPerms().contains(perm) || ConfigHandler.config.globallyDefined(this.player.getServerWorld(), perm))
            return false;
        if (mode > 1)
            mode = -1;
        boolean has = this.defaultGroups.containsKey(group);
        Map<ClaimPermission, Boolean> perms = has ? this.defaultGroups.get(group) : new HashMap<>();
        if (mode == -1)
            perms.remove(perm);
        else
            perms.put(perm, mode == 1);
        if (!has)
            this.defaultGroups.put(group, perms);
        return true;
    }

    public boolean setTrappedRescue() {
        Claim claim = ((IPlayerClaimImpl) this.player).getCurrentClaim();
        if (this.trappedTick < 0 && claim != null && !this.player.getUuid().equals(claim.getOwner())) {
            this.trappedTick = 101;
            this.trappedPos = this.player.getPos();
            return true;
        }
        return false;
    }

    public boolean setTeleportTo(BlockPos tp) {
        if (this.trappedTick < 0) {
            this.trappedTick = 101;
            this.trappedPos = this.player.getPos();
            this.tpPos = tp;
            return true;
        }
        return false;
    }

    public void tick(Claim currentClaim, Consumer<Claim> cons) {
        EntityInteractEvents.updateClaim(this.player, currentClaim, cons);
        boolean tool = this.player.getMainHandStack().getItem() == ConfigHandler.config.claimingItem
                || this.player.getOffHandStack().getItem() == ConfigHandler.config.claimingItem;
        boolean stick = this.player.getMainHandStack().getItem() == ConfigHandler.config.inspectionItem
                || this.player.getOffHandStack().getItem() == ConfigHandler.config.inspectionItem;
        this.displayToAdd.forEach(add -> {
            if (!this.claimDisplayList.add(add)) {
                this.claimDisplayList.removeIf(c -> c.equals(add) && c.type != add.type);
                this.claimDisplayList.add(add);
            }
        });
        this.displayToAdd.clear();
        this.claimDisplayList.removeIf(d -> d.display(this.player, !tool && !stick));
        if (++this.lastBlockTick > ConfigHandler.config.ticksForNextBlock) {
            this.addClaimBlocks(1);
            this.lastBlockTick = 0;
        }
        if (this.cornerRenderPos != null) {
            if (this.cornerRenderPos[1] != this.cornerRenderPos[2])
                this.player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleIndicators.SETCORNER, true, this.cornerRenderPos[0] + 0.5, this.cornerRenderPos[2] + 0.25, this.cornerRenderPos[3] + 0.5, 0, 0.25f, 0, 0, 2));
            this.player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleIndicators.SETCORNER, true, this.cornerRenderPos[0] + 0.5, this.cornerRenderPos[1] + 0.25, this.cornerRenderPos[3] + 0.5, 0, 0.25f, 0, 0, 2));
        }
        if (--this.confirmTick < 0)
            this.confirmDeleteAll = false;
        if (this.displayEditing != null)
            this.displayEditing.display(this.player, !tool && !stick);
        if (!tool) {
            this.setEditingCorner(null);
            this.setEditClaim(null, 0);
            this.claimBlockMessage = false;
        } else if (!this.claimBlockMessage) {
            this.claimBlockMessage = true;
            this.player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.claimBlocksFormat,
                    this.getClaimBlocks(), this.getAdditionalClaims(), this.usedClaimBlocks()), Formatting.GOLD), false);
        }
        this.actionCooldown--;
        if (--this.trappedTick >= 0) {
            if (this.trappedTick == 0) {
                if (this.tpPos != null) {
                    BlockPos.Mutable tpTo = this.tpPos.mutableCopy();
                    Vec3d offset = new Vec3d(this.tpPos.getX() + 0.5, this.tpPos.getY() + 0.01, this.tpPos.getZ() + 0.5).subtract(this.player.getPos());
                    int yHighest = this.player.world.getChunk(this.tpPos.getX() >> 4, this.tpPos.getZ() >> 4, ChunkStatus.HEIGHTMAPS).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, this.tpPos.getX() & 15, this.tpPos.getZ() & 15);
                    Box box = this.player.getBoundingBox().offset(offset);
                    if (tpTo.getY() < yHighest) {
                        while (tpTo.getY() < yHighest) {
                            if (this.player.world.getBlockCollisions(this.player, box, (state, pos) -> true).allMatch(VoxelShape::isEmpty))
                                break;
                            tpTo.set(tpTo.getX(), tpTo.getY() + 1, tpTo.getZ());
                            box = box.offset(0, 1, 0);
                        }
                        tpTo.set(tpTo.getX(), tpTo.getY() + 1, tpTo.getZ());
                    } else
                        tpTo.set(tpTo.getX(), yHighest, tpTo.getZ());
                    if(this.player.hasVehicle())
                        this.player.stopRiding();
                    this.player.teleport(tpTo.getX() + 0.5, tpTo.getY(), tpTo.getZ() + 0.5);
                    this.tpPos = null;
                } else {
                    Vec3d tp = TeleportUtils.getTeleportPos(this.player, this.player.getPos(), ClaimStorage.get(this.player.getServerWorld()),
                            ((IPlayerClaimImpl) this.player).getCurrentClaim().getDimensions(),
                            TeleportUtils.roundedBlockPos(this.player.getPos()).mutableCopy(), (claim, nPos) -> false);
                    if(this.player.hasVehicle())
                        this.player.stopRiding();
                    this.player.teleport(tp.getX(), tp.getY(), tp.getZ());
                }
            } else if (this.player.getPos().squaredDistanceTo(this.trappedPos) > 0.15) {
                this.trappedTick = -1;
                this.trappedPos = null;
                this.player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.trappedMove, Formatting.RED), false);
            }
        }
        this.deathPickupTick--;
        if (!this.player.isDead())
            this.calculateShouldDrop = true;
    }

    public void unlockDeathItems() {
        this.deathPickupTick = 1200;
    }

    public boolean deathItemsUnlocked() {
        return this.deathPickupTick > 0;
    }

    public void clone(PlayerClaimData data) {
        this.claimBlocks = data.claimBlocks;
        this.additionalClaimBlocks = data.additionalClaimBlocks;
        this.defaultGroups.clear();
        this.defaultGroups.putAll(data.defaultGroups);
        if (data.setDeathItemOwner())
            this.player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.unlockDropsCmd, "/flan unlockDrops"), Formatting.GOLD), false);
    }

    public void updateScoreboard() {
        int claims = this.updateClaimScores();
        updateScoreFor(this.player, ClaimCriterias.USED, this.usedBlocks);
        updateScoreFor(this.player, ClaimCriterias.FREE, this.claimBlocks + this.additionalClaimBlocks - this.usedBlocks);
        updateScoreFor(this.player, ClaimCriterias.CLAIMS, claims);
    }

    private int updateClaimScores() {
        int usedClaimsBlocks = 0;
        int claimsAmount = 0;
        for (ServerWorld world : this.player.getServer().getWorlds()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.player.getUuid());
            if (claims != null) {
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
                claimsAmount += claims.size();
            }
        }
        this.usedBlocks = usedClaimsBlocks;
        return claimsAmount;
    }

    private int calculateUsedClaimBlocks() {
        int usedClaimsBlocks = 0;
        for (ServerWorld world : this.player.getServer().getWorlds()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.player.getUuid());
            if (claims != null) {
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
            }
        }
        return usedClaimsBlocks;
    }

    public boolean setDeathItemOwner() {
        if (!this.player.isDead())
            return false;
        if (this.calculateShouldDrop) {
            BlockPos rounded = TeleportUtils.roundedBlockPos(this.player.getPos().add(0, this.player.getActiveEyeHeight(this.player.getPose(), this.player.getDimensions(this.player.getPose())), 0));
            this.shouldProtectDrop = ClaimStorage.get(this.player.getServerWorld()).getForPermissionCheck(rounded)
                    .canInteract(this.player, PermissionRegistry.LOCKITEMS, rounded);
            this.calculateShouldDrop = false;
        }
        return this.shouldProtectDrop;
    }

    public void save(MinecraftServer server) {
        Flan.log("Saving player data for player {} with uuid {}", this.player.getName(), this.player.getUuid());
        Path dir = ConfigHandler.getPlayerSavePath(server);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(this.player.getUuid() + ".json");
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException e) {
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("ClaimBlocks", this.claimBlocks);
            obj.addProperty("AdditionalBlocks", this.additionalClaimBlocks);
            obj.addProperty("LastSeen", LocalDateTime.now().format(Flan.onlineTimeFormatter));
            JsonObject defPerm = new JsonObject();
            this.defaultGroups.forEach((key, value) -> {
                JsonObject perm = new JsonObject();
                value.forEach((key1, value1) -> perm.addProperty(key1.id, value1));
                defPerm.add(key, perm);
            });
            obj.add("DefaultGroups", defPerm);

            JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
            ConfigHandler.GSON.toJson(obj, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(MinecraftServer server) {
        Flan.log("Reading player data for player {} with uuid {}", this.player.getName(), this.player.getUuid());
        try {
            Path file = ConfigHandler.getPlayerSavePath(server).resolve(this.player.getUuid() + ".json");
            if (!Files.exists(file)) {
                Flan.log("No player data found for player {} with uuid {}", this.player.getName(), this.player.getUuid());
                return;
            }
            JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            Flan.debug("Read following json data {} from file {}", obj, file.getFileName());
            this.claimBlocks = obj.get("ClaimBlocks").getAsInt();
            this.additionalClaimBlocks = obj.get("AdditionalBlocks").getAsInt();
            JsonObject defP = ConfigHandler.fromJson(obj, "DefaultGroups");
            defP.entrySet().forEach(e -> {
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(p -> {
                        try {
                            this.editDefaultPerms(e.getKey(), PermissionRegistry.get(p.getKey()), p.getValue().getAsBoolean() ? 1 : 0);
                        } catch (NullPointerException ex) {
                            Flan.logger.error("Error reading Permission {} for personal group {} for player {}. Permission doesnt exist", p.getKey(), e.getKey(), this.player.getName().asString());
                        }
                    });
                }
            });
            updateScoreFor(this.player, ClaimCriterias.AMOUNT, this.claimBlocks + this.additionalClaimBlocks);
            this.updateClaimScores();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateScoreFor(ServerPlayerEntity player, ScoreboardCriterion criterion, int val) {
        player.getScoreboard().forEachScore(criterion, player.getEntityName(), (scoreboardPlayerScore) -> {
            scoreboardPlayerScore.setScore(val);
        });
    }

    public static void editForOfflinePlayer(MinecraftServer server, UUID uuid, int additionalClaimBlocks) {
        Flan.log("Adding {} addional claimblocks for offline player with uuid {}", additionalClaimBlocks, uuid);
        Path dir = ConfigHandler.getPlayerSavePath(server);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(uuid.toString() + ".json");
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException e) {
            }
            JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            if (obj == null)
                obj = new JsonObject();
            int additionalBlocks = ConfigHandler.fromJson(obj, "AdditionalBlocks", 0);
            obj.addProperty("AdditionalBlocks", additionalBlocks + additionalClaimBlocks);
            Flan.debug("Attempting to write following json data {} to file {}", obj, file.getFileName());
            JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
            ConfigHandler.GSON.toJson(obj, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readGriefPreventionPlayerData(MinecraftServer server, ServerCommandSource src) {
        Flan.log("Reading grief prevention data");
        File griefPrevention = server.getSavePath(WorldSavePath.ROOT).resolve("plugins/GriefPreventionData/PlayerData").toFile();
        if (!griefPrevention.exists()) {
            src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.cantFindData, griefPrevention.getAbsolutePath()), Formatting.DARK_RED), false);
            return false;
        }
        for (File f : griefPrevention.listFiles()) {
            try {
                if (f.getName().contains("."))
                    continue;
                if (f.getName().startsWith("$")) {

                } else {
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(f.getName()));
                    if (player != null) {
                        PlayerClaimData data = PlayerClaimData.get(player);
                        reader.readLine();
                        data.claimBlocks = Integer.parseInt(reader.readLine());
                        data.additionalClaimBlocks = Integer.parseInt(reader.readLine());
                    } else {
                        File dir = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "/claimData/");
                        if (!dir.exists())
                            dir.mkdir();
                        File file = new File(dir, f.getName() + ".json");
                        if (!file.exists())
                            file.createNewFile();
                        reader.readLine();
                        FileWriter writer = new FileWriter(file);
                        JsonObject obj = new JsonObject();
                        obj.addProperty("ClaimBlocks", reader.readLine());
                        obj.addProperty("AdditionalBlocks", reader.readLine());
                        ConfigHandler.GSON.toJson(obj, writer);
                        writer.close();
                    }
                    reader.close();
                }
            } catch (Exception e) {
                src.sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.errorFile, f.getName(), Formatting.RED)), false);
            }
        }
        return true;
    }
}