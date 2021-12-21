package io.github.flemmli97.flan;

import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.BlockInteractEvents;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.event.PlayerEvents;
import io.github.flemmli97.flan.event.WorldEvents;
import io.github.flemmli97.flan.integration.playerability.PlayerAbilityEvents;
import io.github.flemmli97.flan.player.PlayerDataHandler;
import io.github.flemmli97.flan.scoreboard.ClaimCriterias;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;

public class Flan implements ModInitializer {

    public static final Logger logger = LogManager.getLogger("flan");

    public static boolean permissionAPI, gunpowder, playerAbilityLib, ftbRanks, diceMCMoneySign;

    public static final DateTimeFormatter onlineTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockInteractEvents::breakBlocks);
        AttackBlockCallback.EVENT.register(BlockInteractEvents::startBreakBlocks);
        UseEntityCallback.EVENT.register(EntityInteractEvents::useAtEntity);
        AttackEntityCallback.EVENT.register(EntityInteractEvents::attackEntity);
        UseItemCallback.EVENT.register(ItemInteractEvents::useItem);
        ServerLifecycleEvents.SERVER_STARTING.register(Flan::serverLoad);
        ServerLifecycleEvents.SERVER_STARTED.register(Flan::serverFinishLoad);
        ServerTickEvents.START_SERVER_TICK.register(WorldEvents::serverTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerEvents.onLogout(handler.player));

        CommandRegistrationCallback.EVENT.register(CommandClaim::register);

        permissionAPI = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        gunpowder = FabricLoader.getInstance().isModLoaded("gunpowder-currency");
        playerAbilityLib = FabricLoader.getInstance().isModLoaded("playerabilitylib");
        if (playerAbilityLib)
            PlayerAbilityEvents.register();

        ClaimCriterias.init();
    }

    public static void lockRegistry(MinecraftServer server) {
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

    public static void error(String msg, Object... o) {
        Flan.logger.error(msg, o);
    }

    public static void serverLoad(MinecraftServer server) {
        Flan.lockRegistry(server);
        ConfigHandler.serverLoad(server);
    }

    public static void serverFinishLoad(MinecraftServer server) {
        PlayerDataHandler.deleteInactivePlayerData(server);
    }
}
