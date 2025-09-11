package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ConfirmCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("confirm").then(Commands.argument("confirm", StringArgumentType.string())
                .suggests((ctx, sb) -> SharedSuggestionProvider.suggest(List.of("confirm", "deny"), sb)).executes(ConfirmCommand::confirmCommand)));
    }

    private static int confirmCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        String confirm = StringArgumentType.getString(context, "confirm");
        if (!confirm.equals("confirm") && !confirm.equals("deny")) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCommand.args", ChatFormatting.RED), false);
            return 0;
        }
        int res = data.runPendingCommand(confirm.equals("confirm"));
        if (res == -1) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCommand.none", ChatFormatting.RED), false);
            return 0;
        }
        return res;
    }
}
