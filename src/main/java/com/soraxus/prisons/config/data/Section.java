package com.soraxus.prisons.config.data;

import lombok.val;
import net.ultragrav.serializer.Meta;

import java.util.ArrayList;
import java.util.List;

public class Section extends Base {
    private List<Section> subsections = new ArrayList<>();
    private List<Config> configs = new ArrayList<>();

    public Section(String name, String[] description) {
        super(name, description);
    }

    public void load(Meta meta) {
        Meta sub = meta.getOrSet(getName(), new Meta());
        for (Section val : subsections) {
            val.load(sub);
        }
        for (Config val : configs) {
            val.load(sub);
        }
    }

    public void save(Meta meta) {
        Meta sub = new Meta();
        for (Section val : subsections) {
            val.save(sub);
        }
        for (Config val : configs) {
            val.save(sub);
        }
        meta.set(getName(), new Meta());
    }
}
