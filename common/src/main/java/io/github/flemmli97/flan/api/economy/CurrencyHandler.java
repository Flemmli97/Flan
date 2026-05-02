package io.github.flemmli97.flan.api.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * Interface for different currency impl
 */
public interface CurrencyHandler {

    /**
     * This is called whenever a buy action is done in the mod.
     *
     * @param price   Amount of currency to be used for this transaction.
     * @param message Easy way to send a message
     * @return This should return true if the transaction was successful and false if not (e.g. due to insufficient funds)
     */
    boolean withdraw(Player player, double price, Consumer<Component> message);

    /**
     * This is called whenever a sell action is done in the mod
     *
     * @param price   Amount of currency to be used for this transaction.
     * @param message Easy way to send a message
     * @return This should return true if the transaction was successful and false if not. For selling returns true usually
     */
    boolean deposit(Player player, double price, Consumer<Component> message);
}
