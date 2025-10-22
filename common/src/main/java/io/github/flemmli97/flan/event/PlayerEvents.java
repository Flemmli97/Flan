package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
import io.github.flemmli97.flan.mixin.IHungerAccessor;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IOwnedItem;
import io.github.flemmli97.flan.utils.PlayerDropHandler;
import io.github.flemmli97.flan.utils.TeleportUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.phys.Vec3;

public class PlayerEvents {

    public static void saveClaimData(Player player) {
        if (player instanceof ServerPlayer)
            PlayerClaimData.get((ServerPlayer) player).save(player.getServer());
    }

    public static void readClaimData(Player player) {
        if (player instanceof ServerPlayer)
            PlayerClaimData.get((ServerPlayer) player).read(player.getServer());
    }

    public static void onLogout(Player player) {
        if (player.getServer() != null)
            LogoutTracker.getInstance(player.getServer()).track(player.getUUID());
    }

    public static boolean growBonemeal(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            BlockState state = serverPlayer.level().getBlockState(context.getClickedPos());
            BlockPos.MutableBlockPos pos = context.getClickedPos().mutable();
            ResourceLocation perm = InteractionOverrideManager.getInstance().getItemUse(context.getItemInHand().getItem());
            /**
             * {@link ItemInteractEvents#onItemUseBlock} handles this case already.
             * Sadly need to check again. In case its used in a claim. Less expensive than aoe check
             */
            if (perm != null && !ClaimStorage.get(serverPlayer.serverLevel()).getForPermissionCheck(pos).canInteract(serverPlayer, perm, pos, false))
                return false;
            int range = 0;
            Registry<ConfiguredFeature<?, ?>> registry = serverPlayer.level().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
            if (state.getBlock() instanceof MossBlock) {
                VegetationPatchConfiguration cfg = featureRange(registry, CaveFeatures.MOSS_PATCH_BONEMEAL, VegetationPatchConfiguration.class);
                if (cfg != null) {
                    range = cfg.xzRadius.getMaxValue() + 1;
                    pos.set(pos.getX(), pos.getY() + cfg.verticalRange + 1, pos.getZ());
                }
            } else if (state.getBlock() instanceof GrassBlock) {
                range = 4;
            } else if (state.is(Blocks.CRIMSON_NYLIUM)) {
                NetherForestVegetationConfig cfg = featureRange(registry, NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL, NetherForestVegetationConfig.class);
                if (cfg != null) {
                    range = cfg.spreadWidth;
                    pos.set(pos.getX(), pos.getY() + cfg.spreadHeight + 1, pos.getZ());
                }
            } else if (state.is(Blocks.WARPED_NYLIUM)) {
                NetherForestVegetationConfig cfg = featureRange(registry, NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL, NetherForestVegetationConfig.class);
                NetherForestVegetationConfig cfg2 = featureRange(registry, NetherFeatures.NETHER_SPROUTS_BONEMEAL, NetherForestVegetationConfig.class);
                TwistingVinesConfig cfg3 = featureRange(registry, NetherFeatures.TWISTING_VINES_BONEMEAL, TwistingVinesConfig.class);
                int w1 = cfg == null ? 0 : cfg.spreadWidth;
                int w2 = cfg2 == null ? 0 : cfg2.spreadWidth;
                int w3 = cfg3 == null ? 0 : cfg3.spreadWidth();
                int h1 = cfg == null ? 0 : cfg.spreadHeight;
                int h2 = cfg2 == null ? 0 : cfg2.spreadHeight;
                int h3 = cfg3 == null ? 0 : cfg3.spreadHeight();
                range = Math.max(Math.max(w1, w2), w3);
                int y = Math.max(Math.max(h1, h2), h3);
                pos.set(pos.getX(), pos.getY() + y + 1, pos.getZ());
            }
            if (range > 0 && perm != null && !ClaimStorage.get(serverPlayer.serverLevel()).canInteract(pos, range, serverPlayer, perm, false)) {
                serverPlayer.displayClientMessage(ClaimUtils.translatedText("flan.tooCloseClaim", ChatFormatting.DARK_RED), true);
                return true;
            }
        }
        return false;
    }

    public static float canSpawnFromPlayer(Entity entity, float old) {
        BlockPos pos;
        if (entity instanceof ServerPlayer player &&
                !ClaimStorage.get(player.serverLevel()).getForPermissionCheck(pos = player.blockPosition()).canInteract(player, BuiltinPermission.PLAYERMOBSPAWN, pos, false))
            return -1;
        return old;
    }

    public static boolean canWardenSpawnTrigger(BlockPos pos, ServerPlayer player) {
        return ClaimStorage.get(player.serverLevel()).getForPermissionCheck(pos).canInteract(player, BuiltinPermission.PLAYERMOBSPAWN, pos, false);
    }

    public static boolean canSculkTrigger(BlockPos pos, ServerPlayer player) {
        return ClaimStorage.get(player.serverLevel()).getForPermissionCheck(pos).canInteract(player, BuiltinPermission.SCULK, pos, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends FeatureConfiguration> T featureRange(Registry<ConfiguredFeature<?, ?>> registry, ResourceKey<ConfiguredFeature<?, ?>> key, Class<T> clss) {
        return registry.getHolder(key).map(r -> {
            if (clss.isInstance(r.value().config()))
                return (T) r.value().config();
            return null;
        }).orElse(null);
    }

    public static boolean xpAbsorb(Player player) {
        if (player instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level());
            BlockPos pos = player.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract((ServerPlayer) player, BuiltinPermission.XP, pos, false);
        }
        return false;
    }

    public static boolean canCollideWith(Player player, Entity entity) {
        if (player instanceof ServerPlayer sPlayer) {
            if (entity instanceof ItemEntity itemEntity) {
                IOwnedItem ownedItem = (IOwnedItem) entity;
                if (ownedItem.flan$getDeathPlayer() != null) {
                    ServerPlayer other = sPlayer.getServer().getPlayerList().getPlayer(ownedItem.flan$getDeathPlayer());
                    if (other == null)
                        return false;
                    return ownedItem.flan$getDeathPlayer().equals(player.getUUID()) || PlayerClaimData.get(other).deathItemsUnlocked();
                }
                if (sPlayer.getUUID().equals(ownedItem.flan$getPlayerOrigin()))
                    return true;
                ClaimStorage storage = ClaimStorage.get(sPlayer.serverLevel());
                BlockPos pos = sPlayer.blockPosition();
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim != null) {
                    if (claim instanceof Claim real && real.allowedEntries.isAllowed(ClaimAllowListKey.ITEM_PICKUP, itemEntity.getItem()::is, itemEntity.getItem()::is)) {
                        return true;
                    }
                    return claim.canInteract(sPlayer, BuiltinPermission.PICKUP, pos, false);
                }
            }
        }
        return true;
    }

    public static boolean canDropItem(Player player, ItemStack stack) {
        PlayerDropHandler dropHandler = ((PlayerDropHandler) player);
        if (!dropHandler.flan$forcedDropState() && !player.isDeadOrDying() && player instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level());
            BlockPos pos = player.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            boolean allow = true;
            if (claim != null) {
                if (!(claim instanceof Claim real) || !real.allowedEntries.isAllowed(ClaimAllowListKey.ITEM_DROP, stack::is, stack::is)) {
                    allow = claim.canInteract((ServerPlayer) player, BuiltinPermission.DROP, pos, false);
                }
            }
            if (!allow) {
                if (player.getInventory().add(stack) && !stack.isEmpty()) {
                    dropHandler.flan$setForcedDrop(true);
                    ItemEntity itemEntity = player.drop(stack, false);
                    dropHandler.flan$setForcedDrop(false);
                    if (itemEntity != null) {
                        itemEntity.setNoPickUpDelay();
                        itemEntity.setTarget(player.getUUID());
                    }
                }
                NonNullList<ItemStack> stacks = NonNullList.create();
                for (int j = 0; j < player.containerMenu.slots.size(); ++j) {
                    ItemStack itemStack2 = player.containerMenu.slots.get(j).getItem();
                    stacks.add(itemStack2.isEmpty() ? ItemStack.EMPTY : itemStack2);
                }
                ((ServerPlayer) player).connection.send(new ClientboundContainerSetContentPacket(player.containerMenu.containerId, 0, stacks, player.inventoryMenu.getCarried()));
            }
            return allow;
        }
        return true;
    }

    public static void updateDroppedItem(Player player, ItemEntity entity) {
        ((IOwnedItem) entity).flan$setOriginPlayer((player));
    }

    public static Claim currentClaimTick(ServerPlayer player, Claim currentClaim) {
        Vec3 pos = player.position();
        BlockPos rounded = TeleportUtils.roundedBlockPos(pos.add(0, player.getEyeHeight(player.getPose()), 0));
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        Claim newClaim = currentClaim;
        if (currentClaim != null) {
            if (!currentClaim.intersects(player.getBoundingBox())) {
                boolean isSub = currentClaim.parentClaim() != null;
                Claim claim = isSub ? storage.getClaimAt(rounded) : currentClaim.parentClaim();
                if (claim == null) {
                    currentClaim.displayLeaveTitle(player);
                    if (!gameModeCanFly(player.gameMode.getGameModeForPlayer()))
                        CrossPlatformStuff.INSTANCE.toggleCreativeFlight(player, false);
                } else {
                    Claim sub = claim.getSubClaim(rounded);
                    boolean display = true;
                    if (sub != null)
                        claim = sub;
                    else {
                        display = currentClaim.enterTitle != null;
                        if (claim.enterTitle == null)
                            currentClaim.displayLeaveTitle(player);
                    }
                    if (display)
                        claim.displayEnterTitle(player);
                }
                newClaim = claim;
            } else {
                if (currentClaim.parentClaim() == null) {
                    Claim sub = currentClaim.getSubClaim(rounded);
                    if (sub != null) {
                        currentClaim = sub;
                        currentClaim.displayEnterTitle(player);
                        newClaim = currentClaim;
                    }
                }
                if (!player.isSpectator()) {
                    BlockPos.MutableBlockPos bPos = rounded.mutable();
                    boolean isSub = currentClaim.parentClaim() != null;
                    Claim mainClaim = isSub ? currentClaim.parentClaim() : currentClaim;
                    Entity vehicle = player.getVehicle();
                    if (!mainClaim.canInteract(player, BuiltinPermission.CANSTAY, bPos, true) ||
                            (vehicle instanceof VehicleEntity && !vehicle.isControlledByLocalInstance() && !mainClaim.canInteract(player, BuiltinPermission.VEHICLE_PASS, bPos, true))) {
                        Claim sub = isSub ? currentClaim : null;
                        Vec3 tp = TeleportUtils.getTeleportPos(player, pos, storage, new TeleportUtils.Area2D(sub != null ? sub.getDimensions() : mainClaim.getDimensions()), true, bPos, (claim, nPos) -> claim.canInteract(player, BuiltinPermission.CANSTAY, nPos, false));
                        if (vehicle != null) {
                            if (!vehicle.isControlledByLocalInstance()) {
                                // Otherwise cannot teleport as the client controls it
                                player.stopRiding();
                            }
                            vehicle.teleportTo(tp.x(), tp.y(), tp.z());
                        }
                        player.teleportTo(tp.x(), tp.y(), tp.z());
                    }
                    rounded = bPos;
                    currentClaim.applyEffects(player);
                }
            }
        } else if (player.tickCount % 3 == 0) {
            Claim claim = storage.getClaimAt(rounded);
            Claim sub = claim != null ? claim.getSubClaim(rounded) : null;
            if (sub != null)
                claim = sub;
            if (claim != null) {
                claim.displayEnterTitle(player);
            }
            newClaim = claim;
        }
        IPermissionContainer permissionContainer = newClaim != null ? newClaim : storage.getForPermissionCheck(rounded);
        if (player.getAbilities().flying && !gameModeCanFly(player.gameMode.getGameModeForPlayer()) && !permissionContainer.canInteract(player, BuiltinPermission.ALLOW_FLIGHT, rounded, true)) {
            player.getAbilities().flying = false;
            player.setDeltaMovement(Vec3.ZERO);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
            player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
        } else if (!gameModeCanFly(player.gameMode.getGameModeForPlayer())) {
            CrossPlatformStuff.INSTANCE.toggleCreativeFlight(player, permissionContainer.canInteract(player, BuiltinPermission.MAY_FLIGHT, rounded, false));
        }
        if (player.getFoodData().getSaturationLevel() < 2 && permissionContainer.canInteract(player, BuiltinPermission.NOHUNGER, rounded, false)) {
            ((IHungerAccessor) player.getFoodData()).setSaturation(2);
        }
        return newClaim;
    }

    protected static boolean gameModeCanFly(GameType gameType) {
        return gameType == GameType.CREATIVE || gameType == GameType.SPECTATOR;
    }
}
