package io.github.flemmli97.flan.integration.gunpowder.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.api.module.currency.dataholders.StoredBalance;
import io.github.gunpowder.api.module.currency.modelhandlers.BalanceHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.math.BigDecimal;

public class CommandCurrencyImpl {

    public static int sellClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!Flan.gunpowder) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.gunpowderMissing, Formatting.DARK_RED), false);
            return 0;
        }
        if (ConfigHandler.config.sellPrice == -1) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.sellDisabled, Formatting.DARK_RED), false);
            return 0;
        }
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayer());
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < amount) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.sellFail, Formatting.DARK_RED), false);
            return 0;
        }
        StoredBalance bal = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class).getUser(context.getSource().getPlayer().getUuid());
        BigDecimal price = BigDecimal.valueOf(amount * ConfigHandler.config.sellPrice);
        bal.setBalance(bal.getBalance().add(price));
        data.setAdditionalClaims(data.getAdditionalClaims() - amount);
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.sellSuccess, amount, price), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int buyClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!Flan.gunpowder) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.gunpowderMissing, Formatting.DARK_RED), false);
            return 0;
        }
        if (ConfigHandler.config.buyPrice == -1) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.buyDisabled, Formatting.DARK_RED), false);
            return 0;
        }
        StoredBalance bal = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class).getUser(context.getSource().getPlayer().getUuid());
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        BigDecimal price = BigDecimal.valueOf(amount * ConfigHandler.config.buyPrice);
        if (bal.getBalance().compareTo(price) > 0) {
            PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayer());
            data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            bal.setBalance(bal.getBalance().subtract(price));
            context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.buySuccess, amount, price), Formatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.buyFail, Formatting.DARK_RED), false);
        return 0;
    }
}
