package io.github.flemmli97.flan.fabric.platform.integration.currency;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.krripe.beconomy.api.BEconomy;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class BeconomyImpl implements CurrencyHandler {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Flan.MODID, "beconomy");

    public static void register() {
        CurrencyRegistry.add(ID, new OctoEconomyImpl());
    }

    @Override
    public boolean withdraw(Player player, double price, Consumer<Component> message) {
        BigDecimal bal = BEconomy.INSTANCE.getAPI().getBalance(player.getUUID(), this.getCurrency());
        BigDecimal priceDec = BigDecimal.valueOf(price);
        if (bal.compareTo(priceDec) >= 0) {
            BEconomy.INSTANCE.getAPI().decreaseBalance(player.getUUID(), this.getCurrency(), priceDec);
            return true;
        }
        return false;
    }

    @Override
    public boolean deposit(Player player, double value, Consumer<Component> message) {
        BigDecimal price = BigDecimal.valueOf(value);
        BEconomy.INSTANCE.getAPI().increaseBalance(player.getUUID(), this.getCurrency(), price);
        return true;
    }

    private String getCurrency() {
        return ConfigHandler.CONFIG.currencyType.isEmpty() ? "default_currency" : ConfigHandler.CONFIG.currencyType;
    }
}