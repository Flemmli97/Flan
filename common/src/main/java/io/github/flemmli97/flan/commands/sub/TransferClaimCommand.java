package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.data.IPlayerData;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.OfflinePlayerData;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class TransferClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("transferClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_TRANSFER)).then(Commands.argument("player", GameProfileArgument.gameProfile()).executes(TransferClaimCommand::transferClaim)));
    }

    private static int transferClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Collection<GameProfile> profs = GameProfileArgument.getGameProfiles(context, "player");
        if (profs.size() != 1) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.onlyOnePlayer", ChatFormatting.RED), false);
            return 0;
        }
        GameProfile prof = profs.iterator().next();
        ClaimStorage storage = ClaimStorage.get(player.level());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.RED), false);
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        boolean enoughBlocks = true;
        if (!data.isAdminIgnoreClaim()) {
            MinecraftServer server = context.getSource().getServer();
            ServerPlayer newOwner = server.getPlayerList().getPlayer(prof.getId());
            IPlayerData newData = newOwner != null ? PlayerClaimData.get(newOwner) : new OfflinePlayerData(server, prof.getId());
            enoughBlocks = newData.canUseClaimBlocks(claim.getPlane());
        }
        if (!enoughBlocks) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.ownerTransferNoBlocks", ChatFormatting.RED), false);
            if (PermissionNodeHandler.INSTANCE.perm(context.getSource(), PermissionNodeHandler.CMD_BYPASS_MODE, true))
                player.displayClientMessage(ClaimUtils.translatedText("flan.ownerTransferNoBlocksAdmin", ChatFormatting.RED), false);
            return 0;
        }
        if (!storage.transferOwner(claim, player, prof.getId())) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.ownerTransferFail", ChatFormatting.RED), false);
            return 0;
        }
        player.displayClientMessage(ClaimUtils.translatedText("flan.ownerTransferSuccess", prof.getName(), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }
}
