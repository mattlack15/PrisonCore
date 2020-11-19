package com.soraxus.prisons.bunkers.base.elements.animation;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.util.ArmorStandFactory;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.string.TextUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

/**
 * Handles damage animations
 */
public class DamageAnimationHandler {
    /**
     * The UUID of the current damage indicator stand
     */
    private UUID damageStand = null;
    /**
     * The UUID of the current health stand
     */
    private UUID healthStand = null;
    /**
     * The instance of BunkerElement this damage animation handler belongs to
     */
    private final BunkerElement parent;
    /**
     * The amount of time until I die
     */
    private int keepAliveTicks = 0;

    private int keepAliveTicksDamage = 0;
    private double currentDamage = 0D;

    private volatile boolean cleaned = false;

    /**
     * The height of the element
     */
    private final double elementHeight;
    /**
     * idfk
     */
    private final int liveTicks;
    private double prevHealth = 0;
    private final Random random = new Random(System.currentTimeMillis());

    /**
     * A constructor
     *
     * @param parent         The instance of BunkerElement this damage animation handler belongs to
     * @param elementHeight  The height of the element
     * @param keepAliveTicks The amount of time until I die
     */
    public DamageAnimationHandler(BunkerElement parent, double elementHeight, int keepAliveTicks) {
        this.parent = parent;
        this.elementHeight = elementHeight;
        this.liveTicks = keepAliveTicks;
    }

    /**
     * Tick this animation handler
     * Updates armor stands and shit
     */
    public synchronized void tick() {
        if (keepAliveTicks <= 0)
            return;

        keepAliveTicks--;
        if (healthStand != null && prevHealth != parent.getHealth()) {
            validateHealthStand();
            prevHealth = parent.getHealth();
            if (keepAliveTicks == 0) {
                ArmorStand stand = getStand(healthStand);
                if (stand != null) {
                    stand.remove();
                    healthStand = null;
                }
            }
        }

        if (damageStand != null) {
            ArmorStand stand = getStand(damageStand);
            if (stand != null) {
                stand.teleport(stand.getLocation().add(stand.getVelocity()));
                stand.setVelocity(stand.getVelocity().multiply(0.973F).subtract(new Vector(0, 0.015, 0)));
                if ((keepAliveTicksDamage > 0 && --keepAliveTicksDamage == 0) || keepAliveTicks == 0) {
                    keepAliveTicksDamage = 0;
                    stand.remove();
                    damageStand = null;
                }
            }
        }
    }

    /**
     * Animate a damage indicator on this health bar
     *
     * @param damage Amount of damage
     */
    public synchronized void animate(double damage) {
        if (cleaned)
            return;
        keepAliveTicks = liveTicks;
        if (keepAliveTicksDamage == 0) {
            keepAliveTicksDamage = liveTicks / 6;
            currentDamage = 0D;
        }
        this.validateHealthStand();
        this.validateDamageStand(damage);
    }

    /**
     * Update the damage indicator on this health bar
     *
     * @param damage Amount of damage
     */
    private void updateDamageStand(double damage) {
        if (damageStand == null)
            return;
        ArmorStand stand = getStand(damageStand);
        if (stand != null) {
            stand.setCustomName(ChatColor.RED + "" + MathUtils.round(damage, 1));
        }
    }

    /**
     * Get the armor stand with a certain UUID
     *
     * @param id UUID of the armor stand
     * @return The armor stand
     */
    private ArmorStand getStand(UUID id) {
        for (Entity entity : parent.getBunker().getWorld().getBukkitWorld().getEntities())
            if (entity instanceof ArmorStand && entity.getUniqueId().equals(id))
                return (ArmorStand) entity;
        return null;
    }

    /**
     * Validate this health stand
     */
    private void validateHealthStand() {
        if (this.healthStand == null) {
            this.healthStand = ArmorStandFactory.createText(parent.getLocation().add(parent.getShape().getX() / 2D * BunkerManager.TILE_SIZE_BLOCKS, elementHeight, parent.getShape().getY() / 2D * BunkerManager.TILE_SIZE_BLOCKS),
                    getHealthString()).getUniqueId();
        } else {
            ArmorStand stand = getStand(this.healthStand);
            if (stand != null) {
                stand.setCustomName(getHealthString());
            }
        }
    }

    /**
     * Validate this damage stand
     *
     * @param damage Amount of damage
     */
    private void validateDamageStand(double damage) {
        if (this.damageStand == null) {

            //Create stand
            ArmorStand stand = ArmorStandFactory.createText(parent.getLocation().add(parent.getShape().getX() / 2D * BunkerManager.TILE_SIZE_BLOCKS, elementHeight, parent.getShape().getY() / 2D * BunkerManager.TILE_SIZE_BLOCKS),
                    ChatColor.RED + "" + MathUtils.round(damage, 1));
            this.damageStand = stand.getUniqueId();

            //Random velocity
            stand.setVelocity(new Vector(random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.2 - 0.1, random.nextDouble() * 0.5 - 0.25));
        } else {
            currentDamage += damage;
            this.updateDamageStand(currentDamage);
        }
    }

    /**
     * Get the string that should be displayed in the health bar
     *
     * @return THE STRING
     */
    private String getHealthString() {
        double health = Math.round(parent.getHealth() * 10D) / 10D;
        double maxHealth = Math.round(parent.getMaxHealth() * 10D) / 10D;
        String bar = TextUtil.generateBar('a', 'f', 45, (int) Math.round(health * 10D), (int) Math.round(maxHealth * 10D));
        return bar + " " + ChatColor.BLUE + health + ChatColor.WHITE + "/" + ChatColor.BLUE + maxHealth;
    }

    /**
     * Delete literally everything
     */
    public void cleanup() {
        cleaned = true;
        ArmorStand stand;
        if (damageStand != null && (stand = this.getStand(damageStand)) != null) {
            stand.remove();
            damageStand = null;
        }
        if (healthStand != null && (stand = this.getStand(healthStand)) != null) {
            stand.remove();
            healthStand = null;
        }
    }
}
