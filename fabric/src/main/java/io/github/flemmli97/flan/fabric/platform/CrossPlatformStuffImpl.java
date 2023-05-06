package io.github.flemmli97.flan.fabric.platform;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import io.github.flemmli97.flan.fabric.FabricRegistryWrapper;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public class CrossPlatformStuffImpl implements CrossPlatformStuff {

    @Override
    public Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public SimpleRegistryWrapper<MobEffect> registryStatusEffects() {
        return new FabricRegistryWrapper<>(BuiltInRegistries.MOB_EFFECT);
    }

    @Override
    public SimpleRegistryWrapper<Block> registryBlocks() {
        return new FabricRegistryWrapper<>(BuiltInRegistries.BLOCK);
    }

    @Override
    public SimpleRegistryWrapper<Item> registryItems() {
        return new FabricRegistryWrapper<>(BuiltInRegistries.ITEM);
    }

    @Override
    public SimpleRegistryWrapper<EntityType<?>> registryEntities() {
        return new FabricRegistryWrapper<>(BuiltInRegistries.ENTITY_TYPE);
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
