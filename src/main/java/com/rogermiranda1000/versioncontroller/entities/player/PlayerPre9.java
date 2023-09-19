package com.rogermiranda1000.versioncontroller.entities.player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * PlayerManager for version < 1.9
 */
public class PlayerPre9 implements PlayerManager {
    @Nullable
    private static final Method getItemInHandMethod = PlayerPre9.getItemInHandMethod();

    @Nullable
    private static final Method setItemInHandMethod = PlayerPre9.setItemInHandMethod();

    @Nullable
    private static Method getItemInHandMethod() {
        try {
            return PlayerInventory.class.getMethod("getItemInHand");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static Method setItemInHandMethod() {
        try {
            return PlayerInventory.class.getMethod("setItemInHand", ItemStack.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions") // ignore NPE
    @Override
    public ItemStack[] getItemInHand(PlayerInventory playerInventory) {
        try {
            ItemStack[] r = new ItemStack[1];
            r[0] = (ItemStack) PlayerPre9.getItemInHandMethod.invoke(playerInventory);
            return r;
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException e) {
            //e.printStackTrace();
            return new ItemStack[0];
        }
    }

    @SuppressWarnings("ConstantConditions") // ignore NPE
    @Override
    public void setItemInHand(PlayerInventory playerInventory, ItemStack item, boolean leftHand) {
        if (leftHand) throw new IllegalArgumentException("Left hand can't be used in <1.9");
        try {
            PlayerPre9.setItemInHandMethod.invoke(playerInventory, item);
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException ignore) {}
    }
}
