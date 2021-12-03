package io.github.flemmli97.flan.fabric;

import io.github.flemmli97.flan.FabricRegistryWrapper;
import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static MobEffect effectFromString(String s) {
        return Registry.MOB_EFFECT.get(new ResourceLocation(s));
    }

    public static String stringFromEffect(MobEffect s) {
        return Registry.MOB_EFFECT.getKey(s).toString();
    }

    public static SimpleRegistryWrapper<MobEffect> registryStatusEffects() {
        return new FabricRegistryWrapper<>(Registry.MOB_EFFECT);
    }

    public static SimpleRegistryWrapper<Block> registryBlocks() {
        return new FabricRegistryWrapper<>(Registry.BLOCK);
    }

    public static SimpleRegistryWrapper<Item> registryItems() {
        return new FabricRegistryWrapper<>(Registry.ITEM);
    }

    public static SimpleRegistryWrapper<EntityType<?>> registryEntities() {
        return new FabricRegistryWrapper<>(Registry.ENTITY_TYPE);
    }

    public static boolean isInventoryTile(BlockEntity blockEntity) {
        return blockEntity instanceof Container || blockEntity instanceof WorldlyContainerHolder;
    }

    public static boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag);
    }
}
