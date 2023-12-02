package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.Flan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public interface CrossPlatformStuff {

    CrossPlatformStuff INSTANCE = Flan.getPlatformInstance(CrossPlatformStuff.class,
            "io.github.flemmli97.flan.fabric.platform.CrossPlatformStuffImpl",
            "io.github.flemmli97.flan.forge.platform.CrossPlatformStuffImpl");

    Path configPath();

    boolean isInventoryTile(BlockEntity blockEntity);

    boolean blockDataContains(CompoundTag nbt, String tag);
}
