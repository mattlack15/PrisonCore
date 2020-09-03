package com.soraxus.prisons.bunkers.base.resources;

import com.soraxus.prisons.bunkers.base.Bunker;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkillManager implements GravSerializable {
    private final Bunker bunker;
    private Map<Skill, Integer> skills;

    public SkillManager(Bunker bunker) {
        this.bunker = bunker;
        this.skills = new ConcurrentHashMap<>();
    }

    public SkillManager(GravSerializer serializer, Bunker bunker) {
        this(bunker);
        this.skills = serializer.readObject();
    }

    public int getSkillLevel(Skill skill) {
        return skills.getOrDefault(skill, 1);
    }

    public void setSkillLevel(Skill skill, int level) {
        skills.put(skill, level);
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeObject(skills);
    }
}
