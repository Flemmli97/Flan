package io.github.flemmli97.flan.integration.permissionapi.fabric;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandPermissionImpl {

    public static boolean perm(CommandSource src, String perm, boolean adminCmd) {
        if (!Flan.permissionAPI)
            return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
        if (adminCmd)
            return Permissions.check(src, perm, ConfigHandler.config.permissionLevel);
        return Permissions.check(src, perm, true);
    }

    public static boolean perm(ServerPlayerEntity src, String perm, boolean adminCmd) {
        if (!Flan.permissionAPI)
            return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
        if (adminCmd)
            return Permissions.check(src, perm, ConfigHandler.config.permissionLevel);
        return Permissions.check(src, perm, true);
    }
}
