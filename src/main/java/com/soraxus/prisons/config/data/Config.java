package com.soraxus.prisons.config.data;

import com.soraxus.prisons.util.list.ElementableList;
import com.soraxus.prisons.util.menus.Menu;
import net.ultragrav.serializer.Meta;
import org.bukkit.entity.HumanEntity;

import java.util.List;

@SuppressWarnings("unchecked")
public class Config extends Base {
    private final ElementableList<ConfigValue<?>> values = new ElementableList<>();

    public Config(String name, String[] description) {
        super(name, description);
    }

    void load(Meta meta) {
        Meta sub = meta.getOrSet(getName(), new Meta());
        for (ConfigValue<?> val : values) {
            val.load(sub);
        }
    }

    void save(Meta meta) {
        Meta sub = new Meta();
        for (ConfigValue<?> val : values) {
            val.save(sub);
        }
        meta.set(getName(), new Meta());
    }

    public <T> ConfigValue<T> getValue(String name) {
        return (ConfigValue<T>) values.byFunction(name, ConfigValue::getName);
    }

    public Menu createMenu() {
        if (values.size() > 9*6) {
            // TODO: Paged layout?
            return null;
        } else {
            return new Menu(getName(), (int) Math.ceil(values.size() / 9D)) {
                @Override
                public void open(HumanEntity p, Object... data) {
                    super.open(p, data);
                }

                void setup() {
                    for (int i = 0; i < values.size(); i ++) {
                        setElement(i, values.get(i).createElement());
                    }
                }
            };
        }
    }
}