package io.github.flemmli97.flan.integration.permissions;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionNodeHandler {

    public static final String cmdReload = "flan.command.reload";
    public static final String cmdGriefPrevention = "flan.command.read.griefprevention";

    public static final String claimCreate = "flan.claim.create";

    public static final String cmdMenu = "flan.command.menu";
    public static final String cmdTrapped = "flan.command.trapped";
    public static final String cmdPGroup = "flan.command.personal";
    public static final String cmdInfo = "flan.command.info";
    public static final String cmdTransfer = "flan.command.transfer";

    public static final String cmdDelete = "flan.command.delete";
    public static final String cmdDeleteAll = "flan.command.delete.all";
    public static final String cmdDeleteSub = "flan.command.delete.sub";
    public static final String cmdDeleteSubAll = "flan.command.delete.sub.all";

    public static final String cmdList = "flan.command.list";
    public static final String cmdListAll = "flan.command.list.all";

    public static final String cmdClaimMode = "flan.command.claim.mode";
    public static final String cmdAdminMode = "flan.command.admin.mode";
    public static final String cmdAdminSet = "flan.command.admin.claim";
    public static final String cmdAdminList = "flan.command.admin.list";
    public static final String cmdAdminDelete = "flan.command.admin.delete";
    public static final String cmdAdminGive = "flan.command.admin.give";

    public static final String cmdGroup = "flan.command.group";
    public static final String cmdPermission = "flan.command.permission";

    public static final String cmdSell = "flan.command.buy";
    public static final String cmdBuy = "flan.command.sell";

    public static final String cmdUnlockAll = "flan.command.unlock.all";
    public static final String cmdName = "flan.command.name";

    public static final String cmdHome = "flan.command.home";
    public static final String cmdTeleport = "flan.command.teleport";

    public static final String permClaimBlocks = "flan.claim.blocks.max";
    public static final String permMaxClaims = "flan.claims.amount";

    public static boolean perm(ServerCommandSource src, String perm) {
        return perm(src, perm, false);
    }

    public static boolean perm(ServerCommandSource src, String perm, boolean adminCmd) {
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

    public static boolean permBelowEqVal(ServerPlayerEntity src, String perm, int val, int fallback) {
        return val <= fallback;
    }
}