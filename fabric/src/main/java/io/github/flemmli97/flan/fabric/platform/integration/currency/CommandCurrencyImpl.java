package io.github.flemmli97.flan.fabric.platform.integration.currency;

import com.epherical.octoecon.OctoEconomy;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.currency.CommandCurrency;
import io.github.flemmli97.flan.platform.integration.currency.CommonCurrency;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class CommandCurrencyImpl implements CommandCurrency {

    private static final ResourceLocation eightyEconomyCurrencyName = new ResourceLocation("eights_economy", "dollars");

    @Override
    public boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(ClaimUtils.translatedText("flan.sellDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        int common = CommonCurrency.sell(player, blocks, value, message);
        if (common != -1)
            return common == 1;
        if (Flan.octoEconomy) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(ClaimUtils.translatedText("flan.sellFail", ChatFormatting.DARK_RED));
                return false;
            }
            Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(eightyEconomyCurrencyName);
            if (currency == null) {
                message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
                return false;
            }
            UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                    .getOrCreatePlayerAccount(player.getUUID());
            double price = blocks * value;
            user.depositMoney(currency, price, "flan.claimblocks.sell");
            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
            message.accept(ClaimUtils.translatedText("flan.sellSuccess", blocks, price, ChatFormatting.GOLD));
        }
        if (Flan.diamondCurrency) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(ClaimUtils.translatedText("flan.sellFail", ChatFormatting.DARK_RED));
                return false;
            }
            double price = blocks * value;
            DiamondUtils.getDatabaseManager().changeBalance(player.getUUID().toString(), (int) price);
            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
            message.accept(ClaimUtils.translatedText("flan.sellSuccess", blocks, price, ChatFormatting.GOLD));
        }
        message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
        return false;
    }

    @Override
    public boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(ClaimUtils.translatedText("flan.buyDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        int common = CommonCurrency.buy(player, blocks, value, message);
        if (common != -1)
            return common == 1;
        if (Flan.octoEconomy) {
            Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(eightyEconomyCurrencyName);
            if (currency == null) {
                message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
                return false;
            }
            UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                    .getOrCreatePlayerAccount(player.getUUID());
            double price = Math.max(0, blocks * value);
            if (user.getBalance(currency) >= price) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                user.withdrawMoney(currency, price, "flan.claimblocks.buy");
                message.accept(ClaimUtils.translatedText("flan.buySuccess", blocks, price, ChatFormatting.GOLD));
                return true;
            }
            message.accept(ClaimUtils.translatedText("flan.buyFail", ChatFormatting.DARK_RED));
            return false;
        }
        if (Flan.diamondCurrency) {
            double price = Math.max(0, blocks * value);
            if (DiamondUtils.getDatabaseManager().getBalanceFromUUID(player.getUUID().toString()) >= price) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                DiamondUtils.getDatabaseManager().changeBalance(player.getUUID().toString(), -(int) price);
                message.accept(ClaimUtils.translatedText("flan.buySuccess", blocks, price, ChatFormatting.GOLD));
                return true;
            }
            message.accept(ClaimUtils.translatedText("flan.buyFail", ChatFormatting.DARK_RED));
            return false;
        }
        message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
        return false;
    }
}
