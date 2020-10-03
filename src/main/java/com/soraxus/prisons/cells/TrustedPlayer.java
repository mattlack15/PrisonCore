package com.soraxus.prisons.cells;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class TrustedPlayer implements GravSerializable {
    @Setter
    public String name;
    public UUID id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrustedPlayer that = (TrustedPlayer) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    public static TrustedPlayer deserialize(GravSerializer serializer) {
        return new TrustedPlayer(serializer.readString(), serializer.readUUID());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeString(name);
        serializer.writeUUID(id);
    }
}
