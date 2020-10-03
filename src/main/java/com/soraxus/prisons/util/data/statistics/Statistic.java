package com.soraxus.prisons.util.data.statistics;

import com.soraxus.prisons.util.string.TextUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Statistic {
    private String name;
    private String displayName;
    @Setter
    private long value;

    public Statistic(String name) {
        this.name = name;
        this.displayName = TextUtil.capitalize(name);
        this.value = 0;
    }

    public void increment(long am) {
        this.value += am;
    }
}
