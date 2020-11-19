package com.soraxus.prisons.gangs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

@AllArgsConstructor
public class GangMember {
    private GangMemberManager parent;
    @Getter
    private UUID gang;
    @Getter
    private String memberName;
    @Getter
    private UUID member;
    @Getter
    private GangRole gangRole;

    public static GangMember fromSection(ConfigurationSection section, GangMemberManager parent) {
        UUID gang = null;
        if (section.getString("gang") != null)
            gang = UUID.fromString(section.getString("gang"));
        UUID member = UUID.fromString(section.getString("member"));
        String name = section.getString("name");
        GangRole gangRole1 = GangRole.MEMBER;
        try {
            gangRole1 = GangRole.values()[section.getInt("gangRole")];
        } catch (Exception e) {
            System.out.println("Warning: Gang Member had invalid gangRole for member: " + member);
        }
        return new GangMember(parent, gang, name, member, gangRole1);
    }

    public synchronized void setGang(UUID gangId) {
        UUID oldGangId = this.gang;
        this.gang = gangId;

        //Gang Cache Maintenance
        if (oldGangId != null) {
            Gang oldGang = GangManager.instance.getLoadedGang(oldGangId);
            if (oldGang != null)
                oldGang.removeMember(this);
        }

        if (gangId != null) {
            Gang newGang = GangManager.instance.getLoadedGang(gangId);
            if (newGang != null)
                newGang.addMember(this);
        }

        //Set role to base role if null
        if (gangId == null)
            setGangRole(GangRole.values()[0]);

        //Queue a save op
        parent.saveMember(this);
    }

    public synchronized void setGangRole(GangRole rank) {
        this.gangRole = rank;
        if (!rank.isDuplicatable()) {
            Gang gang = GangManager.instance.getLoadedGang(this.gang);
            if (gang != null) {
                gang.getMembers().forEach(m -> {
                    if (m.getGangRole().equals(rank) && !m.getMember().equals(member))
                        m.setGangRole(GangRole.values()[rank.ordinal() - 1 < 0 ? 1 : rank.ordinal() - 1]);
                });
            }
        }
        parent.saveMember(this);
    }

    public synchronized void toSection(ConfigurationSection section) {
        if (gang != null)
            section.set("gang", gang.toString());
        section.set("gangRole", gangRole.ordinal());
        section.set("member", member.toString());
        section.set("name", memberName);
    }
}
