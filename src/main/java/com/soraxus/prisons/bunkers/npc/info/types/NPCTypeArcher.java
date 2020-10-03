package com.soraxus.prisons.bunkers.npc.info.types;

import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.info.BunkerNPCTypeInfo;
import com.soraxus.prisons.util.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class NPCTypeArcher implements BunkerNPCTypeInfo {
    @Override
    public int getGenerationTimeTicks(int level) {
        return 0;
    }

    @Override
    public Storage[] getCost(int level) {
        return new Storage[] {
                new Storage(BunkerResource.TIMBER, 0, 0),
                new Storage(BunkerResource.STONE, 0, 0),
        };
    }

    @Override
    public double getMaxHealth(int level) {
        return 50 * level;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public List<String> getUpgradePerks(int currentLevel) {
        List<String> l = new ArrayList<>();
        double health = getMaxHealth(currentLevel);
        double nextHealth = getMaxHealth(currentLevel+1);
        double dHealth = MathUtils.round(nextHealth - health, 1);
        l.add("&a+&f" + dHealth + " &chealth");
        l.add("&a+&f20% &9damage");
        if(currentLevel == 1) {
            l.add("&a+ &eArrow Barrage");
        } else if(currentLevel == 2) {
            l.add("&a+ &eExplosive Arrow");
        } else if(currentLevel == 3) {
            l.add("&a+ &ePoison Arrow");
        }

        return l;
    }
}
