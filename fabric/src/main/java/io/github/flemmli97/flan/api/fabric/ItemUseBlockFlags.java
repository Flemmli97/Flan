package io.github.flemmli97.flan.api.fabric;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;

/**
 * Interface for deciding what right click action should be prevented
 * This is used when handling fabrics {@link UseBlockCallback#EVENT}
 */
public interface ItemUseBlockFlags {

    void flan$stopCanUseBlocks(boolean flag);

    void flan$stopCanUseItems(boolean flag);

    /**
     * If false prevents interaction with a block
     */
    boolean flan$allowUseBlocks();

    /**
     * If false prevents right-clicking with an item on a block
     */
    boolean flan$allowUseItems();

    static ItemUseBlockFlags fromPlayer(ServerPlayer player) {
        return (ItemUseBlockFlags) player.gameMode;
    }
}
