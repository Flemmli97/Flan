package io.github.flemmli97.flan.integration.gunpowder.forge;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class CommandCurrencyImpl {

    public static int sellClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.gunpowderMissing, Formatting.DARK_RED), false);
        return 0;
    }

    public static int buyClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.gunpowderMissing, Formatting.DARK_RED), false);
        return 0;
    }
}
