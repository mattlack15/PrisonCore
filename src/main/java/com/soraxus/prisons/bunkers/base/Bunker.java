package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.AvgMeasure;
import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.army.BunkerArmy;
import com.soraxus.prisons.bunkers.base.elements.ElementCore;
import com.soraxus.prisons.bunkers.base.elements.ElementWorkerHut;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementWall;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp.ElementArmyCamp;
import com.soraxus.prisons.bunkers.base.elements.envoy.ElementEnvoy;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementPond;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementRock;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementStump;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementTree;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.elements.storage.StorageElement;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.handler.BunkerInfoMessageHandler;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.base.resources.SkillManager;
import com.soraxus.prisons.bunkers.base.shops.BunkerElementShop;
import com.soraxus.prisons.bunkers.matchmaking.BunkerMatchMaker;
import com.soraxus.prisons.bunkers.matchmaking.Match;
import com.soraxus.prisons.bunkers.matchmaking.stats.BunkerMatchStatistics;
import com.soraxus.prisons.bunkers.npc.BunkerNPC;
import com.soraxus.prisons.bunkers.npc.BunkerNPCSkillManager;
import com.soraxus.prisons.bunkers.npc.NPCManager;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import com.soraxus.prisons.bunkers.shop.BunkerShop;
import com.soraxus.prisons.bunkers.tools.ToolUtils;
import com.soraxus.prisons.bunkers.util.BunkerSchematics;
import com.soraxus.prisons.bunkers.workers.Task;
import com.soraxus.prisons.bunkers.workers.Worker;
import com.soraxus.prisons.bunkers.world.BunkerWorld;
import com.soraxus.prisons.event.bunkers.BunkerDisableEvent;
import com.soraxus.prisons.event.bunkers.BunkerEnableEvent;
import com.soraxus.prisons.event.bunkers.BunkerMatchEndEvent;
import com.soraxus.prisons.gangs.Gang;
import com.soraxus.prisons.gangs.GangManager;
import com.soraxus.prisons.util.EventSubscription;
import com.soraxus.prisons.util.EventSubscriptions;
import com.soraxus.prisons.util.display.chat.ChatBuilder;
import com.soraxus.prisons.util.display.chat.ClickUtil;
import com.soraxus.prisons.util.display.chat.HoverUtil;
import com.soraxus.prisons.util.list.ElementableList;
import com.soraxus.prisons.util.math.MathUtils;
import com.soraxus.prisons.util.particles.ParticleShape;
import com.soraxus.prisons.util.particles.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class Bunker implements GravSerializable {
    private final NPCManager npcManager = new NPCManager();
    private final BunkerInfoMessageHandler infoMessageHandler = new BunkerInfoMessageHandler(this);
    Map<UUID, Vector3D> previousLocations = new HashMap<>();
    Map<UUID, Location> previousLocations2 = new HashMap<>();
    private TileMap tileMap = new TileMap(this);
    private Gang gang;
    private BunkerWorld world;
    private Meta meta = new Meta();
    private SkillManager skillManager = new SkillManager(this);
    @Setter
    private IntVector3D bunkerLocation;
    private long seed = System.currentTimeMillis();
    @Setter
    private String mapSchematicName;
    @Setter
    private int destroyedTicksLeft = 120;
    private BunkerMatchStatistics matchStatistics = new BunkerMatchStatistics();
    private BunkerNPCSkillManager npcSkillManager = new BunkerNPCSkillManager(this);
    private UUID id;
    private final ReentrantLock resourceLock = new ReentrantLock(true);
    @Getter
    private boolean newlyCreated = false;

    public Bunker(Gang gang, String mapSchematicName) {
        this.newlyCreated = true;
        this.gang = gang;
        this.id = gang.getId();
        this.mapSchematicName = mapSchematicName;
        Random random = new Random(seed);

        BunkerElement core = new ElementCore(this);
        this.setElement((BunkerManager.BUNKER_SIZE_TILES - core.getShape().getX()) / 2, (BunkerManager.BUNKER_SIZE_TILES - core.getShape().getY()) / 2, core);
        this.setElement(0, 0, new ElementWorkerHut(this));
        this.setElement(1, 0, new ElementWorkerHut(this)); //Remove 3 of them after testing
        this.setElement(1, 1, new ElementWorkerHut(this));
        this.setElement(0, 1, new ElementWorkerHut(this));


        //Random trees, rocks, rivers, stumps
        for (int i = 0, bound = random.nextInt(BunkerManager.BUNKER_SIZE_TILES) + 128; i < bound; i++) {
            int t = random.nextInt(4);
            if (t == 0) {
                BunkerElement element = new ElementRock(this, random.nextInt(1) + 1);
                this.setElement(random.nextInt(BunkerManager.BUNKER_SIZE_TILES), random.nextInt(BunkerManager.BUNKER_SIZE_TILES), element);
            }
            if (t == 1) {
                BunkerElement element = new ElementTree(this, random.nextInt(1) + 1, random.nextInt(2) + 1);
                this.setElement(random.nextInt(BunkerManager.BUNKER_SIZE_TILES), random.nextInt(BunkerManager.BUNKER_SIZE_TILES), element);
            }
            if (t == 2) {
                BunkerElement element = new ElementPond(this, random.nextInt(1) + 1);
                this.setElement(random.nextInt(BunkerManager.BUNKER_SIZE_TILES), random.nextInt(BunkerManager.BUNKER_SIZE_TILES), element);
            }
            if (t == 3) {
                BunkerElement element = new ElementStump(this, random.nextInt(2) + 1);
                this.setElement(random.nextInt(BunkerManager.BUNKER_SIZE_TILES), random.nextInt(BunkerManager.BUNKER_SIZE_TILES), element);
            }
        }
    }

    public Bunker(GravSerializer serializer) {
        if (serializer != null) {
            deserialize(serializer);
        }
    }

    public static void setWorld(Bunker bunker, BunkerWorld world) {
        bunker.world = world;
    }

    public List<Worker> getWorkers() {
        List<Worker> list = new ArrayList<>();
        for (ElementWorkerHut elementWorkerHut : tileMap.byClass(ElementWorkerHut.class)) {
            if (elementWorkerHut.isEnabled())
                if (elementWorkerHut.getWorker() != null)
                    list.add(elementWorkerHut.getWorker());
        }
        return list;
    }

    public Worker getFreeWorker() {
        for (ElementWorkerHut elementWorkerHut : tileMap.byClass(ElementWorkerHut.class)) {
            if (elementWorkerHut.isEnabled() && !elementWorkerHut.getWorker().isWorking()) {
                return elementWorkerHut.getWorker();
            }
        }
        return null;
    }

    public ElementCore getCore() {
        return tileMap.byClass(ElementCore.class).get(0);
    }

    /**
     * Call after initialization/generation - Enables all elements that were enabled before last disable()
     */
    public void enable() {
        if(!Bukkit.isPrimaryThread())
            throw new RuntimeException("Bunker::enable must be called in main thread!");
        BunkerEnableEvent event = new BunkerEnableEvent(this, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        this.getTileMap().getElements().forEach(e -> {
            if (e.getGenerationSettings().isNeedsEnabling()) {
                try {
                    e.enable();
                } catch (Exception ex) {
                    System.out.println("Could not enable element of type " + e.getType());
                    ex.printStackTrace();
                }
            }
        });
        EventSubscriptions.instance.subscribe(this);
    }

    /**
     * Call before serialization - Disables all elements
     */
    public void disable() {
        if(!Bukkit.isPrimaryThread())
            throw new RuntimeException("Bunker::enable must be called in main thread!");
        BunkerDisableEvent event = new BunkerDisableEvent(this, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        this.getTileMap().getElements().forEach(e -> {
            if (e.isEnabled()) {
                e.disable();
            }
        });
        EventSubscriptions.instance.unSubscribeAll(this);
    }

    /**
     * Gets the schematic for the map of this bunker
     */
    public Schematic getMapSchematic() {
        return BunkerSchematics.getWithoutThrow("maps/" + this.mapSchematicName);
    }

    /**
     * Check if a BunkerElement can be placed at a certain position
     *
     * @param ex      Tile X Position
     * @param ez      Tile Z Position
     * @param element BunkerElement to check for
     * @return {@code true} if there are no other elements within the space
     */
    public boolean canPlace(int ex, int ez, BunkerElement element) {
        return getTileMap().canPlace(ex, ez, element);
    }

    public BunkerElement getElement(int ex, int ez) {
        return getTileMap().byPosition(new IntVector2D(ex, ez));
    }

    public BunkerElement getElement(IntVector2D vec) {
        return getTileMap().byPosition(vec);
    }

    public boolean setElement(IntVector2D position, BunkerElement el) {
        return setElement(position.getX(), position.getY(), el);
    }

    /**
     * Set an element at a specific position
     *
     * @param ex      Tile X Position
     * @param ez      Tile Z Position
     * @param element BunkerElement to place
     * @return true if successful false if an element already occupies that space
     */
    public boolean setElement(int ex, int ez, BunkerElement element) {
        return getTileMap().setElement(ex, ez, element);
    }

    /**
     * Remove the element at the specified position
     *
     * @param position The position
     */
    public void removeElement(IntVector2D position) {
        removeElement(position.getX(), position.getY());
    }

    /**
     * Remove the element at the specified position
     *
     * @param ex The x position
     * @param ez The z or y position... the second position value
     */
    public void removeElement(int ex, int ez) {
        getTileMap().removeElement(ex, ez);
    }

    private void deserialize(GravSerializer serializer) {
        this.seed = serializer.readLong();
        UUID gangId = serializer.readUUID();
        this.gang = GangManager.instance.getOrLoadGang(gangId);
        this.id = gangId;
        tileMap = serializer.readObject(this);
        previousLocations = serializer.readObject();
        this.mapSchematicName = serializer.readString();
        this.meta = serializer.readObject();

        this.skillManager = this.meta.getOrSet("skillManager", new SkillManager(this), this);
        this.matchStatistics = this.meta.getOrSet("matchStatistics", new BunkerMatchStatistics());
        this.npcSkillManager = this.meta.getOrSet("npcSkillManager", new BunkerNPCSkillManager(this), this);
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeLong(this.seed);
        serializer.writeUUID(this.getId());
        serializer.writeObject(this.tileMap);
        serializer.writeObject(this.previousLocations);
        serializer.writeString(mapSchematicName);

        this.meta.set("skillManager", this.skillManager);
        this.meta.set("matchStatistics", this.matchStatistics);
        this.meta.set("npcSkillManager", this.npcSkillManager);
        serializer.writeObject(meta);
    }

    /**
     * Get the id of this bunker
     *
     * @return The id of this bunker
     */
    public UUID getId() {
        return id;
    }

    public synchronized Future<Void> unload() {
        return this.unload(true);
    }

    /**
     * Call asynchronously
     */
    public synchronized Future<Void> unload(boolean save) {
        if (save)
            return BunkerManager.instance.saveAndUnloadBunker(this);
        return BunkerManager.instance.unloadBunker(this);
    }

    /**
     * Teleport someone into this bunker
     */
    public void teleport(Player player) {
        if (player.getLocation().getWorld().getName().equalsIgnoreCase(world.getName())) {
            return;
        }
        Vector3D tpLoc = previousLocations.get(player.getUniqueId());
        previousLocations2.put(player.getUniqueId(), player.getLocation());
        System.out.println("My world's id is " + this.getWorld().getBukkitWorld().getUID().toString());
        if (tpLoc != null) {
            player.teleport(tpLoc.toBukkitVector().toLocation(this.getWorld().getBukkitWorld()));
        } else {
            player.teleport(new Location(this.getWorld().getBukkitWorld(), this.getWorld().getSizeChunks() * 8, 100, this.getWorld().getSizeChunks() * 8));
        }
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    /**
     * Teleport a player to the last known teleport from location, according to this bunker's cache
     *
     * @param player The player
     * @return Whether the teleportation was successful
     */
    public boolean teleportBack(Player player) {
        if (!player.getLocation().getWorld().getName().equalsIgnoreCase(world.getName())) {
            Bukkit.broadcastMessage("Player World: " + player.getLocation().getWorld().getName() + " is not bunker");
            return false;
        }
        Location tpLoc = previousLocations2.get(player.getUniqueId());
        if (tpLoc == null)
            return false;
        previousLocations.put(player.getUniqueId(), Vector3D.fromBukkitVector(player.getLocation().toVector()));
        player.teleport(tpLoc);
        return true;
    }

    /**
     * Get an element shop for a specific tile
     *
     * @param tileLocation the tile location
     */
    public BunkerShop getElementShop(IntVector2D tileLocation) {
        return new BunkerElementShop(this, tileLocation);
    }

    public void setPreviousLocation(UUID id, Vector3D loc) {
        previousLocations.put(id, loc);
    }

    public void setPreviousLocation2(UUID id, Location loc) {
        previousLocations2.put(id, loc);
    }

    /**
     * Get the rating of this bunker.
     * The rating of a bunker is a value between 0 and 10 and
     * is calculated from many variables such as amount of elements, level of elements,
     * and win rates of the bunker
     *
     * @return The rating of this bunker
     */
    public double getRating() {
        AvgMeasure measure = new AvgMeasure(0, 0);

        //Elements
        List<BunkerElement> elements = getTileMap().getElements();
        elements.removeIf(e -> e instanceof NaturalElement || e instanceof ElementWorkerHut);

        for (BunkerElement element : elements) {
            if (element instanceof NaturalElement || element instanceof ElementWorkerHut)
                continue;
            double effectiveLevel = element.getLevel() / (double) element.getMaxLevel() * 10D;
            if (element instanceof StorageElement && (element.getType() == null || !element.getType().equals(BunkerElementType.ESSENTIAL_CORE))) {
                ((StorageElement) element).getStorageList().forEach(s -> measure.addEntry(effectiveLevel + (s.getAmount() / s.getCap())));
            } else {
                double weight = 1D;
                if (element instanceof ElementWall)
                    weight = 0.1D;
                if (element instanceof ElementCore)
                    weight = Math.max(elements.size() / 15D, 1D);
                measure.addEntry(effectiveLevel, weight);
            }
        }

        //Win rate
        measure.addEntry(Math.min(matchStatistics.getWinRate() * measure.getAvg() + 1, 10), 4.0D);

        return MathUtils.round(measure.getAvg(), 2);
    }

    public void tick() {
        tileMap.tick();
        npcManager.tick();
        infoMessageHandler.tick();
        for (Worker worker : getWorkers()) {
            if (worker.isWorking()) {
                Task task = worker.getTask();
                task.updateHolo();
                if (!task.isFinished())
                    task.update();
            }
        }

        // Envoys
        ENVOYS:
        if (MathUtils.isRandom(1, 69420 * 2.5)) {
            ElementEnvoy envoy = new ElementEnvoy(null, this);
            IntVector2D pos = tileMap.getRandomPlaceablePosition(envoy.getShape());
            if (pos == null) {
                break ENVOYS; // Cannot spawn envoy
            }
            tileMap.setElement(pos.getX(), pos.getY(), envoy);
            envoy.build();

            getWorld().getBukkitWorld().getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ARMORSTAND_BREAK, 1f, 1f));
        }

        //Rebuild elements
        if (destroyedTicksLeft > 0 && --destroyedTicksLeft <= 0) {
            getTileMap().getElements().forEach(e -> {
                if (e.isDestroyed()) {
                    e.unDestroy();
                }
                if (e.getHealth() != e.getMaxHealth()) {
                    e.setHealth(e.getMaxHealth());
                }
            });
        }

        //Particles
        if(particleCounter.incrementAndGet() == 7) {
            particleCounter.set(0);
            if (getWorld().getBukkitWorld() != null) {
                for (Player player : getWorld().getBukkitWorld().getPlayers()) {
                    ItemStack stack = player.getInventory().getItemInMainHand();
                    if (stack != null && ToolUtils.isDefaultTool(stack)) {
                        IntVector2D tileLoc = getWorld().getTileAt(player.getLocation());
                        ParticleShape shape = ParticleUtils.createSquare(getWorld().getBukkitWorld(), Particle.VILLAGER_HAPPY, new Vector3D(getTileMap().getTileLocation(tileLoc)), new Vector3D(getTileMap().getTileLocation(tileLoc).add(BunkerManager.TILE_SIZE_BLOCKS, 0, BunkerManager.TILE_SIZE_BLOCKS)));
                        shape.draw();
                    }
                }
            }
        }
    }

    private AtomicInteger particleCounter = new AtomicInteger();

    public boolean shouldTick() {
        return true;
    }

    /**
     * Add an amount of a type of resource to this bunker's stores
     *
     * @param generatingType The type of resource
     * @param amount         The amount of resources
     */
    public void addResource(BunkerResource generatingType, double amount) {

        resourceLock.lock();
        try {
            double addAm = amount;
            for (StorageElement el : getTileMap().byClass(StorageElement.class)) {
                if (addAm <= 0) {
                    break;
                }
                if (el.getStorage(generatingType) != null) {
                    double addable = Math.min(el.getStorage(generatingType).getCap() - el.getHolding(generatingType), addAm);
                    if (addable > 0) {
                        el.addHolding(generatingType, addable);
                        addAm -= addable;
                    }
                }
            }
        } finally {
            resourceLock.unlock();
        }
    }

    /**
     * Get a map of all the available resources this bunker has
     */
    public Map<BunkerResource, Double> getResources() {

        resourceLock.lock();
        try {
            Map<BunkerResource, Double> ret = new HashMap<>();
            for (StorageElement el : getTileMap().byClass(StorageElement.class)) {
                for (Storage storage : el.getStorageList()) {
                    ret.put(storage.getResource(), ret.getOrDefault(storage.getResource(), 0D) + storage.getAmount());
                }
            }
            return ret;
        } finally {
            resourceLock.unlock();
        }
    }

    /**
     * Get a list (copy) of all the available NPCs this bunker has
     */
    public List<BunkerNPC> getAvailableNPCs() {
        List<BunkerNPC> npcList = new ArrayList<>();
        for (ElementArmyCamp camps : getTileMap().byClass(ElementArmyCamp.class)) {
            npcList.addAll(camps.getNPCs());
        }
        return npcList;
    }

    /**
     * Get and remove an npc of some type from this bunker's stores
     *
     * @param type The type of npc
     * @return The npc
     */
    public BunkerNPC getAndRemoveNPC(BunkerNPCType type) {
        for (ElementArmyCamp camps : getTileMap().byClass(ElementArmyCamp.class)) {
            BunkerNPC npc = camps.removeOne(type);
            if (npc != null)
                return npc;
        }
        return null;
    }

    /**
     * Get the amount of a specified type of npc that this bunker has available
     *
     * @param type The type of npc
     */
    public int getAvailableNPCCount(BunkerNPCType type) {
        return (int) getAvailableNPCs().stream()
                .filter(npc -> npc.getType().equals(type))
                .count();
    }

    public Map<BunkerResource, Storage> getCombinedStorages() {
        resourceLock.lock();
        try {
            Map<BunkerResource, Storage> storages = new HashMap<>();
            for (StorageElement el : getTileMap().byClass(StorageElement.class)) {
                for (Storage storage : el.getStorageList()) {
                    if (!storages.containsKey(storage.getResource())) {
                        storages.put(storage.getResource(), new Storage(storage.getResource(), 0, 0));
                    }
                    storages.get(storage.getResource()).addStorage(storage);
                }
            }
            return storages;
        } finally {
            resourceLock.unlock();
        }
    }

    public BunkerArmy getArmy() {
        ElementableList<BunkerNPC> npcs = new ElementableList<>();
        npcs.addAllLists(getTileMap().byClass(ElementArmyCamp.class), ElementArmyCamp::getNPCs);
        return new BunkerArmy(this, npcs);
    }

    public double getResourceAmount(BunkerResource resource) {
        resourceLock.lock();
        try {
            if (getResources().containsKey(resource))
                return getResources().get(resource);
            return 0;
        } finally {
            resourceLock.unlock();
        }
    }

    public void removeResources(BunkerResource resource, double amount) {
        resourceLock.lock();
        try {
            for (StorageElement el : getTileMap().byClass(StorageElement.class)) {
                if (amount <= 0) {
                    break;
                }
                if (el.getStorage(resource) != null) {
                    double removable = Math.min(el.getHolding(resource), amount);
                    if (removable > 0) {
                        el.addHolding(resource, -removable);
                        amount -= removable;
                    }
                }
            }
        } finally {
            resourceLock.unlock();
        }
    }

    public void removeResources(Storage... storages) {
        resourceLock.lock();
        try {
            for (Storage storage : storages) {
                removeResources(storage.getResource(), storage.getAmount());
            }
        } finally {
            resourceLock.unlock();
        }
    }

    public boolean hasResources(Storage... storages) {
        resourceLock.lock();
        try {
            for (Storage storage : storages) {
                if (this.getResourceAmount(storage.getResource()) < storage.getAmount())
                    return false;
            }
            return true;
        } finally {
            resourceLock.unlock();
        }
    }

    //

    /**
     * Gets the players in the world that holds this bunker
     */
    public List<Player> getContainingPlayers() {
        return this.getWorld().getBukkitWorld().getPlayers();
    }

    //Matches

    /**
     * Returns the match in which this bunker is defending if any exist
     */
    public Match getDefendingMatch() {
        return BunkerMatchMaker.instance.getMatchFromDefender(this);
    }

    /**
     * Returns the match in which this bunker is defending if any exist
     */
    public Match getAttackingMatch() {
        return BunkerMatchMaker.instance.getMatchFromAttacker(this);
    }

    //Messaging
    public void messageMembers(String message) {
        if (gang == null)
            return;
        String messageToSend = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Bunker " + ChatColor.WHITE + "▶ " + ChatColor.translateAlternateColorCodes('&', message);
        gang.getMembers().forEach(m -> {
            if (m == null || m.getMember() == null) {
                return;
            }
            Player player = Bukkit.getPlayer(m.getMember());
            if (player == null) {
                return;
            }
            player.sendMessage(messageToSend);
        });
    }

    /**
     * Message all players in the bunker world
     *
     * @param message The message to send
     */
    public void messageWorld(String message) {
        String messageToSend = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Bunker " + ChatColor.WHITE + "▶ " + ChatColor.translateAlternateColorCodes('&', message);
        getWorld().getBukkitWorld().getPlayers().forEach(p -> p.sendMessage(messageToSend));
    }

    /**
     * Send a message to all gang members in the bunker world
     *
     * @param message Message to send to all gang members
     */
    public void messageMembersInWorld(String message) {
        if (gang == null)
            return;
        String messageToSend = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Bunker " + ChatColor.WHITE + "▶ " + ChatColor.translateAlternateColorCodes('&', message);
        gang.getMembers().forEach(m -> {
            if (getWorld().getBukkitWorld().getPlayers().contains(Bukkit.getPlayer(m.getMember())))
                Bukkit.getPlayer(m.getMember()).sendMessage(messageToSend);
        });
    }

    /**
     * Send a message to all gang members in the bunker world
     *
     * @param message Message to send to all gang members
     */
    public void messageMembersInWorld(ChatBuilder message) {
        if (gang == null)
            return;
        gang.getMembers().forEach(m -> {
            Player pl = Bukkit.getPlayer(m.getMember());
            if (pl == null)
                return;
            if (getWorld().getBukkitWorld().getPlayers().contains(pl))
                message.send(pl);
        });
    }

    /**
     * Send a message to a specific player
     *
     * @param player  Player to send a message to
     * @param message Message to send to the player
     */
    public void messageMember(Player player, String message) {
        ChatBuilder.prefix( "&d&lBunker &f▶ ")
                .addText(message)
                .send(player);
    }

    /**
     * Send the messages that are sent when you first go to your bunker
     */
    public void sendExplanationMessages() {
        ChatBuilder chat = new ChatBuilder("\n\n&f&lWelcome to your bunker!\n")
                .addText("&7Bunkers is a new game similar to ")
                .addText("&eClash of Clans", HoverUtil.text("&7Read more"), ClickUtil.url("https://supercell.com/en/games/clashofclans/"))
                .addText("&7.\n")
                .addText("&7The aim is to build up your army in order to attack other bunkers\n")
                .addText("&7while also building up your defenses in case of an attack.\n")
                .addText("&7To start off, grab an edit wand from ")
                .addText("&e/gang bunker", HoverUtil.text("&7Click to run!"), ClickUtil.command("/gang bunker"))
                .addText("&7 and begin\n")
                .addText("&7by breaking down some rocks and trees.\n\n")
                .addText("&eIn order to open your core's menu, sneak right click it\n\n");
        messageMembersInWorld(chat);
    }

    //Save match information
    @EventSubscription
    private void onMatchEnd(BunkerMatchEndEvent event) {
        if (event.getMatch().getDefender() == this ||
                event.getMatch().getAttacker() == this) {
            this.matchStatistics.addMatch(event.getMatch().getMatchInfoSnapshot());
        }
    }
}
