package io.github.flemmli97.flan.commands.sub;

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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayerCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("fakePlayer").executes(FakePlayerCommand::toggleFakePlayer)
                .then(Commands.literal("add").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_FAKE_PLAYER)).then(Commands.argument("uuid", UuidArgument.uuid()).executes(FakePlayerCommand::addFakePlayer)))
                .then(Commands.literal("remove").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CMD_FAKE_PLAYER))
                        .then(Commands.argument("uuid", UuidArgument.uuid()).suggests((context, build) -> {
                            List<String> list = new ArrayList<>();
                            CommandSourceStack src = context.getSource();
                            ClaimStorage storage = ClaimStorage.get(src.getLevel());
                            Claim claim = storage.getClaimAt(src.getPlayerOrException().blockPosition());
                            if (claim != null && claim.canInteract(src.getPlayerOrException(), BuiltinPermission.EDITPERMS, src.getPlayerOrException().blockPosition())) {
                                list = claim.getAllowedFakePlayerUUID();
                            }
                            return SharedSuggestionProvider.suggest(list, build);
                        }).executes(FakePlayerCommand::removeFakePlayer))));
    }

    private static int toggleFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        data.setFakePlayerNotif(!data.hasFakePlayerNotificationOn());
        player.displayClientMessage(ClaimUtils.translatedText("flan.fakePlayerNotification", data.hasFakePlayerNotificationOn(), ChatFormatting.GOLD), false);
        return 1;
    }

    private static int addFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyFakePlayer(context, false);
    }

    private static int removeFakePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return modifyFakePlayer(context, true);
    }

    private static int modifyFakePlayer(CommandContext<CommandSourceStack> context, boolean remove) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.level());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            ClaimUtils.noClaimMessage(player);
            return 0;
        }
        if (PlayerClaimData.get(player).getClaimMode().isSubclaim) {
            Claim sub = claim.getSubClaim(player.blockPosition());
            if (sub != null)
                claim = sub;
        }
        if (!claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition())) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
            return 0;
        }
        UUID uuid = UuidArgument.getUuid(context, "uuid");
        if (claim.modifyFakePlayerUUID(uuid, remove)) {
            if (!remove)
                player.displayClientMessage(ClaimUtils.translatedText("flan.uuidFakeAdd", uuid, ChatFormatting.GOLD), false);
            else
                player.displayClientMessage(ClaimUtils.translatedText("flan.uuidFakeRemove", uuid, ChatFormatting.GOLD), false);
            return 1;
        }
        player.displayClientMessage(ClaimUtils.translatedText("flan.uuidFakeModifyNo", ChatFormatting.RED), false);
        return 0;
    }
}
