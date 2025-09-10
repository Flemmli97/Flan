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
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public class TeleportCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("teleport").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_TELEPORT))
                .then(Commands.literal("self").then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, ctx.getSource().getPlayerOrException().getUUID()))
                        .executes(TeleportCommand::teleport)))
                .then(Commands.literal("global").then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, null))
                        .executes(TeleportCommand::teleportAdminClaims)))
                .then(Commands.literal("other").then(Commands.argument("player", GameProfileArgument.gameProfile()).then(Commands.argument("claim", StringArgumentType.string()).suggests((ctx, b) -> CommandHelpers.claimSuggestions(ctx, b, CommandHelpers.singleProfile(ctx, "player").getId()))
                        .executes(src -> TeleportCommand.teleport(src, CommandHelpers.singleProfile(src, "player").getId()))))));
    }

    private static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return teleport(context, context.getSource().getPlayerOrException().getUUID());
    }

    private static int teleportAdminClaims(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return teleport(context, null);
    }

    private static int teleport(CommandContext<CommandSourceStack> context, UUID owner) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(context, "claim");
        Optional<Claim> claims = ClaimStorage.get(player.level()).allClaimsFromPlayer(owner)
                .stream().filter(claim -> {
                    if (claim.getClaimName().isEmpty())
                        return claim.getClaimID().toString().equals(name);
                    return claim.getClaimName().equals(name);
                }).findFirst();
        if (claims.isEmpty()) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.teleportNoClaim", ChatFormatting.RED), false);
            return 0;
        }
        return claims.map(claim -> {
            BlockPos pos = claim.getHomePos();
            if (claim.canInteract(player, BuiltinPermission.TELEPORT, pos, false)) {
                PlayerClaimData data = PlayerClaimData.get(player);
                if (data.setTeleportTo(pos)) {
                    context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.teleportHome", ChatFormatting.GOLD), false);
                    return Command.SINGLE_SUCCESS;
                }
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.teleportHomeFail", ChatFormatting.RED), false);
            } else
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), false);
            return 0;
        }).orElse(0);
    }
}
