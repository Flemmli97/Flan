package io.github.flemmli97.flan.gui;

import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class CustomInteractListScreenHandler extends PagedServerOnlyScreenHandler<CustomInteractListScreenHandler.Data> {

    private boolean removeMode;

    private CustomInteractListScreenHandler(int syncId, Inventory playerInventory, Data data) {
        super(syncId, playerInventory, 6, data);
    }

    public static void openMenu(Player player, Type type, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new CustomInteractListScreenHandler(syncId, inv, new Data(claim, type));
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText(type.translationKey);
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                        ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 3) {
                ItemStack stack = ServerScreenHelper.createStack(Items.ANVIL, ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                this.slots.get(i).set(stack);
            } else if (i == 4) {
                ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                        ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                List<ItemStack> stacks = switch (this.data.type) {
                    case ITEM -> this.data.claim.allowedItems.asStacks();
                    case BLOCKBREAK -> this.data.claim.allowedBreakBlocks.asStacks();
                    case BLOCKUSE -> this.data.claim.allowedUseBlocks.asStacks();
                    case ENTITYATTACK -> this.data.claim.allowedEntityAttack.asStacks();
                    case ENTITYUSE -> this.data.claim.allowedEntityUse.asStacks();
                };
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < stacks.size()) {
                    ItemStack stack = stacks.get(id);
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("Index", id));
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
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                switch (this.data.type) {
                    case ITEM -> {
                        if (s.startsWith("#"))
                            this.data.claim.allowedItems.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(s.substring(1)))));
                        else {
                            Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(s));
                            if (item != Items.AIR)
                                this.data.claim.allowedItems.addAllowedItem(Either.left(item));
                        }
                    }
                    case BLOCKBREAK -> {
                        if (s.startsWith("#"))
                            this.data.claim.allowedBreakBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(s.substring(1)))));
                        else {
                            Block block = BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(s));
                            if (block != Blocks.AIR)
                                this.data.claim.allowedBreakBlocks.addAllowedItem(Either.left(block));
                        }
                    }
                    case BLOCKUSE -> {
                        if (s.startsWith("#"))
                            this.data.claim.allowedUseBlocks.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(s.substring(1)))));
                        else {
                            Block block = BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(s));
                            if (block != Blocks.AIR)
                                this.data.claim.allowedUseBlocks.addAllowedItem(Either.left(block));
                        }
                    }
                    case ENTITYATTACK -> {
                        if (s.startsWith("#"))
                            this.data.claim.allowedEntityAttack.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.parse(s.substring(1)))));
                        else {
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.parse(s));
                            if (entityType != EntityType.PIG || s.equals("minecraft:pig"))
                                this.data.claim.allowedEntityAttack.addAllowedItem(Either.left(entityType));
                        }
                    }
                    case ENTITYUSE -> {
                        if (s.startsWith("#"))
                            this.data.claim.allowedEntityUse.addAllowedItem(Either.right(TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.parse(s.substring(1)))));
                        else {
                            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.parse(s));
                            if (entityType != EntityType.PIG || s.equals("minecraft:pig"))
                                this.data.claim.allowedEntityUse.addAllowedItem(Either.left(entityType));
                        }
                    }
                }
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.data.type, this.data.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.data.type, this.data.claim));
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
        if (!stack.isEmpty()) {
            CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);
            int idx = nbt != null ? nbt.copyTag().getIntOr("Index", 0) : 0;
            if (this.removeMode) {
                switch (this.data.type) {
                    case ITEM -> this.data.claim.allowedItems.removeAllowedItem(idx);
                    case BLOCKBREAK -> this.data.claim.allowedBreakBlocks.removeAllowedItem(idx);
                    case BLOCKUSE -> this.data.claim.allowedUseBlocks.removeAllowedItem(idx);
                }
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            }
        }
        return false;
    }

    @Override
    protected PageSettings pageSettings() {
        int size = switch (this.data.type) {
            case ITEM -> this.data.claim.allowedItems.size();
            case BLOCKBREAK -> this.data.claim.allowedBreakBlocks.size();
            case BLOCKUSE -> this.data.claim.allowedUseBlocks.size();
            case ENTITYATTACK -> this.data.claim.allowedEntityAttack.size();
            case ENTITYUSE -> this.data.claim.allowedEntityUse.size();
        };
        return new PageSettings((size - 1) / 28, 47, 51);
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