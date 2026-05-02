package io.github.flemmli97.flan.fabric.platform.integration.currency;

import com.epherical.octoecon.OctoEconomy;
import com.epherical.octoecon.api.Currency;
import com.epherical.octoecon.api.user.UniqueUser;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class OctoEconomyImpl implements CurrencyHandler {

    private static final ResourceLocation EIGHTY_ECONOMY_CURRENCY_NAME = ResourceLocation.fromNamespaceAndPath("eights_economy", "dollars");

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flan.MODID, "octo_economy");

    private ResourceLocation currency;

    public static void register() {
        CurrencyRegistry.add(ID, new OctoEconomyImpl());
    }

    @Override
    public boolean withdraw(Player player, double price, Consumer<Component> message) {
        Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(this.getCurrency());
        if (currency == null) {
            message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
            return false;
        }
        UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                .getOrCreatePlayerAccount(player.getUUID());
        if (user.getBalance(currency) >= price) {
            user.withdrawMoney(currency, price, "flan.buy");
            return true;
        }
        return false;
    }

    @Override
    public boolean deposit(Player player, double price, Consumer<Component> message) {
        Currency currency = OctoEconomy.getInstance().getCurrentEconomy().getCurrency(this.getCurrency());
        if (currency == null) {
            message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
            return false;
        }
        UniqueUser user = OctoEconomy.getInstance().getCurrentEconomy()
                .getOrCreatePlayerAccount(player.getUUID());
        user.depositMoney(currency, price, "flan.sell");
        return true;
    }

    private ResourceLocation getCurrency() {
        if (ConfigHandler.CONFIG.currencyType.isEmpty()) {
            return EIGHTY_ECONOMY_CURRENCY_NAME;
        }
        if (this.currency == null || !this.currency.toString().equals(ConfigHandler.CONFIG.currencyType)) {
            this.currency = ResourceLocation.tryParse(ConfigHandler.CONFIG.currencyType);
        }
        return this.currency;
    }
}
