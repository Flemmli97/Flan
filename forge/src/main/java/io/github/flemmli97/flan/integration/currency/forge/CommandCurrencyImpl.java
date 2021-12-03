package io.github.flemmli97.flan.integration.currency.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

public class CommandCurrencyImpl {

    public static int sellClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        /*if (!Flan.diceMCMoneySign) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, ChatFormatting.DARK_RED), false);
            return 0;
        }
        if (ConfigHandler.config.sellPrice == -1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.sellDisabled, ChatFormatting.DARK_RED), false);
            return 0;
        }
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < amount) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.sellFail, ChatFormatting.DARK_RED), false);
            return 0;
        }
        double price = amount * ConfigHandler.config.sellPrice;
        MoneyWSD.get(context.getSource().getLevel()).changeBalance(MoneyMod.AcctTypes.PLAYER.key, context.getSource().getPlayerOrException().getUUID(), price);
        data.setAdditionalClaims(data.getAdditionalClaims() - amount);
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.sellSuccess, amount, price), ChatFormatting.GOLD), false);*/
        return Command.SINGLE_SUCCESS;
    }

    public static int buyClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        /*if (!Flan.diceMCMoneySign) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, ChatFormatting.DARK_RED), false);
            return 0;
        }
        if (ConfigHandler.config.buyPrice == -1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.buyDisabled, ChatFormatting.DARK_RED), false);
            return 0;
        }
        UUID uuid = context.getSource().getPlayerOrException().getUUID();
        MoneyWSD manager = MoneyWSD.get(context.getSource().getLevel());
        double bal = manager.getBalance(MoneyMod.AcctTypes.PLAYER.key, uuid);
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        double price = amount * ConfigHandler.config.buyPrice;
        if (bal >= price) {
            PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
            data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            manager.changeBalance(MoneyMod.AcctTypes.PLAYER.key, uuid, -price);
            context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.buySuccess, amount, price), ChatFormatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.buyFail, ChatFormatting.DARK_RED), false);*/
        return 0;
    }
}
