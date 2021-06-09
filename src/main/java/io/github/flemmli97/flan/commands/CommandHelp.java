package io.github.flemmli97.flan.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp {

    public static int helpMessage(CommandContext<ServerCommandSource> context, Collection<CommandNode<ServerCommandSource>> nodes) {
        int page = IntegerArgumentType.getInteger(context, "page");
        return helpMessage(context, page, nodes);
    }

    public static int helpMessage(CommandContext<ServerCommandSource> context, int page, Collection<CommandNode<ServerCommandSource>> nodes) {
        List<String> subCommands = registeredCommands(context, nodes);
        int max = subCommands.size() / 8;
        if (page > max)
            page = max;
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.helpHeader, page), Formatting.GOLD), false);
        for (int i = 8 * page; i < 8 * (page + 1); i++)
            if (i < subCommands.size()) {
                MutableText cmdText = PermHelper.simpleColoredText("- " + subCommands.get(i), Formatting.GRAY);
                context.getSource().sendFeedback(cmdText.fillStyle(cmdText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help cmd " + subCommands.get(i)))), false);
            }
        MutableText pageText = PermHelper.simpleColoredText((page > 0 ? "  " : "") + " ", Formatting.DARK_GREEN);
        if (page > 0) {
            MutableText pageTextBack = PermHelper.simpleColoredText("<<", Formatting.DARK_GREEN);
            pageTextBack.fillStyle(pageTextBack.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help " + (page - 1))));
            pageText = pageTextBack.append(pageText);
        }
        if (page < max) {
            MutableText pageTextNext = PermHelper.simpleColoredText(">>");
            pageTextNext.fillStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help " + (page + 1))));
            pageText = pageText.append(pageTextNext);
        }
        context.getSource().sendFeedback(pageText, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int helpCmd(CommandContext<ServerCommandSource> context) {
        String command = StringArgumentType.getString(context, "command");
        String[] cmdHelp = ConfigHandler.lang.cmdLang.getCommandHelp(command);
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.helpCmdHeader, Formatting.DARK_GREEN), false);
        for (int i = 0; i < cmdHelp.length; i++) {
            if (i == 0) {
                context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.helpCmdSyntax, cmdHelp[i]), Formatting.GOLD), false);
                context.getSource().sendFeedback(PermHelper.simpleColoredText(""), false);
            } else {
                context.getSource().sendFeedback(PermHelper.simpleColoredText(cmdHelp[i], Formatting.GOLD), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static List<String> registeredCommands(CommandContext<ServerCommandSource> context, Collection<CommandNode<ServerCommandSource>> nodes) {
        return nodes.stream().filter(node -> node.canUse(context.getSource())).map(CommandNode::getName).collect(Collectors.toList());
    }
}
