package io.github.flemmli97.flan.forge;

import io.github.flemmli97.flan.ForgeRegistryWrapper;
import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static SimpleRegistryWrapper<StatusEffect> registryStatusEffects() {
        return new ForgeRegistryWrapper<>(ForgeRegistries.POTIONS);
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
        return blockEntity instanceof Inventory || blockEntity instanceof InventoryProvider || blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
    }

    public static boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag) || nbt.getCompound("ForgeData").contains(tag);
    }
}
