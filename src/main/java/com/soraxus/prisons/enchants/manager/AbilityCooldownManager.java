package com.soraxus.prisons.enchants.manager;

import com.google.common.collect.Lists;
import com.soraxus.prisons.enchants.ModuleEnchants;
import com.soraxus.prisons.enchants.api.enchant.AbstractCE;
import com.soraxus.prisons.util.display.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class AbilityCooldownManager extends BukkitRunnable {
	public static AbilityCooldownManager instance;
	private Map<Player, Map<String, Cooldown>> cooldowns = new HashMap<>();
	public AbilityCooldownManager() {
		instance = this;
		this.runTaskTimerAsynchronously(ModuleEnchants.instance.getParent().getPlugin(), 0, 2);
	}

	@SuppressWarnings("serial")
	public Cooldown addCooldown(Player p, String identifier, Cooldown cooldown) {
		if(!this.cooldowns.containsKey(p)) {
			this.cooldowns.put(p, new HashMap<String, Cooldown>(){{put(identifier, cooldown);}});
			return cooldown;
		}
		this.cooldowns.get(p).put(identifier, cooldown);
		return cooldown;
	}

	@Override
	public void run() {
		this.cooldowns.forEach((p, m) -> {
			String format = "<enchant> &c&l: <cooldownbar> &f<cooldown>";
			if(m.size() <= 0) {
				return;
			}
			String identifier = Lists.newArrayList(m.keySet()).get(m.size()-1);
			Cooldown cooldown = m.get(identifier);
			format = format.replaceAll("<player>", p.getName());
			format = format.replaceAll("<identifier>", identifier);
			format = format.replaceAll("<cooldownbar>", this.getCooldownBar(cooldown));
			format = format.replaceAll("<cooldown>", "" + (cooldown.getCooldown() / 20));
			try {
				ActionBar.send(p, org.bukkit.ChatColor.translateAlternateColorCodes('&', format));
			} catch (Exception e) {
				e.printStackTrace();
			}
			for(String enchs : m.keySet()) {
				Cooldown cooldowns = m.get(enchs);
				if(cooldowns.isExpired()) {
					m.remove(identifier);
					if(cooldowns.getDoWhenExpire() != null) {
						cooldowns.getDoWhenExpire().run();
					}
					return;
				}
				cooldowns.decreaseBy(2);
			}
		});
	}
	public Cooldown getCooldown(Player p, AbstractCE e) {
		if(!this.cooldowns.containsKey(p)) {
			return null;
		}
		if(!this.cooldowns.get(p).containsKey(e)) {
			return null;
		}
		return this.cooldowns.get(p).get(e);
	}
	public boolean isExpired(Player p, AbstractCE e) {
		if(!this.cooldowns.containsKey(p)) {
			return true;
		}
		if(!this.cooldowns.get(p).containsKey(e)) {
			return true;
		}
		return this.cooldowns.get(p).get(e).isExpired();
	}
	public static class Cooldown {
		private Runnable doWhenExpire;
		private float originalCooldown;
		private float cooldown;
		private boolean reversed;
		public Cooldown(float cooldown) {
			this.originalCooldown = cooldown;
			this.cooldown = cooldown;
		}
		public boolean isReversed() {
			return reversed;
		}
		public void setReversed(boolean val) {
			this.reversed = val;
		}
		public boolean isExpired() {
			if(cooldown <= 0) {
				return true;
			}
			return false;
		}
		public void decreaseBy(float f) {
			this.cooldown -= f;
		}
		public float getCooldown() {
			return this.cooldown;
		}
		public float getOriginalCooldown() {
			return originalCooldown;
		}
		public Runnable getDoWhenExpire() {
			return doWhenExpire;
		}
		public void setDoWhenExpire(Runnable doWhenExpire) {
			this.doWhenExpire = doWhenExpire;
		}
	}
	public String getCooldownBar(Cooldown cooldown) {
		StringBuilder output = new StringBuilder((cooldown.isReversed() ? ChatColor.RED : ChatColor.GREEN) + "" + ChatColor.BOLD);
		float percentage = cooldown.getCooldown() / cooldown.getOriginalCooldown();
		if(!cooldown.isReversed()) { percentage = 1 - percentage; }
		percentage *= 100;
		for(int i = 0; i < 50; i++) {
			if(i*2 < percentage) {
				output.append(":");
			} else if(i*2-2 < percentage){
				output.append(ChatColor.WHITE.toString()).append(ChatColor.BOLD.toString()).append(":");
			} else {
				output.append(":");
			}
		}
		return output.toString();
	}
}