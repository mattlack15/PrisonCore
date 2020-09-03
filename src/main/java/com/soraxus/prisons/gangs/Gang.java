package com.soraxus.prisons.gangs;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.gangs.cmd.CmdGang;
import com.soraxus.prisons.util.maps.MapUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Gang {
    @Getter
    private UUID id = UUID.randomUUID();
    private String name;
    private String description = "No description :/";
    private List<GangMember> cachedMembers = new ArrayList<>();
    private Map<String, String> invited = new HashMap<>();
    private GangManager parent;

    protected Gang(GangManager parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public synchronized boolean hasBunker() {
        return getBunker() != null;
    }

    public synchronized Bunker getBunker() {
        return BunkerManager.instance.getLoadedBunker(this.getId());
    }

    public synchronized CompletableFuture<Bunker> loadBunker() {
        return BunkerManager.instance.createOrLoadBunker(this);
    }

    public synchronized CompletableFuture<Bunker> getOrLoadBunker() {
        Bunker b = getBunker();
        if (b == null) {
            return loadBunker();
        }
        CompletableFuture<Bunker> f = new CompletableFuture<>();
        f.complete(b);
        return f;
    }

    protected static Gang fromSection(ConfigurationSection section, GangManager parent, GangMemberManager memberManager) {
        UUID id = UUID.fromString(section.getString("id"));
        String name = section.getString("name");
        String description = section.getString("description");
        Map<String, String> invited = MapUtil.stringToMap(section.getString("invited"));
        Gang gang = new Gang(parent, name);
        gang.description = description;
        gang.id = id;
        gang.invited = invited;

        //Load members
        for (String ids : section.getStringList("members")) {
            UUID memberId = UUID.fromString(ids);
            gang.cachedMembers.add(memberManager.getOrLoadMember(memberId));
        }

        return gang;
    }

    public synchronized List<GangMember> getMembers() {
        return new ArrayList<>(cachedMembers);
    }

    public synchronized String getName() {
        return this.name;
    }

    public synchronized void setName(String name) {
        String oldName = this.getName();
        this.name = name;
        parent.setGangNameInIndex(oldName, null);
        parent.setGangNameInIndex(name, this.getId());
        parent.saveGang(this);
    }

    public synchronized String getDescription() {
        return this.description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
        parent.saveGang(this);
    }

    //Invitations
    public synchronized void invite(UUID player, String playerName) {
        this.invited.put(player.toString(), playerName);
    }

    public synchronized boolean isInvited(UUID player) {
        return this.invited.containsKey(player.toString());
    }

    public synchronized boolean isInvited(String playerName) {
        for (String names : invited.values())
            if (names.equalsIgnoreCase(playerName))
                return true;
        return false;
    }

    public synchronized String checkInvited(String playerName) {
        for (String n : invited.values())
            if (n.equalsIgnoreCase(playerName))
                return n;
        return null;
    }

    public synchronized void unInvite(UUID player) {
        this.invited.remove(player.toString());
    }

    public synchronized void unInvite(String playerName) {
        for (Map.Entry<String, String> entry : new ArrayList<>(invited.entrySet())) {
            if (entry.getValue().equalsIgnoreCase(playerName)) {
                this.invited.remove(entry.getKey());
            }
        }
    }

    public synchronized Map<UUID, String> getInvitedPlayers() {
        Map<UUID, String> map = new HashMap<>();
        for (Map.Entry<String, String> entry : new ArrayList<>(invited.entrySet())) {
            map.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        return map;
    }

    //Messaging
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
    public synchronized GangRelation getRelationTo(UUID gang) {
        return GangRelationsManager.instance.getRelation(this.getId(), gang);
    }

    public synchronized void setRelation(UUID gang, GangRelation relation) {
        GangRelationsManager.instance.setRelation(this.getId(), gang, relation);
    }

    //Cached Members maintenance
    protected synchronized void setCachedMembers(List<GangMember> members) {
        this.cachedMembers = members;
    }

    public synchronized void addMember(GangMember member) {
        if (member == null)
            return;
        if (this.getMembers().contains(member))
            return;
        if (!this.getId().equals(member.getGang())) {
            cachedMembers.add(member);
            member.setGang(this.getId());
            parent.saveGang(this);
        }
    }

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
        section.set("invited", MapUtil.mapToString(invited));
        List<String> ids = new ArrayList<>();
        this.cachedMembers.forEach(m -> ids.add(m.getMember().toString()));
        section.set("members", ids);
    }

    public synchronized void disband() {
        parent.disbandGang(this.getId());
    }

    private volatile boolean disbanded = false;

    public synchronized void onDisband() {
        disbanded = true;
        this.getMembers().forEach(this::removeMember);
        //Todo delete bunker
    }

}
