package com.soraxus.prisons.config;

import com.soraxus.prisons.config.data.Section;
import com.soraxus.prisons.util.list.ElementableList;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.Meta;

public class ConfigManager {
    public static ConfigManager instance;

    private ModuleConfig parent;

    private ElementableList<Section> configs = new ElementableList<Section>();

    public ConfigManager(ModuleConfig parent) {
        this.parent = parent;
        instance = this;
    }

    public void saveAll() {
        for (Section section : configs) {
            Meta meta = new Meta();
            section.save(meta);
            meta.asJson(); // TODO: Mongo support

        }
    }
}
