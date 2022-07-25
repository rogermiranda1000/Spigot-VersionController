package com.rogermiranda1000.versioncontroller.items;

import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ItemManager for version < 1.9
 */
public class ItemPre9 extends ItemManager {
    @Nullable
    private static final Method getItemInHandMethod = ItemPre9.getItemInHandMethod();

    @Nullable
    private static final Method setItemInHandMethod = ItemPre9.setItemInHandMethod();

    @Nullable
    private static final Method getItemByIdMethod = ItemPre9.getItemByIdMethod();

    @Nullable
    private static final Method getDurabilityMethod = ItemPre9.getDurabilityMethod();

    @Nullable
    private static final Method setDurabilityMethod = ItemPre9.setDurabilityMethod();

    @Nullable
    private static Method getDurabilityMethod() {
        try {
            return ItemStack.class.getMethod("getDurability");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static Method setDurabilityMethod() {
        try {
            return ItemStack.class.getMethod("setDurability", short.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

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

    private static Method getItemByIdMethod() {
        try {
            Class<?> nmsItemClass = Class.forName(VersionController.nmsPackage + ".Item");
            return nmsItemClass.getMethod("getById", int.class);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions") // ignore NPE
    @Override
    public ItemStack[] getItemInHand(PlayerInventory playerInventory) {
        try {
            ItemStack[] r = new ItemStack[1];
            r[0] = (ItemStack) ItemPre9.getItemInHandMethod.invoke(playerInventory);
            return r;
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException e) {
            //e.printStackTrace();
            return new ItemStack[0];
        }
    }

    @SuppressWarnings("ConstantConditions") // ignore NPE
    @Override
    public void setItemInHand(PlayerInventory playerInventory, ItemStack item) {
        try {
            ItemPre9.setItemInHandMethod.invoke(playerInventory, item);
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException ignore) {}
    }

    /**
     * @author https://www.spigotmc.org/threads/check-if-material-is-an-item.310715/
     * @return If type can be given as item to the user or not
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isItem(ItemStack item) {
        try {
            return ItemPre9.getItemByIdMethod.invoke(null, item.getType().getId()) != null;
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ex) {
            return false;
        }
    }

    @Override
    public int getDurability(ItemStack item) throws IllegalArgumentException {
        try {
            return (int)ItemPre9.setDurabilityMethod.invoke(item);
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ex) {
            return 0;
        }
    }

    /**
     * Prior to 1.13
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void setDurability(ItemStack item, int damage) {
        try {
            ItemPre9.setDurabilityMethod.invoke(item, (short)damage);
        } catch (InvocationTargetException | IllegalAccessException | NullPointerException ignore) { }
    }
}
