package io.github.flemmli97.flan.gui;

import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class CustomInteractListScreenHandler extends ServerOnlyScreenHandler<CustomInteractListScreenHandler.Data> {

    private final Claim claim;
    private final Type type;

    private boolean removeMode;

    private CustomInteractListScreenHandler(int syncId, Inventory playerInventory, Data data) {
        super(syncId, playerInventory, 6, data);
        this.claim = data.claim;
        this.type = data.type;
    }

    public static void openMenu(Player player, Type type, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new CustomInteractListScreenHandler(syncId, inv, new Data(claim, type));
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText(ConfigHandler.langManager.get(type.translationKey));
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Data data) {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.TNT);
                stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenBack"), ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenAdd"), ChatFormatting.DARK_GREEN));
                inv.updateStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setHoverName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenRemoveMode"), this.removeMode ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse")), ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                List<ItemStack> stacks = switch (data.type) {
                    case ITEM -> data.claim.allowedItems.asStacks();
                    case BLOCKBREAK -> data.claim.allowedBreakBlocks.asStacks();
                    case BLOCKUSE -> data.claim.allowedUseBlocks.asStacks();
                };
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < stacks.size()) {
                    ItemStack stack = stacks.get(id);
                    stack.getOrCreateTag().putInt("Index", id);
                    inv.updateStack(i, stack);
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
                switch (this.type) {
                    case ITEM -> {
                        if (s.startsWith("#"))
                            this.claim.allowedItems.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(s.substring(1)))));
                        else
                            this.claim.allowedItems.addAllowedItem(Either.left(BuiltInRegistries.ITEM.get(new ResourceLocation(s))));
                    }
                    case BLOCKBREAK -> {
                        if (s.startsWith("#"))
                            this.claim.allowedBreakBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(s.substring(1)))));
                        else
                            this.claim.allowedBreakBlocks.addAllowedItem(Either.left(BuiltInRegistries.BLOCK.get(new ResourceLocation(s))));
                    }
                    case BLOCKUSE -> {
                        if (s.startsWith("#"))
                            this.claim.allowedUseBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(s.substring(1)))));
                        else
                            this.claim.allowedUseBlocks.addAllowedItem(Either.left(BuiltInRegistries.BLOCK.get(new ResourceLocation(s))));
                    }
                }
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.type, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.type, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.setHoverName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenRemoveMode"), this.removeMode ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse")), ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            CompoundTag nbt = stack.getTag();
            int idx = nbt != null ? nbt.getInt("Index") : 0;
            if (this.removeMode) {
                switch (this.type) {
                    case ITEM -> this.claim.allowedItems.removeAllowedItem(idx);
                    case BLOCKBREAK -> this.claim.allowedBreakBlocks.removeAllowedItem(idx);
                    case BLOCKUSE -> this.claim.allowedUseBlocks.removeAllowedItem(idx);
                }
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            }
        }
        return false;
    }

    public record Data(Claim claim, Type type) {
    }

    public enum Type {
        ITEM("screenMenuItemUse"),
        BLOCKBREAK("screenMenuBlockBreak"),
        BLOCKUSE("screenMenuBlockUse");

        public final String translationKey;

        Type(String translationKey) {
            this.translationKey = translationKey;
        }
    }
}