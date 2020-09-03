package com.soraxus.prisons.pickaxe.levels;

import com.soraxus.prisons.util.math.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.ultragrav.serializer.GravSerializer;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PickaxeLevelInfo {
    private int level;
    private int xp;

    public int getRequiredXp() {
        return PickaxeLevelManager.instance.getRequiredXp(level);
    }

    public double getPercentage() {
        return MathUtils.round(xp / (double) getRequiredXp(), 2);
    }

    public void increment(int amount) {
        xp += amount;
        while (xp + amount >= getRequiredXp()) {
            amount -= getRequiredXp();
            level++;
            xp = 0;
        }
    }

    public void write(GravSerializer s) {
        s.writeInt(level);
        s.writeInt(xp);
    }

    public void read(GravSerializer s) {
        this.level = s.readInt();
        this.xp = s.readInt();
    }
}
