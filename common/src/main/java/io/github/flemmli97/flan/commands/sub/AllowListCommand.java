package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
import io.github.flemmli97.flan.commands.CommandHelpers;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class AllowListCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("ignoreList").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_CLAIM_IGNORE, false))
                .then(AllowListCommand.buildClaimEntryCommand(false))
                .then(AllowListCommand.buildClaimEntryCommand(true)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildClaimEntryCommand(boolean remove) {
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(remove ? "remove" : "add");
        ClaimAllowListKey.keys().entrySet().stream().sorted(Map.Entry.comparingByKey(ClaimPermission.NAMESPACE_FIRST))
                .forEach(entry -> {
                    if (remove) {
                        base.then(Commands.literal(entry.getKey().toString())
                                .then(Commands.argument("entry", ResourceOrTagKeyArgument.resourceOrTagKey(entry.getValue().registry())).suggests((src, b) -> CommandHelpers.claimEntryListSuggestion(src, b, entry.getValue()))
                                        .executes(src -> removeClaimListEntries(src, entry.getValue()))));
                    } else {
                        base.then(Commands.literal(entry.getKey().toString())
                                .then(Commands.argument("entry", ResourceOrTagKeyArgument.resourceOrTagKey(entry.getValue().registry()))
                                        .executes(src -> addClaimListEntries(src, entry.getValue()))));
                    }
                });
        return base;
    }

    private static int addClaimListEntries(CommandContext<CommandSourceStack> context, ClaimAllowListKey<?> key) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        Claim rootClaim = ClaimUtils.checkReturn(player, BuiltinPermission.CLAIMMESSAGE, ClaimUtils.genericNoPermMessage(player));
        if (rootClaim == null)
            return 0;
        Claim claim = data.getClaimMode().isSubclaim ? rootClaim.getSubClaim(player.blockPosition()) : rootClaim;
        if (claim == null)
            return 0;
        String result = addClaimListEntry(context, key, claim);
        MutableComponent cmdFeed = ClaimUtils.translatedText("flan.addIgnoreEntry", result, key.id().toString()).withStyle(ChatFormatting.GOLD);
        context.getSource().sendSuccess(() -> cmdFeed, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeClaimListEntries(CommandContext<CommandSourceStack> context, ClaimAllowListKey<?> key) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        Claim rootClaim = ClaimUtils.checkReturn(player, BuiltinPermission.CLAIMMESSAGE, ClaimUtils.genericNoPermMessage(player));
        if (rootClaim == null)
            return 0;
        Claim claim = data.getClaimMode().isSubclaim ? rootClaim.getSubClaim(player.blockPosition()) : rootClaim;
        if (claim == null)
            return 0;
        String value = context.getArgument("entry", ResourceOrTagKeyArgument.Result.class).asPrintable();
        claim.allowedEntries.get(key).removeAllowedItem(value);
        MutableComponent cmdFeed = ClaimUtils.translatedText("flan.removeIgnoreEntry", value, key.id().toString()).withStyle(ChatFormatting.GOLD);
        context.getSource().sendSuccess(() -> cmdFeed, false);
        return Command.SINGLE_SUCCESS;
    }

    private static <T> String addClaimListEntry(CommandContext<CommandSourceStack> context, ClaimAllowListKey<T> key, Claim claim) throws CommandSyntaxException {
        ResourceOrTagKeyArgument.Result<T> value = CommandHelpers.getRegistryType(context, "entry", key.registry());
        Registry<T> registry = context.getSource().registryAccess().registryOrThrow(key.registry());
        value.unwrap().ifRight(tag -> claim.allowedEntries.get(key).addAllowedItem(Either.right(tag)))
                .ifLeft(id -> registry.getOptional(id).ifPresent(entry -> claim.allowedEntries.get(key).addAllowedItem(Either.left(entry))));
        return value.asPrintable();
    }
}
