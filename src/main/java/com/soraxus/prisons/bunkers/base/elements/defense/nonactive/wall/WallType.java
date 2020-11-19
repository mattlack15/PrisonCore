package com.soraxus.prisons.bunkers.base.elements.defense.nonactive.wall;

import com.soraxus.prisons.util.ArrayUtils;
import lombok.Getter;

@Getter
public enum WallType {
    ZERO(new int[]{0, 0, 0, 0}),
    ONE(new int[]{0, 1, 0, 0}),
    CORNER(new int[]{0, 1, 1, 0}),
    LINE(new int[]{0, 1, 0, 1}),
    T(new int[]{1, 1, 1, 0}),
    CROSS(new int[]{1, 1, 1, 1});

    private final int[][] sides;

    WallType(int[]... sides) {
        this.sides = sides;
    }

    public static WallParameter getWall(int[] in) {
        if (in.length != 4) {
            throw new IllegalArgumentException("Wall data array should be of length 4");
        }
        for (WallType type : values()) {
            int m = type.getMatch(in);
            if (m != -1) {
                return new WallParameter(type, m);
            }
        }
        throw new IllegalStateException("This shouldn't happen (WallType)");
    }

    public int getMatch(int[] in) {
        for (int i = 0; i < 4; i++) {
            for (int[] poss : sides) {
                if (ArrayUtils.equals(in, poss)) {
                    return i;
                }
            }
            ArrayUtils.shift(in);
        }
        return -1;
    }

    public String getSchematicName(int level) {
        return "wall-" + level + "-" + name().toLowerCase();
    }
}
