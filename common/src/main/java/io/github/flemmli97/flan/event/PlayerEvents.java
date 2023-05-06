package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

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
            BlockState state = serverPlayer.level.getBlockState(context.getClickedPos());
            BlockPos.MutableBlockPos pos = context.getClickedPos().mutable();
            ClaimPermission perm = ObjectToPermissionMap.getFromItem(context.getItemInHand().getItem());
            /**
             * {@link ItemInteractEvents#onItemUseBlock} handles this case already.
             * Sadly need to check again. In case its used in a claim. Less expensive than aoe check
             */
            if (!ClaimStorage.get(serverPlayer.getLevel()).getForPermissionCheck(pos).canInteract(serverPlayer, perm, pos, false))
                return false;
            int range = 0;
            if (state.getBlock() instanceof MossBlock) {
                VegetationPatchConfiguration cfg = CaveFeatures.MOSS_PATCH_BONEMEAL.value().config();
                range = cfg.xzRadius.getMaxValue() + 1;
                pos.set(pos.getX(), pos.getY() + cfg.verticalRange + 1, pos.getZ());
            } else if (state.getBlock() instanceof GrassBlock) {
                range = 4;
            } else if (state.is(Blocks.CRIMSON_NYLIUM)) {
                NetherForestVegetationConfig cfg = NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL.value().config();
                range = cfg.spreadWidth;
                pos.set(pos.getX(), pos.getY() + cfg.spreadHeight + 1, pos.getZ());
            } else if (state.is(Blocks.WARPED_NYLIUM)) {
                NetherForestVegetationConfig cfg = NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL.value().config();
                NetherForestVegetationConfig cfg2 = NetherFeatures.NETHER_SPROUTS_BONEMEAL.value().config();
                TwistingVinesConfig cfg3 = NetherFeatures.TWISTING_VINES_BONEMEAL.value().config();
                range = Math.max(Math.max(cfg.spreadWidth, cfg2.spreadWidth), cfg3.spreadWidth());
                int y = Math.max(Math.max(cfg.spreadHeight, cfg2.spreadHeight), cfg3.spreadHeight());
                pos.set(pos.getX(), pos.getY() + y + 1, pos.getZ());
            }
            if (range > 0 && perm != null && !ClaimStorage.get(serverPlayer.getLevel()).canInteract(pos, range, serverPlayer, perm, false)) {
                serverPlayer.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("tooCloseClaim"), ChatFormatting.DARK_RED), true);
                return true;
            }
        }
        return false;
    }

    public static float canSpawnFromPlayer(Entity entity, float old) {
        BlockPos pos;
        if (entity instanceof ServerPlayer player &&
                !ClaimStorage.get(player.getLevel()).getForPermissionCheck(pos = player.blockPosition()).canInteract(player, PermissionRegistry.PLAYERMOBSPAWN, pos, false))
            return -1;
        return old;
    }

    public static boolean canWardenSpawnTrigger(BlockPos pos, ServerPlayer player) {
        return ClaimStorage.get(player.getLevel()).getForPermissionCheck(pos).canInteract(player, PermissionRegistry.PLAYERMOBSPAWN, pos, false);
    }

    public static boolean canSculkTrigger(BlockPos pos, ServerPlayer player) {
        return ClaimStorage.get(player.getLevel()).getForPermissionCheck(pos).canInteract(player, PermissionRegistry.SCULK, pos, false);
    }
}
