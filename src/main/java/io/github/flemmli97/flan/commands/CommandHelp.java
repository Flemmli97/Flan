package io.github.flemmli97.flan.commands;

import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
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
        List<String> subCommands = nodes.stream().filter(node -> node.canUse(context.getSource())).map(CommandNode::getName).collect(Collectors.toList());
        int max = subCommands.size() / 8;
        if (page > max)
            page = max;
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.helpHeader, page), Formatting.GOLD), false);
        for (int i = 8 * page; i < 8 * (page + 1); i++)
            if (i < subCommands.size())
                context.getSource().sendFeedback(PermHelper.simpleColoredText("   -" + subCommands.get(i), Formatting.GRAY), false);
        MutableText pageText = PermHelper.simpleColoredText((page > 0 ? "  " : "") + "         ", Formatting.DARK_GREEN);
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
}
