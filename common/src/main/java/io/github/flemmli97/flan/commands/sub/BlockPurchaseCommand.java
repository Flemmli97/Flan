package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BlockPurchaseCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("buy").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_BUY, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(BlockPurchaseCommand::buyClaimBlocks)))
                .then(Commands.literal("sell").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_SELL, false))
                        .then(Commands.argument("amount", IntegerArgumentType.integer()).executes(BlockPurchaseCommand::sellClaimBlocks)));
    }

    private static int sellClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.CONFIG.buySellHandler.sell(context.getSource().getPlayerOrException(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendSuccess(() -> m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }

    private static int buyClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean b = ConfigHandler.CONFIG.buySellHandler.buy(context.getSource().getPlayerOrException(), Math.max(0, IntegerArgumentType.getInteger(context, "amount")), m -> context.getSource().sendSuccess(() -> m, false));
        return b ? Command.SINGLE_SUCCESS : 0;
    }
}
