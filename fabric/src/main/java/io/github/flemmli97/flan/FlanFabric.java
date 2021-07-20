package io.github.flemmli97.flan;

import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.integration.playerability.PlayerAbilityEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class FlanFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockInteractEvents::breakBlocks);
        AttackBlockCallback.EVENT.register(BlockInteractEvents::startBreakBlocks);
        UseEntityCallback.EVENT.register(EntityInteractEvents::useAtEntity);
        AttackEntityCallback.EVENT.register(EntityInteractEvents::attackEntity);
        UseItemCallback.EVENT.register(ItemInteractEvents::useItem);
        ServerLifecycleEvents.SERVER_STARTING.register(FlanFabric::serverLoad);

        CommandRegistrationCallback.EVENT.register(CommandClaim::register);

        Flan.permissionAPI = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        Flan.gunpowder = FabricLoader.getInstance().isModLoaded("gunpowder-currency");
        Flan.playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        if (Flan.playerAbilityLib)
            PlayerAbilityEvents.register();
    }

    public static void serverLoad(MinecraftServer server) {
        ConfigHandler.serverLoad(server);
        ObjectToPermissionMap.reload(server);
        Flan.lockRegistry(server);
    }
}
