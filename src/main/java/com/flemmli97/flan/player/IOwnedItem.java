package com.flemmli97.flan.player;

import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public interface IOwnedItem {

    void setOriginPlayer(PlayerEntity player);

    UUID getPlayerOrigin();

}
