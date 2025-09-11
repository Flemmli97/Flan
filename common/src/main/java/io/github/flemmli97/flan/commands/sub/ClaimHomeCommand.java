package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class ClaimHomeCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("setHome").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_HOME)).executes(ClaimHomeCommand::setClaimHome));
    }

    private static int setClaimHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.getClaimFromMode(context, player, BuiltinPermission.EDITCLAIM);
        if (claim == null)
            return 0;
        BlockPos pos = CommandClaim.pos(context);
        claim.setHomePos(pos);
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.setHome", pos.getX(), pos.getY(), pos.getZ(), ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }
}
