package io.github.flemmli97.flan.gui;

import com.google.common.collect.Lists;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionEditScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private final Claim claim;

    private boolean removeMode;

    protected PotionEditScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 6, claim);
        this.claim = claim;
    }

    public static void openPotionMenu(Player player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PotionEditScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText("flan.screenPotions");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Claim claim) {
        Map<Holder<MobEffect>, Integer> potions = claim.getPotions();
        List<Holder<MobEffect>> key = Lists.newArrayList(potions.keySet());
        key.sort(Comparator.comparing(Holder::getRegisteredName));
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                inv.updateStack(i, close);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                inv.updateStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < potions.size()) {
                    Holder<MobEffect> effect = key.get(id);
                    ItemStack effectStack = new ItemStack(Items.POTION);
                    MutableComponent txt = ClaimUtils.translatedText(effect.value().getDescriptionId());
                    Collection<MobEffectInstance> inst = Collections.singleton(new MobEffectInstance(effect, 0, potions.get(effect)));
                    var color = PotionContents.getColorOptional(inst).stream().boxed().findFirst();
                    effectStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), color, List.of(), Optional.empty()));
                    CustomData.update(DataComponents.CUSTOM_DATA, effectStack, tag -> tag.putString("FlanEffect", effect.getRegisteredName()));
                    txt.append(Component.literal("-" + potions.get(effect)));
                    Component comp = ServerScreenHelper.coloredGuiText("flan.screenPotionText", txt, ChatFormatting.DARK_BLUE);
                    effectStack.set(DataComponents.CUSTOM_NAME, comp);
                    inv.updateStack(i, effectStack);
                }
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
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                String[] potion = s.split(";");
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
                    }
                }
                this.claim.addPotion(holder.get(), amp);
                player.closeContainer();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty() && this.removeMode) {
            String effect = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("FlanEffect");
            if (!effect.isEmpty())
                BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(effect))
                        .ifPresent(this.claim::removePotion);
            slot.set(ItemStack.EMPTY);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
        }
        return false;
    }
}
