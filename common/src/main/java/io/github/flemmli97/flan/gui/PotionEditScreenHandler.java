package io.github.flemmli97.flan.gui;

import com.google.common.collect.Lists;
import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PotionEditScreenHandler extends ServerOnlyScreenHandler {

    private final Claim claim;

    private boolean removeMode;

    protected PotionEditScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 6, claim);
        this.claim = claim;
    }

    public static void openPotionMenu(PlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PotionEditScreenHandler(syncId, inv, claim);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText("Claim Potions");
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData) {
        if (additionalData == null)
            return;
        Claim claim = (Claim) additionalData[0];
        Map<StatusEffect, Integer> potions = claim.getPotions();
        List<StatusEffect> key = Lists.newArrayList(potions.keySet());
        key.sort(Comparator.comparing(CrossPlatformStuff::stringFromEffect));
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(ServerScreenHelper.coloredGuiText("Back", Formatting.DARK_RED));
                inv.setStack(i, close);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setCustomName(ServerScreenHelper.coloredGuiText("Add", Formatting.DARK_GREEN));
                inv.setStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setCustomName(ServerScreenHelper.coloredGuiText("Remove Mode: " + this.removeMode, Formatting.DARK_RED));
                inv.setStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.setStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < potions.size()) {
                    StatusEffect effect = key.get(id);
                    ItemStack effectStack = new ItemStack(Items.POTION);
                    TranslatableText txt = new TranslatableText(effect.getTranslationKey());
                    Collection<StatusEffectInstance> inst = Collections.singleton(new StatusEffectInstance(effect, 0, potions.get(effect)));
                    effectStack.getOrCreateTag().putString("FlanEffect", CrossPlatformStuff.stringFromEffect(effect));
                    effectStack.getTag().putInt("CustomPotionColor", PotionUtil.getColor(inst));
                    effectStack.setCustomName(txt.setStyle(txt.getStyle().withItalic(false).withFormatting(Formatting.DARK_BLUE)).append(ServerScreenHelper.coloredGuiText("-" + potions.get(effect), Formatting.DARK_BLUE)));
                    inv.setStack(i, effectStack);
                }
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 4 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeHandledScreen();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeHandledScreen();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                String[] potion = s.split(";");
                this.claim.addPotion(CrossPlatformStuff.effectFromString(potion[0]), Integer.parseInt(potion[1]));
                player.closeHandledScreen();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeHandledScreen();
                player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.setCustomName(ServerScreenHelper.coloredGuiText("Remove Mode: " + this.removeMode, Formatting.DARK_RED));
            slot.setStack(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty() && this.removeMode) {
            String effect = stack.getOrCreateTag().getString("FlanEffect");
            this.claim.removePotion(CrossPlatformStuff.effectFromString(effect));
            slot.setStack(ItemStack.EMPTY);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_BAT_DEATH, 1, 1f);
        }
        return false;
    }
}
