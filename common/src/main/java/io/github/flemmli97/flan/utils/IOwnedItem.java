package io.github.flemmli97.flan.utils;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface IOwnedItem {

    void setOriginPlayer(Player player);

    UUID getDeathPlayer();

    UUID getPlayerOrigin();

}
