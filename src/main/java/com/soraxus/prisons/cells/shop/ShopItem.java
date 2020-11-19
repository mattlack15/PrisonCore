package com.soraxus.prisons.cells.shop;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

//Currently not thread-safe
public class ShopItem implements GravSerializable {

    public enum ShopItemType {
        BUY, SELL
    }

    @Getter
    @Setter
    private ItemStack item;
    @Getter
    @Setter
    private int stock = 0;
    @Getter
    @Setter
    private long cost = 0;

    @Getter
    @Setter
    private ShopItemType type;

    public ShopItem(ItemStack item, ShopItemType type) {
        this.type = type;
        this.item = item.clone();
        item.setAmount(1);
    }

    public ShopItem(GravSerializer serializer) {
        this.type = serializer.readObject();
        this.item = this.itemFromString(serializer.readString());
        this.cost = serializer.readLong();
        this.stock = serializer.readInt();
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeObject(type);
        serializer.writeString(this.itemToString(this.item));
        serializer.writeLong(this.cost);
        serializer.writeInt(this.stock);
    }

    public void add(int amount) {
        stock += amount;
    }

    public void remove(int amount) {
        stock = Math.max(0, stock - amount);
    }

    public boolean matchesType(ItemStack stack) {
        if(stack == null)
            return false;
        ItemStack stack1 = stack.clone();
        stack1.setAmount(item.getAmount());
        return itemToString(stack1).equals(itemToString(item));
    }

    private String itemToString(ItemStack stack) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", stack);
        return configuration.saveToString();
    }
    private ItemStack itemFromString(String string) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.loadFromString(string);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return configuration.getItemStack("item");
    }
}
