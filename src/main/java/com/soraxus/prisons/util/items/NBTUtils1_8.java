///*
// * Copyright (c) 2020. UltraDev
// */
//
//package com.soraxus.prisons.util.items;
//
//import net.minecraft.server.v1_8_R3.*;
//import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;

//public class NBTUtils1_8 extends NBTUtil {
//    @NotNull
//    public net.minecraft.server.v1_8_R3.ItemStack convertToNMS(@NotNull ItemStack displayItem) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(displayItem);
//        if (!nmsItem.hasTag()) {
//            nmsItem.setTag(new NBTTagCompound());
//        }
//        return nmsItem;
//    }
//
//    @NotNull
//    private ItemStack convertToBukkit(net.minecraft.server.v1_8_R3.ItemStack displayItem) {
//        return CraftItemStack.asBukkitCopy(displayItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setInt(ItemStack displayItem, String key, int value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setInt(key, value);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setDouble(ItemStack displayItem, String key, double value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setDouble(key, value);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setLong(ItemStack displayItem, String key, long value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setLong(key, value);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setString(ItemStack displayItem, String key, String value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setString(key, value);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setBigDecimal(ItemStack displayItem, String key, @NotNull BigDecimal value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setString(key, value.toString());
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setBigInteger(ItemStack displayItem, String key, @NotNull BigInteger value) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setString(key, value.toString());
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public Integer getInt(ItemStack displayItem, String key) throws IllegalArgumentException {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag().getInt(key);
//    }
//
//    @Override
//    @NotNull
//    public Double getDouble(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag().getDouble(key);
//    }
//
//
//    @Override
//    @NotNull
//    public Long getLong(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag().getLong(key);
//    }
//
//    @Override
//    public String getString(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        if (!nmsItem.getTag().hasKey(key)) {
//            return null;
//        }
//        return nmsItem.getTag().getString(key);
//    }
//
//    @Override
//    @Contract("_, _ -> new")
//    @NotNull
//    public BigDecimal getBigDecimal(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return new BigDecimal(nmsItem.getTag().getString(key));
//    }
//
//    @Override
//    @Contract("_, _ -> new")
//    @NotNull
//    public BigInteger getBigInteger(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return new BigInteger(nmsItem.getTag().getString(key));
//    }
//
//    @Override
//    public boolean hasTag(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag().hasKey(key);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack remove(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().remove(key);
//        return convertToBukkit(nmsItem);
//    }
//
//    @NotNull
//    public ItemStack setNBT(ItemStack displayItem, NBTTagCompound nbts) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        nmsItem.setTag(nbts);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack setByteArray(ItemStack displayItem, String key, byte[] bytes) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        nmsItem.getTag().setByteArray(key, bytes);
//        return convertToBukkit(nmsItem);
//    }
//
//    @Override
//    @NotNull
//    public byte[] getByteArray(ItemStack displayItem, String key) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag().getByteArray(key);
//    }
//
//    @Override
//    public boolean hasNBTCompound(ItemStack displayItem) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        return nmsItem.hasTag();
//    }
//
//    @NotNull
//    public NBTTagCompound getNBTCompound(ItemStack displayItem) {
//        net.minecraft.server.v1_8_R3.ItemStack nmsItem = convertToNMS(displayItem);
//        assert nmsItem.getTag() != null;
//        return nmsItem.getTag();
//    }
//}
