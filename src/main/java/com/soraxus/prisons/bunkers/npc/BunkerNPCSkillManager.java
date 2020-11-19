package com.soraxus.prisons.bunkers.npc;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BunkerNPCSkillManager implements GravSerializable {
    private Map<BunkerNPCType, Integer> levelMap = new ConcurrentHashMap<>();

    @Getter
    private final Bunker parent;

    public BunkerNPCSkillManager(Bunker parent) {
        this.parent = parent;
    }

    public BunkerNPCSkillManager(GravSerializer serializer, Bunker parent) {
        this.parent = parent;
        serializer.readInt();
        this.levelMap = new ConcurrentHashMap<>(serializer.readObject());
    }

    public int getLevel(BunkerNPCType type) {
        Integer result = levelMap.get(type);
        if (result == null)
            return 1;
        return result;
    }

    public void setLevel(BunkerNPCType type, int level) {
        this.levelMap.put(type, level);
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeInt(1); //Version number
        gravSerializer.writeObject(levelMap);
    }
}
