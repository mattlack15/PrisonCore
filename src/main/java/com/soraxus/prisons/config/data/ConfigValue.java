package com.soraxus.prisons.config.data;

import com.soraxus.prisons.util.menus.MenuElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.ultragrav.serializer.Meta;

@Getter
public abstract class ConfigValue<T> extends Base {
    @Setter(AccessLevel.PROTECTED)
    private T value;

    public ConfigValue(String name, String[] description, T def) {
        super(name, description);
        this.value = def;
    }

    void load(Meta data) {
        this.value = data.getOrSet(getName(), value);
    }
    void save(Meta data) {
        data.set(getName(), value);
    }

    public abstract MenuElement createElement();
}
