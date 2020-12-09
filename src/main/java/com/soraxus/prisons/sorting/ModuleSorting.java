package com.soraxus.prisons.sorting;

import com.soraxus.prisons.core.CoreModule;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.event.PrisonBlockBreakEvent;
import com.soraxus.prisons.event.PrisonPreSellEvent;
import com.soraxus.prisons.selling.SellItem;
import com.soraxus.prisons.selling.autosell.AutoSellInfo;
import com.soraxus.prisons.selling.autosell.AutoSellManager;
import com.soraxus.prisons.selling.autosell.command.AutoSellCmd;
import com.soraxus.prisons.selling.mutlipliers.Multiplier;
import com.soraxus.prisons.selling.mutlipliers.MultiplierInfo;
import com.soraxus.prisons.selling.mutlipliers.MultiplierManager;
import com.soraxus.prisons.selling.mutlipliers.command.CommandMultiplier;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.maps.LockingMap;
import com.soraxus.prisons.util.menus.MenuElement;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
public class ModuleSorting extends CoreModule {
    public static ModuleSorting instance;

    private LockingMap<String, SortingTask<?>> tasks = new LockingMap<>();

    @Override
    public String getName() {
        return "Sorting";
    }

    @Override
    public MenuElement getGUI(MenuElement backButton) {
        return null;
    }

    @Override
    protected void onEnable() {
        instance = this;
        Scheduler.scheduleSyncRepeatingTask(this::run, 10, 10);
    }

    public void submitTask(String name, SortingTask<?> task) {
        tasks.put(name, task);
    }

    public <T> SortingTask<T> getTask(String name) {
        return (SortingTask<T>) tasks.get(name);
    }

    private void run() {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        AtomicInteger todoCount = new AtomicInteger();
        for (SortingTask<?> task : tasks.values()) {
            if (task.isRequireSync()) {
                task.cache();
            } else {
                todoCount.incrementAndGet();
                pool.submit(() -> {
                    task.cache();
                    if (todoCount.decrementAndGet() == 0) {
                        lock.lock();
                        cond.signal();
                        lock.unlock();
                    }
                });
            }
        }
        new Thread(() -> {
            lock.lock();
            try {
                if (todoCount.get() > 0) {
                    cond.awaitUninterruptibly();
                }
            } finally {
                lock.unlock();
            }
            for (SortingTask<?> task : tasks.values()) {
                task.sort();
            }
        }).start();
    }
}
