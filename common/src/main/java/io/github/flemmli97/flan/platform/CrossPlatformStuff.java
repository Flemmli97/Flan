package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.utils.VanillaFlightStateTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public interface CrossPlatformStuff {

    CrossPlatformStuff INSTANCE = Flan.getPlatformInstance(CrossPlatformStuff.class,
            "io.github.flemmli97.flan.fabric.platform.CrossPlatformStuffImpl",
            "io.github.flemmli97.flan.neoforge.platform.CrossPlatformStuffImpl");

    Path configPath();

    default boolean isDataGen() {
        return false;
    }

    boolean isModLoaded(String mod);

    boolean isInventoryTile(BlockEntity blockEntity);

    boolean blockDataContains(CompoundTag nbt, String tag);

    void reloadConfig(MinecraftServer server);

    default void toggleCreativeFlight(ServerPlayer player, boolean flag) {
        ((VanillaFlightStateTracker) player).flan$toggleFlight(flag);
    }
}
