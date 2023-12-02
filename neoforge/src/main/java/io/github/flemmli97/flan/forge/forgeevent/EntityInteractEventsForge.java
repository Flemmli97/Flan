package io.github.flemmli97.flan.forge.forgeevent;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

public class EntityInteractEventsForge {

    public static void attackEntity(AttackEntityEvent event) {
        InteractionResult result = EntityInteractEvents.attackSimple(event.getEntity(), event.getTarget(), true);
        if (result == InteractionResult.FAIL) {
            event.setCanceled(true);
        }
    }

    public static void useAtEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        Entity target = event.getTarget();
        InteractionResult result = EntityInteractEvents.useAtEntity(event.getEntity(), event.getLevel(), event.getHand(), target,
                new EntityHitResult(target, event.getLocalPos().add(target.getX(), target.getY(), target.getZ())));
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static void useEntity(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = EntityInteractEvents.useEntity(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static void projectileHit(ProjectileImpactEvent event) {
        if (!(event.getEntity() instanceof Projectile))
            return;
        boolean stop = EntityInteractEvents.projectileHit((Projectile) event.getEntity(), event.getRayTraceResult());
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
        boolean prevent = EntityInteractEvents.xpAbsorb(event.getEntity());
        if (prevent)
            event.setCanceled(true);
    }

    public static void canDropItem(ItemTossEvent event) {
        boolean canDrop = EntityInteractEvents.canDropItem(event.getPlayer(), event.getEntity().getItem());
        if (!canDrop) {
            event.setCanceled(true);
        }
    }

    /**
     * EntityInteractEvents.witherCanDestroy
     * EntityInteractEvents.canSnowGolemInteract
     */
    public static void mobGriefing(EntityMobGriefingEvent event) {
        if (event.getEntity() instanceof WitherBoss && !EntityInteractEvents.witherCanDestroy((WitherBoss) event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
        if (event.getEntity() instanceof SnowGolem && !EntityInteractEvents.canSnowGolemInteract((SnowGolem) event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    public static void entityLightningHit(EntityStruckByLightningEvent event) {
        if (EntityInteractEvents.preventLightningConvert(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
