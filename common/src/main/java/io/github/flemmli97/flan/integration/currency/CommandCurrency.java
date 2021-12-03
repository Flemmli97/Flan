package io.github.flemmli97.flan.integration.currency;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;

public class CommandCurrency {

    @ExpectPlatform
    public static int sellClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int buyClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        throw new AssertionError();
    }
}
