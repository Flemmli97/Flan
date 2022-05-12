package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.LogoutTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.state.BlockState;

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

    public static boolean mossBonemeal(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            BlockState state = serverPlayer.level.getBlockState(context.getClickedPos());
            if (state.getBlock() instanceof MossBlock) {
                BlockPos.MutableBlockPos mutable = context.getClickedPos().mutable();
                int range = CaveFeatures.MOSS_PATCH_BONEMEAL.value().config()
                        .xzRadius.getMaxValue() + 1;
                ClaimPermission perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
                if (perm != null && !ClaimStorage.get(serverPlayer.getLevel()).canInteract(mutable, range, serverPlayer, perm, false)) {
                    serverPlayer.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("tooCloseClaim"), ChatFormatting.DARK_RED), true);
                    return true;
                }
            }
        }
        return false;
    }
}
