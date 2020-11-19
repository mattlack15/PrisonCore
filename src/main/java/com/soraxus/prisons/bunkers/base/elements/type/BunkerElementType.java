package com.soraxus.prisons.bunkers.base.elements.type;

import com.soraxus.prisons.bunkers.base.elements.ElementCore;
import com.soraxus.prisons.bunkers.base.elements.ElementWorkerHut;
import com.soraxus.prisons.bunkers.base.elements.decoration.ElementPath;
import com.soraxus.prisons.bunkers.base.elements.defense.active.barracks.ElementBarracks;
import com.soraxus.prisons.bunkers.base.elements.defense.active.mortar.ElementMortar;
import com.soraxus.prisons.bunkers.base.elements.defense.active.tower.archer.ElementArcherTower;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementGate;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementMineField;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.ElementWall;
import com.soraxus.prisons.bunkers.base.elements.defense.nonactive.camp.ElementArmyCamp;
import com.soraxus.prisons.bunkers.base.elements.entertainment.ElementPlot;
import com.soraxus.prisons.bunkers.base.elements.envoy.ElementEnvoy;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementFarm;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementGeneratorStone;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementGeneratorTimber;
import com.soraxus.prisons.bunkers.base.elements.generator.ElementWell;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementPond;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementRock;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementStump;
import com.soraxus.prisons.bunkers.base.elements.natural.ElementTree;
import com.soraxus.prisons.bunkers.base.elements.research.ElementLaboratory;
import com.soraxus.prisons.bunkers.base.elements.storage.*;
import com.soraxus.prisons.bunkers.base.elements.type.info.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.ultragrav.utils.IntVector2D;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BunkerElementType {
    // ----------------------- Natural -------------------------
    NATURAL_TREE_SMALL_1(
            "Small Tree 1",
            new InfoNaturalGeneric(),
            IntVector2D.ONE,
            "This is a tree",
            (b, o) -> new ElementTree(b, 1, 1)
    ),
    NATURAL_TREE_MEDIUM_1(
            "Medium Tree 1",
            new InfoNaturalGeneric(),
            new IntVector2D(2, 2),
            "This is a tree pt. 2",
            (b, o) -> new ElementTree(b, 1, 2)
    ),
    NATURAL_POND_1(
            "Pond 1",
            new InfoNaturalGeneric(),
            IntVector2D.ONE,
            "This is a pond",
            (b, o) -> new ElementPond(b, 1)
    ),
    NATURAL_ROCK_1(
            "Rock 1",
            new InfoNaturalGeneric(),
            IntVector2D.ONE,
            "This is a rock",
            (b, o) -> new ElementRock(b, 1)
    ),
    NATURAL_STUMP_1(
            "Stump 1",
            new InfoNaturalGeneric(),
            IntVector2D.ONE,
            "This is a dead tree",
            (b, o) -> new ElementStump(b, 1)
    ),

    // ------------------- Essential ------------------
    ESSENTIAL_CORE(
            "Core",
            new InfoEssentialCore(),
            new IntVector2D(3, 3),
            "You need this. Don't question it",
            (b, o) -> new ElementCore(b)
    ),
    ESSENTIAL_WORKER_HUT(
            "Worker Hut",
            new InfoEssentialWorkerHut(),
            IntVector2D.ONE,
            "House for your hard working slaves",
            (b, o) -> new ElementWorkerHut(b)
    ),

    // ---------------- Defensive -------------
    DEFENSIVE_MINE_FIELD(
            "Mine Field",
            new InfoDefensiveMine(),
            IntVector2D.ONE,
            "One wrong step, and boom you go!",
            (b, o) -> new ElementMineField(b)
    ),
    DEFENSIVE_WALL(
            "Wall",
            new InfoDefensiveWall(),
            IntVector2D.ONE,
            "Block your enemies out, protect your bunker",
            (b, o) -> new ElementWall(b)
    ),
    DEFENSIVE_GATE(
            "Gate",
            new InfoDefensiveGate(),
            IntVector2D.ONE,
            "Block your enemies out, but with a gate",
            (b, o) -> new ElementGate(b)
    ),
    DEFENSIVE_ARCHER_TOWER(
            "Archer Tower",
            new InfoDefensiveArcherTower(),
            IntVector2D.ONE,
            "Shoot your enemies with flying sticks",
            (b, o) -> new ElementArcherTower(b)
    ),
    DEFENSIVE_MORTAR(
            "Mortar",
            new InfoDefensiveMortar(),
            new IntVector2D(2, 2),
            "Fire bombs at your enemy",
            (b, o) -> new ElementMortar(b)
    ),

    // -------------- Generators ---------------
    GENERATOR_TIMBER(
            "Timber Generator",
            new InfoGeneratorTimber(),
            IntVector2D.ONE,
            "Generates timber",
            (b, o) -> new ElementGeneratorTimber(b)
    ),
    GENERATOR_STONE(
            "Stone Generator",
            new InfoGeneratorStone(),
            IntVector2D.ONE,
            "Generates stone",
            (b, o) -> new ElementGeneratorStone(b)
    ),
    GENERATOR_FARM(
            "Farm",
            new InfoFarm(),
            IntVector2D.ONE,
            "Source of food",
            (b, o) -> new ElementFarm(b)
    ),
    GENERATOR_WELL(
            "Well",
            new InfoWell(),
            IntVector2D.ONE,
            "Source of water",
            (b, o) -> new ElementWell(b)
    ),

    // ------------------ Storage -------------
    STORAGE_TIMBER(
            "Timber Storage",
            new InfoStorageTimber(),
            IntVector2D.ONE,
            "Store your timber",
            (b, o) -> new ElementStorageTimber(b)
    ),
    STORAGE_STONE("Stone Storage",
            new InfoStorageStone(),
            IntVector2D.ONE,
            "Store your rocks",
            (b, o) -> new ElementStorageStone(b)),

    STORAGE_WATER("Water Tank",
            new InfoWaterTank(),
            IntVector2D.ONE,
            "Big water bottle",
            (b, o) -> new ElementStorageWater(b)),

    STORAGE_FOOD("Food Bank",
            new InfoStorageFood(),
            IntVector2D.ONE,
            "Store your food",
            (b, o) -> new ElementStorageFood(b)),

    // ------------------ Army -------------
    ARMY_BARRACKS("Barracks",
            new InfoArmyBarracks(),
            new IntVector2D(2, 2),
            "Make hardened warriors ready for battle!",
            (b, o) -> new ElementBarracks(b)),
    ARMY_CAMP("Army Camp",
            new InfoArmyCamp(),
            new IntVector2D(2, 2),
            "Camp for your dedicated warriors",
            (b, o) -> new ElementArmyCamp(b)),
    RESEARCH_LABORATORY("Laboratory",
            new InfoArmyLaboratory(),
            new IntVector2D(2, 2),
            "Do research, make your warriors work harder, better, faster, stronger",
            (b, o) -> new ElementLaboratory(b)),

    // ------------------- Envoy --------------
    ENVOY_ENVOY("Envoy",
            new InfoNaturalGeneric(),
            IntVector2D.ONE,
            "Right click me to get some stuff",
            (b, o) -> new ElementEnvoy(null, b)
    ),
    // ------------------ Entertainment --------------
    ENTERTAINMENT_PLOT("Plot",
            new InfoPlot(),
            new IntVector2D(3, 3),
            "Build whatever you please",
            (b, o) -> new ElementPlot(b)),

    //------------------- Decoration --------------
    DECORATION_PATH("Path",
            new InfoDecorationPath(),
            IntVector2D.ONE,
            "A path :/",
            (b, o) -> new ElementPath(b));

    private final String name;
    private final BunkerElementTypeInfo info;
    private final IntVector2D baseLevelShape;
    private final String description;
    private final BunkerElementConstructor constructor;

    private double getCostMultiplier(int level) {
        return Math.pow(1.321, level);
    }

    public Storage[] getBuildCost(int level) {
        return info.getBuildCost(level);
    }

    public int getBuildTimeTicks(int level) {
        return info.getBuildTimeTicks(level);
    }
}
