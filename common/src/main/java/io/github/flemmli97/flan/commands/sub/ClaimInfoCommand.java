package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ClaimInfoCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("info").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_INFO)).executes(ctx -> ClaimInfoCommand.claimInfo(ctx, Claim.InfoType.ALL))
                .then(Commands.argument("type", StringArgumentType.word()).suggests((src, b) -> CommandHelpers.enumSuggestion(Claim.InfoType.class, b)).executes(ClaimInfoCommand::claimInfo)));
    }

    private static int claimInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return claimInfo(context, CommandHelpers.parseEnum(Claim.InfoType.class, StringArgumentType.getString(context, "type"), Claim.InfoType.ALL));
    }

    private static int claimInfo(CommandContext<CommandSourceStack> context, Claim.InfoType infoType) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.fromContext(context);
        if (claim == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.RED));
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getClaimMode().isSubclaim) {
            Claim sub = claim.getSubClaim(BlockPos.containing(context.getSource().getPosition()));
            if (sub != null) {
                List<Component> info = sub.infoString(player, infoType);
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimSubHeader", ChatFormatting.AQUA), false);
                for (Component text : info)
                    context.getSource().sendSuccess(() -> text, false);
                return Command.SINGLE_SUCCESS;
            }
        }
        List<Component> info = claim.infoString(player, infoType);
        for (Component text : info)
            context.getSource().sendSuccess(() -> text, false);
        return Command.SINGLE_SUCCESS;
    }
}
