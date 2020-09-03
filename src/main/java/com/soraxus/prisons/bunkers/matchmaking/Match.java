package com.soraxus.prisons.bunkers.matchmaking;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.army.BunkerArmy;
import com.soraxus.prisons.bunkers.matchmaking.stats.MatchStats;
import com.soraxus.prisons.bunkers.npc.*;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.util.ItemBuilder;
import com.soraxus.prisons.util.SavedInventory;
import com.soraxus.prisons.util.display.hotbar.HotbarSelector;
import com.soraxus.prisons.util.display.hotbar.HotbarSelectorManager;
import com.soraxus.prisons.util.display.hotbar.SelectableElement;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Match {
    public static final int MATCH_TIME_TICKS = 60 * 10 * 20;

    @Getter
    private final Bunker attacker;
    @Getter
    private final Bunker defender;

    @Getter
    private final BunkerMatchMaker matchMaker;

    @Getter
    private final MatchStats defendingStats = new MatchStats();
    @Getter
    private final MatchStats attackingStats = new MatchStats();

    @Getter
    private final NPCManager npcManager = new NPCManager() {
        @Override
        public void addNPC(BunkerNPC npc) {
            super.addNPC(npc);
            npc.setMatch(Match.this);
        }
    };

    private final ReentrantLock attackerLock = new ReentrantLock(true);
    private final List<UUID> attackers = new ArrayList<>();
    private final Map<UUID, SavedInventory> savedInventoryMap = new ConcurrentHashMap<>();

    private final AtomicInteger ticksLeft = new AtomicInteger(MATCH_TIME_TICKS);

    private volatile boolean started = false;

    protected Match(@NotNull Bunker attacker, @NotNull Bunker defender, BunkerMatchMaker matchMaker) {
        this.attacker = attacker;
        this.defender = defender;
        this.matchMaker = matchMaker;
    }

    public List<AvailableTarget<?>> getAttackerTargets() {
        Bunker bunker = defender;
        List<AvailableTarget<?>> targetList = new ArrayList<>();
        NPCManager manager = bunker.getNpcManager();
        manager.getNpcList().forEach(n -> {
            if (n.getController() != null && n.getController().isSpawned()) {
                targetList.add(new NPCAvailableTarget(n));
            }
        });
        bunker.getTileMap().getElements().forEach(e -> {
            if (e.isVisibleToAttackers() && e.isEnabled() && !e.isDestroyed()) {
                targetList.add(new ElementAvailableTarget(e));
            }
        });
        return targetList;
    }

    public List<AvailableTarget<?>> getDefenderTargets() {
        List<AvailableTarget<?>> targetList = new ArrayList<>();
        NPCManager manager = attacker.getNpcManager();
        manager.getNpcList().forEach(n -> {
            if (n.getController() != null && n.getController().isSpawned()) {
                targetList.add(new NPCAvailableTarget(n));
            }
        });
        return targetList;
    }

    /**
     * @throws IllegalStateException if a match like this one is already running
     */
    public synchronized void start(Player... attackers) {
        for (Player player : attackers) {
            addAttacker(player);
        }
        if (started)
            return;
        started = true;
        attackerLock.lock();
        try {
            for (Player player : attackers) {
                setupAttacker(player);
            }
        } finally {
            attackerLock.unlock();
        }
    }

    public void addAttacker(Player player) {
        boolean started1 = false;
        attackerLock.lock();
        try {
            if (attackers.contains(player.getUniqueId()))
                return;
            if (started)
                started1 = true; //Just cuz this gotta be synchronized
            this.attackers.add(player.getUniqueId());
        } finally {
            attackerLock.unlock();
        }
        if (started1)
            setupAttacker(player); //And I don't want this to hold the lock
    }

    private void setupAttacker(Player player) {
        defender.teleport(player);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 1F, 1F);
        sendMatchInfo(player);
        getAttackHotbarSelector().open(player);
    }

    public void removeAttacker(Player player) {
        defender.teleportBack(player);

        //Remove from list
        attackerLock.lock();
        try {
            attackers.remove(player.getUniqueId());
        } finally {
            attackerLock.unlock();
        }

        HotbarSelectorManager.getInstance().getSelector(player).close();

        if (attackers.size() == 0 && started) {
            this.end();
        }
    }

    private void sendMatchInfo(Player player) {
        int ticks = ticksLeft.get();
        int timeLeft = (int) Math.round(ticks / 20D);
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "--------------------");

        player.sendMessage(ChatColor.BLUE + "Enemy Rating: " + ChatColor.WHITE + defender.getRating());
        player.sendMessage(ChatColor.BLUE + "Time Left: " + ChatColor.WHITE + timeLeft + "s");
        BunkerArmy army = attacker.getArmy();
        player.sendMessage(ChatColor.BLUE + "Available Warriors: " + ChatColor.WHITE + army.getAvailableWarriors().size());

        player.sendMessage(ChatColor.BLUE + "INFO: ");
        player.sendMessage(ChatColor.GRAY + "Spawn warriors and destroy");
        player.sendMessage(ChatColor.GRAY + "your enemy's defenses. Then");
        player.sendMessage(ChatColor.GRAY + "collect their loot!");

        player.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "--------------------");
    }

    public void end() {
        if (!started)
            return;
        this.started = false;

        boolean success = this.defender.getCore().isDestroyed();

        //Remove all NPCs
        this.npcManager.destroyAllNPCs();

        //Send messages
        this.getDefender().messageWorld("&cMatch Ended!");
        this.getDefender().getWorld().getBukkitWorld().getPlayers().forEach(p -> sendMatchStats(p, getAttackingStats()));

        //Remove all attackers
        new ArrayList<>(this.attackers).forEach(a -> removeAttacker(Bukkit.getPlayer(a)));

        BunkerMatchMaker.instance.freeMatch(this);
    }

    private void sendMatchStats(Player player, MatchStats stats) {
        //TODO
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Match) {
            return ((Match) object).attacker.equals(this.attacker) && ((Match) object).defender.equals(this.defender);
        }
        return false;
    }

    public void tick() {
        this.npcManager.tick();
        if (ticksLeft.decrementAndGet() <= 0) {
            this.end();
        }
    }

    public HotbarSelector getAttackHotbarSelector() {
        AtomicInteger selectedNPC = new AtomicInteger();
        AtomicReference<HotbarSelector> npcSelector = new AtomicReference<>();
        HotbarSelector selector = new HotbarSelector(
                null,
                new SelectableElement(
                        new ItemBuilder(Material.EGG, 64)
                                .setName("&f&lSpawn Eggs")
                                .addLore("",
                                        "&7Use these to spawn your fiercest soldiers",
                                        "&7on the battlefield!",
                                        "",
                                        "&fLeft Click - &aSpawn")
                                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                                .build(),
                        (e) -> {
                            e.getItem().setAmount(64);
                            if (e.getClickedBlock() != null) {
                                Location spawn = e.getClickedBlock().getLocation().add(0, 1, 0);
                                BunkerNPC npc = this.getAttacker().getAndRemoveNPC(BunkerNPCType.values()[selectedNPC.get()]);
                                if (npc != null)
                                    npc.spawn(this.attacker, this.getNpcManager(), spawn);
                            }
                            return false;
                        }
                ),
                null, null,
                new SelectableElement(new ItemBuilder(BunkerNPCType.values()[selectedNPC.get()].getDisplayItem().clone())
                        .setAmount((int) Math.min(64, this.getAttacker().getAvailableNPCCount(BunkerNPCType.values()[selectedNPC.get()])))
                        .build(), (e) -> {
                    npcSelector.get().open(e.getPlayer());
                    return false;
                }),
                null, null,
                new SelectableElement(
                        new ItemBuilder(Material.REDSTONE_BLOCK, 1).setName("&c&lEnd")
                                .addLore("&7Click to chicken out of this attack")
                                .addLore("&8JK you're not a chicken").build(),
                        (e) -> {
                            this.end();
                            return true;
                        }
                )
        );

        List<SelectableElement> elementList = new ArrayList<>();
        for (int i = 0, length = BunkerNPCType.values().length; i < length && i < 9; i++) {
            int finalI = i;
            elementList.add(new SelectableElement(
                    new ItemBuilder(BunkerNPCType.values()[i].getDisplayItem().clone())
                            .setAmount((int) Math.min(64, this.getAttacker().getAvailableNPCCount(BunkerNPCType.values()[i])))
                            .build(), (e) -> {
                selectedNPC.set(finalI);
                selector.open(e.getPlayer());
                return false;
            }));
        }

        npcSelector.set(new HotbarSelector(elementList));

        return selector;
    }
}
