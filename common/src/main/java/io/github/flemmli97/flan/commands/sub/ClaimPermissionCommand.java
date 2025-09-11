package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.CommandClaim;
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ClaimPermissionCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("permission").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_PERMISSION))
                .then(Commands.literal("personal").then(Commands.argument("group", StringArgumentType.string()).suggests(CommandHelpers::personalGroupSuggestion)
                        .then(Commands.argument("permission", ResourceLocationArgument.id()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                .then(Commands.argument("toggle", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(ClaimPermissionCommand::editPersonalPerm)))))
                .then(Commands.literal("global").then(Commands.argument("permission", ResourceLocationArgument.id()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, false))
                        .then(Commands.argument("toggle", StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(ClaimPermissionCommand::editGlobalPerm))))
                .then(Commands.literal("group").then(Commands.argument("group", StringArgumentType.string()).suggests(CommandHelpers::groupSuggestion)
                        .then(Commands.argument("permission", ResourceLocationArgument.id()).suggests((ctx, b) -> CommandHelpers.permSuggestions(ctx, b, true))
                                .then(Commands.argument("toggle", StringArgumentType.word())
                                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"default", "true", "false"}, b)).executes(ClaimPermissionCommand::editGroupPerm))))));
    }

    private static int editGlobalPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, null, mode);
    }

    private static int editGroupPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        return editPerms(context, StringArgumentType.getString(context, "group"), mode);
    }

    private static int editPerms(CommandContext<CommandSourceStack> context, String group, int mode) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Claim claim = CommandClaim.getClaimFromMode(context, player, BuiltinPermission.EDITPERMS);
        if (claim == null)
            return 0;
        ResourceLocation perm = ResourceLocationArgument.getId(context, "permission");
        if (group != null && PermissionManager.getInstance().isGlobalPermission(perm)) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.nonGlobalOnly", perm, ChatFormatting.DARK_RED));
            return 0;
        }
        if (PermissionManager.getInstance().get(perm) == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noSuchPerm", perm, ChatFormatting.DARK_RED));
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (group == null) {
            claim.editGlobalPerms(player, perm, mode);
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.editPerm", perm, setPerm, ChatFormatting.GOLD), false);
        } else {
            claim.editPerms(player, group, perm, mode);
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.editPermGroup", perm, group, setPerm, ChatFormatting.GOLD), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int editPersonalPerm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String group = StringArgumentType.getString(context, "group");
        int mode = switch (StringArgumentType.getString(context, "toggle")) {
            case "true" -> 1;
            case "default" -> -1;
            default -> 0;
        };
        ResourceLocation perm = ResourceLocationArgument.getId(context, "permission");
        if (PermissionManager.getInstance().isGlobalPermission(perm)) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.nonGlobalOnly", perm, ChatFormatting.DARK_RED));
            return 0;
        }
        if (PermissionManager.getInstance().get(perm) == null) {
            context.getSource().sendFailure(ClaimUtils.translatedText("flan.noSuchPerm", perm, ChatFormatting.DARK_RED));
            return 0;
        }
        String setPerm = mode == 1 ? "true" : mode == 0 ? "false" : "default";
        if (PlayerClaimData.get(player).editDefaultPerms(group, perm, mode))
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.editPersonalGroup", group, perm, setPerm, ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }
}
