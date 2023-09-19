package com.rogermiranda1000.versioncontroller.entities.player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * PlayerManager for version >= 1.9
 */
public class PlayerPost9 implements PlayerManager {
    @Override
    public ItemStack[] getItemInHand(PlayerInventory playerInventory) {
        ItemStack[] r = new ItemStack[2];
        r[0] = playerInventory.getItemInMainHand();
        r[1] = playerInventory.getItemInOffHand();
        return r;
    }

    @Override
    public void setItemInHand(PlayerInventory playerInventory, ItemStack item, boolean leftHand) {
        if (!leftHand) playerInventory.setItemInMainHand(item);
        else playerInventory.setItemInOffHand(item);
    }
}
