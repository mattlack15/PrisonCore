/*
 * Copyright (c) 2020. UltraDev
 */

package com.soraxus.prisons.economy;

import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.data.PlayerData;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.items.NBTUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Max balance: 18,446,744,073,709,551,615
@Getter
public class Economy {
    public static final Map<String, Economy> economies = new HashMap<>();

    public static final Economy money = new Economy("money", "&e$%s");
    public static final Economy tokens = new Economy("tokens", "&e%s Tokens");
    public static final Economy stars = new Economy("stars", "&e%s Stars");

    private String name;
    private String format;
    private long defaultBalance;

    public Economy(String name) {
        this(name, 0);
    }

    public Economy(String name, long defaultBalance) {
        this(name, null, defaultBalance);
    }

    public Economy(String name, String format) {
        this(name, format, 0);
    }

    public Economy(String name, String format, long defaultBalance) {
        this.name = name;
        this.format = format;
        this.defaultBalance = defaultBalance;
        economies.put(name, this);
    }

    public synchronized long getBalance(UUID id) {
        return PlayerData.getLong(id, "economy." + name);
    }

    public synchronized long setBalance(UUID id, long amount) {
        return PlayerData.set(id, "economy." + name, amount);
    }

    public synchronized long addBalance(UUID id, long amount) {
        return setBalance(id, getBalance(id) + amount);
    }

    public synchronized long removeBalance(UUID id, long amount) {
        return setBalance(id, getBalance(id) - amount);
    }

    public synchronized boolean tryRemoveBalance(UUID id, long amount) {
        if(hasBalance(id, amount)) {
            removeBalance(id, amount);
            return true;
        }
        return false;
    }

    public synchronized boolean hasBalance(UUID id, long amount) {
        return getBalance(id) >= amount;
    }

    public synchronized long resetBalance(UUID id) {
        return setBalance(id, defaultBalance);
    }

    public String format(String num) {
        return String.format(format, num);
    }

    public ItemStack createNote(long amount) {
        return new ItemBuilder(Material.PAPER)
                .addNBT("voucher." + this.getName(), amount)
                .setName(getFormat().replace("%s", NumberUtils.toReadableNumber(amount)) + " &bBank Note")
                .addLore("&7Right click to &aclaim")
                .build();
    }

    public boolean isValidNote(ItemStack voucher) {
        return voucher != null && NBTUtils.instance.hasTag(voucher, "voucher." + getName());
    }

    public long getNoteValue(ItemStack voucher) {
        if (!isValidNote(voucher))
            return 0;
        return NBTUtils.instance.getLong(voucher, "voucher." + getName());
    }
}