package com.soraxus.prisons.ranks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Rank {
    private String name;
    private String displayName;
    private long cost;
    private List<String> cmds;

    public long getCostForPrestige(int prestige) {
        return (long) (((double) cost) * (1 + (prestige * 0.1D)));
    }

    public Rank(ConfigurationSection section) {
        this.name = section.getName();
        this.displayName = section.getString("display-name", name);
        this.cost = section.getLong("cost", 0L);
        cmds = Collections.synchronizedList(section.getStringList("cmds"));
    }

    public void serialize(ConfigurationSection section) {
        section.set("display-name", displayName);
        section.set("cost", cost);
        section.set("cmds", cmds);
    }
}
