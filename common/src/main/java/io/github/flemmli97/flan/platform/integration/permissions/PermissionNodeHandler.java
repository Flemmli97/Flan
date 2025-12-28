package io.github.flemmli97.flan.platform.integration.permissions;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.Mth;

public interface PermissionNodeHandler {

    String CMD_RELOAD = "flan.command.reload";
    String CMD_GRIEF_PREVENTION = "flan.command.read.griefprevention";

    String CLAIM_CREATE = "flan.claim.create";
    String CLAIM_ADMIN_CREATE = "flan.claim.admin.create";

    String CMD_MENU = "flan.command.menu";
    String CMD_TRAPPED = "flan.command.trapped";
    String CMD_PERSONAL = "flan.command.personal";
    String CMD_INFO = "flan.command.info";
    String CMD_TRANSFER = "flan.command.transfer";

    String CMD_DELETE = "flan.command.delete";
    String CMD_DELETE_ALL = "flan.command.delete.all";
    String CMD_DELETE_SUB = "flan.command.delete.sub";
    String CMD_DELETE_SUB_ALL = "flan.command.delete.sub.all";

    String CMD_LIST = "flan.command.list";
    String CMD_LIST_ALL = "flan.command.list.all";

    String CMD_CLAIM_MODE = "flan.command.claim.mode";
    String CMD_BYPASS_MODE = "flan.command.bypass.claim";
    String CMD_ADMIN_SET = "flan.command.admin.claim";
    String CMD_ADMIN_LIST = "flan.command.admin.list";
    String CMD_ADMIN_DELETE = "flan.command.admin.delete";
    String CMD_ADMIN_GIVE = "flan.command.admin.give";
    String ADMIN_BYPASS = "flan.bypass.admin.mode";

    String CMD_GROUP = "flan.command.group";
    String CMD_FAKE_PLAYER = "flan.command.fakeplayer";
    String CMD_PERMISSION = "flan.command.permission";
    String CMD_CLAIM_IGNORE = "flan.command.claim.ignore";

    String CMD_SELL = "flan.command.sell";
    String CMD_BUY = "flan.command.buy";

    String CMD_UNLOCK_ALL = "flan.command.unlock.all";
    String CMD_NAME = "flan.command.name";

    String CMD_HOME = "flan.command.home";
    String CMD_TELEPORT = "flan.command.teleport";

    String PERM_CLAIM_BLOCKS = "flan.claim.blocks.max";
    String PERM_MAX_CLAIMS = "flan.claims.amount";
    String PERM_CLAIM_BLOCKS_CAP = "flan.claim.blocks.cap";
    String PERM_CLAIM_BLOCKS_BONUS = "flan.claim.blocks.bonus";

    PermissionNodeHandler INSTANCE = Flan.getPlatformInstance(PermissionNodeHandler.class,
            "io.github.flemmli97.flan.fabric.platform.integration.permissions.PermissionNodeHandlerImpl",
            "io.github.flemmli97.flan.neoforge.platform.integration.permissions.PermissionNodeHandlerImpl");

    static PermissionLevel permissionOfLevel(int level) {
        return PermissionLevel.byId(Mth.clamp(level, 0, PermissionLevel.OWNERS.id()));
    }

    static boolean hasPermissionOfLevel(PermissionSet set, int level) {
        return set.hasPermission(new Permission.HasCommandLevel(permissionOfLevel(level)));
    }

    default boolean perm(CommandSourceStack src, String perm) {
        return this.perm(src, perm, false);
    }

    default boolean perm(CommandSourceStack src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks || !(src.getEntity() instanceof ServerPlayer player))
            return !adminCmd || hasPermissionOfLevel(src.permissions(), ConfigHandler.CONFIG.permissionLevel);
        return FTBRanksAPI.getPermissionValue(player, perm).asBoolean().orElse(!adminCmd || hasPermissionOfLevel(player.permissions(), ConfigHandler.CONFIG.permissionLevel));
    }

    default boolean perm(ServerPlayer src, String perm, boolean adminCmd) {
        if (!Flan.ftbRanks)
            return !adminCmd || hasPermissionOfLevel(src.permissions(), ConfigHandler.CONFIG.permissionLevel);
        return FTBRanksAPI.getPermissionValue(src, perm).asBoolean().orElse(!adminCmd || hasPermissionOfLevel(src.permissions(), ConfigHandler.CONFIG.permissionLevel));
    }

    default boolean permBelowEqVal(ServerPlayer src, String perm, int val, int fallback) {
        if (Flan.ftbRanks) {
            int max = FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(fallback);
            return val <= max;
        }
        return val <= fallback;
    }

    default int permVal(ServerPlayer src, String perm, int fallback) {
        if (Flan.ftbRanks) {
            return FTBRanksAPI.getPermissionValue(src, perm).asInteger().orElse(fallback);
        }
        return fallback;
    }
}