package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.PermHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ServerScreenHelper {

    public static ItemStack emptyFiller() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.setCustomName(PermHelper.simpleColoredText(""));
        return stack;
    }

    public static ItemStack fromPermission(Claim claim, EnumPermission perm, String group) {
        ItemStack stack = perm.getItem();
        stack.setCustomName(new LiteralText(perm.toString()).setStyle(Style.EMPTY.withFormatting(Formatting.GOLD)));
        ListTag lore = new ListTag();
        Text trans = new LiteralText(perm.translation).setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW));
        lore.add(StringTag.of(Text.Serializer.toJson(trans)));
        String permFlag;
        if (group == null) {
            if (claim.parentClaim() == null)
                permFlag = "" + (claim.permEnabled(perm) == 1);
            else {
                switch (claim.permEnabled(perm)) {
                    case -1:
                        permFlag = "default";
                        break;
                    case 1:
                        permFlag = "true";
                        break;
                    default:
                        permFlag = "false";
                        break;
                }
            }
        } else {
            switch (claim.groupHasPerm(group, perm)) {
                case -1:
                    permFlag = "default";
                    break;
                case 1:
                    permFlag = "true";
                    break;
                default:
                    permFlag = "false";
                    break;
            }
        }
        Text text = new LiteralText("Enabled: " + permFlag).setStyle(Style.EMPTY.withFormatting(permFlag.equals("true") ? Formatting.GREEN : Formatting.RED));
        lore.add(StringTag.of(Text.Serializer.toJson(text)));
        stack.getOrCreateSubTag("display").put("Lore", lore);
        return stack;
    }

    public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
        player.networkHandler.sendPacket(
                new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x, player.getPos().y, player.getPos().z, vol, pitch));

    }
}
