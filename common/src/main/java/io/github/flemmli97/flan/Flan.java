package io.github.flemmli97.flan;

import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.ClaimPermissionCheck;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Flan {

    public static final Logger logger = LogManager.getLogger("flan");

    public static boolean permissionAPI, gunpowder, playerAbilityLib, ftbRanks, diceMCMoneySign;

    public static final DateTimeFormatter onlineTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    public static void error(String msg, Object... o) {
        Flan.logger.error(msg, o);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPlatformInstance(Class<T> abstractClss, String... impls) {
        if(impls == null || impls.length == 0)
            throw new IllegalStateException("Couldn't create an instance of " + abstractClss + ". No implementations provided!");
        Class<?> clss = null;
        int i = 0;
        while(clss == null && i < impls.length) {
            try {
                clss = Class.forName(impls[i]);
            } catch (ClassNotFoundException ignored) {
            }
            i++;
        }
        if(clss == null)
            Flan.logger.fatal("No Implementation of " + abstractClss + " found with given paths " + Arrays.toString(impls));
        if (clss != null && abstractClss.isAssignableFrom(clss)) {
            try {
                Constructor<T> constructor = (Constructor<T>) clss.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                Flan.logger.fatal("Implementation of " + clss + " needs to provide an no arg constructor");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Couldn't create an instance of " + abstractClss);
    }
}
