package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.Consumer;

public class PermHelper {

    public static boolean check(ServerPlayerEntity player, BlockPos pos, Claim claim, ClaimPermission perm, Consumer<Optional<Boolean>> cons) {
        if (claim == null) {
            cons.accept(Optional.empty());
            return false;
        }
        boolean hasPerm = claim.canInteract(player, perm, pos);
        cons.accept(Optional.of(hasPerm));
        return hasPerm;
    }

    public static Claim checkReturn(ServerPlayerEntity player, ClaimPermission perm, Consumer<Optional<Boolean>> cons) {
        BlockPos pos = player.getBlockPos();
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(pos);
        return check(player, pos, claim, perm, cons) ? claim : null;
    }

    public static void noClaimMessage(ServerPlayerEntity player) {
        player.sendMessage(new LiteralText(ConfigHandler.langManager.get("noClaim")).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_RED)), false);
    }

    public static Consumer<Optional<Boolean>> genericNoPermMessage(ServerPlayerEntity player) {
        return (b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.sendMessage(simpleColoredText(ConfigHandler.langManager.get("noPermission"), Formatting.DARK_RED), false);
        });
    }

    public static MutableText simpleColoredText(String text, Formatting... formatting) {
        return new LiteralText(text).setStyle(formatting != null ? Style.EMPTY.withFormatting(formatting) : Style.EMPTY);
    }
}
