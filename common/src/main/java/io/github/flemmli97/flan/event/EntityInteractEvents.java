package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.IHungerAccessor;
import io.github.flemmli97.flan.mixin.IPersistentProjectileVars;
import io.github.flemmli97.flan.player.IOwnedItem;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.TeleportUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class EntityInteractEvents {

    public static InteractionResult attackEntity(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        return attackSimple(player, entity, true);
    }

    public static InteractionResult useAtEntity(Player player, Level world, InteractionHand hand, Entity entity, /* Nullable */ EntityHitResult hitResult) {
        if (!(player instanceof ServerPlayer) || player.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (entity instanceof ArmorStand) {
                if (!claim.canInteract((ServerPlayer) player, PermissionRegistry.ARMORSTAND, pos, true))
                    return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult useEntity(Player p, Level world, InteractionHand hand, Entity entity) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (entity instanceof Boat)
                return claim.canInteract(player, PermissionRegistry.BOAT, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof AbstractMinecart) {
                if (entity instanceof AbstractMinecartContainer)
                    return claim.canInteract(player, PermissionRegistry.OPENCONTAINER, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
                return claim.canInteract(player, PermissionRegistry.MINECART, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            }
            if (entity instanceof Villager)
                return claim.canInteract(player, PermissionRegistry.TRADING, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof ItemFrame)
                return claim.canInteract(player, PermissionRegistry.ITEMFRAMEROTATE, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof OwnableEntity tame) {
                if (tame.getOwnerUUID() != null && tame.getOwnerUUID().equals(player.getUUID()))
                    return InteractionResult.PASS;
            }
            return claim.canInteract(player, PermissionRegistry.ANIMALINTERACT, pos, true) ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean canInteract(Entity entity) {
        ResourceLocation id = CrossPlatformStuff.registryEntities().getIDFrom(entity.getType());
        return ConfigHandler.config.ignoredEntityTypes.contains(id.getNamespace())
                || ConfigHandler.config.ignoredEntityTypes.contains(id.toString())
                || entity.getTags().stream().anyMatch(ConfigHandler.config.entityTagIgnore::contains);
    }

    public static boolean projectileHit(Projectile proj, HitResult res) {
        if (proj.level.isClientSide)
            return false;
        Entity owner = proj.getOwner();
        if (owner instanceof ServerPlayer player) {
            if (res.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRes = (BlockHitResult) res;
                BlockPos pos = blockRes.getBlockPos();
                BlockState state = proj.level.getBlockState(pos);
                ClaimPermission perm;
                if (proj instanceof ThrownEnderpearl)
                    perm = PermissionRegistry.ENDERPEARL;
                else if (proj instanceof ThrownEgg || proj instanceof ThrownPotion)
                    perm = PermissionRegistry.PROJECTILES;
                else
                    perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
                if (perm != PermissionRegistry.ENDERPEARL && perm != PermissionRegistry.TARGETBLOCK && perm != PermissionRegistry.PROJECTILES)
                    return false;
                ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                boolean flag = !claim.canInteract(player, perm, pos, true);
                if (flag) {
                    if (proj instanceof AbstractArrow pers) {
                        ((IPersistentProjectileVars) pers).setInBlockState(pers.level.getBlockState(pos));
                        Vec3 vec3d = blockRes.getLocation().subtract(pers.getX(), pers.getY(), pers.getZ());
                        pers.setDeltaMovement(vec3d);
                        Vec3 vec3d2 = vec3d.normalize().scale(0.05000000074505806D);
                        pers.setPosRaw(pers.getX() - vec3d2.x, pers.getY() - vec3d2.y, pers.getZ() - vec3d2.z);
                        pers.playSound(((IPersistentProjectileVars) pers).getSoundEvent(), 1.0F, 1.2F / (pers.level.random.nextFloat() * 0.2F + 0.9F));
                        ((IPersistentProjectileVars) pers).setInGround(true);
                        pers.shakeTime = 7;
                        pers.setCritArrow(false);
                        pers.setPierceLevel((byte) 0);
                        pers.setSoundEvent(SoundEvents.ARROW_HIT);
                        pers.setShotFromCrossbow(false);
                        ((IPersistentProjectileVars) pers).resetPiercingStatus();
                    }
                    if (proj instanceof ThrownEnderpearl)
                        proj.remove(Entity.RemovalReason.KILLED);
                    //TODO: find a way to properly update chorus fruit break on hit
                    //player.getServer().send(new ServerTask(player.getServer().getTicks()+2, ()->player.world.updateListeners(pos, state, state, 2)));
                }
                return flag;
            } else if (res.getType() == HitResult.Type.ENTITY) {
                if (proj instanceof ThrownEnderpearl) {
                    ClaimStorage storage = ClaimStorage.get((ServerLevel) proj.level);
                    IPermissionContainer claim = storage.getForPermissionCheck(proj.blockPosition());
                    return claim.canInteract(player, PermissionRegistry.ENDERPEARL, proj.blockPosition(), true);
                }
                Entity hit = ((EntityHitResult) res).getEntity();
                boolean fail = attackSimple(player, hit, true) != InteractionResult.PASS;
                if (fail && proj instanceof AbstractArrow pers && ((AbstractArrow) proj).getPierceLevel() > 0) {
                    IntOpenHashSet pierced = ((IPersistentProjectileVars) pers).getPiercedEntities();
                    if (pierced == null)
                        pierced = new IntOpenHashSet(5);
                    pierced.add(hit.getId());
                    ((IPersistentProjectileVars) pers).setPiercedEntities(pierced);
                    pers.setPierceLevel((byte) (pers.getPierceLevel() + 1));
                }
                return fail;
            }
        }
        return false;
    }

    public static boolean preventDamage(Entity entity, DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer)
            return attackSimple((ServerPlayer) source.getEntity(), entity, true) != InteractionResult.PASS;
        else if (source.isExplosion() && !entity.level.isClientSide && !(entity instanceof ServerPlayer || entity instanceof Enemy)) {
            IPermissionContainer claim = ClaimStorage.get((ServerLevel) entity.level).getForPermissionCheck(entity.blockPosition());
            return claim != null && !claim.canInteract(null, PermissionRegistry.EXPLOSIONS, entity.blockPosition());
        }
        return false;
    }

    public static InteractionResult attackSimple(Player p, Entity entity, boolean message) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator() || canInteract(entity))
            return InteractionResult.PASS;
        if (entity instanceof Enemy)
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        BlockPos pos = entity.blockPosition();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (entity instanceof ArmorStand || !(entity instanceof LivingEntity))
                return claim.canInteract(player, PermissionRegistry.BREAKNONLIVING, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
            if (entity instanceof Player)
                return claim.canInteract(player, PermissionRegistry.HURTPLAYER, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
            return claim.canInteract(player, PermissionRegistry.HURTANIMAL, pos, message) ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean xpAbsorb(Player player) {
        if (player instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            BlockPos pos = player.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract((ServerPlayer) player, PermissionRegistry.XP, pos, false);
        }
        return false;
    }

    public static boolean canCollideWith(Player player, Entity entity) {
        if (player instanceof ServerPlayer sPlayer) {
            if (entity instanceof ItemEntity) {
                IOwnedItem ownedItem = (IOwnedItem) entity;
                if (ownedItem.getDeathPlayer() != null) {
                    ServerPlayer other = sPlayer.getServer().getPlayerList().getPlayer(ownedItem.getDeathPlayer());
                    if (other == null)
                        return false;
                    return ownedItem.getDeathPlayer().equals(player.getUUID()) || PlayerClaimData.get(other).deathItemsUnlocked();
                }
                if (sPlayer.getUUID().equals(ownedItem.getPlayerOrigin()))
                    return true;
                ClaimStorage storage = ClaimStorage.get(sPlayer.getLevel());
                BlockPos pos = sPlayer.blockPosition();
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim != null)
                    return claim.canInteract(sPlayer, PermissionRegistry.PICKUP, pos, false);
            }
        }
        return true;
    }

    public static boolean canDropItem(Player player, ItemStack stack) {
        if (!player.isDeadOrDying() && player instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get((ServerLevel) player.level);
            BlockPos pos = player.blockPosition();
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            boolean allow = true;
            if (claim != null)
                allow = claim.canInteract((ServerPlayer) player, PermissionRegistry.DROP, pos, false);
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
        if (wither.level.isClientSide)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) wither.level);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                pos.setWithOffset(wither.blockPosition(), x, 3, z);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (!claim.canInteract(null, PermissionRegistry.WITHER, pos, false))
                    return false;
            }
        return true;
    }

    public static boolean canEndermanInteract(EnderMan enderman, BlockPos pos) {
        if (enderman.level.isClientSide)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) enderman.level);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        return claim.canInteract(null, PermissionRegistry.ENDERMAN, pos, false);
    }

    public static boolean canSnowGolemInteract(SnowGolem snowgolem) {
        if (snowgolem.level.isClientSide)
            return true;
        int x, y, z;
        for (int l = 0; l < 4; ++l) {
            x = Mth.floor(snowgolem.getX() + (l % 2 * 2 - 1) * 0.25F);
            y = Mth.floor(snowgolem.getY());
            z = Mth.floor(snowgolem.getZ() + (l / 2 % 2 * 2 - 1) * 0.25F);
            BlockPos pos = new BlockPos(x, y, z);
            IPermissionContainer claim = ClaimStorage.get((ServerLevel) snowgolem.level).getForPermissionCheck(pos);
            if (!claim.canInteract(null, PermissionRegistry.SNOWGOLEM, pos, false))
                return false;
        }
        return true;
    }

    public static void updateDroppedItem(Player player, ItemEntity entity) {
        ((IOwnedItem) entity).setOriginPlayer((player));
    }

    public static void updateClaim(ServerPlayer player, Claim currentClaim, Consumer<Claim> cons) {
        Vec3 pos = player.position();
        BlockPos rounded = TeleportUtils.roundedBlockPos(pos.add(0, player.getStandingEyeHeight(player.getPose(), player.getDimensions(player.getPose())), 0));
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        if (currentClaim != null) {
            if (!currentClaim.intersects(player.getBoundingBox())) {
                Claim claim = storage.getClaimAt(rounded);
                cons.accept(claim);
                if (claim == null)
                    currentClaim.displayLeaveTitle(player);
                else
                    claim.displayEnterTitle(player);
            } else {
                if (!player.isSpectator()) {
                    BlockPos.MutableBlockPos bPos = rounded.mutable();
                    if (!currentClaim.canInteract(player, PermissionRegistry.CANSTAY, bPos, true)) {
                        Claim sub = currentClaim.getSubClaim(bPos);
                        Vec3 tp = TeleportUtils.getTeleportPos(player, pos, storage, sub != null ? sub.getDimensions() : currentClaim.getDimensions(), true, bPos, (claim, nPos) -> claim.canInteract(player, PermissionRegistry.CANSTAY, nPos, false));
                        if (player.isPassenger())
                            player.stopRiding();
                        player.teleportToWithTicket(tp.x(), tp.y(), tp.z());
                    }
                    if (player.getAbilities().flying && !player.isCreative() && !currentClaim.canInteract(player, PermissionRegistry.FLIGHT, rounded, true)) {
                        player.getAbilities().flying = false;
                        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                    }
                    if (player.getFoodData().getSaturationLevel() < 2 && currentClaim.canInteract(player, PermissionRegistry.NOHUNGER, bPos, false)) {
                        ((IHungerAccessor) player.getFoodData()).setSaturation(2);
                    }
                    currentClaim.applyEffects(player);
                }
            }
        } else if (player.tickCount % 3 == 0) {
            Claim claim = storage.getClaimAt(rounded);
            cons.accept(claim);
            if (claim != null)
                claim.displayEnterTitle(player);
        }
    }

    public static boolean canFrostwalkerFreeze(ServerLevel world, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer) {
            IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(pos);
            return claim.canInteract((ServerPlayer) entity, PermissionRegistry.FROSTWALKER, pos, false);
        }
        return true;
    }

    public static boolean preventLightningConvert(Entity entity) {
        if (entity.level.isClientSide || entity instanceof Enemy)
            return false;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) entity.level);
        IPermissionContainer claim = storage.getForPermissionCheck(entity.blockPosition());
        return !claim.canInteract(null, PermissionRegistry.LIGHTNING, entity.blockPosition(), false);
    }
}
