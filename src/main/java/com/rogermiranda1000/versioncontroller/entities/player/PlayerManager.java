package com.rogermiranda1000.versioncontroller.entities.player;

import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface PlayerManager {
    /**
     * Implement method to get the item(s) in hand
     * @param playerInventory Inventory
     * @return Item(s) holded
     */
    ItemStack[] getItemInHand(PlayerInventory playerInventory);

    default ItemStack[] getItemInHand(Player p) {
        return this.getItemInHand(p.getInventory());
    }

    void setItemInHand(PlayerInventory playerInventory, ItemStack item, boolean leftHand);

    default void setItemInHand(PlayerInventory playerInventory, ItemStack item) {
        this.setItemInHand(playerInventory, item, false);
    }

    /**
     * Check if player is holding an item
     * @param p Player
     * @param i Item
     * @return If the player is holding that item (true), or not (false)
     */
    default boolean hasItemInHand(Player p, ItemStack i) {
        for (ItemStack item : this.getItemInHand(p)) {
            if (VersionController.get().sameItem(i, item)) return true;
        }

        return false;
    }
}
