package io.github.flemmli97.flan.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.display.ClaimDisplay;
import io.github.flemmli97.flan.player.display.DisplayBox;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import io.github.flemmli97.flan.utils.IPlayerClaimImpl;
import io.github.flemmli97.flan.utils.TeleportUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

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

    private final UUID editDisplay = UUID.randomUUID();
    private final UUID display3D = UUID.randomUUID();

    private int claimBlocks, additionalClaimBlocks, confirmTick, actionCooldown;
    //Scoreboard tracking
    private int usedBlocks;

    private int lastBlockTick, trappedTick = -1, deathPickupTick;
    private Vec3 trappedPos;
    private BlockPos tpPos;
    private ClaimEditingMode editMode = ClaimEditingMode.DEFAULT;
    private ClaimingMode claimingMode = ClaimingMode.DEFAULT;
    private Claim editingClaim;
    private ClaimDisplay displayEditing;

    private BlockPos firstCorner;

    private final Set<ClaimDisplay> claimDisplayList = new HashSet<>();
    private final Set<ClaimDisplay> displayToAdd = new HashSet<>();

    private final ServerPlayer player;

    private boolean confirmDeleteAll, adminIgnoreClaim, claimBlockMessage;

    private final Map<String, Map<ResourceLocation, Boolean>> defaultGroups = new HashMap<>();

    private boolean shouldProtectDrop, calculateShouldDrop = true;

    private final Map<UUID, Map<UUID, Long>> fakePlayerNotif = new HashMap<>();

    private boolean fakePlayerNotification = true;

    private long lastClaimTime;

    public final ClientBlockDisplayTracker clientBlockDisplayTracker;

    public int claimingRange = 64;

    public PlayerClaimData(ServerPlayer player) {
        this.player = player;
        this.claimBlocks = ConfigHandler.CONFIG.startingBlocks;
        this.clientBlockDisplayTracker = new ClientBlockDisplayTracker(player);
    }

    public static PlayerClaimData get(ServerPlayer player) {
        return ((IPlayerClaimImpl) player).get();
    }

    @Override
    public int getClaimBlocks() {
        return Math.min(this.claimBlocks, PermissionNodeHandler.INSTANCE.permVal(this.player, PermissionNodeHandler.permClaimBlocksCap, this.claimBlocks))
                + PermissionNodeHandler.INSTANCE.permVal(this.player, PermissionNodeHandler.permClaimBlocksBonus, 0);
    }

    public void setClaimBlocks(int amount) {
        this.claimBlocks = amount;
        updateScoreFor(this.player, ClaimCriterias.AMOUNT, this.claimBlocks + this.additionalClaimBlocks);
        updateScoreFor(this.player, ClaimCriterias.FREE, this.claimBlocks + this.additionalClaimBlocks - this.usedBlocks);
    }

    public void addClaimBlocksDirect(int amount) {
        this.setClaimBlocks(this.claimBlocks + amount);
    }

    public boolean addClaimBlocks(int amount) {
        if (this.canIncrease(this.claimBlocks + amount)) {
            this.setClaimBlocks(this.claimBlocks + amount);
            return true;
        }
        return false;
    }

    private boolean canIncrease(int blocks) {
        return PermissionNodeHandler.INSTANCE.permBelowEqVal(this.player, PermissionNodeHandler.permClaimBlocks, blocks, ConfigHandler.CONFIG.maxClaimBlocks);
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
        if (ConfigHandler.CONFIG.maxClaimBlocks == -1)
            return true;
        return amount <= this.remainingClaimBlocks();
    }

    @Override
    public int usedClaimBlocks() {
        return this.calculateUsedClaimBlocks();
    }

    @Override
    public int remainingClaimBlocks() {
        return this.getClaimBlocks() + this.getAdditionalClaims() - this.usedClaimBlocks();
    }

    public long nextClaimCooldown() {
        return ConfigHandler.CONFIG.nextClaimCooldown <= 0 ? 0 : Math.max(0, this.player.level().getGameTime() - this.lastClaimTime - ConfigHandler.CONFIG.nextClaimCooldown);
    }

    public void updateLastClaim() {
        this.lastClaimTime = this.player.level().getGameTime();
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
        else if (this.displayEditing != null) {
            this.displayEditing.onRemoved(this.player);
            this.displayEditing = null;
        }
        this.editingClaim = claim;
    }

    public void addDisplayClaim(IPermissionContainer cont, EnumDisplayType type, int height) {
        if (cont instanceof Claim claim) {
            this.displayToAdd.add(new ClaimDisplay(claim, type, height));
            if (type == EnumDisplayType.MAIN)
                for (Claim sub : claim.getAllSubclaims())
                    this.displayToAdd.add(new ClaimDisplay(sub, EnumDisplayType.SUB, height));
        }
    }

    public void addDisplayClaim(DisplayBox display, EnumDisplayType type, int height) {
        if (!display.isRemoved())
            this.displayToAdd.add(new ClaimDisplay(display, this.player.serverLevel(), type, height));
    }

    public ClaimEditingMode getEditMode() {
        return this.editMode;
    }

    public void setEditMode(ClaimEditingMode mode) {
        this.editMode = mode;
        this.setEditClaim(null, 0);
        this.setEditingCorner(null);
    }

    public ClaimingMode getClaimingMode() {
        if (this.editingClaim != null && this.editingClaim.is3d())
            return ClaimingMode.DIMENSION_3D;
        if (!ConfigHandler.CONFIG.main3dClaims && this.editMode == ClaimEditingMode.DEFAULT)
            return ClaimingMode.DEFAULT;
        return this.claimingMode;
    }

    public void setClaimingMode(ClaimingMode claimingMode) {
        this.claimingMode = claimingMode;
        this.setEditClaim(null, 0);
        this.setEditingCorner(null);
    }

    public BlockPos editingCorner() {
        return this.firstCorner;
    }

    public void setEditingCorner(BlockPos pos) {
        if (pos != null) {
            this.clientBlockDisplayTracker.resetFakeBlocks(this.display3D);
        } else {
            this.clientBlockDisplayTracker.resetFakeBlocks(this.editDisplay);
        }
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

    public Map<String, Map<ResourceLocation, Boolean>> playerDefaultGroups() {
        return this.defaultGroups;
    }

    public boolean editDefaultPerms(String group, ResourceLocation perm, int mode) {
        if (PermissionManager.INSTANCE.isGlobalPermission(perm) || ConfigHandler.CONFIG.globallyDefined(this.player.serverLevel(), perm))
            return false;
        if (mode > 1)
            mode = -1;
        boolean has = this.defaultGroups.containsKey(group);
        Map<ResourceLocation, Boolean> perms = has ? this.defaultGroups.get(group) : new HashMap<>();
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
        if (this.trappedTick < 0 && claim != null && !this.player.getUUID().equals(claim.getOwner())) {
            this.trappedTick = 101;
            this.trappedPos = this.player.position();
            return true;
        }
        return false;
    }

    public boolean setTeleportTo(BlockPos tp) {
        if (this.trappedTick < 0) {
            this.trappedTick = 101;
            this.trappedPos = this.player.position();
            this.tpPos = tp;
            return true;
        }
        return false;
    }

    public void tick(Claim currentClaim, Consumer<Claim> cons) {
        EntityInteractEvents.updateClaim(this.player, currentClaim, cons);
        boolean tool = ConfigHandler.isClaimingTool(this.player.getMainHandItem())
                || ConfigHandler.isClaimingTool(this.player.getOffhandItem());
        boolean stick = ConfigHandler.isInspectionTool(this.player.getMainHandItem())
                || ConfigHandler.isInspectionTool(this.player.getOffhandItem());
        this.displayToAdd.forEach(add -> {
            if (!this.claimDisplayList.add(add)) {
                this.claimDisplayList.removeIf(c -> c.equals(add) && c.type != add.type);
                this.claimDisplayList.add(add);
            }
        });
        this.displayToAdd.clear();
        this.claimDisplayList.removeIf(d -> {
            boolean remove = d.display(this.player, !tool && !stick);
            if (remove)
                d.onRemoved(this.player);
            return remove;
        });
        if (++this.lastBlockTick > ConfigHandler.CONFIG.ticksForNextBlock) {
            this.addClaimBlocks(1);
            this.lastBlockTick = 0;
        }
        if (tool) {
            this.claimingRange = this.getClaimingMode() == ClaimingMode.DIMENSION_3D && this.editingCorner() != null ? 10 : 64;
            BlockPos pos = ItemInteractEvents.rayTargetPos(this.player);
            if (pos != null && !pos.equals(this.firstCorner)) {
                this.clientBlockDisplayTracker.displayFakeBlocks(this.display3D,
                        new ClientBlockDisplayTracker.DisplayData(pos, Blocks.YELLOW_WOOL.defaultBlockState()));
            } else {
                this.clientBlockDisplayTracker.resetFakeBlocks(this.display3D);
            }
        } else {
            this.clientBlockDisplayTracker.resetFakeBlocks(this.display3D);
            this.claimingRange = 64;
        }
        if (this.firstCorner != null && this.player.tickCount % 3 == 0) {
            this.clientBlockDisplayTracker.displayFakeBlocks(this.editDisplay, new ClientBlockDisplayTracker.DisplayData(
                    this.firstCorner, Blocks.SEA_LANTERN.defaultBlockState()
            ));
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
            if (this.shouldDisplayClaimToolMessage()) {
                this.player.displayClientMessage(PermHelper.translatedText("flan.claimBlocksFormat",
                        this.getClaimBlocks(), this.getAdditionalClaims(), this.usedClaimBlocks(), this.remainingClaimBlocks(), ChatFormatting.GOLD), false);
                this.addDisplayClaim(currentClaim, EnumDisplayType.MAIN, this.player.blockPosition().getY());
            }
        }
        this.actionCooldown--;
        if (--this.trappedTick >= 0) {
            if (this.trappedTick == 0) {
                if (this.tpPos != null) {
                    BlockPos.MutableBlockPos tpTo = this.tpPos.mutable();
                    Vec3 offset = new Vec3(this.tpPos.getX() + 0.5, this.tpPos.getY() + 0.01, this.tpPos.getZ() + 0.5).subtract(this.player.position());
                    int yHighest = this.player.level().getChunk(this.tpPos.getX() >> 4, this.tpPos.getZ() >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING, this.tpPos.getX() & 15, this.tpPos.getZ() & 15);
                    AABB box = this.player.getBoundingBox().move(offset);
                    if (tpTo.getY() < yHighest) {
                        while (tpTo.getY() < yHighest) {
                            if (this.player.level().noCollision(this.player, box))
                                break;
                            tpTo.set(tpTo.getX(), tpTo.getY() + 1, tpTo.getZ());
                            box = box.move(0, 1, 0);
                        }
                        tpTo.set(tpTo.getX(), tpTo.getY() + 1, tpTo.getZ());
                    } else
                        tpTo.set(tpTo.getX(), yHighest, tpTo.getZ());
                    if (this.player.isPassenger())
                        this.player.stopRiding();
                    this.player.teleportToWithTicket(tpTo.getX() + 0.5, tpTo.getY(), tpTo.getZ() + 0.5);
                    this.tpPos = null;
                } else {
                    Vec3 tp = TeleportUtils.getTeleportPos(this.player, this.player.position(), ClaimStorage.get(this.player.serverLevel()),
                            new TeleportUtils.Area2D(((IPlayerClaimImpl) this.player).getCurrentClaim().getDimensions()),
                            TeleportUtils.roundedBlockPos(this.player.position()).mutable(), (claim, nPos) -> false);
                    if (this.player.isPassenger())
                        this.player.stopRiding();
                    this.player.teleportToWithTicket(tp.x(), tp.y(), tp.z());
                }
            } else if (this.player.position().distanceToSqr(this.trappedPos) > 0.15) {
                this.trappedTick = -1;
                this.trappedPos = null;
                this.player.displayClientMessage(PermHelper.translatedText("flan.trappedMove", ChatFormatting.RED), false);
            }
        }
        this.deathPickupTick--;
        if (!this.player.isDeadOrDying())
            this.calculateShouldDrop = true;
    }

    private boolean shouldDisplayClaimToolMessage() {
        return ItemInteractEvents.canClaimWorld(this.player.serverLevel(), this.player)
                && ConfigHandler.CONFIG.maxClaimBlocks > 0;
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
        if (data.setDeathItemOwner()) {
            this.player.displayClientMessage(PermHelper.translatedText("flan.unlockDropsCmd", "/flan unlockDrops", ChatFormatting.GOLD), false);
        }
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
        for (ServerLevel world : this.player.getServer().getAllLevels()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.player.getUUID());
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
        for (ServerLevel world : this.player.getServer().getAllLevels()) {
            Collection<Claim> claims = ClaimStorage.get(world).allClaimsFromPlayer(this.player.getUUID());
            if (claims != null) {
                usedClaimsBlocks += claims.stream().filter(claim -> !claim.isAdminClaim()).mapToInt(Claim::getPlane).sum();
            }
        }
        return usedClaimsBlocks;
    }

    public boolean setDeathItemOwner() {
        if (!this.player.isDeadOrDying())
            return false;
        if (this.calculateShouldDrop) {
            BlockPos rounded = TeleportUtils.roundedBlockPos(this.player.position().add(0, this.player.getStandingEyeHeight(this.player.getPose(), this.player.getDimensions(this.player.getPose())), 0));
            this.shouldProtectDrop = ClaimStorage.get(this.player.serverLevel()).getForPermissionCheck(rounded)
                    .canInteract(this.player, BuiltinPermission.LOCKITEMS, rounded)
                    && !this.player.getServer().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            this.calculateShouldDrop = false;
        }
        return this.shouldProtectDrop;
    }

    public void setFakePlayerNotif(boolean on) {
        this.fakePlayerNotification = on;
    }

    public boolean hasFakePlayerNotificationOn() {
        return this.fakePlayerNotification;
    }

    public void notifyFakePlayerInteraction(ServerPlayer fakePlayer, BlockPos pos, Claim claim) {
        if (!this.fakePlayerNotification)
            return;
        Map<UUID, Long> map = this.fakePlayerNotif.computeIfAbsent(claim.getClaimID(), o -> new HashMap<>());
        Long last = map.get(fakePlayer.getUUID());
        if (last == null || this.player.serverLevel().getGameTime() - 1200 > last) {
            Component claimMsg = PermHelper.translatedText("flan.fakePlayerNotification1", claim.getWorld().dimension().location().toString(), pos, ChatFormatting.DARK_RED);
            this.player.sendSystemMessage(claimMsg);
            String cmdStr = String.format("/flan fakePlayer add %s", fakePlayer.getUUID());
            Component cmd = PermHelper.translatedText("flan.clickableComponent")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmdStr))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(cmdStr))));
            Component msg = PermHelper.translatedText("flan.fakePlayerNotification2", cmd);
            this.player.sendSystemMessage(msg);
            cmdStr = "/flan fakePlayer";
            cmd = PermHelper.translatedText("flan.clickableComponent")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmdStr))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(cmdStr))));
            msg = PermHelper.translatedText("flan.fakePlayerNotification3", cmd);
            this.player.sendSystemMessage(msg);
            map.put(fakePlayer.getUUID(), this.player.serverLevel().getGameTime());
        }
    }

    public void save(MinecraftServer server) {
        Flan.log("Saving player data for player {} with uuid {}", this.player.getName(), this.player.getUUID());
        Path dir = ConfigHandler.getPlayerSavePath(server);
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(this.player.getUUID() + ".json");
            try {
                Files.createFile(file);
            } catch (FileAlreadyExistsException e) {
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("ClaimBlocks", this.claimBlocks);
            obj.addProperty("AdditionalBlocks", this.additionalClaimBlocks);
            obj.addProperty("LastSeen", LocalDateTime.now().format(Flan.ONLINE_TIME_FORMATTER));
            JsonObject defPerm = new JsonObject();
            this.defaultGroups.forEach((key, value) -> {
                JsonObject perm = new JsonObject();
                value.forEach((key1, value1) -> perm.addProperty(key1.toString(), value1));
                defPerm.add(key, perm);
            });
            obj.add("DefaultGroups", defPerm);
            obj.addProperty("FakePlayerNotification", this.fakePlayerNotification);

            JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
            ConfigHandler.GSON.toJson(obj, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(MinecraftServer server) {
        Flan.log("Reading player data for player {} with uuid {}", this.player.getName(), this.player.getUUID());
        try {
            Path file = ConfigHandler.getPlayerSavePath(server).resolve(this.player.getUUID() + ".json");
            if (!Files.exists(file)) {
                Flan.log("No player data found for player {} with uuid {}", this.player.getName(), this.player.getUUID());
                return;
            }
            JsonReader reader = ConfigHandler.GSON.newJsonReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
            JsonObject obj = ConfigHandler.GSON.fromJson(reader, JsonObject.class);
            reader.close();
            Flan.debug("Read following json data {} from file {}", obj, file.getFileName());
            JsonElement claimBlockEl = obj.get("ClaimBlocks");
            if (claimBlockEl.isJsonPrimitive())
                this.claimBlocks = claimBlockEl.getAsInt();
            this.additionalClaimBlocks = obj.get("AdditionalBlocks").getAsInt();
            JsonObject defP = ConfigHandler.fromJson(obj, "DefaultGroups");
            defP.entrySet().forEach(e -> {
                if (e.getValue().isJsonObject()) {
                    e.getValue().getAsJsonObject().entrySet().forEach(p ->
                            this.editDefaultPerms(e.getKey(), BuiltinPermission.tryLegacy(p.getKey()), p.getValue().getAsBoolean() ? 1 : 0));
                }
            });
            this.fakePlayerNotification = ConfigHandler.fromJson(obj, "FakePlayerNotification", true);
            updateScoreFor(this.player, ClaimCriterias.AMOUNT, this.claimBlocks + this.additionalClaimBlocks);
            this.updateClaimScores();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateScoreFor(ServerPlayer player, ObjectiveCriteria criterion, int val) {
        player.getScoreboard().forAllObjectives(criterion, player.getScoreboardName(), (scoreboardPlayerScore) -> scoreboardPlayerScore.setScore(val));
    }

    public static void editForOfflinePlayer(MinecraftServer server, UUID uuid, int additionalClaimBlocks, boolean base) {
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
            if (base) {
                int blocks = ConfigHandler.fromJson(obj, "ClaimBlocks", 0);
                obj.addProperty("ClaimBlocks", blocks + additionalClaimBlocks);
            } else {
                int additionalBlocks = ConfigHandler.fromJson(obj, "AdditionalBlocks", 0);
                obj.addProperty("AdditionalBlocks", additionalBlocks + additionalClaimBlocks);
            }
            Flan.debug("Attempting to write following json data {} to file {}", obj, file.getFileName());
            JsonWriter jsonWriter = ConfigHandler.GSON.newJsonWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
            ConfigHandler.GSON.toJson(obj, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readGriefPreventionPlayerData(MinecraftServer server, CommandSourceStack src) {
        Flan.log("Reading grief prevention data");
        File griefPrevention = server.getWorldPath(LevelResource.ROOT).resolve("plugins/GriefPreventionData/PlayerData").toFile();
        if (!griefPrevention.exists()) {
            src.sendSuccess(() -> PermHelper.translatedText("flan.cantFindData", griefPrevention.getAbsolutePath(), ChatFormatting.DARK_RED), false);
            return false;
        }
        for (File f : griefPrevention.listFiles()) {
            try {
                if (f.getName().contains("."))
                    continue;
                if (f.getName().startsWith("$")) {

                } else {
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    ServerPlayer player = server.getPlayerList().getPlayer(UUID.fromString(f.getName()));
                    if (player != null) {
                        PlayerClaimData data = PlayerClaimData.get(player);
                        reader.readLine();
                        data.claimBlocks = Integer.parseInt(reader.readLine());
                        data.additionalClaimBlocks = Integer.parseInt(reader.readLine());
                    } else {
                        File dir = new File(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile(), "/claimData/");
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
                src.sendSuccess(() -> PermHelper.translatedText("flan.errorFile", f.getName(), ChatFormatting.RED), false);
            }
        }
        return true;
    }
}
