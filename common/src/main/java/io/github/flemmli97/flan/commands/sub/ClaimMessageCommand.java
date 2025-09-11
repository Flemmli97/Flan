package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class ClaimMessageCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder, CommandBuildContext buildContext) {
        builder.then(Commands.literal("claimMessage").then(Commands.argument("type", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"enter", "leave"}, b))
                .then(Commands.argument("title", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"title", "subtitle"}, b))
                        .then(Commands.literal("text").then(Commands.argument("component", ComponentArgument.textComponent(buildContext)).executes(ctx -> ClaimMessageCommand.editClaimMessages(ctx, ComponentArgument.getComponent(ctx, "component")))))
                        .then(Commands.literal("string").then(Commands.argument("message", StringArgumentType.string()).executes(ClaimMessageCommand::editClaimMessages))))));
    }

    private static int editClaimMessages(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return editClaimMessages(context, Component.literal(StringArgumentType.getString(context, "message")));
    }

    private static int editClaimMessages(CommandContext<CommandSourceStack> context, Component text) throws CommandSyntaxException {
        if (text instanceof MutableComponent) {
            Style style = text.getStyle();
            if (style.isEmpty())
                style = style.applyFormat(ChatFormatting.WHITE);
            if (!style.isItalic())
                style = style.withItalic(false);
            ((MutableComponent) text).setStyle(style);
        }
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.getClaimFromMode(context, player, BuiltinPermission.CLAIMMESSAGE);
        if (claim == null)
            return 0;
        boolean sub = StringArgumentType.getString(context, "title").equals("subtitle");
        boolean enter = StringArgumentType.getString(context, "type").equals("enter");
        String feedback;
        if (enter) {
            if (sub) {
                claim.setEnterTitle(claim.enterTitle, text);
                feedback = "flan.setEnterSubMessage";
            } else {
                claim.setEnterTitle(text, claim.enterSubtitle);
                feedback = "flan.setEnterMessage";
            }
        } else {
            if (sub) {
                claim.setLeaveTitle(claim.leaveTitle, text);
                feedback = "flan.setLeaveSubMessage";
            } else {
                claim.setLeaveTitle(text, claim.leaveSubtitle);
                feedback = "flan.setLeaveMessage";
            }
        }
        MutableComponent cmdFeed = ClaimUtils.translatedText(feedback, text).withStyle(ChatFormatting.GOLD);
        context.getSource().sendSuccess(() -> cmdFeed, false);
        return Command.SINGLE_SUCCESS;
    }
}
