package com.soraxus.prisons.bunkers.base.elements.envoy;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ElementEnvoy extends BunkerElement {
    /**
     * All non-abstract BunkerElement child classes must have an exact matching constructor
     * They may have more than one constructor but one of them must be matching for de-serialization
     *
     * @param serializer Serialized BunkerElement object to deserialize. Null if new object
     * @param bunker     The Bunker this element is a part of
     */
    public ElementEnvoy(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker);
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public @NotNull Schematic getSchematic(int level, boolean destroyed) {
        return BunkerSchematics.get("envoy-1" + (destroyed ? "-destroyed" : ""));
    }

    @Override
    public void onTick() {
    }

    @Override
    public double getMaxHealth() {
        return 1;
    }

    @Override
    public String getName() {
        return "Envoy";
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.ENVOY_ENVOY;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }

    private Storage open() {
        Bunker bunker = getBunker();
        BunkerResource[] resources = BunkerResource.values();
        Map<BunkerResource, Storage> storages = bunker.getCombinedStorages();
        resources = Arrays.stream(resources).filter(r -> {
            Storage st = storages.get(r);
            if (st == null) {
                return false;
            }
            return st.getCap() - st.getAmount() > 0;
        }).toArray(BunkerResource[]::new);
        if (resources.length == 0) {
            this.remove();
            return null;
        }
        BunkerResource res = resources[ThreadLocalRandom.current().nextInt(resources.length)];

        int am = MathUtils.lowWeightedInt(
                bunker.getCore().getLevel(),
                50 * bunker.getCore().getLevel(),
                2
        );

        bunker.addResource(res, am);
        this.remove();
        return new Storage(res, am, 0);
    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        Storage storage = open();
        if (storage != null) {
            getBunker().messageMember(e.getPlayer(), ChatColor.GREEN + "+" + storage.getAmount() + " " + storage.getResource().fullDisplay());
        } else {
            getBunker().messageMember(e.getPlayer(), ChatColor.RED + "Hmmm... this chest seems to be empty. Unlucky me D:");
        }
        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.2f);
    }
}
