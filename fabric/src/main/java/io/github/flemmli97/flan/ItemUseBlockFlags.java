package io.github.flemmli97.flan;

import net.minecraft.server.network.ServerPlayerEntity;

public interface ItemUseBlockFlags {

    void stopCanUseBlocks(boolean flag);

    void stopCanUseItems(boolean flag);

    static ItemUseBlockFlags fromPlayer(ServerPlayerEntity player) {
        return (ItemUseBlockFlags) player.interactionManager;
    }
}
