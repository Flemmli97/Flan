package io.github.flemmli97.flan.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp {

    public static int helpMessage(CommandContext<CommandSourceStack> context, Collection<CommandNode<CommandSourceStack>> nodes) {
        int page = IntegerArgumentType.getInteger(context, "page");
        return helpMessage(context, page, nodes);
    }

    public static int helpMessage(CommandContext<CommandSourceStack> context, int page, Collection<CommandNode<CommandSourceStack>> nodes) {
        List<String> subCommands = registeredCommands(context, nodes);
        subCommands.remove("?");
        int max = subCommands.size() / 8;
        if (page > max)
            page = max;
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.helpHeader, page), ChatFormatting.GOLD), false);
        for (int i = 8 * page; i < 8 * (page + 1); i++)
            if (i < subCommands.size()) {
                MutableComponent cmdText = PermHelper.simpleColoredText("- " + subCommands.get(i), ChatFormatting.GRAY);
                context.getSource().sendSuccess(cmdText.withStyle(cmdText.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help cmd " + subCommands.get(i)))), false);
            }
        MutableComponent pageText = PermHelper.simpleColoredText((page > 0 ? "  " : "") + " ", ChatFormatting.DARK_GREEN);
        if (page > 0) {
            MutableComponent pageTextBack = PermHelper.simpleColoredText("<<", ChatFormatting.DARK_GREEN);
            pageTextBack.withStyle(pageTextBack.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help " + (page - 1))));
            pageText = pageTextBack.append(pageText);
        }
        if (page < max) {
            MutableComponent pageTextNext = PermHelper.simpleColoredText(">>");
            pageTextNext.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flan help " + (page + 1))));
            pageText = pageText.append(pageTextNext);
        }
        context.getSource().sendSuccess(pageText, false);
        return Command.SINGLE_SUCCESS;
    }

    public static int helpCmd(CommandContext<CommandSourceStack> context) {
        String command = StringArgumentType.getString(context, "command");
        return helpCmd(context, command);
    }

    public static int helpCmd(CommandContext<CommandSourceStack> context, String command) {
        String[] cmdHelp = ConfigHandler.lang.cmdLang.getCommandHelp(command);
        context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.helpCmdHeader, ChatFormatting.DARK_GREEN), false);
        for (int i = 0; i < cmdHelp.length; i++) {
            if (i == 0) {
                context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.helpCmdSyntax, cmdHelp[i]), ChatFormatting.GOLD), false);
                context.getSource().sendSuccess(PermHelper.simpleColoredText(""), false);
            } else {
                context.getSource().sendSuccess(PermHelper.simpleColoredText(cmdHelp[i], ChatFormatting.GOLD), false);
            }
        }
        if (command.equals("help")) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.wiki, ChatFormatting.GOLD), false);
            MutableComponent wiki = PermHelper.simpleColoredText("https://github.com/Flemmli97/Flan/wiki", ChatFormatting.GREEN);
            wiki.setStyle(wiki.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Flemmli97/Flan/wiki")));
            context.getSource().sendSuccess(wiki, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static List<String> registeredCommands(CommandContext<CommandSourceStack> context, Collection<CommandNode<CommandSourceStack>> nodes) {
        return nodes.stream().filter(node -> node.canUse(context.getSource())).map(CommandNode::getName).collect(Collectors.toList());
    }
}
