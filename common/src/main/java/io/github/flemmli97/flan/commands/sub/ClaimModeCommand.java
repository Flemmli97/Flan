package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.ClaimMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClaimModeCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("switchMode").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_CLAIM_MODE))
                .then(Commands.argument("mode", StringArgumentType.word()).suggests((src, b) -> CommandHelpers.enumSuggestion(ClaimMode.class, b))
                        .executes(ClaimModeCommand::switchEditMode)));
    }

    private static int switchEditMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String s = StringArgumentType.getString(context, "mode");
        ClaimMode mode = CommandHelpers.parseEnum(ClaimMode.class, s, null);
        if (mode == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.claimMode", s, ChatFormatting.GOLD));
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setEditMode(mode);
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimMode", Component.translatable(data.getClaimMode().translationKey)
                .withStyle(ChatFormatting.AQUA), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }
}
