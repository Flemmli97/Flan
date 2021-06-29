package io.github.flemmli97.flan;

import io.github.flemmli97.flan.api.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.PermissionRegistry;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;

public class Flan implements ModInitializer {

    public static final Logger logger = LogManager.getLogger("flan");

    public static boolean permissionAPI, gunpowder, playerAbilityLib;

    public static final DateTimeFormatter onlineTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockInteractEvents::breakBlocks);
        AttackBlockCallback.EVENT.register(BlockInteractEvents::startBreakBlocks);
        UseEntityCallback.EVENT.register(EntityInteractEvents::useAtEntity);
        AttackEntityCallback.EVENT.register(EntityInteractEvents::attackEntity);
        UseItemCallback.EVENT.register(ItemInteractEvents::useItem);
        ServerLifecycleEvents.SERVER_STARTING.register(ConfigHandler::serverLoad);
        ServerLifecycleEvents.SERVER_STARTING.register(ObjectToPermissionMap::reload);
        ServerLifecycleEvents.SERVER_STARTING.register(this::lockRegistry);

        CommandRegistrationCallback.EVENT.register(CommandClaim::register);

        permissionAPI = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        gunpowder = FabricLoader.getInstance().isModLoaded("gunpowder-currency");
        playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        if (playerAbilityLib)
            PlayerAbilityEvents.register();
    }

    public void lockRegistry(MinecraftServer server) {
        PermissionRegistry.lock();
    }

    public static void log(String msg, Object... o) {
        if (ConfigHandler.config.log)
            logger.info(msg, o);
    }

    public static void debug(String msg, Object... o) {
        if (ConfigHandler.config.log)
            logger.debug(msg, o);
    }
}
