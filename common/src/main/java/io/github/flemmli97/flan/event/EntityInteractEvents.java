package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.IPersistentProjectileVars;
import io.github.flemmli97.flan.utils.TeleportUtils;
import io.github.flemmli97.flan.utils.VehiclePositionTracker;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityInteractEvents {

    public static InteractionResult useAtEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) level);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (claim instanceof Claim real && real.allowedEntries.isAllowed(ClaimAllowListKey.ENTITY_USE,
                    type -> type == entity.getType(), entity.getType()::is))
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

    public static InteractionResult useEntity(Player p, Level level, InteractionHand hand, Entity entity) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) level);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (claim instanceof Claim real && real.allowedEntries.isAllowed(ClaimAllowListKey.ENTITY_USE,
                    type -> type == entity.getType(), entity.getType()::is))
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
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(proj.getType());
            if (ConfigHandler.CONFIG.ignoredProjectileTypes.contains(id.getNamespace()) || ConfigHandler.CONFIG.ignoredProjectileTypes.contains(id.toString())) {
                return false;
            }
            if (res.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRes = (BlockHitResult) res;
                BlockPos pos = blockRes.getBlockPos();
                BlockState state = proj.level().getBlockState(pos);
                ResourceLocation perm = InteractionOverrideManager.getInstance().getProjectileEntityInteract(proj);
                if (perm == null)
                    perm = InteractionOverrideManager.getInstance().getProjectileBlockInteract(state.getBlock());
                if (perm == null) {
                    return false;
                }
                ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level());
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                boolean fail = !claim.canInteract(player, perm, pos, true);
                if (fail) {
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
                    } else {
                        proj.discard();
                    }
                }
                return fail;
            } else if (res.getType() == HitResult.Type.ENTITY) {
                if (proj instanceof ThrownEnderpearl) {
                    ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level());
                    IPermissionContainer claim = storage.getForPermissionCheck(proj.blockPosition());
                    return !claim.canInteract(player, BuiltinPermission.ENDERPEARL, proj.blockPosition(), true);
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
            if (claim instanceof Claim real && real.allowedEntries.isAllowed(ClaimAllowListKey.ENTITY_ATTACK,
                    type -> type == entity.getType(), entity.getType()::is))
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

    public static boolean canFrostwalkerFreeze(ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer) {
            IPermissionContainer claim = ClaimStorage.get(level).getForPermissionCheck(pos);
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

    public static void handleVehiclePass(Entity entity) {
        if (entity.level().isClientSide)
            return;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) entity.level());
        Claim claim = storage.getClaimAt(entity.blockPosition());
        Vec3 last = null;
        if (entity instanceof VehiclePositionTracker tracker) {
            last = tracker.flan$updateAndGetLastPosition();
        }
        if (claim == null) {
            return;
        }
        Claim sub = claim.getSubClaim(entity.blockPosition());
        if (sub != null) {
            claim = sub;
        }
        BlockPos oldPos = last != null ? ofPos(last) : BlockPos.containing(entity.xo, entity.yo, entity.zo);
        Claim claimOld = storage.getClaimAt(oldPos);
        if (claimOld != null) {
            sub = claimOld.getSubClaim(entity.blockPosition());
            if (sub != null) {
                claimOld = sub;
            }
        }
        if (claimOld != claim && !claim.canInteract(null, BuiltinPermission.VEHICLE_PASS, entity.blockPosition(), false)) {
            BlockPos rounded = TeleportUtils.roundedBlockPos(entity.position());
            Vec3 tp = TeleportUtils.getTeleportPos(entity, entity.position(), storage,
                    new TeleportUtils.Area2D(claim.getDimensions()), true, rounded.mutable(),
                    (c, nPos) -> c.canInteract(null, BuiltinPermission.VEHICLE_PASS, nPos, false));
            if (!entity.isControlledByLocalInstance()) {
                // Otherwise cannot teleport as the client controls it
                entity.ejectPassengers();
            }
            entity.teleportTo(tp.x(), tp.y(), tp.z());
        }
    }

    private static BlockPos ofPos(Vec3 pos) {
        return BlockPos.containing(pos.x(), pos.y(), pos.z());
    }
}
