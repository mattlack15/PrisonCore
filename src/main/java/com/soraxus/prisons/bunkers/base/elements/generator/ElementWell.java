package com.soraxus.prisons.bunkers.base.elements.generator;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;
import org.bukkit.ChatColor;

public class ElementWell extends GeneratorElement {


    public ElementWell(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker, BunkerResource.WATER);
        this.getTextColour = ChatColor.BLUE + "";
    }

    public ElementWell(Bunker bunker) {
        this(null, bunker);
    }

    @Override
    public int getDelay() {
        return 25;
    }

    @Override
    public double getAmount() {
        return 1;
    }

    @Override
    public IntVector2D getShape() {
        return IntVector2D.ONE;
    }

    @Override
    public double getMaxHealth() {
        return 100;
    }

    @Override
    public BunkerElementType getType() {
        return BunkerElementType.GENERATOR_WELL;
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
