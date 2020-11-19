package com.soraxus.prisons.ranks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PRankPlayer implements GravSerializable {

    private final UUID player;
    private int prestige;
    private int rankIndex;

    public static PRankPlayer deserialize(GravSerializer serializer, UUID player) {
        return new PRankPlayer(player, serializer.readInt(), serializer.readInt());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeInt(prestige);
        serializer.writeInt(rankIndex);
    }
}
