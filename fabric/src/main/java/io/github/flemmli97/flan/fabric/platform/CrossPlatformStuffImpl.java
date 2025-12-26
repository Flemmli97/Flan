package io.github.flemmli97.flan.fabric.platform;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.fabric.platform.integration.playerability.PlayerAbilityEvents;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import io.github.flemmli97.flan.platform.integration.webmap.BluemapIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
    public boolean isModLoaded(String mod) {
        return FabricLoader.getInstance().isModLoaded(mod);
    }

    @Override
    public boolean isInventoryTile(BlockEntity blockEntity) {
        return blockEntity instanceof Container || blockEntity instanceof WorldlyContainerHolder;
    }

    @Override
    public boolean blockDataContains(CompoundTag nbt, String tag) {
        return nbt.contains(tag);
    }

    @Override
    public void reloadConfig(MinecraftServer server) {
        ConfigHandler.reloadConfigs(server);
        if (FabricLoader.getInstance().isModLoaded("bluemap"))
            BluemapIntegration.updateBluemapState(server);
    }

    @Override
    public void toggleCreativeFlight(ServerPlayer player, boolean flag) {
        if (Flan.playerAbilityLib) {
            PlayerAbilityEvents.toggleCreativeFlight(player, flag);
            return;
        }
        CrossPlatformStuff.super.toggleCreativeFlight(player, flag);
    }
}
