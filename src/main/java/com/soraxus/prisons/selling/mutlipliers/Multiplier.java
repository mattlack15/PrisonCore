package com.soraxus.prisons.selling.mutlipliers;

import com.soraxus.prisons.selling.ModuleSelling;
import com.soraxus.prisons.util.time.DateUtils;
import com.soraxus.prisons.util.items.ItemUtils;
import com.soraxus.prisons.util.items.NBTUtils;
import com.soraxus.prisons.util.string.PlaceholderUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Multiplier {
    private long length;
    private long startTime;

    private double multi;
    private BossBar bar;


    @NotNull
    @Contract("_ -> new")
    public static Multiplier parseFromItem(final ItemStack item) {
        assert isMultiplier(item);
        final long length = NBTUtils.instance.getLong(item, "multi_length");
        final double multi = NBTUtils.instance.getDouble(item, "multi_multi");
        return new Multiplier(length, multi);
    }

    public static boolean isMultiplier(final ItemStack item) {
        return ItemUtils.isType(item, "multiplier");
    }

    public ItemStack addNBT(ItemStack item) {
        ItemStack ret = NBTUtils.instance.setString(item, "type", "multiplier");
        ret = NBTUtils.instance.setLong(ret, "multi_length", this.length);
        ret = NBTUtils.instance.setDouble(ret, "multi_multi", this.multi);
        return ret;
    }

    public Multiplier(long length, double multi) {
        this.length = length;
        this.multi = multi;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public boolean isRunning() {
        return startTime > 0;
    }

    public void pause() {
        this.length = this.getRemainingTime();
        this.startTime = -1L;
    }

    public boolean isActive() {
        return this.getRemainingTime() >= 0L;
    }

    public long getRemainingTime() {
        if (this.startTime == -1L) {
            return this.length;
        }
        final long endTime = this.startTime + this.length;
        final long currentTime = System.currentTimeMillis();
        return endTime - currentTime;
    }

    public float getPercentRemaining() {
        return this.getRemainingTime() / (float) this.length;
    }

    public String getRemainingTimeStr() {
        return DateUtils.convertTime(this.getRemainingTime() / 1000L);
    }

    public String getLengthStr() {
        return DateUtils.convertTime(this.getLength() / 1000L);
    }

    public ItemStack getItem() {
        return MultiplierManager.instance.getItem(this);
    }

    public Map<String, String> getPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("length", DateUtils.convertTime(this.length / 1000L));
        placeholders.put("timeRemaining", getRemainingTimeStr());
        placeholders.put("multi", "" + getMulti());
        return placeholders;
    }

    public String getBossBar() {
        return PlaceholderUtil.replacePlaceholders(ModuleSelling.instance.getMultiplierData().getString("bossbar"), getPlaceholders());
    }

    public void render(@NotNull final Player player) {
        if (this.bar == null) {
            this.bar = Bukkit.createBossBar("Multiplier", BarColor.BLUE, BarStyle.SOLID);
        }
        if (this.bar.getPlayers() == null || !this.bar.getPlayers().contains(player)) {
            this.bar.addPlayer(player);
        }
        this.bar.setVisible(true);
        this.bar.setTitle(this.getBossBar());
        this.bar.setProgress(this.getPercentRemaining());
    }

    public void unrender(@NotNull final Player player) {
        if (this.bar.getPlayers().contains(player)) {
            this.bar.removePlayer(player);
        }
        if (this.bar.getPlayers().isEmpty()) {
            this.bar.setVisible(false);
        }
    }

    public String expiryMessage() {
        return null; // TODO: Expiry message?
    }

    public void expire(@NotNull final Player player) {
        player.sendMessage(this.expiryMessage());
        this.unrender(player);
    }

    public void addTime(long time) {
        this.length += time;
    }

    public void end() {
        pause();
        bar.setVisible(false);
        bar.removeAll();
    }
}
