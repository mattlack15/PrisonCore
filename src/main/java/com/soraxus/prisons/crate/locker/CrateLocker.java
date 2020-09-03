package com.soraxus.prisons.crate.locker;

import com.soraxus.prisons.crate.Crate;
import com.soraxus.prisons.crate.CrateManager;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CrateLocker {
    private int tier;
    private Map<String, Integer> amounts;

    public CrateLocker() {
        this(1, new HashMap<>());
    }

    public CrateLocker(ItemStack item) {
        this(
                NBTUtils.instance.getInt(item, "cltier"),
                LockerUtil.deserialize(NBTUtils.instance.getString(item, "cldata"))
        );
    }

    public ItemStack getItem() {
        ItemBuilder builder = new ItemBuilder(Material.DROPPER, 1)
                .addLore("§7Store all your extra crates in this")
                .addLore("§7handy Crate Locker! Craft 4 of these")
                .addLore("§7together to upgrade them to a higher")
                .addLore("§7tier and hold more crates!")
                .addLore("");
        int cap = LockerUtil.calculateCap(tier);
        for (Crate type : CrateManager.instance.getLoaded()) {
            String typeN = type.getName();
            builder.addLore(type.getDisplayName() + "§7: " + amounts.getOrDefault(typeN, 0) + " / " + cap);
        }
        ItemStack it = builder.build();
        it = NBTUtils.instance.setString(it, "type", "cratelocker");
        it = NBTUtils.instance.setInt(it, "cltier", tier);
        it = NBTUtils.instance.setString(it, "cldata", LockerUtil.serialize(amounts));

        return it;
    }
}