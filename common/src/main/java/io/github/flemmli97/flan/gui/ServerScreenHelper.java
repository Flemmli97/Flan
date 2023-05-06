package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.Config;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerScreenHelper {

    public static ItemStack emptyFiller() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.setHoverName(PermHelper.simpleColoredText(""));
        return stack;
    }

    public static ItemStack fromPermission(Claim claim, ClaimPermission perm, String group) {
        ItemStack stack = perm.getItem();
        stack.setHoverName(ServerScreenHelper.coloredGuiText(perm.id, ChatFormatting.GOLD));
        List<Component> lore = new ArrayList<>();
        for (String pdesc : ConfigHandler.langManager.getArray(perm.id + ".desc")) {
            Component trans = ServerScreenHelper.coloredGuiText(pdesc, ChatFormatting.YELLOW);
            lore.add(trans);
        }
        Config.GlobalType global = ConfigHandler.config.getGlobal(claim.getWorld(), perm);
        if (!claim.isAdminClaim() && !global.canModify()) {
            Component text = ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenUneditable"), ChatFormatting.DARK_RED);
            lore.add(text);
            String permFlag = global.getValue() ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse");
            Component text2 = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenEnableText"), permFlag), permFlag.equals(ConfigHandler.langManager.get("screenTrue")) ? ChatFormatting.GREEN : ChatFormatting.RED);
            lore.add(text2);
        } else {
            String permFlag;
            if (group == null) {
                if (claim.parentClaim() == null)
                    permFlag = "" + (claim.permEnabled(perm) == 1);
                else {
                    permFlag = switch (claim.permEnabled(perm)) {
                        case -1 -> ConfigHandler.langManager.get("screenDefault");
                        case 1 -> ConfigHandler.langManager.get("screenTrue");
                        default -> ConfigHandler.langManager.get("screenFalse");
                    };
                }
            } else {
                permFlag = switch (claim.groupHasPerm(group, perm)) {
                    case -1 -> ConfigHandler.langManager.get("screenDefault");
                    case 1 -> ConfigHandler.langManager.get("screenTrue");
                    default -> ConfigHandler.langManager.get("screenFalse");
                };
            }
            Component text = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenEnableText"), permFlag), permFlag.equals(ConfigHandler.langManager.get("screenTrue")) ? ChatFormatting.GREEN : ChatFormatting.RED);
            lore.add(text);
        }
        addLore(stack, lore);
        return stack;
    }

    public static ItemStack getFromPersonal(ServerPlayer player, ClaimPermission perm, String group) {
        ItemStack stack = perm.getItem();
        stack.setHoverName(ServerScreenHelper.coloredGuiText(perm.id, ChatFormatting.GOLD));
        ListTag lore = new ListTag();
        for (String pdesc : ConfigHandler.langManager.getArray(perm.id + ".desc")) {
            Component trans = ServerScreenHelper.coloredGuiText(pdesc, ChatFormatting.YELLOW);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(trans)));
        }
        Config.GlobalType global = ConfigHandler.config.getGlobal(player.getLevel(), perm);
        if (!global.canModify()) {
            Component text = ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenUneditable"), ChatFormatting.DARK_RED);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(text)));
            String permFlag = String.valueOf(global.getValue());
            Component text2 = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenEnableText"), permFlag), permFlag.equals(ConfigHandler.langManager.get("screenTrue")) ? ChatFormatting.GREEN : ChatFormatting.RED);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(text2)));
        } else {
            String permFlag;
            Map<ClaimPermission, Boolean> map = PlayerClaimData.get(player).playerDefaultGroups().getOrDefault(group, new HashMap<>());
            if (map.containsKey(perm))
                permFlag = map.get(perm) ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse");
            else
                permFlag = ConfigHandler.langManager.get("screenDefault");
            Component text = ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenEnableText"), permFlag), permFlag.equals(ConfigHandler.langManager.get("screenTrue")) ? ChatFormatting.GREEN : ChatFormatting.RED);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        stack.getOrCreateTagElement("display").put("Lore", lore);
        return stack;
    }

    public static void playSongToPlayer(ServerPlayer player, SoundEvent event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.level.getRandom().nextLong()));
    }

    public static void playSongToPlayer(ServerPlayer player, Holder<SoundEvent> event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(event, SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.level.getRandom().nextLong()));
    }

    public static Component coloredGuiText(String text, ChatFormatting... formattings) {
        return Component.literal(text).setStyle(Style.EMPTY.withItalic(false).applyFormats(formattings));
    }

    public static void addLore(ItemStack stack, Component text) {
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        stack.getOrCreateTagElement("display").put("Lore", lore);
    }

    public static void addLore(ItemStack stack, List<Component> texts) {
        ListTag lore = new ListTag();
        texts.forEach(text -> lore.add(StringTag.valueOf(Component.Serializer.toJson(text))));
        stack.getOrCreateTagElement("display").put("Lore", lore);
    }
}
