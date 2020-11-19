package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementGate;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementWall;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;

import java.util.Arrays;
import java.util.List;

public class WallRotation {
    public static WallParameter get(BunkerElement[] neighbours, Class<? extends BunkerElement> connectable) {
        int[] bls = Arrays.stream(neighbours)
                .mapToInt(n -> {
                    if (n == null || connectable.isInstance(n))
                        return 0;
                    return 1;
                }).toArray();
        return WallType.getWall(bls);
    }

    public static WallParameter get(BunkerElement[] neighbours, List<Class<? extends BunkerElement>> connectables) {
        if (connectables.size() == 1) {
            return get(neighbours, connectables.get(0));
        }
        int[] bls = Arrays.stream(neighbours)
                .mapToInt(n -> {
                    if (n == null)
                        return 0;
                    for (Class<? extends BunkerElement> clazz : connectables) {
                        if (clazz.isInstance(n)) {
                            return 1;
                        }
                    }
                    return 0 ;
                }).toArray();
        return WallType.getWall(bls);
    }
}
