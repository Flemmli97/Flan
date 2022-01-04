package io.github.flemmli97.flan;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.nio.file.Path;

public class CrossPlatformStuff {

    @ExpectPlatform
    public static Path configPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleRegistryWrapper<MobEffect> registryStatusEffects() {
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

    @ExpectPlatform
    public static boolean blockDataContains(CompoundTag nbt, String tag) {
        throw new AssertionError();
    }

    public static boolean isRealPlayer(Player player) {
        throw new AssertionError();
    }
}
