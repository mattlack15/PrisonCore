package com.soraxus.prisons.bunkers.matchmaking;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.gangs.GangMemberManager;
import com.soraxus.prisons.util.Synchronizer;
import com.soraxus.prisons.util.display.hotbar.HotbarSelector;
import com.soraxus.prisons.util.display.hotbar.SelectableElement;
import com.soraxus.prisons.util.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BunkerMatchSelector {
    private Player player;
    private final AtomicReference<Bunker> playerBunker = new AtomicReference<Bunker>();
    private boolean loadedBefore;
    private Bunker currentBunker;

    private List<UUID> allMatches;
    private List<UUID> matches;

    public BunkerMatchSelector(Player player) {
        this.player = player;
        UUID gangId = GangMemberManager.instance.getMember(player.getUniqueId()).getGang();
        Gang gang = GangManager.instance.getOrLoadGang(gangId);

        playerBunker.set(gang.getBunker());
        boolean next = true;
        if (playerBunker.get() == null) {
            next = false;
            gang.loadBunker().thenAccept((b) -> {
                playerBunker.set(b);
                loadNextBunker();
            });
        }

        allMatches = BunkerMatchMaker.instance.findMatches(playerBunker.get().getRating());
        allMatches.remove(gangId);
        Collections.shuffle(allMatches);
        reset();

        getSelector().open(player);

        if (next)
            loadNextBunker();
    }

    private void reset() {
        matches = new ArrayList<>(allMatches);
    }

    private UUID getNextMatch() {
        if (matches.size() == 0) {
            reset();
        }
        return matches.remove(0);
    }

    private HotbarSelector getSelector() {
        ItemStack accept = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 5)
                .setName("§a§lAttack!")
                .build();
        ItemStack next = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 14)
                .setName("§c§lNext")
                .build();

        SelectableElement acceptEl = new SelectableElement(accept, (e) -> {
            BunkerMatchMaker.instance.reserveMatch(playerBunker.get(), currentBunker).start(player);
            return false; //Match will create a new selector
        });
        SelectableElement nextEl = new SelectableElement(next, (e) -> {
            if (loadNextBunker()) {
                Bunker bunker = playerBunker.get();
                if (bunker != null) {
                    bunker.messageMember(e.getPlayer(), "&7Loading next bunker...");
                }
            }
            return false;
        });

        SelectableElement exitEl = new SelectableElement(new ItemBuilder(Material.REDSTONE)
                .setName("&c&lExit").addLore("&7Click to stop searching").build(), (e) -> true);

        return new HotbarSelector(
                acceptEl,
                acceptEl,
                acceptEl,
                null,
                exitEl,
                null,
                nextEl,
                nextEl,
                nextEl
        );
    }

    private final AtomicBoolean loadingNextBunker = new AtomicBoolean(false);

    public boolean loadNextBunker() {
        if (!loadingNextBunker.compareAndSet(false, true)) {
            return false;
        }
        Bunker toUnload = loadedBefore ? currentBunker : null;
        UUID bunkerId = getNextMatch();

        if (bunkerId == null)
            return false;

        System.out.println("Trying to get bunker " + bunkerId.toString());

        if (!BunkerManager.instance.getFile(bunkerId).exists()) {
            allMatches.remove(bunkerId);
            loadNextBunker();
            return false;
        }

        currentBunker = BunkerManager.instance.getLoadedBunker(bunkerId);
        loadedBefore = false;
        if (currentBunker == null) {
            final Bunker unload = toUnload; //Not redundant trust me
            BunkerManager.instance.loadBunkerAsync(bunkerId).thenAccept(b -> {
                currentBunker = b;
                Synchronizer.synchronize(() -> {
                    try {
                        this.teleport();
                        if (unload != null) {
                            unload.unload(false);
                        }
                    } finally {
                        loadingNextBunker.set(false);
                    }
                });
            });
            loadedBefore = true;
        } else {
            try {
                teleport();
                if (toUnload != null) {
                    BunkerManager.instance.tryUnload(toUnload, false);
                }
            } finally {
                loadingNextBunker.set(false);
            }
        }
        return true;
    }

    public void teleport() {
        currentBunker.teleport(player);
    }
}
