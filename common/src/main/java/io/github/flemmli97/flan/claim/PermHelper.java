package io.github.flemmli97.flan.claim;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PermHelper {

    public static boolean check(ServerPlayer player, BlockPos pos, Claim claim, ResourceLocation perm, Consumer<Optional<Boolean>> cons) {
        if (claim == null) {
            cons.accept(Optional.empty());
            return false;
        }
        boolean hasPerm = claim.canInteract(player, perm, pos);
        cons.accept(Optional.of(hasPerm));
        return hasPerm;
    }

    public static Claim checkReturn(ServerPlayer player, ResourceLocation perm, Consumer<Optional<Boolean>> cons) {
        BlockPos pos = player.blockPosition();
        Claim claim = ClaimStorage.get(player.serverLevel()).getClaimAt(pos);
        return check(player, pos, claim, perm, cons) ? claim : null;
    }

    public static void noClaimMessage(ServerPlayer player) {
        player.displayClientMessage(translatedText("flan.noClaim", ChatFormatting.DARK_RED), false);
    }

    public static Consumer<Optional<Boolean>> genericNoPermMessage(ServerPlayer player) {
        return (b -> {
            if (!b.isPresent())
                PermHelper.noClaimMessage(player);
            else if (!b.get())
                player.displayClientMessage(translatedText("flan.noPermission", ChatFormatting.DARK_RED), false);
        });
    }

    public static MutableComponent translatedText(String key, Object... compArgs) {
        List<ChatFormatting> formattings = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (Object obj : compArgs) {
            if (obj instanceof ChatFormatting formatting)
                formattings.add(formatting);
            else
                args.add(obj);
        }
        return Component.translatable(key,
                args.toArray()).setStyle(Style.EMPTY.applyFormats(formattings.toArray(ChatFormatting[]::new)));
    }
}
