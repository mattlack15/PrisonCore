package com.soraxus.prisons.profiles;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProfileComment implements GravSerializable {
    private long creationTime;
    private String comment;
    private UUID commenter;
    private String commenterDisplayName;
    private boolean privateComment;

    public static ProfileComment deserialize(GravSerializer serializer) {
        return new ProfileComment(serializer.readLong(), serializer.readString(), serializer.readUUID(), serializer.readString(), serializer.readBoolean());
    }

    @Override
    public void serialize(GravSerializer serializer) {
        serializer.writeLong(creationTime);
        serializer.writeString(comment);
        serializer.writeUUID(commenter);
        serializer.writeString(commenterDisplayName);
        serializer.writeBoolean(privateComment);
    }
}
