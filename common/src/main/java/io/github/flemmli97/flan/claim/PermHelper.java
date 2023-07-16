package io.github.flemmli97.flan.claim;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Consumer;

public class PermHelper {

    public static boolean check(ServerPlayer player, BlockPos pos, Claim claim, ClaimPermission perm, Consumer<Optional<Boolean>> cons) {
        if (claim == null) {
            cons.accept(Optional.empty());
            return false;
        }
        boolean hasPerm = claim.canInteract(player, perm, pos);
        cons.accept(Optional.of(hasPerm));
        return hasPerm;
    }

    public static Claim checkReturn(ServerPlayer player, ClaimPermission perm, Consumer<Optional<Boolean>> cons) {
        BlockPos pos = player.blockPosition();
        Claim claim = ClaimStorage.get(player.serverLevel()).getClaimAt(pos);
        return check(player, pos, claim, perm, cons) ? claim : null;
    }

    public static void noClaimMessage(ServerPlayer player) {
        player.displayClientMessage(Component.literal(ConfigHandler.langManager.get("noClaim")).setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_RED)), false);
    }

    public static Consumer<Optional<Boolean>> genericNoPermMessage(ServerPlayer player) {
        return (b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.displayClientMessage(simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
        });
    }

    public static MutableComponent simpleColoredText(String text, ChatFormatting... formatting) {
        return Component.literal(text).setStyle(formatting != null ? Style.EMPTY.applyFormats(formatting) : Style.EMPTY);
    }
}
