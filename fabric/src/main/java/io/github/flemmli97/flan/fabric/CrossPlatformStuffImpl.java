package io.github.flemmli97.flan.fabric;

import io.github.flemmli97.flan.FabricRegistryWrapper;
import io.github.flemmli97.flan.SimpleRegistryWrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.nio.file.Path;

public class CrossPlatformStuffImpl {

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static StatusEffect effectFromString(String s) {
        return Registry.STATUS_EFFECT.get(new Identifier(s));
    }

    public static String stringFromEffect(StatusEffect s) {
        return Registry.STATUS_EFFECT.getId(s).toString();
    }

    public static SimpleRegistryWrapper<StatusEffect> registryStatusEffects() {
        return new FabricRegistryWrapper<>(Registry.STATUS_EFFECT);
    }

    public static SimpleRegistryWrapper<Block> registryBlocks() {
        return new FabricRegistryWrapper<>(Registry.BLOCK);
    }

    public static SimpleRegistryWrapper<Item> registryItems() {
        return new FabricRegistryWrapper<>(Registry.ITEM);
    }
}
