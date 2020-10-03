package com.soraxus.prisons.cells.minions;

import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.UUID;

public class MinionArmorStand {
    @Getter
    private final Minion parent;

    @Getter
    private double position = 0;
    @Getter
    private UUID armorStand;

    public MinionArmorStand(Minion parent) {
        this.parent = parent;
    }

    public void setPosition(double position) {
        this.position = position;
        ArmorStand stand = getStand();
        if (stand == null)
            return;

        double ext = Math.PI / 2D * position + (Math.PI / 2D * 3D);
        stand.setRightArmPose(new EulerAngle(ext, 0, 0));
    }

    public void spawn() {
        destroy();
        ArmorStand stand = (ArmorStand) this.parent.getParent().getParent().getWorld().getBukkitWorld()
                .spawnEntity(this.parent.getLocation().toBukkitVector().add(new Vector(0.5, 0, 0.5)).toLocation(
                        this.parent.getParent().getParent().getWorld().getBukkitWorld()
                ), EntityType.ARMOR_STAND);

        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(true);

        Vector v = this.parent.getMiningBlockLocation().toBukkitVector().subtract(this.parent.getLocation().toBukkitVector());

        stand.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_PICKAXE));
        stand.teleport(stand.getLocation().setDirection(v));

        int armourColour = 0x88A2B6;

        stand.getEquipment().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE, 1)
                .setLeatherColour(armourColour).build());
        stand.getEquipment().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS, 1)
                .setLeatherColour(armourColour).build());
        stand.getEquipment().setBoots(new ItemBuilder(Material.LEATHER_BOOTS, 1)
                .setLeatherColour(armourColour).build());

        stand.setCustomName(parent.getName());
        stand.setCustomNameVisible(true);

        stand.getEquipment().setHelmet(new ItemBuilder(Material.SKULL_ITEM, 1)
                .setupAsSkull(parent.getSettings().getSkullName()).build());
        this.armorStand = stand.getUniqueId();
        setPosition(position);
        EventSubscriptions.instance.subscribe(this);
    }

    public void destroy() {
        ArmorStand stand = getStand();
        if (stand == null && armorStand != null) {
            Location loc = this.parent.getLocation().toBukkitVector().toLocation(this.parent.getParent().getParent().getWorld().getBukkitWorld());
            loc.getChunk().load();
            stand = getStand();
            if (stand == null)
                return;
        } else if (stand == null)
            return;
        stand.remove();
        this.armorStand = null;
        EventSubscriptions.instance.subscribe(this);
    }

    public ArmorStand getStand() {
        if (this.armorStand == null)
            return null;
        for (Entity entity : this.parent.getParent().getParent().getWorld().getBukkitWorld().getEntitiesByClass(ArmorStand.class)) {
            if (entity.getUniqueId().equals(this.armorStand)) {
                return (ArmorStand) entity;
            }
        }
        return null;
    }

    @EventSubscription
    private void onEquip(PlayerArmorStandManipulateEvent event) {
        if (event.getRightClicked().getUniqueId().equals(this.armorStand))
            event.setCancelled(true);
    }

    @EventSubscription
    private void onBreak(EntityDamageEvent event) {
        if(event.getEntity().getUniqueId().equals(this.armorStand))
            event.setCancelled(true);
    }
}
