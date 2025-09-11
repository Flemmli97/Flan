package io.github.flemmli97.flan.commands.sub;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.commands.PendingCommand;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.ClaimMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("delete").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_DELETE)).executes(DeleteClaimCommand::deleteClaim))
                .then(Commands.literal("deleteAll").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_DELETE_ALL)).executes(DeleteClaimCommand::deleteAllClaim))
                .then(Commands.literal("deleteSubClaim").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_DELETE_SUB)).executes(DeleteClaimCommand::deleteSubClaim))
                .then(Commands.literal("deleteAllSubClaims").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_DELETE_SUB_ALL)).executes(DeleteClaimCommand::deleteAllSubClaim))
                .then(Commands.literal("adminDelete").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_ADMIN_DELETE, true)).executes(DeleteClaimCommand::adminDelete)
                        .then(Commands.literal("all").then(Commands.argument("players", GameProfileArgument.gameProfile())
                                .executes(DeleteClaimCommand::adminDeleteAll))));
    }

    private static int deleteClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(context.getSource().getLevel());
        Claim claim = CommandClaim.fromContext(context);
        boolean check = CommandClaim.check(player, player.blockPosition(), claim, BuiltinPermission.EDITCLAIM, b -> {
            if (b.isEmpty())
                context.getSource().sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.DARK_RED));
            else if (!b.get())
                context.getSource().sendFailure(ClaimUtils.translatedText("flan.deleteClaimError", ChatFormatting.DARK_RED));
        });
        if (!check)
            return 0;
        if (!storage.deleteClaim(claim, true, PlayerClaimData.get(player).getClaimMode(), context.getSource().getLevel())) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.deleteSubClaimError", ChatFormatting.DARK_RED));
            return 0;
        } else {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.deleteClaim", ChatFormatting.RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
        data.deferCommand(new PendingCommand(context, () -> {
            for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
                ClaimStorage storage = ClaimStorage.get(level);
                storage.allClaimsFromPlayer(player.getUUID()).forEach((claim) -> storage.deleteClaim(claim, true, data.getClaimMode(), level));
            }
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.deleteAllClaim", ChatFormatting.GOLD), false);
            return Command.SINGLE_SUCCESS;
        }));
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCommand", ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteSubClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.fromContext(context);
        if (claim == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.RED));
            return 0;
        }
        Claim sub = claim.getSubClaim(CommandClaim.pos(context));
        if (sub == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noSubClaim", ChatFormatting.RED));
            return 0;
        }
        boolean check = CommandClaim.check(player, player.blockPosition(), claim, BuiltinPermission.EDITCLAIM, b -> {
            if (b.isEmpty())
                context.getSource().sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.DARK_RED));
            else if (!b.get())
                context.getSource().sendFailure(ClaimUtils.translatedText("flan.deleteClaimError", ChatFormatting.DARK_RED));
            else
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.deleteSubClaim", ChatFormatting.DARK_RED), false);
        });
        if (!check)
            return 0;
        claim.deleteSubClaim(sub);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteAllSubClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.fromContext(context);
        boolean check = CommandClaim.check(player, CommandClaim.pos(context), claim, BuiltinPermission.EDITCLAIM, CommandClaim.genericNoPermMessage(context.getSource()));
        if (!check)
            return 0;
        PlayerClaimData data = PlayerClaimData.get(context.getSource().getPlayerOrException());
        data.deferCommand(new PendingCommand(context, () -> {
            List<Claim> subs = claim.getAllSubclaims();
            subs.forEach(claim::deleteSubClaim);
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.deleteSubClaimAll", ChatFormatting.DARK_RED), false);
            return Command.SINGLE_SUCCESS;
        }));
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCommand", ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDelete(CommandContext<CommandSourceStack> context) {
        CommandSourceStack src = context.getSource();
        ClaimStorage storage = ClaimStorage.get(src.getLevel());
        Claim claim = CommandClaim.fromContext(context);
        if (claim == null) {
            src.sendSuccess(() -> ClaimUtils.translatedText("flan.noClaim", ChatFormatting.RED), false);
            return 0;
        }
        storage.deleteClaim(claim, true, ClaimMode.DEFAULT, src.getLevel());
        src.sendSuccess(() -> ClaimUtils.translatedText("flan.deleteClaim", ChatFormatting.RED), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int adminDeleteAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack src = context.getSource();
        Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "players");
        if (src.getEntity() instanceof ServerPlayer player) {
            PlayerClaimData data = PlayerClaimData.get(player);
            data.deferCommand(new PendingCommand(context, () -> {
                List<String> players = new ArrayList<>();
                for (GameProfile prof : profiles) {
                    for (ServerLevel level : src.getLevel().getServer().getAllLevels()) {
                        ClaimStorage storage = ClaimStorage.get(level);
                        storage.allClaimsFromPlayer(prof.getId()).forEach((claim) -> storage.deleteClaim(claim, true, ClaimMode.DEFAULT, level));
                    }
                    players.add(prof.getName());
                }
                src.sendSuccess(() -> ClaimUtils.translatedText("flan.adminDeleteAll", players, ChatFormatting.GOLD), true);
                return Command.SINGLE_SUCCESS;
            }));
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.confirmCommand", ChatFormatting.GOLD), true);
            return Command.SINGLE_SUCCESS;
        }
        List<String> players = new ArrayList<>();
        for (GameProfile prof : profiles) {
            for (ServerLevel level : src.getLevel().getServer().getAllLevels()) {
                ClaimStorage storage = ClaimStorage.get(level);
                storage.allClaimsFromPlayer(prof.getId()).forEach((claim) -> storage.deleteClaim(claim, true, ClaimMode.DEFAULT, level));
            }
            players.add(prof.getName());
        }
        src.sendSuccess(() -> ClaimUtils.translatedText("flan.adminDeleteAll", players, ChatFormatting.GOLD), true);
        return Command.SINGLE_SUCCESS;
    }
}
