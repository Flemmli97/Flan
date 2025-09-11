package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.gui.ClaimMenuScreenHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class OpenMenuCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("menu").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_MENU)).executes(OpenMenuCommand::openMenu));
    }

    private static int openMenu(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = ClaimStorage.get(player.level()).getClaimAt(player.blockPosition());
        if (claim == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.DARK_RED));
            return 0;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (!data.getClaimMode().isSubclaim) {
            ClaimMenuScreenHandler.openClaimMenu(player, claim);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
        } else {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub == null) {
                context.getSource().sendFailure(ClaimUtils.translatedText("flan.noSubClaim", ChatFormatting.DARK_RED));
                return 0;
            }
            ClaimMenuScreenHandler.openClaimMenu(player, sub);
        }
        return Command.SINGLE_SUCCESS;
    }
}
