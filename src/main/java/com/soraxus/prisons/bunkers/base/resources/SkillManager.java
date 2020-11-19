package com.soraxus.prisons.bunkers.base.resources;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.util.maps.ConcurrentCustomMap;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

public class SkillManager implements GravSerializable {
    private final Bunker bunker;
    private ConcurrentCustomMap<Skill, Integer> skills;
    private ConcurrentCustomMap<Skill, Double> progress;

    public SkillManager(Bunker bunker) {
        this.bunker = bunker;
        this.skills = new ConcurrentCustomMap<>();
        this.progress = new ConcurrentCustomMap<>();
    }

    public SkillManager(GravSerializer serializer, Bunker bunker) {
        this(bunker);
        this.skills = new ConcurrentCustomMap<>(serializer.readObject());
    }

    public int getSkillLevel(Skill skill) {
        return skills.getOrSet(skill, 1);
    }

    public void setSkillLevel(Skill skill, int level) {
        skills.put(skill, level);
    }

    public void addSkillLevel(Skill skill, int amount) {
        getSkillLevel(skill);
        skills.computeIfPresent(skill, (k, v) -> v + amount);
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeObject(skills);
    }
}
