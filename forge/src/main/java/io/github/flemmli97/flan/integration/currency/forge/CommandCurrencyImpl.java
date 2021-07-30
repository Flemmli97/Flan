package io.github.flemmli97.flan.integration.currency.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dicemc.money.MoneyMod;
import dicemc.money.storage.MoneyWSD;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class CommandCurrencyImpl {

    public static int sellClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!Flan.diceMCMoneySign) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, Formatting.DARK_RED), false);
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
        double price = amount * ConfigHandler.config.sellPrice;
        MoneyWSD.get(context.getSource().getWorld()).changeBalance(MoneyMod.AcctTypes.PLAYER.key, context.getSource().getPlayer().getUuid(), price);
        data.setAdditionalClaims(data.getAdditionalClaims() - amount);
        context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.sellSuccess, amount, price), Formatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int buyClaimBlocks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!Flan.diceMCMoneySign) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, Formatting.DARK_RED), false);
            return 0;
        }
        if (ConfigHandler.config.buyPrice == -1) {
            context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.buyDisabled, Formatting.DARK_RED), false);
            return 0;
        }
        UUID uuid = context.getSource().getPlayer().getUuid();
        MoneyWSD manager = MoneyWSD.get(context.getSource().getWorld());
        double bal = manager.getBalance(MoneyMod.AcctTypes.PLAYER.key, uuid);
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        double price = amount * ConfigHandler.config.buyPrice;
        if (bal >= price) {
            PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayer());
            data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            manager.changeBalance(MoneyMod.AcctTypes.PLAYER.key, uuid, -price);
            context.getSource().sendFeedback(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.buySuccess, amount, price), Formatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendFeedback(PermHelper.simpleColoredText(ConfigHandler.lang.buyFail, Formatting.DARK_RED), false);
        return 0;
    }
}
