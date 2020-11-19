package com.soraxus.prisons.bunkers.base.resources;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum Skill {
    WOODWORKING("Woodworking", BunkerResource.TIMBER, 5, 50),
    STONECUTTING("Stonecutting", BunkerResource.STONE, 10, 50),
    METALWORKING("Metalworking", BunkerResource.GOLD, 15, 50);

    private String name;
    private BunkerResource resourceType;
    private int researchCostBase;
    private double requirementBase;

    public int getResearchCost(int level) {
        return researchCostBase * (level + 1);
    }

    public int getRequiredLevel(double resourceAmount) {
        return (int) Math.ceil(resourceAmount / requirementBase);
    }
}
