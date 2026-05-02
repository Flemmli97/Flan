package io.github.flemmli97.flan.platform.integration.currency;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ImpactorImpl implements CurrencyHandler {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flan.MODID, "impactor");

    public static void register() {
        CurrencyRegistry.add(ID, new ImpactorImpl());
    }

    @Override
    public boolean withdraw(Player player, double price, Consumer<Component> message) {
        CompletableFuture<Account> future = EconomyService.instance().account(player.getUUID());
        return future.thenApplyAsync(acc -> {
            BigDecimal bal = acc.balanceAsync().join();
            BigDecimal priceDec = BigDecimal.valueOf(price);
            if (bal.compareTo(priceDec) >= 0) {
                acc.withdrawAsync(priceDec);
                return true;
            }
            return false;
        }).join();
    }

    @Override
    public boolean deposit(Player player, double value, Consumer<Component> message) {
        CompletableFuture<Account> future = EconomyService.instance().account(player.getUUID());
        future.thenAcceptAsync(acc -> {
            BigDecimal price = BigDecimal.valueOf(value);
            acc.depositAsync(price);
        });
        return true;
    }
}
