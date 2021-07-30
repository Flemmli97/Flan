package io.github.flemmli97.flan.integration.currency;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.server.command.ServerCommandSource;

public class CommandCurrency {

    @ExpectPlatform
    public static int sellClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int buyClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        throw new AssertionError();
    }
}
