package com.soraxus.prisons.gangs;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.economy.Economy;
import com.soraxus.prisons.gangs.cmd.CmdGang;
import com.soraxus.prisons.privatemines.PrivateMine;
import com.soraxus.prisons.privatemines.PrivateMineManager;
import com.soraxus.prisons.util.maps.MapUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Gang {
    @Getter
    private UUID id = UUID.randomUUID();
    private String name;
    private String description = "No description :/";
    @Getter
    @Setter
    private long xp = 0;
    private List<GangMember> cachedMembers = new ArrayList<>();
    private Map<String, String> invited = new HashMap<>();
    private GangManager parent;

    protected Gang(GangManager parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * @return Whether or not this gang has an associated bunker.
     */
    public synchronized boolean hasBunker() {
        return BunkerManager.instance.getFile(this.id).exists();
    }

    /**
     * Get the bunker associated with this gang if it is loaded.
     *
     * @return The bunker, if it is loaded.
     */
    public synchronized Bunker getBunker() {
        return BunkerManager.instance.getLoadedBunker(this.getId());
    }

    /**
     * Load the bunker associated with this gang
     */
    public synchronized CompletableFuture<Bunker> loadBunker() {
        return BunkerManager.instance.loadBunkerAsync(this.getId());
    }

    public CompletableFuture<Bunker> createBunkerAsync() {
        return BunkerManager.instance.createOrLoadBunker(this);
    }

    /**
     * Get the bunker associated with this gang, or load it if it is not loaded
     */
    public synchronized CompletableFuture<Bunker> getOrLoadBunker() {
        Bunker b = getBunker();
        if (b == null) {
            return loadBunker();
        }
        CompletableFuture<Bunker> f = new CompletableFuture<>();
        f.complete(b);
        return f;
    }

    //Private Mines

    public CompletableFuture<PrivateMine> createOrLoadMine() {
        return PrivateMineManager.instance.createOrLoadPrivateMineAsync(this);
    }

    public CompletableFuture<PrivateMine> loadMine() {
        return PrivateMineManager.instance.loadPrivateMineAsync(this);
    }

    public PrivateMine getMine() {
        return PrivateMineManager.instance.getLoadedPrivateMine(this.getId());
    }

    public void deleteMine() {
        PrivateMineManager.instance.deletePrivateMine(this.getMine());
    }

    // Eco

    public long getBalance() {
        return Economy.money.getBalance(this.getId());
    }

    public void setBalance(long balance) {
        Economy.money.setBalance(this.getId(), balance);
    }

    public void addBalance(long balance) {
        Economy.money.addBalance(this.getId(), balance);
    }

    public void removeBalance(long balance) {
        Economy.money.removeBalance(this.getId(), balance);
    }

    //

    protected static Gang fromSection(ConfigurationSection section, GangManager parent, GangMemberManager memberManager) {
        UUID id = UUID.fromString(section.getString("id"));
        String name = section.getString("name");
        String description = section.getString("description");
        int level = section.getInt("level");
        long xp = section.getLong("xp");
        Map<String, String> invited = MapUtil.stringToMap(section.getString("invited"));

        Gang gang = new Gang(parent, name);
        gang.description = description;
        gang.id = id;
        gang.invited = invited;
        gang.xp = xp;

        //Load members
        for (String ids : section.getStringList("members")) {
            UUID memberId = UUID.fromString(ids);
            gang.cachedMembers.add(memberManager.getOrLoadMember(memberId));
        }
        return gang;
    }

    /**
     * Get the cached members of this gang
     */
    public synchronized List<GangMember> getMembers() {
        return new ArrayList<>(cachedMembers);
    }

    /**
     * Get the name of this gang
     */
    public synchronized String getName() {
        return this.name;
    }

    /**
     * Set the name of this gang
     *
     * @param name the name to set to
     */
    public synchronized void setName(String name) {
        String oldName = this.getName();
        this.name = name;
        parent.uncacheGang(oldName);
        parent.cacheGang(this);
        parent.saveGang(this);
    }

    /**
     * Get the description of this gang
     *
     * @return the description of this gang
     */
    public synchronized String getDescription() {
        return this.description;
    }

    /**
     * Set the description of this gang
     *
     * @param description the new description of this gang
     */
    public synchronized void setDescription(String description) {
        this.description = description;
        parent.saveGang(this);
    }

    //Invitations

    /**
     * Invite a player to this gang, requires a player name
     *
     * @param player     The player's UUID
     * @param playerName The player's name
     */
    public synchronized void invite(UUID player, String playerName) {
        this.invited.put(player.toString(), playerName);
    }

    /**
     * Check if a player has been invited to this gang
     *
     * @param player The player's UUID
     * @return whether or not the player has been invited to this gang
     */
    public synchronized boolean isInvited(UUID player) {
        return this.invited.containsKey(player.toString());
    }

    /**
     * Check if a player has been invited to this gang
     *
     * @param playerName The player's name
     * @return whether or not the player has been invited to this gang
     */
    public synchronized boolean isInvited(String playerName) {
        for (String names : invited.values())
            if (names.equalsIgnoreCase(playerName))
                return true;
        return false;
    }

    /**
     * Check if a player has been invited to this gang, returning the capitalization-fixed version of the name
     *
     * @param playerName The player's name
     * @return null, if the player has not been invited to this gang, otherwise returns the capitalization-fixed version of the name
     */
    public synchronized String checkInvited(String playerName) {
        for (String n : invited.values())
            if (n.equalsIgnoreCase(playerName))
                return n;
        return null;
    }

    /**
     * Uninvite a player from this gang
     *
     * @param player The player
     */
    public synchronized void unInvite(UUID player) {
        this.invited.remove(player.toString());
    }

    /**
     * Uninvite a player from this gang
     *
     * @param playerName The player's name
     */
    public synchronized void unInvite(String playerName) {
        for (Map.Entry<String, String> entry : new ArrayList<>(invited.entrySet())) {
            if (entry.getValue().equalsIgnoreCase(playerName)) {
                this.invited.remove(entry.getKey());
            }
        }
    }

    /**
     * Get all the players that have been invited to this gang
     *
     * @return All the players that have been invited to this gang
     */
    public synchronized Map<UUID, String> getInvitedPlayers() {
        Map<UUID, String> map = new HashMap<>();
        for (Map.Entry<String, String> entry : new ArrayList<>(invited.entrySet())) {
            map.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        return map;
    }

    //Messaging

    /**
     * Broadcast a message to all players in this gang
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message) {
        message = CmdGang.PREFIX + ChatColor.GRAY + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        String finalMessage = message;
        getMembers().forEach(m -> {
            Player player = Bukkit.getPlayer(m.getMember());
            if (player != null) {
                player.sendMessage(finalMessage);
            }
        });
    }

    //Relations

    /**
     * Get the relation between this gang and another gang
     *
     * @param gang The other gang
     * @return The relation
     */
    public synchronized GangRelation getRelationTo(UUID gang) {
        return GangRelationsManager.instance.getRelation(this.getId(), gang);
    }

    /**
     * Set the relation between this gang and another gang
     *
     * @param gang     The other gang
     * @param relation The relation
     */
    public synchronized void setRelation(UUID gang, GangRelation relation) {
        GangRelationsManager.instance.setRelation(this.getId(), gang, relation);
    }

    //Cached Members maintenance
    protected synchronized void setCachedMembers(List<GangMember> members) {
        this.cachedMembers = members;
    }

    /**
     * Pretty much just a synchronization tool so that a member can't be added between checking isInvited and another thread adding a member
     *
     * @param member     The member to try to add
     * @param condition The condition
     * @return True if the member was added, false otherwise
     */
    public synchronized boolean addMemberWithCondition(GangMember member, Supplier<Boolean> condition) {
        if (!condition.get())
            return false;
        this.addMember(member);
        return true;
    }


    /**
     * Add a member to this gang
     *
     * @param member The member
     */
    public synchronized void addMember(GangMember member) {
        if (member == null)
            return;
        if (this.getMembers().contains(member))
            return;
        if (!this.getId().equals(member.getGang())) {
            cachedMembers.add(member);
            member.setGang(this.getId());
            parent.saveGang(this);
        } else {
            cachedMembers.add(member);
        }
    }

    /**
     * Remove a member from this gang
     *
     * @param member The member
     */
    public synchronized void removeMember(GangMember member) {
        if (member == null)
            return;
        while (this.getMembers().contains(member))
            cachedMembers.remove(member);
        if (this.getId().equals(member.getGang())) {
            member.setGang(null);
        }
        if (this.getMembers().size() == 0) {
            if (!disbanded)
                this.disband();
            return;
        }
        if (member.getGangRole().equals(GangRole.LEADER)) {
            List<GangMember> sorted = cachedMembers.stream()
                    .sorted(Comparator.comparingInt(m -> m.getGangRole().ordinal()))
                    .collect(Collectors.toList());
            sorted.get(sorted.size() - 1).setGangRole(GangRole.LEADER);
        }
        parent.saveGang(this);
    }

    //Persistence
    protected synchronized void toSection(ConfigurationSection section) {
        section.set("id", getId().toString());
        section.set("name", getName());
        section.set("description", getDescription());
        section.set("xp", xp);
        section.set("invited", MapUtil.mapToString(invited));
        List<String> ids = new ArrayList<>();
        this.cachedMembers.forEach(m -> ids.add(m.getMember().toString()));
        section.set("members", ids);
    }

    /**
     * Check if a player is a member of this gang
     *
     * @param player The player to check
     * @return Whether the player is a member of this gang
     */
    public synchronized boolean isMember(UUID player) {
        for (GangMember member : this.getMembers())
            if (member.getMember().equals(player))
                return true;
        return false;
    }

    /**
     * Disband this gang
     */
    public synchronized void disband() {
        parent.disbandGang(this.getId());
    }

    private volatile boolean disbanded = false;

    synchronized void onDisband() {
        disbanded = true;
        this.getMembers().forEach(this::removeMember);
        BunkerManager.instance.deleteBunkerAsync(this.id);
    }


    //XP

    /**
     * Add xp to this gang
     *
     * @param amount the amount of xp to add
     */
    public void addXp(long amount) {
        int old = getLevelInt();
        xp += amount;
        if (getLevelInt() > old) {
            this.broadcastMessage("§fYour gang leveled up to §5Level " + getLevelInt() + "§f!");
        }
    }

    public int getLevelInt() {
        return (int) Math.floor(getLevel());
    }

    public double getLevel() {
        return Math.cbrt(xp / 1000D);
    }
}
