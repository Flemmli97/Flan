package io.github.flemmli97.flan.neoforge.platform;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

import java.nio.file.Path;

public class CrossPlatformStuffImpl implements CrossPlatformStuff {

    @Override
    public Path configPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDataGen() {
        return DatagenModLoader.isRunningDataGen();
    }

    @Override
    public boolean isModLoaded(String mod) {
        return ModList.get().isLoaded(mod);
    }

    @Override
    public boolean isInventoryTile(BlockEntity blockEntity) {
        return blockEntity instanceof Container || blockEntity instanceof WorldlyContainerHolder ||
                blockEntity.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(),
                        null) != null;
    }

    @Override
    public boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag)
                || nbt.getCompoundOrEmpty("NeoForgeData").contains(tag);
    }

    @Override
    public void toggleCreativeFlight(ServerPlayer player, boolean flag) {
        AttributeInstance inst = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (inst != null) {
            if (flag && !inst.hasModifier(Flan.CLAIM_FLIGHT_ID)) {
                inst.addTransientModifier(new AttributeModifier(Flan.CLAIM_FLIGHT_ID, 1, AttributeModifier.Operation.ADD_VALUE));
            } else if (flag && inst.hasModifier(Flan.CLAIM_FLIGHT_ID)) {
                inst.removeModifier(Flan.CLAIM_FLIGHT_ID);
            }
        }
    }
}
