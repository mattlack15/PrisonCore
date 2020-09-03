package com.soraxus.prisons.pickaxe.crystals;

import com.soraxus.prisons.util.CastUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.ultragrav.serializer.GravSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrystalInfo {
    private List<Crystal> crystals = new ArrayList<>(6);

    private void clearCrystals() {
        crystals = new ArrayList<>(6);
        for(int i = 0; i < 6; i++)
            crystals.add(null); // Create empty slots
    }

    public void read(GravSerializer s) {
        this.crystals = CastUtil.cast(s.readObject());
    }

    public void write(GravSerializer s) {
        s.writeObject(this.crystals);
    }

    public Map<CrystalType, Double> totalPercent() {
        Map<CrystalType, Double> ret = new HashMap<>();
        for (Crystal cl : crystals) {
            if (cl == null) {
                continue;
            }
            ret.put(cl.getType(), ret.getOrDefault(cl.getType(), 0D) + cl.getPercent());
        }
        return ret;
    }
}
