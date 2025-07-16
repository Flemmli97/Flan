package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.IHungerAccessor;
import io.github.flemmli97.flan.mixin.IPersistentProjectileVars;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IOwnedItem;
import io.github.flemmli97.flan.utils.TeleportUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityInteractEvents {

    public static InteractionResult useAtEntity(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (claim instanceof Claim real && real.canInteractWithEntity(entity))
                return InteractionResult.PASS;
            ResourceLocation perm = InteractionOverrideManager.getInstance().getEntityInteract(entity.getType());
            if (perm != null) {
                return claim.canInteract(serverPlayer, perm, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            }
            if (entity instanceof ArmorStand) {
                if (!claim.canInteract(serverPlayer, BuiltinPermission.ARMORSTAND, pos, true))
                    return InteractionResult.FAIL;
            }
            if (entity instanceof Mob)
                return claim.canInteract(serverPlayer, BuiltinPermission.ANIMALINTERACT, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult useEntity(Player p, Level world, InteractionHand hand, Entity entity) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (claim instanceof Claim real && real.canInteractWithEntity(entity))
                return InteractionResult.PASS;
            ResourceLocation perm = InteractionOverrideManager.getInstance().getEntityInteract(entity.getType());
            if (perm != null) {
                return claim.canInteract(player, perm, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            }
            switch (entity) {
                case Boat boat -> {
                    return claim.canInteract(player, BuiltinPermission.BOAT, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                }
                case AbstractMinecart minecart -> {
                    if (entity instanceof AbstractMinecartContainer)
                        return claim.canInteract(player, BuiltinPermission.OPENCONTAINER, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                    return claim.canInteract(player, BuiltinPermission.MINECART, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                }
                case AbstractVillager villager -> {
                    return claim.canInteract(player, BuiltinPermission.TRADING, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                }
                case ItemFrame itemFrame -> {
                    return claim.canInteract(player, BuiltinPermission.ITEMFRAMEROTATE, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                }
                case OwnableEntity ownable -> {
                    if (ownable.getOwnerUUID() != null && ownable.getOwnerUUID().equals(player.getUUID()))
                        return InteractionResult.PASS;
                }
                default -> {
                }
            }
            return claim.canInteract(player, BuiltinPermission.ANIMALINTERACT, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean canInteract(Entity entity) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return ConfigHandler.CONFIG.ignoredEntityTypes.contains(id.getNamespace())
                || ConfigHandler.CONFIG.ignoredEntityTypes.contains(id.toString())
                || entity.getTags().stream().anyMatch(ConfigHandler.CONFIG.entityTagIgnore::contains);
    }

    public static boolean projectileHit(Projectile proj, HitResult res) {
        if (proj.level().isClientSide)
            return false;
        Entity owner = proj.getOwner();
        if (owner instanceof ServerPlayer player) {
            if (res.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRes = (BlockHitResult) res;
                BlockPos pos = blockRes.getBlockPos();
                BlockState state = proj.level().getBlockState(pos);
                ResourceLocation perm;
                if (proj instanceof ThrownEnderpearl)
                    perm = BuiltinPermission.ENDERPEARL;
                else if (proj instanceof WindCharge)
                    perm = BuiltinPermission.WIND_CHARGE;
                else if (proj instanceof ThrownEgg || proj instanceof ThrownPotion)
                    perm = BuiltinPermission.PROJECTILES;
                else
                    perm = InteractionOverrideManager.getInstance().getBlockInteract(state.getBlock());
                if (perm != BuiltinPermission.ENDERPEARL
                        && perm != BuiltinPermission.TARGETBLOCK
                        && perm != BuiltinPermission.PROJECTILES
                        && perm != BuiltinPermission.WIND_CHARGE)
                    return false;
                ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level());
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                boolean flag = !claim.canInteract(player, perm, pos, true);
                if (flag) {
                    if (proj instanceof AbstractArrow pers) {
                        ((IPersistentProjectileVars) pers).setInBlockState(pers.level().getBlockState(pos));
                        Vec3 vec3d = blockRes.getLocation().subtract(pers.getX(), pers.getY(), pers.getZ());
                        pers.setDeltaMovement(vec3d);
                        Vec3 vec3d2 = vec3d.normalize().scale(0.05);
                        pers.setPosRaw(pers.getX() - vec3d2.x, pers.getY() - vec3d2.y, pers.getZ() - vec3d2.z);
                        pers.playSound(((IPersistentProjectileVars) pers).getSoundEvent(), 1.0F, 1.2F / (pers.level().random.nextFloat() * 0.2F + 0.9F));
                        ((IPersistentProjectileVars) pers).setInGround(true);
                        pers.shakeTime = 7;
                        pers.setCritArrow(false);
                        ((IPersistentProjectileVars) pers).setPiercingLevel((byte) 0);
                        pers.setSoundEvent(SoundEvents.ARROW_HIT);
                        ((IPersistentProjectileVars) pers).resetPiercingStatus();
                    }
                    if (proj instanceof ThrownEnderpearl)
                        proj.remove(Entity.RemovalReason.KILLED);
                }
                return flag;
            } else if (res.getType() == HitResult.Type.ENTITY) {
                if (proj instanceof ThrownEnderpearl) {
                    ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level());
                    IPermissionContainer claim = storage.getForPermissionCheck(proj.blockPosition());
                    return claim.canInteract(player, BuiltinPermission.ENDERPEARL, proj.blockPosition(), true);
                }
                Entity hit = ((EntityHitResult) res).getEntity();
                boolean fail = attackSimple(player, hit, true) != InteractionResult.PASS;
                if (fail && proj instanceof AbstractArrow pers && pers.getPierceLevel() > 0) {
                    IntOpenHashSet pierced = ((IPersistentProjectileVars) pers).getPiercedEntities();
                    if (pierced == null)
                        pierced = new IntOpenHashSet(5);
                    pierced.add(hit.getId());
                    ((IPersistentProjectileVars) pers).setPiercedEntities(pierced);
                    ((IPersistentProjectileVars) pers).setPiercingLevel((byte) (pers.getPierceLevel() + 1));
                }
                proj.hurtMarked = true;
                return fail;
            }
        }
        return false;
    }

    public static boolean preventDamage(Entity entity, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer)
            return attackSimple((ServerPlayer) source.getEntity(), entity, true) != InteractionResult.PASS;
        else if (source.is(DamageTypeTags.IS_EXPLOSION) && !entity.level().isClientSide && !(entity instanceof ServerPlayer || entity instanceof Enemy)) {
            IPermissionContainer claim = ClaimStorage.get((ServerLevel) entity.level()).getForPermissionCheck(entity.blockPosition());
            return claim != null && !claim.canInteract(null, BuiltinPermission.EXPLOSIONS, entity.blockPosition());
        }
        return false;
    }

    public static InteractionResult attackSimple(Player p, Entity entity, boolean message) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (claim instanceof Claim real && real.canAttackEntity(entity))
                return InteractionResult.PASS;
            if (entity.hasCustomName() && !claim.canInteract(player, BuiltinPermission.HURTNAMED, pos, message)) {
                return InteractionResult.FAIL;
            }
            ResourceLocation perm = InteractionOverrideManager.getInstance().getEntityAttack(entity.getType());
            if (perm != null)
                return claim.canInteract(player, perm, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof ArmorStand || !(entity instanceof LivingEntity))
                return claim.canInteract(player, BuiltinPermission.BREAKNONLIVING, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof Player)
                return claim.canInteract(player, BuiltinPermission.HURTPLAYER, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
            return claim.canInteract(player, BuiltinPermission.HURTANIMAL, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
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
            if (entity instanceof ItemEntity) {
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
                if (claim != null)
                    return claim.canInteract(sPlayer, BuiltinPermission.PICKUP, pos, false);
            }
        }
        return true;
    }

    public static boolean canDropItem(Player player, ItemStack stack) {
        if (!player.isDeadOrDying() && player instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level());
            BlockPos pos = player.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            boolean allow = true;
            if (claim != null)
                allow = claim.canInteract((ServerPlayer) player, BuiltinPermission.DROP, pos, false);
            if (!allow) {
                player.getInventory().add(stack);
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

    public static boolean witherCanDestroy(WitherBoss wither) {
        if (wither.level().isClientSide)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) wither.level());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                pos.setWithOffset(wither.blockPosition(), x, 3, z);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (!claim.canInteract(null, BuiltinPermission.WITHER, pos, false))
                    return false;
            }
        return true;
    }

    public static boolean canEndermanInteract(EnderMan enderman, BlockPos pos) {
        if (enderman.level().isClientSide)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) enderman.level());
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract(null, BuiltinPermission.ENDERMAN, pos, false);
    }

    public static boolean canSnowGolemInteract(SnowGolem snowgolem) {
        if (snowgolem.level().isClientSide)
            return true;
        int x, y, z;
        for (int l = 0; l < 4; ++l) {
            x = Mth.floor(snowgolem.getX() + (l % 2 * 2 - 1) * 0.25F);
            y = Mth.floor(snowgolem.getY());
            z = Mth.floor(snowgolem.getZ() + (l / 2 % 2 * 2 - 1) * 0.25F);
            BlockPos pos = new BlockPos(x, y, z);
            IPermissionContainer claim = ClaimStorage.get((ServerLevel) snowgolem.level()).getForPermissionCheck(pos);
            if (!claim.canInteract(null, BuiltinPermission.SNOWGOLEM, pos, false))
                return false;
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
                    Entity passenger = player.getVehicle();
                    if (!mainClaim.canInteract(player, BuiltinPermission.CANSTAY, bPos, true) || (passenger instanceof Boat && !mainClaim.canInteract(player, BuiltinPermission.BOAT, bPos, true))) {
                        Claim sub = isSub ? currentClaim : null;
                        Vec3 tp = TeleportUtils.getTeleportPos(player, pos, storage, new TeleportUtils.Area2D(sub != null ? sub.getDimensions() : mainClaim.getDimensions()), true, bPos, (claim, nPos) -> claim.canInteract(player, BuiltinPermission.CANSTAY, nPos, false));
                        if (passenger != null) {
                            player.stopRiding();
                            passenger.teleportTo(tp.x(), tp.y(), tp.z());
                        }
                        player.teleportTo(tp.x(), tp.y(), tp.z());
                    }
                    if (player.getAbilities().flying && !gameModeCanFly(player.gameMode.getGameModeForPlayer()) && !mainClaim.canInteract(player, BuiltinPermission.ALLOW_FLIGHT, rounded, true)) {
                        player.getAbilities().flying = false;
                        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                    } else if (!gameModeCanFly(player.gameMode.getGameModeForPlayer())) {
                        CrossPlatformStuff.INSTANCE.toggleCreativeFlight(player, currentClaim.canInteract(player, BuiltinPermission.MAY_FLIGHT, rounded, false));
                    }
                    if (player.getFoodData().getSaturationLevel() < 2 && mainClaim.canInteract(player, BuiltinPermission.NOHUNGER, bPos, false)) {
                        ((IHungerAccessor) player.getFoodData()).setSaturation(2);
                    }
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
        return newClaim;
    }

    protected static boolean gameModeCanFly(GameType gameType) {
        return gameType == GameType.CREATIVE || gameType == GameType.SPECTATOR;
    }

    public static boolean canFrostwalkerFreeze(ServerLevel world, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer) {
            IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) entity, BuiltinPermission.PLACE, pos, false);
        }
        return true;
    }

    public static boolean preventLightningConvert(Entity entity) {
        if (entity.level().isClientSide || entity instanceof Enemy)
            return false;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) entity.level());
        IPermissionContainer claim = storage.getForPermissionCheck(entity.blockPosition());
        return !claim.canInteract(null, BuiltinPermission.LIGHTNING, entity.blockPosition(), false);
    }
}
