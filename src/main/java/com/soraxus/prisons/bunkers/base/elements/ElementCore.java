package com.soraxus.prisons.bunkers.base.elements;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.storage.MenuStorageElement;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.storage.StorageElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.base.resources.MenuSkills;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BHoloTextBox;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.menus.MenuElement;
import com.soraxus.prisons.util.menus.MenuManager;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The core of a bunker
 * Determines the maximum level of all elements in this bunker
 * Cannot be removed
 */
public class ElementCore extends StorageElement {

    private BHoloTextBox infoBox;

    /**
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The bunker this element is a part of
     */
    public ElementCore(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    @Override
    public double getCapacity(BunkerResource resource) {
        return 50;
    }

    public ElementCore(Bunker bunker) {
        super(bunker,
                new Storage(BunkerResource.TIMBER, 0, 50),
                new Storage(BunkerResource.STONE, 0, 50)
        );
    }

    @Override
    public IntVector2D getShape() {
        return new IntVector2D(3, 3);
    }

    @NotNull
    @Override
    public Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.getWithoutThrow("core-" + (level) + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ESSENTIAL_CORE;
    }

    @Override
    public double getMaxHealth() {
        return this.getLevel() * 300 + 100;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public void onTick() {
        int line = 0;
        infoBox.setOrMake(line++, "&a&lINFO");
        infoBox.setOrMake(line++, "&eGang: &f" + (getBunker().getGang() != null ? getBunker().getGang().getName() : "Gang not loaded"));
        infoBox.setOrMake(line++, "&b== Current Resources ==");
        Map<BunkerResource, Storage> resources = getBunker().getCombinedStorages();
        for (Map.Entry<BunkerResource, Storage> ent : resources.entrySet()) {
            String message = ent.getKey().getColor() + ent.getKey().getDisplayName() + " &f" + MathUtils.round(ent.getValue().getAmount(), 1) + "&7/&f" + ent.getValue().getCap();
            infoBox.setOrMake(line++, message);
        }
    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getPlayer().isSneaking()) {
            new MenuCoreElement(this).open(e.getPlayer());
        } else if (e.getClickedBlock() != null && e.getClickedBlock().getType().name().contains("DOOR")) {
            e.setCancelled(false);
        }
    }

    @Override
    protected void onEnable() {
        if (infoBox == null) {
            double x = getShape().getX() * BunkerManager.TILE_SIZE_BLOCKS / 6D * 5D;
            infoBox = new BHoloTextBox(getLocation().add(x, 1.5 + (0.3 * this.getStorageList().size()), getShape().getY() * BunkerManager.TILE_SIZE_BLOCKS - 0.5), 0.3, false, () -> getBunker().getWorld().getBukkitWorld());
        }
    }

    @Override
    protected void onDisable() {
        if (infoBox != null)
            infoBox.clear();
        infoBox = null;
    }

    private class MenuCoreElement extends MenuStorageElement {

        public MenuCoreElement(StorageElement element) {
            super(element);
        }

        @Override
        public void setup() {
            super.setup();
            this.setElement(8, new MenuElement(new ItemBuilder(Material.DIAMOND_AXE, 1).setName("&e&lSkillz").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).addLore("&7Click to view your bunker skills")
                    .build()).setClickHandler((e, i) -> new MenuSkills(getBunker(), getBackButton(this)).open(e.getWhoClicked())));
            MenuManager.instance.invalidateInvsForMenu(this);
        }
    }

    @Override
    public boolean onDestroy() {
        if (getBunker().getDefendingMatch() != null) {
            getBunker().getDefendingMatch().end();
        }
        return false;
    }
}