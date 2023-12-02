package io.github.flemmli97.flan.platform.integration.currency;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CommonCurrency {

    public static int sell(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (Flan.impactor) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), ChatFormatting.DARK_RED));
                return 0;
            }
            CompletableFuture<Account> future = EconomyService.instance().account(player.getUUID());
            future.thenAcceptAsync(acc -> {
                BigDecimal price = BigDecimal.valueOf(blocks * value);
                acc.depositAsync(price);
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("sellSuccess"), blocks, price), ChatFormatting.GOLD));
            });
            return 1;
        }
        return -1;
    }

    public static int buy(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (Flan.impactor) {
            CompletableFuture<Account> future = EconomyService.instance().account(player.getUUID());
            return future.thenApplyAsync(acc -> {
                @NotNull BigDecimal bal = acc.balanceAsync().join();
                BigDecimal price = BigDecimal.valueOf(blocks * value);
                if (bal.compareTo(price) >= 0) {
                    acc.withdrawAsync(price);
                    PlayerClaimData data = PlayerClaimData.get(player);
                    data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                    message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccess"), blocks, price), ChatFormatting.GOLD));
                    return 1;
                }
                return 0;
            }).join();
        }
        return -1;
    }
}
