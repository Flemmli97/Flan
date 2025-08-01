package io.github.flemmli97.flan.fabric.platform.integration.claiming;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.common.protection.api.ProtectionProvider;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class FlanProtectionProvider implements ProtectionProvider {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("flan", "provider");

    public static void register() {
        CommonProtection.register(ID, new FlanProtectionProvider());
    }

    @Override
    public boolean isProtected(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel sl)) {
            return false;
        }

        return ClaimStorage.get(sl).getClaimAt(pos) != null;
    }

    @Override
    public boolean isAreaProtected(Level level, AABB area) {
        if (!(level instanceof ServerLevel sl)) return false;

        int minChunkX = (int) Math.floor(area.minX);
        int minChunkZ = (int) Math.floor(area.minZ);
        int maxChunkX = (int) Math.floor(area.maxX);
        int maxChunkZ = (int) Math.floor(area.maxZ);
        ClaimStorage storage = ClaimStorage.get(sl);

        for (int chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
                for (Claim claim : storage.getClaimsAt(chunkX, chunkZ)) {
                    if (claim.intersects(area)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canBreakBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        return ClaimStorage.get(sl).getForPermissionCheck(pos).canInteract(sp, BuiltinPermission.BREAK, pos);
    }

    @Override
    public boolean canExplodeBlock(Level level, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        return ClaimStorage.get(sl).getForPermissionCheck(pos).canInteract(sp, BuiltinPermission.EXPLOSIONS, pos);
    }

    @Override
    public boolean canPlaceBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        return ClaimStorage.get(sl).getForPermissionCheck(pos).canInteract(sp, BuiltinPermission.PLACE, pos);
    }

    @Override
    public boolean canInteractBlock(Level level, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        ResourceLocation perm = InteractionOverrideManager.getInstance().getBlockInteract(sl.getBlockState(pos).getBlock());

        if (perm != null && perm.equals(BuiltinPermission.PROJECTILES))
            perm = BuiltinPermission.OPENCONTAINER;

        if (perm == null) {
            BlockEntity be = level.getBlockEntity(pos);
            perm = be != null && CrossPlatformStuff.INSTANCE.isInventoryTile(be)
                    ? BuiltinPermission.OPENCONTAINER
                    : BuiltinPermission.INTERACTBLOCK;
        }

        return ClaimStorage.get(sl).getForPermissionCheck(pos).canInteract(sp, perm, pos);
    }

    @Override
    public boolean canInteractEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        ResourceLocation permission;

        if (entity instanceof ArmorStand)
            permission = BuiltinPermission.ARMORSTAND;
        else if (entity instanceof Mob)
            permission = BuiltinPermission.ANIMALINTERACT;
        else
            return true;

        return ClaimStorage.get(sl).getForPermissionCheck(entity.blockPosition()).canInteract(sp, permission, entity.blockPosition());
    }

    @Override
    public boolean canDamageEntity(Level level, Entity entity, GameProfile profile, @Nullable Player player) {
        if (!(level instanceof ServerLevel sl)) return true;

        ServerPlayer sp = tryResolvePlayer(sl, profile);

        ResourceLocation permission;

        if (entity instanceof ArmorStand || !(entity instanceof LivingEntity))
            permission = BuiltinPermission.BREAKNONLIVING;
        else if (entity instanceof Player)
            permission = BuiltinPermission.HURTPLAYER;
        else
            permission = BuiltinPermission.HURTANIMAL;

        if (entity.hasCustomName() && !ClaimStorage.get(sl).getForPermissionCheck(entity.blockPosition()).canInteract(sp, BuiltinPermission.HURTNAMED, entity.blockPosition())) {
            return false;
        }

        return ClaimStorage.get(sl).getForPermissionCheck(entity.blockPosition()).canInteract(sp, permission, entity.blockPosition());
    }

    private static ServerPlayer tryResolvePlayer(ServerLevel l, GameProfile profile) {
        if (profile.equals(UNKNOWN))
            return null;

        ServerPlayer online = l.getServer().getPlayerList().getPlayer(profile.getId());

        if (online != null) return online;

        return FakePlayer.get(l, profile);
    }
}
