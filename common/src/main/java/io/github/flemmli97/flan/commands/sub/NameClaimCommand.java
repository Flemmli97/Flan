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
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
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
        PlayerClaimData data = PlayerClaimData.get(player);
        if (!data.getClaimMode().isSubclaim) {
            Claim claim = ClaimUtils.checkReturn(player, BuiltinPermission.EDITPERMS, ClaimUtils.genericNoPermMessage(player));
            if (claim == null)
                return 0;
            boolean nameUsed = ClaimStorage.get(player.level()).allClaimsFromPlayer(claim.getOwner())
                    .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
            if (!nameUsed) {
                String name = StringArgumentType.getString(context, "name");
                claim.setClaimName(name);
                player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameSet", name, ChatFormatting.GOLD), false);
            } else {
                player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameUsed", ChatFormatting.DARK_RED), false);
            }
        } else {
            Claim claim = ClaimStorage.get(player.level()).getClaimAt(player.blockPosition());
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null && (claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition()) || sub.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition()))) {
                boolean nameUsed = claim.getAllSubclaims()
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    sub.setClaimName(name);
                    player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameSet", name, ChatFormatting.GOLD), false);
                } else {
                    player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameUsedSub", ChatFormatting.DARK_RED), false);
                }
            } else if (claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition())) {
                boolean nameUsed = ClaimStorage.get(player.level()).allClaimsFromPlayer(claim.getOwner())
                        .stream().map(Claim::getClaimName).anyMatch(name -> name.equals(StringArgumentType.getString(context, "name")));
                if (!nameUsed) {
                    String name = StringArgumentType.getString(context, "name");
                    claim.setClaimName(name);
                    player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameSet", name, ChatFormatting.GOLD), false);
                } else {
                    player.displayClientMessage(ClaimUtils.translatedText("flan.claimNameUsed", ChatFormatting.DARK_RED), false);
                }
            } else
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
