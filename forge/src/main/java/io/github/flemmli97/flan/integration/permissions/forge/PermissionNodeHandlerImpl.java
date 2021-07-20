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
        if (adminCmd)
            return FTBRanksAPI.getPermissionValue(player, perm).asBooleanOrFalse();
        return FTBRanksAPI.getPermissionValue(player, perm).asBooleanOrTrue();
    }

    public static boolean perm(ServerPlayerEntity src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks)
            return !adminCmd || src.hasPermissionLevel(ConfigHandler.config.permissionLevel);
        if (adminCmd)
            return FTBRanksAPI.getPermissionValue(src, perm).asBooleanOrFalse();
        return FTBRanksAPI.getPermissionValue(src, perm).asBooleanOrTrue();
    }

    public static boolean permBelowEqVal(ServerPlayerEntity src, String perm, int val) {
        if (Flan.ftbRanks) {
            int max = FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(0);
            return val <= max;
        }
        return false;
    }
}
