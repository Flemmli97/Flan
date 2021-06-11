package io.github.flemmli97.flan;

import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Flan {

    public static final Logger logger = LogManager.getLogger("flan");

    public static boolean permissionAPI, gunpowder, playerAbilityLib;

    public static void lockRegistry(MinecraftServer server) {
        PermissionRegistry.lock();
    }

    public static void log(String msg, Object... o) {
        if (ConfigHandler.config.log)
            Flan.logger.info(msg, o);
    }

    public static void debug(String msg, Object... o) {
        if (ConfigHandler.config.log)
            Flan.logger.debug(msg, o);
    }
}
