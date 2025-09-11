package io.github.flemmli97.flan.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.commands.sub.AddClaimCommand;
import io.github.flemmli97.flan.commands.sub.AdminModeCommand;
import io.github.flemmli97.flan.commands.sub.AllowListCommand;
import io.github.flemmli97.flan.commands.sub.BlockPurchaseCommand;
import io.github.flemmli97.flan.commands.sub.ClaimBlocksCommand;
import io.github.flemmli97.flan.commands.sub.ClaimGroupCommand;
import io.github.flemmli97.flan.commands.sub.ClaimHomeCommand;
import io.github.flemmli97.flan.commands.sub.ClaimInfoCommand;
import io.github.flemmli97.flan.commands.sub.ClaimMessageCommand;
import io.github.flemmli97.flan.commands.sub.ClaimModeCommand;
import io.github.flemmli97.flan.commands.sub.ClaimPermissionCommand;
import io.github.flemmli97.flan.commands.sub.ConfirmCommand;
import io.github.flemmli97.flan.commands.sub.DeleteClaimCommand;
import io.github.flemmli97.flan.commands.sub.ExpandCommand;
import io.github.flemmli97.flan.commands.sub.FakePlayerCommand;
import io.github.flemmli97.flan.commands.sub.GriefPreventionCommand;
import io.github.flemmli97.flan.commands.sub.ListClaimCommand;
import io.github.flemmli97.flan.commands.sub.NameClaimCommand;
import io.github.flemmli97.flan.commands.sub.OpenMenuCommand;
import io.github.flemmli97.flan.commands.sub.PersonalGroupCommand;
import io.github.flemmli97.flan.commands.sub.ReloadCommand;
import io.github.flemmli97.flan.commands.sub.SetAdminClaimCommand;
import io.github.flemmli97.flan.commands.sub.TeleportCommand;
import io.github.flemmli97.flan.commands.sub.TransferClaimCommand;
import io.github.flemmli97.flan.commands.sub.TrappedCommand;
import io.github.flemmli97.flan.commands.sub.UnlockDropsCommand;
import io.github.flemmli97.flan.player.ClaimMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class CommandClaim {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, boolean dedicated) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("flan");
        AddClaimCommand.register(builder);
        AdminModeCommand.register(builder);
        AllowListCommand.register(builder);
        BlockPurchaseCommand.register(builder);
        ClaimBlocksCommand.register(builder);
        ClaimGroupCommand.register(builder);
        ClaimHomeCommand.register(builder);
        ClaimInfoCommand.register(builder);
        ClaimMessageCommand.register(builder, buildContext);
        ClaimModeCommand.register(builder);
        ClaimPermissionCommand.register(builder);
        ConfirmCommand.register(builder);
        DeleteClaimCommand.register(builder);
        ExpandCommand.register(builder);
        FakePlayerCommand.register(builder);
        GriefPreventionCommand.register(builder);
        ListClaimCommand.register(builder);
        NameClaimCommand.register(builder);
        OpenMenuCommand.register(builder);
        PersonalGroupCommand.register(builder);
        ReloadCommand.register(builder);
        SetAdminClaimCommand.register(builder);
        TeleportCommand.register(builder);
        TransferClaimCommand.register(builder);
        TrappedCommand.register(builder);
        UnlockDropsCommand.register(builder);
        builder.then(Commands.literal("help").executes(ctx -> CommandHelp.helpMessage(ctx, 0, builder.getArguments()))
                .then(Commands.argument("page", IntegerArgumentType.integer()).executes(ctx -> CommandHelp.helpMessage(ctx, builder.getArguments())))
                .then(Commands.literal("cmd").then(Commands.argument("command", StringArgumentType.word()).suggests((ctx, sb) -> SharedSuggestionProvider.suggest(CommandHelp.registeredCommands(ctx, builder.getArguments()), sb)).executes(CommandHelp::helpCmd))));
        builder.then(Commands.literal("?").executes(ctx -> CommandHelp.helpCmd(ctx, "help")));
        dispatcher.register(builder);
    }

    @Nullable
    public static Claim fromContext(CommandContext<CommandSourceStack> context) {
        return ClaimStorage.get(context.getSource().getLevel()).getClaimAt(pos(context));
    }

    public static BlockPos pos(CommandContext<CommandSourceStack> context) {
        return BlockPos.containing(context.getSource().getPosition());
    }

    @Nullable
    public static Claim getClaimFromMode(CommandContext<CommandSourceStack> context, ServerPlayer player, ResourceLocation perm) {
        PlayerClaimData data = PlayerClaimData.get(player);
        return getClaim(context, player, perm, data.getClaimMode(), false, genericNoPermMessage(context.getSource()));
    }

    @Nullable
    public static Claim getClaim(CommandContext<CommandSourceStack> context, ServerPlayer player, ResourceLocation perm, ClaimMode mode, boolean fallbackMain, Consumer<Optional<Boolean>> cons) {
        BlockPos pos = pos(context);
        Claim claim = ClaimStorage.get(context.getSource().getLevel()).getClaimAt(pos);
        if (mode.isSubclaim) {
            Claim sub = claim.getSubClaim(pos);
            if (sub != null) {
                if (sub.canInteract(player, perm, pos))
                    return sub;
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
                return null;
            }
            if (!fallbackMain) {
                player.displayClientMessage(ClaimUtils.translatedText("flan.noSubClaim", ChatFormatting.RED), false);
                return null;
            }
        }
        return check(player, pos, claim, perm, cons) ? claim : null;
    }

    public static boolean check(ServerPlayer player, BlockPos pos, @Nullable Claim claim, ResourceLocation perm, Consumer<Optional<Boolean>> cons) {
        if (claim == null) {
            cons.accept(Optional.empty());
            return false;
        }
        boolean hasPerm = claim.canInteract(player, perm, pos);
        cons.accept(Optional.of(hasPerm));
        return hasPerm;
    }

    public static Consumer<Optional<Boolean>> genericNoPermMessage(CommandSourceStack source) {
        return (b -> {
            if (b.isEmpty())
                source.sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.DARK_RED));
            else if (!b.get())
                source.sendFailure(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED));
        });
    }
}
