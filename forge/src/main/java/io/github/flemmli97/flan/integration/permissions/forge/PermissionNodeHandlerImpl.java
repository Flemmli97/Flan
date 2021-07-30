package io.github.flemmli97.flan.integration.permissions.forge;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionNodeHandlerImpl {

    public static boolean perm(ServerCommandSource src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks || !(src.getEntity() instanceof ServerPlayerEntity))
            return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(!adminCmd || player.hasPermissionLevel(ConfigHandler.config.permissionLevel));
    }

    public static boolean perm(ServerPlayerEntity src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks)
            return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
        return FTBRanksAPI.getPermissionValue(src, perm).asBoolean().orElse(!adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel));
    }

    public static boolean permBelowEqVal(ServerPlayerEntity src, String perm, int val, int fallback) {
        if (Flan.ftbRanks) {
            int max = FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(fallback);
            return val <= max;
        }
        return val <= fallback;
    }
}
