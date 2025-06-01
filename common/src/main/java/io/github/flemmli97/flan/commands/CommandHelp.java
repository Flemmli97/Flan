package io.github.flemmli97.flan.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.linguabib.api.LanguageAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp {

    public static int helpMessage(CommandContext<CommandSourceStack> context, Collection<CommandNode<CommandSourceStack>> nodes) {
        int page = IntegerArgumentType.getInteger(context, "page");
        return helpMessage(context, page, nodes);
    }

    public static int helpMessage(CommandContext<CommandSourceStack> context, int pageC, Collection<CommandNode<CommandSourceStack>> nodes) {
        List<String> subCommands = registeredCommands(context, nodes);
        subCommands.remove("?");
        int max = subCommands.size() / 8;
        int page = Math.min(pageC, max);
        List<String> header = lang(context, "flan.commands.helpHeader");
        for (String headerTxt : header) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText(headerTxt, page, ChatFormatting.GREEN), false);
        }
        for (int i = 8 * page; i < 8 * (page + 1); i++)
            if (i < subCommands.size()) {
                String sub = subCommands.get(i);
                MutableComponent cmdText = ClaimUtils.translatedText("flan.commands.helpCmdEntry", subCommands.get(i), ChatFormatting.GRAY);
                context.getSource().sendSuccess(() -> cmdText.withStyle(cmdText.getStyle().withClickEvent(new ClickEvent.RunCommand("/flan help cmd " + sub))), false);
            }
        MutableComponent pageText = null;
        if (page > 0) {
            MutableComponent pageTextBack = ClaimUtils.translatedText("flan.commands.help.previous", ChatFormatting.DARK_GREEN);
            pageTextBack.withStyle(pageTextBack.getStyle().withClickEvent(new ClickEvent.RunCommand("/flan help " + (page - 1))));
            pageText = pageTextBack;
        }
        if (page < max) {
            MutableComponent pageTextNext = ClaimUtils.translatedText("flan.commands.help.next", ChatFormatting.DARK_GREEN);
            pageTextNext.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/flan help " + (page + 1))));
            if (pageText != null) {
                pageText = pageText.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(pageTextNext);
            } else
                pageText = pageTextNext;
        }
        if (pageText != null) {
            MutableComponent txt = pageText;
            context.getSource().sendSuccess(() -> txt, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int helpCmd(CommandContext<CommandSourceStack> context) {
        String command = StringArgumentType.getString(context, "command");
        return helpCmd(context, command);
    }

    public static int helpCmd(CommandContext<CommandSourceStack> context, String command) {
        List<String> cmdHelp = lang(context, "flan.command." + command);
        List<String> header = lang(context, "flan.commands.syntaxHeader");
        for (int i = 0; i < cmdHelp.size(); i++) {
            String cmp = cmdHelp.get(i);
            if (i == 0) {
                for (String headerTxt : header) {
                    context.getSource().sendSuccess(() -> ClaimUtils.translatedText(headerTxt,
                            ClaimUtils.translatedText(cmp), ChatFormatting.GREEN), false);
                }
            } else {
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText(cmp, ChatFormatting.GOLD), false);
            }
        }
        if (command.equals("help")) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.wiki", ChatFormatting.GOLD), false);
            MutableComponent wiki = ClaimUtils.translatedText("https://github.com/Flemmli97/Flan/wiki", ChatFormatting.GREEN);
            wiki.setStyle(wiki.getStyle().withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/Flemmli97/Flan/wiki"))));
            context.getSource().sendSuccess(() -> wiki, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static List<String> registeredCommands(CommandContext<CommandSourceStack> context, Collection<CommandNode<CommandSourceStack>> nodes) {
        return nodes.stream().filter(node -> node.canUse(context.getSource())).map(CommandNode::getName).collect(Collectors.toList());
    }

    private static List<String> lang(CommandContext<CommandSourceStack> context, String key) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            return LanguageAPI.getFormattedKeys(player, key);
        }
        return LanguageAPI.getFormattedKeys(LanguageAPI.defaultServerLanguage(), key);
    }
}
