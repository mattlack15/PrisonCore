package com.soraxus.prisons.util;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

//	private class EnchantGlow extends Enchantment {
//		public EnchantGlow() {
//			super(6969);
//		}
//
//		@Override
//		public String getDescription() {
//			return "Glow";
//		}
//
//		@Override
//		public int getMaxLevel() {
//			return 1;
//		}
//
//		@Override
//		public int getStartLevel() {
//			return 1;
//		}
//
//		@Override
//		public EnchantmentTarget getItemTarget() {
//			return EnchantmentTarget.ALL;
//		}
//
//		@Override
//		public boolean isTreasure() {
//			return false;
//		}
//
//		@Override
//		public boolean isCursed() {
//			return false;
//		}
//
//		@Override
//		public boolean conflictsWith(Enchantment enchantment) {
//			return false;
//		}
//
//		@Override
//		public boolean canEnchantItem(ItemStack itemStack) {
//			return true;
//		}
//	}

    private ItemStack item;
//
//	public ItemBuilder(PotionType type, boolean extended, boolean upgraded){
//		this(type, extended, upgraded, false);
//	}

//	public ItemBuilder(PotionType type, boolean extended, boolean upgraded, boolean splash){
//		if(splash) {
//			this.displayItem = new ItemStack(Material.SPLASH_POTION, 1);
//			PotionMeta meta = (PotionMeta) this.displayItem.getItemMeta();
//			meta.setBasePotionData(new PotionData(type, extended, upgraded));
//			this.displayItem.setItemMeta(meta);
//		} else {
//			this.displayItem = new ItemStack(Material.POTION, 1);
//			PotionMeta meta = (PotionMeta) this.displayItem.getItemMeta();
//			meta.setBasePotionData(new PotionData(type, extended, upgraded));
//			this.displayItem.setItemMeta(meta);
//		}
//	}

    public ItemBuilder(Material m) {
        if (m == null) {
            this.item = null;
            return;
        }
        this.item = new ItemStack(m);
    }

    public ItemBuilder(Material m, int amount) {
        if (m == null) {
            this.item = null;
            return;
        }
        this.item = new ItemStack(m, amount);
    }

    public ItemBuilder(Material m, int amount, byte data) {
        if (m == null) {
            this.item = null;
            return;
        }
        this.item = new ItemStack(m, amount, data);
    }

    public ItemBuilder setDurability(short val) {
        this.item.setDurability(val);
        return this;
    }

    public ItemBuilder setDurabilityLeft(short val) {
        this.item.setDurability((short) (this.item.getType().getMaxDurability() - val + 1));
        return this;
    }

    public ArrayList<String> getLore() {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        return Lists.newArrayList(lore);
    }

    public String getName() {
        String name = item.getItemMeta().getDisplayName();
        if (name == null) {
            return "";
        }
        return name;
    }

    public ItemBuilder setupAsSkull(String owner) {
       	this.setType(Material.SKULL_ITEM);
       	this.setDurability((short) 3);
       	this.setSkullOwner(owner);
        /*ArrayList<String> lore = this.getLore();
        String name = this.getName();
        this.item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        this.setSkullOwner(owner);
        this.setName(name);
        lore.forEach(this::addLore);*/
        return this;
    }

	private ItemBuilder setType(Material material) {
    	this.item.setType(material);
    	return this;
	}

	public ItemBuilder setItemMeta(ItemMeta meta) {
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        ItemMeta meta = this.item.getItemMeta();
        meta.addItemFlags(flags);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemMeta getItemMeta() {
        return this.item.getItemMeta();
    }

    public ItemBuilder setUnbreakable(boolean bool) {
        ItemMeta meta = this.item.getItemMeta();
        meta.spigot().setUnbreakable(bool);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addLore(String s) {
        return addLore(s, true);
    }

    public ItemBuilder addLore(String... s) {
        for (String str : s) {
            addLore(str);
        }
        return this;
    }

    public ItemBuilder addLore(List<String> s) {
        for (String str : s) {
            addLore(str);
        }
        return this;
    }

    public ItemBuilder addLore(String s, boolean translateColours) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return this;
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(translateColours ? ChatColor.translateAlternateColorCodes('&', s) : s);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(int index, String s) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return this;
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(index, ChatColor.translateAlternateColorCodes('&', s));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLore(String lore, boolean removeAll) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore1 = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        while (lore1.contains(ChatColor.translateAlternateColorCodes('&', lore))) {
            lore1.remove(ChatColor.translateAlternateColorCodes('&', lore));
            if (!removeAll) {
                break;
            }
        }
        meta.setLore(lore1);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Currently set to not do anything
     */
    public ItemBuilder addGlow() {
//		try{
//			Field field = Enchantment.class.getDeclaredField("acceptingNew");
//			field.setAccessible(true);
//			field.set(null, true);
//			if(Enchantment.getByName(new EnchantGlow().getDescription()) == null){
//				Enchantment.registerEnchantment(new EnchantGlow());
//			}
//		} catch(Exception ignored){}
//		this.displayItem.addUnsafeEnchantment(new EnchantGlow(), 1);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        this.item.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder setLeatherColour(int color) {
        if (!this.item.hasItemMeta() || !(this.item.getItemMeta() instanceof LeatherArmorMeta)) {
            return this;
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(Color.fromRGB(color));
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(int i, String s) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        while (lore.size() <= i) {
            lore.add("");
        }
        lore.set(i, ChatColor.translateAlternateColorCodes('&', s));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setName(String s) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', s));
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return this.item;
    }
}
