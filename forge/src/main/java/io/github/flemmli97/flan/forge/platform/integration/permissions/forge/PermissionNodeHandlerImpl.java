package io.github.flemmli97.flan.forge.platform.integration.permissions.forge;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PermissionNodeHandlerImpl extends PermissionNodeHandler {

    public static void init() {
        INSTANCE = new PermissionNodeHandlerImpl();
    }

    @Override
    public boolean perm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks || !(src.getEntity() instanceof ServerPlayer player))
            return !adminCmd || src.hasPermission(ConfigHandler.config.permissionLevel);
        return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(!adminCmd || player.hasPermissions(ConfigHandler.config.permissionLevel));
    }

    @Override
    public boolean perm(ServerPlayer src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks)
            return !adminCmd || src.hasPermissions(ConfigHandler.config.permissionLevel);
        return FTBRanksAPI.getPermissionValue(src, perm).asBoolean().orElse(!adminCmd || src.hasPermissions(ConfigHandler.config.permissionLevel));
    }

    @Override
    public boolean permBelowEqVal(ServerPlayer src, String perm, int val, int fallback) {
        if (Flan.ftbRanks) {
            int max = FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(fallback);
            return val <= max;
        }
        return val <= fallback;
    }
}
