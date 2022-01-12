package io.github.flemmli97.flan.fabric;

import net.minecraft.server.level.ServerPlayer;

public interface ItemUseBlockFlags {

    void stopCanUseBlocks(boolean flag);

    void stopCanUseItems(boolean flag);

    static ItemUseBlockFlags fromPlayer(ServerPlayer player) {
        return (ItemUseBlockFlags) player.gameMode;
    }
}
