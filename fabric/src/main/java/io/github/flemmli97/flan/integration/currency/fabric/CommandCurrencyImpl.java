package io.github.flemmli97.flan.integration.currency.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class CommandCurrencyImpl {

    public static int sellClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!Flan.gunpowder) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, ChatFormatting.DARK_RED), false);
            return 0;
        }
        /*if (ConfigHandler.config.sellPrice == -1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.sellDisabled, ChatFormatting.DARK_RED), false);
            return 0;
        }
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < amount) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.sellFail, ChatFormatting.DARK_RED), false);
            return 0;
        }
        StoredBalance bal = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class).getUser(context.getSource().getPlayerOrException().getUUID());
        BigDecimal price = BigDecimal.valueOf(amount * ConfigHandler.config.sellPrice);
        bal.setBalance(bal.getBalance().add(price));
        data.setAdditionalClaims(data.getAdditionalClaims() - amount);
        context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.sellSuccess, amount, price), ChatFormatting.GOLD), false);*/
        return Command.SINGLE_SUCCESS;
    }

    public static int buyClaimBlocks(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!Flan.gunpowder) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.currencyMissing, ChatFormatting.DARK_RED), false);
            return 0;
        }
        /*if (ConfigHandler.config.buyPrice == -1) {
            context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.buyDisabled, ChatFormatting.DARK_RED), false);
            return 0;
        }
        StoredBalance bal = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class).getUser(context.getSource().getPlayerOrException().getUUID());
        int amount = Math.max(0, IntegerArgumentType.getInteger(context, "amount"));
        BigDecimal price = BigDecimal.valueOf(amount * ConfigHandler.config.buyPrice);
        if (bal.getBalance().compareTo(price) >= 0) {
            PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
            data.setAdditionalClaims(data.getAdditionalClaims() + amount);
            bal.setBalance(bal.getBalance().subtract(price));
            context.getSource().sendSuccess(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.buySuccess, amount, price), ChatFormatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendSuccess(PermHelper.simpleColoredText(ConfigHandler.lang.buyFail, ChatFormatting.DARK_RED), false);*/
        return 0;
    }
}
