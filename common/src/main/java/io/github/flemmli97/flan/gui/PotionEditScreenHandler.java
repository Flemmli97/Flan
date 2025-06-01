package io.github.flemmli97.flan.gui;

import com.google.common.collect.Lists;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionEditScreenHandler extends PagedServerOnlyScreenHandler<Claim> {

    private boolean removeMode;

    protected PotionEditScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 6, claim);
    }

    public static void openPotionMenu(Player player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PotionEditScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText("flan.screenEffects");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        Map<Holder<MobEffect>, Integer> potions = this.data.getPotions();
        List<Holder<MobEffect>> key = Lists.newArrayList(potions.keySet());
        key.sort(Comparator.comparing(Holder::getRegisteredName));
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                        ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 3) {
                ItemStack stack = ServerScreenHelper.createStack(Items.ANVIL,
                        ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                this.slots.get(i).set(stack);
            } else if (i == 4) {
                ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                        ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < potions.size()) {
                    Holder<MobEffect> effect = key.get(id);
                    ItemStack stack = ServerScreenHelper.createStack(Items.POTION, ServerScreenHelper.coloredGuiText("flan.screenEffectText", ChatFormatting.YELLOW));
                    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(effect, 0, potions.get(effect) - 1)), Optional.empty()));
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("FlanEffect", effect.getRegisteredName()));
                    this.slots.get(i).set(stack);
                } else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 4 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                String[] potion = s.contains("-") ? s.split("-") : s.split(";");
                int amp = 1;
                Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(potion[0]));
                if (holder.map(effect -> effect == MobEffects.LUCK && !potion[0].equals("minecraft:luck")).orElse(true)) {
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    return;
                }
                if (potion.length > 1) {
                    try {
                        amp = Integer.parseInt(potion[1]);
                    } catch (NumberFormatException e) {
                        Flan.LOGGER.error(e);
                    }
                }
                this.data.addPotion(holder.get(), amp);
                player.closeContainer();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                    ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty() && this.removeMode) {
            String effect = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getStringOr("FlanEffect", "");
            if (!effect.isEmpty())
                BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(effect))
                        .ifPresent(this.data::removePotion);
            slot.set(ItemStack.EMPTY);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
        }
        return false;
    }

    @Override
    protected PageSettings pageSettings() {
        return new PageSettings((this.data.getPotions().size() - 1) / 28, 47, 51);
    }
}
