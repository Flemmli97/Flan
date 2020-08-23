package com.flemmli97.flan.claim;

import com.flemmli97.flan.config.ConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.Consumer;

public class PermissionChecker {

    public static boolean check(ServerPlayerEntity player, BlockPos pos, Claim claim, EnumPermission perm, Consumer<Optional<Boolean>> cons){
        if(claim==null) {
            cons.accept(Optional.empty());
            return false;
        }
        boolean hasPerm = claim.canInteract(player, perm, pos);
        cons.accept(Optional.of(hasPerm));
        return hasPerm;
    }

    public static boolean check(ServerPlayerEntity player, BlockPos pos, EnumPermission perm, Consumer<Optional<Boolean>> cons){
        return check(player, pos, ClaimStorage.get(player.getServerWorld()).getClaimAt(pos), perm, cons);
    }

    public static boolean check(ServerPlayerEntity player, EnumPermission perm, Consumer<Optional<Boolean>> cons){
        BlockPos pos = player.getBlockPos();
        return check(player, pos, ClaimStorage.get(player.getServerWorld()).getClaimAt(pos), perm, cons);
    }

    public static Claim checkReturn(ServerPlayerEntity player, BlockPos pos, EnumPermission perm, Consumer<Optional<Boolean>> cons) {
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(pos);
        return check(player, pos, claim, perm, cons)?claim:null;
    }

    public static Claim checkReturn(ServerPlayerEntity player, EnumPermission perm, Consumer<Optional<Boolean>> cons) {
        BlockPos pos = player.getBlockPos();
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(pos);
        return check(player, pos, claim, perm, cons)?claim:null;
    }

    public static void noClaimMessage(ServerPlayerEntity player){
        player.sendMessage(new LiteralText(ConfigHandler.lang.noClaim).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_RED)), false);
    }

    public static Consumer<Optional<Boolean>> genericNoPermMessage(ServerPlayerEntity player){
        return (b ->{
            if(!b.isPresent())
                PermissionChecker.noClaimMessage(player);
            else if(!b.get())
                player.sendMessage(simpleColoredText(ConfigHandler.lang.noPermission, Formatting.DARK_RED), false);
        });
    }

    public static Text simpleColoredText(String text, Formatting... formatting){
        return new LiteralText(text).setStyle(formatting!=null?Style.EMPTY.withFormatting(formatting):Style.EMPTY);
    }
}
