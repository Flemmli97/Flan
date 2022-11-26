package io.github.flemmli97.flan.platform.integration.permissions;

import io.github.flemmli97.flan.Flan;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public interface PermissionNodeHandler {

    String cmdReload = "flan.command.reload";
    String cmdGriefPrevention = "flan.command.read.griefprevention";

    String claimCreate = "flan.claim.create";

    String cmdMenu = "flan.command.menu";
    String cmdTrapped = "flan.command.trapped";
    String cmdPGroup = "flan.command.personal";
    String cmdInfo = "flan.command.info";
    String cmdTransfer = "flan.command.transfer";

    String cmdDelete = "flan.command.delete";
    String cmdDeleteAll = "flan.command.delete.all";
    String cmdDeleteSub = "flan.command.delete.sub";
    String cmdDeleteSubAll = "flan.command.delete.sub.all";

    String cmdList = "flan.command.list";
    String cmdListAll = "flan.command.list.all";

    String cmdClaimMode = "flan.command.claim.mode";
    String cmdAdminMode = "flan.command.admin.mode";
    String cmdAdminSet = "flan.command.admin.claim";
    String cmdAdminList = "flan.command.admin.list";
    String cmdAdminDelete = "flan.command.admin.delete";
    String cmdAdminGive = "flan.command.admin.give";

    String cmdGroup = "flan.command.group";
    String cmdPermission = "flan.command.permission";

    String cmdSell = "flan.command.buy";
    String cmdBuy = "flan.command.sell";

    String cmdUnlockAll = "flan.command.unlock.all";
    String cmdName = "flan.command.name";

    String cmdHome = "flan.command.home";
    String cmdTeleport = "flan.command.teleport";

    String permClaimBlocks = "flan.claim.blocks.max";
    String permMaxClaims = "flan.claims.amount";
    String permClaimBlocksCap = "flan.claim.blocks.cap";

    PermissionNodeHandler INSTANCE = Flan.getPlatformInstance(PermissionNodeHandler.class,
            "io.github.flemmli97.flan.fabric.platform.integration.permissions.PermissionNodeHandlerImpl",
            "io.github.flemmli97.flan.forge.platform.integration.permissions.PermissionNodeHandlerImpl");

    default boolean perm(CommandSourceStack src, String perm) {
        return this.perm(src, perm, false);
    }

    boolean perm(CommandSourceStack src, String perm, boolean adminCmd);

    boolean perm(ServerPlayer src, String perm, boolean adminCmd);

    boolean permBelowEqVal(ServerPlayer src, String perm, int val, int fallback);

    int permVal(ServerPlayer src, String perm, int fallback);
}