package com.soraxus.prisons.bunkers.base.elements.generator;

import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.elements.type.BunkerElementType;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.npc.ElementDrop;
import com.soraxus.prisons.util.EventSubscriptions;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.IntVector2D;

public class ElementFarm extends GeneratorElement {

    public ElementFarm(GravSerializer serializer, Bunker bunker) {
        super(serializer, bunker, BunkerResource.FOOD);
    }

    public ElementFarm(Bunker bunker) {
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
        return BunkerElementType.GENERATOR_FARM;
    }

    @Override
    protected void onEnable() {
        EventSubscriptions.instance.subscribe(this);
    }

    @Override
    public ElementDrop getDropForDamage(double damage) {
        return null;
    }
}
