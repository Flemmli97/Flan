package io.github.flemmli97.flan.fabric.platform.integration.currency;

import com.epherical.octoecon.OctoEconomy;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.currency.CommandCurrency;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.gunpowder.entities.StoredBalance;
import io.github.gunpowder.modelhandlers.BalanceHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class CommandCurrencyImpl implements CommandCurrency {

    private static final ResourceLocation eightyEconomyCurrencyName = new ResourceLocation("eights_economy", "dollars");

    @Override
    public boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellDisabled"), ChatFormatting.DARK_RED));
            return false;
        }
        if (Flan.gunpowder) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), ChatFormatting.DARK_RED));
                return false;
            }
            StoredBalance bal = BalanceHandler.INSTANCE.getUser(player.getUUID());
            BigDecimal price = BigDecimal.valueOf(blocks * value);
            bal.setBalance(bal.getBalance().add(price));
            BalanceHandler.INSTANCE.updateUser(bal);
            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
            message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("sellSuccess"), blocks, price), ChatFormatting.GOLD));
            return true;
        }
        if (Flan.octoEconomy) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), ChatFormatting.DARK_RED));
                return false;
            }
            Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(eightyEconomyCurrencyName);
            if (currency == null) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), ChatFormatting.DARK_RED));
                return false;
            }
            UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                    .getOrCreatePlayerAccount(player.getUUID());
            double price = blocks * value;
            user.depositMoney(currency, price, "flan.claimblocks.sell");
            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
            message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("sellSuccess"), blocks, price), ChatFormatting.GOLD));
        }
        message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), ChatFormatting.DARK_RED));
        return false;
    }

    @Override
    public boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyDisabled"), ChatFormatting.DARK_RED));
            return false;
        }
        if (Flan.gunpowder) {
            StoredBalance bal = BalanceHandler.INSTANCE.getUser(player.getUUID());
            BigDecimal price = BigDecimal.valueOf(Math.max(0, blocks * value));
            if (bal.getBalance().compareTo(price) >= 0) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                bal.setBalance(bal.getBalance().subtract(price));
                BalanceHandler.INSTANCE.updateUser(bal);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccess"), blocks, price), ChatFormatting.GOLD));
                return true;
            }
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFail"), ChatFormatting.DARK_RED));
            return false;
        }
        if (Flan.octoEconomy) {
            Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(eightyEconomyCurrencyName);
            if (currency == null) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), ChatFormatting.DARK_RED));
                return false;
            }
            UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                    .getOrCreatePlayerAccount(player.getUUID());
            double price = Math.max(0, blocks * value);
            if (user.getBalance(currency) >= price) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                user.withdrawMoney(currency, price, "flan.claimblocks.buy");
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccess"), blocks, price), ChatFormatting.GOLD));
                return true;
            }
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFail"), ChatFormatting.DARK_RED));
            return false;
        }
        message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), ChatFormatting.DARK_RED));
        return false;
    }
}
