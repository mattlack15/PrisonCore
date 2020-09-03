package com.soraxus.prisons.selling.autosell;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoSellInfo {
    private boolean enabled = false;
    private long lastMessage = 0;
    private long total = 0;
    private ReentrantLock lock = new ReentrantLock();
}

