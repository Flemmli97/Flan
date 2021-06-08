package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
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

import java.util.HashMap;
import java.util.Map;

public class ServerScreenHelper {

    public static ItemStack emptyFiller() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.setCustomName(PermHelper.simpleColoredText(""));
        return stack;
    }

    public static ItemStack fromPermission(Claim claim, ClaimPermission perm, String group) {
        ItemStack stack = perm.getItem();
        stack.setCustomName(ServerScreenHelper.coloredGuiText(perm.id, Formatting.GOLD));
        ListTag lore = new ListTag();
        for (String pdesc : perm.desc) {
            Text trans = ServerScreenHelper.coloredGuiText(pdesc, Formatting.YELLOW);
            lore.add(StringTag.of(Text.Serializer.toJson(trans)));
        }
        Boolean global = ConfigHandler.config.getGlobal(claim.getWorld(), perm);
        if (!claim.isAdminClaim() && global != null) {
            Text text = ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenUneditable, Formatting.DARK_RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text)));
            String permFlag = global.toString();
            Text text2 = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenEnableText, permFlag), permFlag.equals("true") ? Formatting.GREEN : Formatting.RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text2)));
        } else {
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
            Text text = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenEnableText, permFlag), permFlag.equals("true") ? Formatting.GREEN : Formatting.RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text)));
        }
        stack.getOrCreateSubTag("display").put("Lore", lore);
        return stack;
    }

    public static ItemStack getFromPersonal(ServerPlayerEntity player, ClaimPermission perm, String group) {
        ItemStack stack = perm.getItem();
        stack.setCustomName(ServerScreenHelper.coloredGuiText(perm.id, Formatting.GOLD));
        ListTag lore = new ListTag();
        for (String pdesc : perm.desc) {
            Text trans = ServerScreenHelper.coloredGuiText(pdesc, Formatting.YELLOW);
            lore.add(StringTag.of(Text.Serializer.toJson(trans)));
        }
        Boolean global = ConfigHandler.config.getGlobal(player.getServerWorld(), perm);
        if (global != null) {
            Text text = ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenUneditable, Formatting.DARK_RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text)));
            String permFlag = global.toString();
            Text text2 = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenEnableText, permFlag), permFlag.equals("true") ? Formatting.GREEN : Formatting.RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text2)));
        } else {
            String permFlag;
            Map<ClaimPermission, Boolean> map = PlayerClaimData.get(player).playerDefaultGroups().getOrDefault(group, new HashMap<>());
            if (map.containsKey(perm))
                permFlag = map.get(perm) ? "true" : "false";
            else
                permFlag = "default";
            Text text = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenEnableText, permFlag), permFlag.equals("true") ? Formatting.GREEN : Formatting.RED);
            lore.add(StringTag.of(Text.Serializer.toJson(text)));
        }
        stack.getOrCreateSubTag("display").put("Lore", lore);
        return stack;
    }

    public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
        player.networkHandler.sendPacket(
                new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x, player.getPos().y, player.getPos().z, vol, pitch));
    }

    public static Text coloredGuiText(String text, Formatting... formattings) {
        return new LiteralText(text).setStyle(Style.EMPTY.withItalic(false).withFormatting(formattings));
    }
}
