package io.github.flemmli97.flan;

import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Flan {

    public static final String MODID = "flan";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static boolean permissionAPI, playerAbilityLib, ftbRanks, diceMCMoneySign, octoEconomy,
            diamondCurrency, ftbChunks, gomlServer, mineColonies, commonProtApi, impactor, create;

    public static final DateTimeFormatter ONLINE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static final ResourceLocation CLAIM_FLIGHT_ID = new ResourceLocation(Flan.MODID, "claim_creative_flight");

    public static void log(String msg, Object... o) {
        if (ConfigHandler.CONFIG.log)
            Flan.LOGGER.info(msg, o);
    }

    public static void debug(String msg, Object... o) {
        if (ConfigHandler.CONFIG.log)
            Flan.LOGGER.debug(msg, o);
    }

    public static void error(String msg, Object... o) {
        Flan.LOGGER.error(msg, o);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getPlatformInstance(Class<T> abstractClss, String... impls) {
        if (impls == null || impls.length == 0)
            throw new IllegalStateException("Couldn't create an instance of " + abstractClss + ". No implementations provided!");
        Class<?> clss = null;
        int i = 0;
        while (clss == null && i < impls.length) {
            try {
                clss = Class.forName(impls[i]);
            } catch (ClassNotFoundException ignored) {
            }
            i++;
        }
        if (clss == null)
            Flan.LOGGER.fatal("No Implementation of " + abstractClss + " found with given paths " + Arrays.toString(impls));
        else if (abstractClss.isAssignableFrom(clss)) {
            try {
                Constructor<T> constructor = (Constructor<T>) clss.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                Flan.LOGGER.fatal("Implementation of " + clss + " needs to provide an no arg constructor");
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Couldn't create an instance of " + abstractClss);
    }
}
