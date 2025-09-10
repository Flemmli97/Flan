package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TrappedCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("trapped").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_TRAPPED)).executes(TrappedCommand::trapped));
    }

    private static int trapped(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.setTrappedRescue()) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.trappedRescue", ChatFormatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.trappedFail", ChatFormatting.RED), false);
        }
        return 0;
    }
}
