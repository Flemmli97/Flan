package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class SetAdminClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("setAdminClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_ADMIN_SET, true)).then(Commands.argument("toggle", BoolArgumentType.bool()).executes(SetAdminClaimCommand::toggleAdminClaim)));
    }

    private static int toggleAdminClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.level());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.noClaim", ChatFormatting.RED), false);
            return 0;
        }
        storage.toggleAdminClaim(player, claim, BoolArgumentType.getBool(context, "toggle"));
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.setAdminClaim", claim.isAdminClaim(), ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }
}
