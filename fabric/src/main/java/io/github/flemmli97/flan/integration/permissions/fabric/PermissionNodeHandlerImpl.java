package io.github.flemmli97.flan.integration.permissions.fabric;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PermissionNodeHandlerImpl {

    public static boolean perm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (Flan.permissionAPI) {
            if (adminCmd)
                return Permissions.check(src, perm, ConfigHandler.config.permissionLevel);
            return Permissions.check(src, perm, true);
        }
        if (Flan.ftbRanks && src.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) src.getEntity();
            return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(!adminCmd || player.hasPermissions(ConfigHandler.config.permissionLevel));
        }
        return !adminCmd || src.hasPermission(ConfigHandler.config.permissionLevel);
    }

    public static boolean perm(ServerPlayer src, String perm, boolean adminCmd) {
        if (Flan.permissionAPI) {
            if (adminCmd)
                return Permissions.check(src, perm, ConfigHandler.config.permissionLevel);
            return Permissions.check(src, perm, true);
        }
        if (Flan.ftbRanks) {
            return FTBRanksAPI.getPermissionValue(src, perm).asBoolean().orElse(!adminCmd || src.hasPermissions(ConfigHandler.config.permissionLevel));
        }
        return !adminCmd || src.hasPermissions(ConfigHandler.config.permissionLevel);
    }

    public static boolean permBelowEqVal(ServerPlayer src, String perm, int val, int fallback) {
        if (Flan.ftbRanks) {
            int max = FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(fallback);
            return val <= max;
        }
        return val <= fallback;
    }
}
