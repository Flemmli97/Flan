package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.UUID;
import java.util.stream.Stream;

public class AddClaimCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("add").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CLAIM_CREATE))
                .then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).executes(AddClaimCommand::addClaim)
                        .then(Commands.argument("dimension", IdentifierArgument.id()).requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CLAIM_ADMIN_CREATE, true))
                                .suggests((src, build) -> SharedSuggestionProvider.suggest(src.getSource().getServer().levelKeys().stream().map(k -> k.identifier().toString()).toList(), build))
                                .then(Commands.argument("player", StringArgumentType.word()).suggests((src, build) -> SharedSuggestionProvider.suggest(Stream.concat(Stream.of("+Admin"), src.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getUUID().toString())).toList(), build))
                                        .executes(AddClaimCommand::addClaimAs)))))
                .then(Commands.literal("all").executes(AddClaimCommand::addClaimAll))
                .then(Commands.literal("rect").then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer()).executes(ctx -> AddClaimCommand.addClaimRect(ctx, IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "z")))))));
    }

    private static int addClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!ItemInteractEvents.canClaimWorld(context.getSource().getLevel(), player))
            return 0;
        ClaimStorage storage = ClaimStorage.get(context.getSource().getLevel());
        BlockPos from = BlockPosArgument.getLoadedBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(context, "to");
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaimAs(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String as = StringArgumentType.getString(context, "player");
        Identifier levelID = IdentifierArgument.getId(context, "dimension");
        ServerLevel level = context.getSource().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, levelID));
        if (level == null) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.noSuchLevel", levelID), true);
            return 0;
        }
        UUID uuid = null;
        if (!as.equals("+Admin")) {
            uuid = context.getSource().getServer().services().nameToIdCache().get(as).map(NameAndId::id).orElse(null);
            if (uuid == null) {
                context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.noSuchPlayer", as), true);
                return 0;
            }
        }
        ClaimStorage storage = ClaimStorage.get(level);
        BlockPos from = BlockPosArgument.getLoadedBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(context, "to");
        Claim claim = storage.createAdminClaim(from, to, level, context.getSource().getEntity() instanceof ServerPlayer player && PlayerClaimData.get(player)
                .getClaimMode().is3d);
        if (claim == null) {
            context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimCreationFailCommand"), true);
            return 0;
        }
        if (uuid != null) {
            storage.transferOwner(claim, uuid);
        }
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.claimCreateSuccess", ChatFormatting.GOLD), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int addClaimAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClaimData data = PlayerClaimData.get(player);
        int usable = data.getClaimBlocks() + data.getAdditionalClaims() - data.usedClaimBlocks();
        int size = (int) Math.floor(Math.sqrt(usable));
        return addClaimRect(context, size, size);
    }

    private static int addClaimRect(CommandContext<CommandSourceStack> context, int x, int z) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!ItemInteractEvents.canClaimWorld(context.getSource().getLevel(), player))
            return 0;
        ClaimStorage storage = ClaimStorage.get(context.getSource().getLevel());
        boolean evenX = x % 2 == 0;
        boolean evenZ = z % 2 == 0;
        BlockPos from = player.blockPosition().offset(evenX ? -(int) ((x - 1) * 0.5) : -(int) (x * 0.5), -5, evenZ ? -(int) ((z - 1) * 0.5) : -(int) (z * 0.5));
        BlockPos to = player.blockPosition().offset((int) (x * 0.5), -5, (int) (z * 0.5));
        storage.createClaim(from, to, player);
        return Command.SINGLE_SUCCESS;
    }
}
