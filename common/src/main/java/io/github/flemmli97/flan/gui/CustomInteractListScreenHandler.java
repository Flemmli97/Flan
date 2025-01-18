package io.github.flemmli97.flan.gui;

import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
                return PermHelper.translatedText(type.translationKey);
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Data data) {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.TNT);
                stack.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                inv.updateStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                List<ItemStack> stacks = switch (data.type) {
                    case ITEM -> data.claim.allowedItems.asStacks();
                    case BLOCKBREAK -> data.claim.allowedBreakBlocks.asStacks();
                    case BLOCKUSE -> data.claim.allowedUseBlocks.asStacks();
                    case ENTITYATTACK -> data.claim.allowedEntityAttack.asStacks();
                    case ENTITYUSE -> data.claim.allowedEntityUse.asStacks();
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
                        else {
                            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(s));
                            if (item != Items.AIR)
                                this.claim.allowedItems.addAllowedItem(Either.left(item));
                        }
                    }
                    case BLOCKBREAK -> {
                        if (s.startsWith("#"))
                            this.claim.allowedBreakBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(s.substring(1)))));
                        else {
                            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s));
                            if (block != Blocks.AIR)
                                this.claim.allowedBreakBlocks.addAllowedItem(Either.left(block));
                        }
                    }
                    case BLOCKUSE -> {
                        if (s.startsWith("#"))
                            this.claim.allowedUseBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(s.substring(1)))));
                        else {
                            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s));
                            if (block != Blocks.AIR)
                                this.claim.allowedUseBlocks.addAllowedItem(Either.left(block));
                        }
                    }
                    case ENTITYATTACK -> {
                        if (s.startsWith("#"))
                            this.claim.allowedEntityAttack.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), new ResourceLocation(s.substring(1)))));
                        else {
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(s));
                            if (entityType != EntityType.PIG || s.equals("minecraft:pig"))
                                this.claim.allowedEntityAttack.addAllowedItem(Either.left(entityType));
                        }
                    }
                    case ENTITYUSE -> {
                        if (s.startsWith("#"))
                            this.claim.allowedEntityUse.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), new ResourceLocation(s.substring(1)))));
                        else {
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(s));
                            if (entityType != EntityType.PIG || s.equals("minecraft:pig"))
                                this.claim.allowedEntityUse.addAllowedItem(Either.left(entityType));
                        }
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
            stack.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
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
        ITEM("flan.screenMenuItemUse", "item"),
        BLOCKBREAK("flan.screenMenuBlockBreak", "block_break"),
        BLOCKUSE("flan.screenMenuBlockUse", "block_use"),
        ENTITYATTACK("flan.screenMenuEntityAttack", "entity_attack"),
        ENTITYUSE("flan.screenMenuEntityUse", "entity_use");

        public final String translationKey;
        public final String commandKey;

        Type(String translationKey, String commandKey) {
            this.translationKey = translationKey;
            this.commandKey = commandKey;
        }
    }
}