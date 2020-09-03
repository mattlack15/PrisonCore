package com.soraxus.prisons.bunkers.base.elements.defense.active.barracks;

import com.soraxus.prisons.bunkers.npc.info.BunkerNPCType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

@AllArgsConstructor
@Getter
public class ProcessNPCTraining implements GravSerializable {
    private final BunkerNPCType type;
    private int ticksLeft;
    private final int level;

    public synchronized boolean decrementTicks() {
        return --ticksLeft <= 0;
    }

    @Override
    public void serialize(GravSerializer gravSerializer) {
        gravSerializer.writeObject(type);
        gravSerializer.writeInt(ticksLeft);
        gravSerializer.writeInt(level);
    }

    public static ProcessNPCTraining deserialize(GravSerializer serializer) {
        return new ProcessNPCTraining(serializer.readObject(), serializer.readInt(), serializer.readInt());
    }
}
