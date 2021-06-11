package io.github.flemmli97.flan.forgeevent;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.IPermissionContainer;
import io.github.flemmli97.flan.claim.ObjectToPermissionMap;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.integration.permissionapi.CommandPermission;
import io.github.flemmli97.flan.player.EnumDisplayType;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

public class ItemInteractEventsForge {

    public static TypedActionResult<ItemStack> useItem(PlayerInteractEvent.RightClickItem event) {

        event.set
        PlayerInteractEvent.RightClickItem event
        if (world.isClient || p.isSpectator())
            return TypedActionResult.pass(p.getStackInHand(hand));
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == ConfigHandler.config.claimingItem) {
            HitResult ray = player.rayTrace(64, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                claimLandHandling(player, ((BlockHitResult) ray).getBlockPos());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }
        if (stack.getItem() == ConfigHandler.config.inspectionItem) {
            HitResult ray = player.rayTrace(32, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                inspect(player, ((BlockHitResult) ray).getBlockPos());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }

        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        BlockPos pos = player.getBlockPos();
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim == null)
            return TypedActionResult.pass(stack);
        ClaimPermission perm = ObjectToPermissionMap.getFromItem(stack.getItem());
        if (perm != null)
            return claim.canInteract(player, perm, pos, true) ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        return TypedActionResult.pass(stack);
    }

    private static final Set<Item> blackListedItems = Sets.newHashSet(Items.COMPASS, Items.FILLED_MAP, Items.FIREWORK_ROCKET);

    public static ActionResult onItemUseBlock(ItemUsageContext context) {
        //Check for Fakeplayer. Since there is no api for that directly check the class
        if (!(context.getPlayer() instanceof ServerPlayerEntity) || !context.getPlayer().getClass().equals(ServerPlayerEntity.class) || context.getStack().isEmpty())
            return ActionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) context.getWorld());
        BlockPos placePos = new ItemPlacementContext(context).getBlockPos();
        IPermissionContainer claim = storage.getForPermissionCheck(placePos.add(0, 255, 0));
        if (claim == null)
            return ActionResult.PASS;
        if (blackListedItems.contains(context.getStack().getItem()))
            return ActionResult.PASS;
        boolean actualInClaim = !(claim instanceof Claim) || placePos.getY() >= ((Claim) claim).getDimensions()[4];
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ClaimPermission perm = ObjectToPermissionMap.getFromItem(context.getStack().getItem());
        if (perm != null) {
            if (claim.canInteract(player, perm, placePos, false))
                return ActionResult.PASS;
            else if (actualInClaim) {
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
                return ActionResult.FAIL;
            }
        }
        if (claim.canInteract(player, PermissionRegistry.PLACE, placePos, false)) {
            if (!actualInClaim && context.getStack().getItem() instanceof BlockItem) {
                ((Claim) claim).extendDownwards(placePos);
            }
            return ActionResult.PASS;
        } else if (actualInClaim) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
            BlockState other = context.getWorld().getBlockState(placePos.up());
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(placePos.up(), other));
            PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
            updateHeldItem(player);
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}
