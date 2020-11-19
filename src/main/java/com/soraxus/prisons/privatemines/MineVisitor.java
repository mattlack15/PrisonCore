package com.soraxus.prisons.privatemines;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MineVisitor implements GravSerializable {
    private static final int VERSION = 1; //For future backwards compatibility

    private final UUID visitor;
    private VisitationType visitationType = VisitationType.FREE;
    private final AtomicInteger currentSessionTicks = new AtomicInteger();

    public MineVisitor(@NotNull UUID visitor) {
        this.visitor = visitor;
    }

    public static MineVisitor deserialize(GravSerializer serializer) {
        int version = serializer.readInt();
        MineVisitor visitor = new MineVisitor(serializer.readUUID());
        visitor.visitationType = serializer.readObject();
        visitor.currentSessionTicks.set(serializer.readInt());
        return visitor;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeInt(VERSION);
        serializer.writeUUID(this.visitor);
        serializer.writeObject(this.visitationType);
        serializer.writeInt(this.currentSessionTicks.get());
    }
}
