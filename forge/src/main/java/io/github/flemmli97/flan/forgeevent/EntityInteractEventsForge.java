package io.github.flemmli97.flan.forgeevent;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.Event;

public class EntityInteractEventsForge {

    public static void attackEntity(AttackEntityEvent event) {
        ActionResult result = EntityInteractEvents.attackSimple(event.getPlayer(), event.getTarget(), true);
        if (result == ActionResult.FAIL) {
            event.setCanceled(true);
        }
    }

    public static void useAtEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity target = event.getTarget();
        ActionResult result = EntityInteractEvents.useAtEntity(event.getPlayer(), event.getWorld(), event.getHand(), target,
                new EntityHitResult(target, event.getLocalPos().add(target.getX(), target.getY(), target.getZ())));
        if (result != ActionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static void useEntity(PlayerInteractEvent.EntityInteract event) {
        ActionResult result = EntityInteractEvents.useEntity(event.getPlayer(), event.getWorld(), event.getHand(), event.getTarget());
        if (result != ActionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static void projectileHit(ProjectileImpactEvent event) {
        if (!(event.getEntity() instanceof ProjectileEntity))
            return;
        boolean stop = EntityInteractEvents.projectileHit((ProjectileEntity) event.getEntity(), event.getRayTraceResult());
        if (stop) {
            event.setCanceled(true);
        }
    }

    public static void preventDamage(LivingDamageEvent event) {
        boolean prevent = EntityInteractEvents.preventDamage(event.getEntity(), event.getSource());
        if (prevent) {
            event.setCanceled(true);
        }
    }

    public static void xpAbsorb(PlayerXpEvent.PickupXp event) {
        boolean prevent = EntityInteractEvents.xpAbsorb(event.getPlayer());
        if (prevent)
            event.setCanceled(true);
    }

    public static void canDropItem(ItemTossEvent event) {
        boolean canDrop = EntityInteractEvents.canDropItem(event.getPlayer(), event.getEntityItem().getStack());
        if (!canDrop) {
            event.setCanceled(true);
        }
    }

    /**
     * EntityInteractEvents.witherCanDestroy
     * EntityInteractEvents.canSnowGolemInteract
     */
    public static void mobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof WitherEntity && EntityInteractEvents.witherCanDestroy((WitherEntity) event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
        if (event.getEntity() instanceof SnowGolemEntity && !EntityInteractEvents.canSnowGolemInteract((SnowGolemEntity) event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
