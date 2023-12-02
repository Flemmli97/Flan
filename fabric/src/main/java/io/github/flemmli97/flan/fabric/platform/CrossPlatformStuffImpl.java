package io.github.flemmli97.flan.fabric.platform;

import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public class CrossPlatformStuffImpl implements CrossPlatformStuff {

    @Override
    public Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isInventoryTile(BlockEntity blockEntity) {
        return blockEntity instanceof Container || blockEntity instanceof WorldlyContainerHolder;
    }

    @Override
    public boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag);
    }
}
