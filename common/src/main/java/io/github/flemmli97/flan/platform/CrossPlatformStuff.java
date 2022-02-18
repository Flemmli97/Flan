package io.github.flemmli97.flan.platform;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public abstract class CrossPlatformStuff {

    protected static CrossPlatformStuff INSTANCE;

    public static CrossPlatformStuff instance() {
        return INSTANCE;
    }

    public abstract Path configPath();

    public abstract SimpleRegistryWrapper<MobEffect> registryStatusEffects();

    public abstract SimpleRegistryWrapper<Block> registryBlocks();

    public abstract SimpleRegistryWrapper<Item> registryItems();

    public abstract SimpleRegistryWrapper<EntityType<?>> registryEntities();

    public abstract boolean isInventoryTile(BlockEntity blockEntity);

    public abstract boolean blockDataContains(CompoundTag nbt, String tag);
}
