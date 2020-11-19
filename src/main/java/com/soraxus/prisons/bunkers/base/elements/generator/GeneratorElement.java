package com.soraxus.prisons.bunkers.base.elements.generator;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.bunkers.base.Bunker;
import com.soraxus.prisons.bunkers.base.BunkerElement;
import com.soraxus.prisons.bunkers.base.elements.storage.Storage;
import com.soraxus.prisons.bunkers.base.resources.BunkerResource;
import com.soraxus.prisons.bunkers.util.BHoloTextBox;
import com.soraxus.prisons.util.math.MathUtils;
import net.ultragrav.serializer.GravSerializer;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class GeneratorElement extends BunkerElement {
    private final BunkerResource generatingType;
    private final Storage currentStorage;
    private int ticker = 0;
    private BHoloTextBox textBox;
    protected String getTextColour = "";

    public GeneratorElement(GravSerializer serializer, Bunker bunker, BunkerResource generatingType) {
        super(serializer, bunker);
        this.generatingType = generatingType;
        this.currentStorage = getMeta().getOrSet("storage", new Storage(generatingType, 0, Math.round(getLevel() * 900 / (double) getDelay())));
    }

    protected void generate(double amount) {
        currentStorage.addAmount(amount);
        updateTextBox();
    }

    @Override
    protected void onEnable() {
        this.currentStorage.setCap(Math.round(getLevel() * 900 / (double) getDelay()));
        this.updateTextBox();
    }

    @Override
    protected void onDisable() {
        if (textBox != null)
            this.textBox.clear();
    }

    private void updateTextBox() {
        synchronized (currentStorage) {
            if (currentStorage.getAmount() > currentStorage.getCap() / 30D) {
                if (textBox == null) {
                    textBox = new BHoloTextBox(this.getLocation().add(this.getShape().getX() * BunkerManager.TILE_SIZE_BLOCKS / 2D, this.getSchematic().getDimensions().getY() - this.getSchematic().getOrigin().getY() + 1, this.getShape().getY() * BunkerManager.TILE_SIZE_BLOCKS / 2D),
                            0.3, false, () -> getBunker().getWorld().getBukkitWorld());
                }
                textBox.setOrMake(0, "&aGET");
                textBox.setOrMake(1, "&f" + getTextColour + currentStorage.getAmount());
            } else {
                if (textBox != null)
                    textBox.clear();
            }
        }
    }

    public boolean collect() {
        Storage curr = getBunker().getCombinedStorages().get(generatingType);
        if (curr == null)
            return false;
        double amount; //Avoiding possible deadlocks here
        double amountCollected;
        synchronized (currentStorage) {
            amount = currentStorage.getAmount();
            amountCollected = Math.min(amount, curr.getCap() - curr.getAmount());
            if (amountCollected == 0) {
                return false;
            }
            currentStorage.setAmount(amount - amountCollected);
        }
        getBunker().addResource(generatingType, amountCollected); //^
        updateTextBox();
        return true;
    }

    /**
     * Tick this generator
     * Generates {@code this#getAmount} of {@code this#generatingType} every {@code this#getDelay} ticks
     */
    @Override
    public void onTick() {
        ticker++;
        if (ticker >= getDelay()) {
            generate(getAmount());
            ticker = 0;
        }
    }

    public abstract int getDelay();

    public abstract double getAmount();

    public Storage getCurrentStorage() {
        return currentStorage;
    }

    @Override
    public void onClick(PlayerInteractEvent e) {
        if (this.currentStorage.getAmount() > 0D) {
            double amount = currentStorage.getAmount();
            if (this.collect()) {
                amount -= currentStorage.getAmount();
                amount = MathUtils.round(amount, 1);
                getBunker().messageMembersInWorld("&a+" + amount + " " + generatingType.getColor() + generatingType.getDisplayName());
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1.1f);
            } else {
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 0.25f);
                getBunker().messageMember(e.getPlayer(), "§cYou do not have any more storage for " + generatingType.fullDisplay());
            }
        }
    }
}
