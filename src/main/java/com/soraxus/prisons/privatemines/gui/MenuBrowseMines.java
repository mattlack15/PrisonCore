package com.soraxus.prisons.privatemines.gui;

import com.soraxus.prisons.SpigotPrisonCore;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.mines.ModuleMines;
import com.soraxus.prisons.privatemines.CachedMineInfo;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import com.soraxus.prisons.privatemines.VisitationType;
import com.soraxus.prisons.util.NumberUtils;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.items.ItemBuilder;
import com.soraxus.prisons.util.menus.Menu;
import com.soraxus.prisons.util.menus.MenuElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuBrowseMines extends Menu {
    private final MenuElement backButton;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public MenuBrowseMines(MenuElement backButton) {
        super("Browse mines", 5);
        this.backButton = backButton;
        this.setup();
    }

    public void setup() {

        Iterator<Map.Entry<UUID, CachedMineInfo>> infoIterator = PrivateMineManager.instance.getCachedMineInfos().entrySet().iterator(); //Using iterator to avoid unnecessary copying, for better performance
        this.setupActionableList(10, 10 + 6 + (9 * 3), 10 + 6 + (9 * 3) + 2, 10 + 6 + (9 * 3) + 2 + 8, (index) -> {
            if (!infoIterator.hasNext())
                return null;
            Map.Entry<UUID, CachedMineInfo> infoContainer = infoIterator.next();
            CachedMineInfo info = infoContainer.getValue();
            PrivateMine mine = PrivateMineManager.instance.getLoadedPrivateMine(infoContainer.getKey());
            return new MenuElement(new ItemBuilder(Material.GOLD_PICKAXE)
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .setName("&e" + info.getGangName())
                    .addLore("&fRent: &7" + NumberUtils.toReadableNumber(info.getRentPrice()))
                    .addLore("&fSlots: &7" + (mine == null ? info.getRentableSlots() : mine.getVisitationManager().getAvailableRentedSlots()) + " Available")
                    .build()).setClickHandler((e, i) -> {
                if (mine == null) {
                    getElement(e.getSlot()).addTempLore(this, "", 60);
                    getElement(e.getSlot()).addTempLore(this, "&eLoading...", 60);
                }
                service.submit(() -> {
                    try {
                        Gang gang = GangManager.instance.loadGang(infoContainer.getKey(), false);
                        PrivateMine mine1 = PrivateMineManager.instance.loadPrivateMineAsync(gang).join();
                        new BukkitRunnable() {
                            public void run() {
                                VisitationType type = mine1.getVisitationManager().tryAddVisitor(e.getWhoClicked().getUniqueId());
                                if (type == null) {
                                    e.getWhoClicked().sendMessage(ModuleMines.instance.getPrefix() + "There was no room for you! Sorry :(");
                                    return;
                                }
                                mine1.teleport((Player) e.getWhoClicked());
                                if (type == VisitationType.RENTAL)
                                    e.getWhoClicked().sendMessage(ModuleMines.instance.getPrefix() + "You are now paying " + NumberUtils.toReadableNumber(mine1.getVisitationManager().getRentalPrice()) + " per minute");
                                setup();
                            }
                        }.runTaskLater(SpigotPrisonCore.instance, 0);
                    } catch (Throwable t) {
                        Synchronizer.synchronize(() -> getElement(e.getSlot()).addTempLore(this, "&cError...", 60));
                        t.printStackTrace();
                    }
                });
            });
        }, 0);

        this.setElement(4, backButton);
    }
}
