package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.ModuleBunkers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.ObjectDeserializationException;
import net.ultragrav.utils.IntVector2D;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class Tile implements GravSerializable {
    private Bunker bunker;
    private BunkerElement parent; //Will only be null in like 1 specific case
    private IntVector2D internalPosition;

    /**
     * Load a tile from a serializer
     *
     * @param bunker     Parent bunker
     * @param serializer Serializer
     */
    public Tile(GravSerializer serializer, Bunker bunker) {
        this.bunker = bunker;
        try {
            this.parent = serializer.readObject(bunker);
        } catch (ObjectDeserializationException e) {
            if (e.getDeserializationCause() != ObjectDeserializationException.DeserializationExceptionCause.CLASS_NOT_FOUND)
                throw e;
            this.parent = null;
        }
        this.internalPosition = serializer.readObject();
        if (parent == null) //This is technically redundant depending on how its used but I just want to have it here
            internalPosition = new IntVector2D(0, 0);
    }

    /**
     * Save this tile to a serializer
     *
     * @param serializer Serializer
     */
    @Override
    public void serialize(GravSerializer serializer) {
        if (internalPosition.getX() == 0 && internalPosition.getY() == 0) {
            try {
                serializer.writeMark();
                long ms = System.currentTimeMillis();
                serializer.writeObject(parent);
                ms = System.currentTimeMillis() - ms;
                if (ms > 10) {
                    ModuleBunkers.messageDevs("&cElement of type " + parent.getType().toString() + " took unusually long (&7" + ms + "ms&c) to serialize");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ModuleBunkers.messageDevs("Error while serializing element of type " + (parent != null ? parent.getType() : "null (non existent)") + " see console!");
                ModuleBunkers.messageDevs("Element will be removed from the bunker as a precaution!");
                serializer.writeReset();
                serializer.writeObject(null);
            }
        } else {
            serializer.writeObject(null);
        }
        serializer.writeObject(internalPosition);
    }

    @NotNull
    public IntVector2D getPosition() {
        return parent.getPosition().add(internalPosition);
    }
}
