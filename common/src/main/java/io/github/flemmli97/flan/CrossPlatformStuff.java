package io.github.flemmli97.flan;

import me.shedaniel.architectury.annotations.ExpectPlatform;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;

import java.nio.file.Path;

public class CrossPlatformStuff {

    @ExpectPlatform
    public static Path configPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<StatusEffect> registryStatusEffects() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<Block> registryBlocks() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<Item> registryItems() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<EntityType<?>> registryEntities() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isInventoryTile(BlockEntity blockEntity) {
        throw new AssertionError();
    }

}
