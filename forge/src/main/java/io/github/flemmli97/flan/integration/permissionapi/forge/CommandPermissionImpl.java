package io.github.flemmli97.flan.integration.permissionapi.forge;

import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CommandPermissionImpl {

    public static boolean perm(CommandSource src, String perm, boolean adminCmd) {
        return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
    }

    public static boolean perm(ServerPlayerEntity src, String perm, boolean adminCmd) {
        return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
    }
}
