package io.github.flemmli97.flan.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.ClaimMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;

import java.util.Set;

public class ExpandCommand {

    public static <T extends ArgumentBuilder<CommandSourceStack, T>> void register(ArgumentBuilder<CommandSourceStack, T> builder) {
        builder.then(Commands.literal("expand").requires(src -> PermissionNodeHandler.INSTANCE.perm(src, PermissionNodeHandler.CLAIM_CREATE))
                .then(Commands.argument("distance", IntegerArgumentType.integer()).executes(ExpandCommand::expandClaim)));
    }

    private static int expandClaim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ClaimStorage storage = ClaimStorage.get(player.level());
        ClaimMode mode = PlayerClaimData.get(player).getClaimMode();
        int amount = IntegerArgumentType.getInteger(context, "distance");
        Claim claim = findClaim(context.getSource(), player, storage, mode);
        if (claim == null) {
            return 0;
        }
        if (!checkExpandPermission(context.getSource(), player, claim, mode)) {
            return 0;
        }
        if (amount <= 0) {
            sendExpandError(context.getSource(), "flan.invalidDistance");
            return 0;
        }
        Direction facing = player.getDirection();
        if ((!claim.is3d() || !mode.is3d) && (facing == Direction.UP || facing == Direction.DOWN)) {
            sendExpandError(context.getSource(), facing == Direction.UP ? "flan.expandUpDisabled" : "flan.expandDownDisabled");
            return 0;
        }
        ClaimBox dims = claim.getDimensions();
        Tuple<BlockPos, BlockPos> corners = calculateCorners(dims, facing, amount, claim.parentClaim() != null ? claim.parentClaim().getDimensions() : null);
        boolean success = performExpansion(player, storage, claim, corners);
        if (!success) {
            sendExpandError(context.getSource(), "flan.expandFailed");
            return 0;
        }
        context.getSource().sendSuccess(() -> ClaimUtils.translatedText("flan.expandSuccess", amount, ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    //change expand tuple to own methode for more usability
    private static Tuple<BlockPos, BlockPos> calculateCorners(ClaimBox dims, Direction facing, int amount, ClaimBox restriction) {
        return switch (facing) {
            case SOUTH -> new Tuple<>(
                    new BlockPos(dims.maxX(), dims.minY(), dims.maxZ()),
                    new BlockPos(dims.maxX(), dims.maxY(), restriction != null ? Math.min(dims.maxZ() + amount, restriction.maxZ()) : dims.maxZ() + amount));
            case EAST -> new Tuple<>(
                    new BlockPos(dims.maxX(), dims.minY(), dims.maxZ()),
                    new BlockPos(restriction != null ? Math.min(dims.maxX() + amount, restriction.maxX()) : dims.maxX() + amount, dims.maxY(), dims.maxZ()));
            case NORTH -> new Tuple<>(
                    new BlockPos(dims.minX(), dims.minY(), dims.minZ()),
                    new BlockPos(dims.minX(), dims.maxY(), restriction != null ? Math.max(dims.minZ() - amount, restriction.minZ()) : dims.minZ() - amount));
            case WEST -> new Tuple<>(
                    new BlockPos(dims.minX(), dims.minY(), dims.minZ()),
                    new BlockPos(restriction != null ? Math.max(dims.minX() - amount, restriction.minX()) : dims.minX() - amount, dims.maxY(), dims.minZ()));
            //adding up and down for diagonical logic in future
            case UP -> new Tuple<>(
                    new BlockPos(dims.minX(), dims.maxY(), dims.minZ()),
                    new BlockPos(dims.maxX(), restriction != null ? Math.min(dims.maxY() + amount, restriction.maxY()) : dims.maxY() + amount, dims.maxZ()));
            case DOWN -> new Tuple<>(
                    new BlockPos(dims.minX(), dims.minY(), dims.minZ()),
                    new BlockPos(dims.maxX(), restriction != null ? Math.max(dims.minY() - amount, restriction.minY()) : dims.minY() - amount, dims.maxZ()));
        };
    }

    private static Claim findClaim(CommandSourceStack source, ServerPlayer player, ClaimStorage storage, ClaimMode mode) {
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim == null) {
            source.sendFailure(ClaimUtils.translatedText("flan.noClaim", ChatFormatting.DARK_RED));
            return null;
        }
        if (mode.isSubclaim) {
            Claim subClaim = claim.getSubClaim(player.blockPosition());
            if (subClaim == null) {
                source.sendFailure(ClaimUtils.translatedText("flan.noSubClaim", ChatFormatting.RED));
                return null;
            }
            return subClaim;
        }
        return claim;
    }

    private static boolean performExpansion(ServerPlayer player, ClaimStorage storage, Claim claim, Tuple<BlockPos, BlockPos> corners) {
        if (claim.isSubclaim()) {
            Claim parent = claim.parentClaim();
            if (parent == null) {
                return false;
            }
            Set<Claim> conflicts = parent.resizeSubclaim(claim, corners.getA(), corners.getB());
            return conflicts.isEmpty();
        } else {
            return storage.resizeClaim(claim, corners.getA(), corners.getB(), player);
        }
    }

    private static boolean checkExpandPermission(CommandSourceStack source, ServerPlayer player, Claim claim, ClaimMode mode) {
        if (mode.isSubclaim) {
            Claim parent = claim.parentClaim();
            if (parent == null || !claim.canInteract(player, BuiltinPermission.EDITCLAIM, player.blockPosition())
                    || !parent.canInteract(player, BuiltinPermission.EDITCLAIM, player.blockPosition())) {
                source.sendFailure(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED));
                return false;
            }
        } else {
            if (!claim.canInteract(player, BuiltinPermission.EDITCLAIM, player.blockPosition())) {
                source.sendFailure(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED));
                return false;
            }
        }
        return true;
    }

    private static void sendExpandError(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(ClaimUtils.translatedText("flan.expandError", ClaimUtils.translatedText(key, args)).withStyle(ChatFormatting.RED));
    }
}
