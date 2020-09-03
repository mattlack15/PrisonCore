package com.soraxus.prisons.selling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

@Getter
@AllArgsConstructor
public class SellItem {
    private long price;
    private Material itemMaterial;
    private byte itemData;

    public static SellItem fromSection(ConfigurationSection section) {
        long price = section.getLong("price");
        Material material = Material.matchMaterial(section.getString("material"));
        byte data = (byte) section.getInt("material-data");
        return new SellItem(price, material, data);
    }

    public void saveTo(ConfigurationSection section) {
        section.set("price", price);
        section.set("material", itemMaterial.toString());
        section.set("material-data", (int) itemData);
    }
}
