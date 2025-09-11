package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class NameClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("name").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_NAME)).then(Commands.argument("name", StringArgumentType.string()).executes(NameClaimCommand::nameClaim)));
    }

    private static int nameClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.getClaimFromMode(context, player, BuiltinPermission.EDITPERMS);
        if (claim == null)
            return 0;
        boolean nameUsed;
        if (claim.isSubclaim()) {
            nameUsed = claim.getAllSubclaims()
                    .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
        } else {
            nameUsed = ClaimStorage.get(player.serverLevel()).allClaimsFromPlayer(claim.getOwner())
                    .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
        }
        if (!nameUsed) {
            String name = StringArgumentType.getString(context, "name");
            claim.setClaimName(name);
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimNameSet", name, ChatFormatting.GOLD), false);
        } else {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.claimNameUsed", ChatFormatting.DARK_RED));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }
}
