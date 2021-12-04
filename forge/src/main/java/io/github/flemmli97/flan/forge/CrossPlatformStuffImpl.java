package io.github.flemmli97.flan.forge;

import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static SimpleRegistryWrapper<MobEffect> registryStatusEffects() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.MOB_EFFECTS);
    }

    public static SimpleRegistryWrapper<Block> registryBlocks() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.BLOCKS);
    }

    public static SimpleRegistryWrapper<Item> registryItems() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ITEMS);
    }

    public static SimpleRegistryWrapper<EntityType<?>> registryEntities() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.ENTITIES);
    }

    public static boolean isInventoryTile(BlockEntity blockEntity) {
        return blockEntity instanceof Container || blockEntity instanceof WorldlyContainerHolder || blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
    }

    public static boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag) || nbt.getCompound("ForgeData").contains(tag);
    }
}
