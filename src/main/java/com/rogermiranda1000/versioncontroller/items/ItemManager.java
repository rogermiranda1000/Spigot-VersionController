package com.rogermiranda1000.versioncontroller.items;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public abstract class ItemManager {
    /**
     * Implement method to get the item(s) in hand
     * @param playerInventory Inventory
     * @return Item(s) holded
     */
    public abstract ItemStack[] getItemInHand(PlayerInventory playerInventory);

    /**
     * Get player's inventory and call getItemInHand(PlayerInventory)
     * @param player Player
     * @return Item(s) holded
     */
    public ItemStack[] getItemInHand(Player player) {
        return this.getItemInHand(player.getInventory());
    }

    public abstract void setItemInHand(PlayerInventory playerInventory, ItemStack item);

    public abstract boolean isItem(ItemStack item);

    public abstract int getDurability(ItemStack item) throws IllegalArgumentException;

    public abstract void setDurability(ItemStack item, int damage) throws IllegalArgumentException;

    public abstract ItemStack setUnbreakable(ItemStack item);

    /**
     * It checks the material, name and enchantments of an item
     * @param i First item
     * @param i2 Second item
     * @return If i == i2
     */
    public boolean sameItem(ItemStack i, ItemStack i2) {
        if (i == null && i2 == null) return true;
        if (i == null || i2 == null) return false;

        if (!i2.getType().equals(i.getType())) return false;

        // same enchantments?
        if (i.getEnchantments().size() != i2.getEnchantments().size()) return false;
        for (Map.Entry<Enchantment, Integer> enchantment : i2.getEnchantments().entrySet()) {
            Integer value = i.getEnchantments().get(enchantment.getKey());
            if (value == null || !value.equals(enchantment.getValue())) {
                return false;
            }
        }

        ItemMeta m = i.getItemMeta(),
                m2 = i2.getItemMeta();
        if (m == null && m2 == null) return true;
        if (m == null || m2 == null) return false;

        // same lore?
        if (m.hasLore() != m2.hasLore()) return false;
        if (m.hasLore()) {
            List<String> l1 = m.getLore(),
                    l2 = m2.getLore();
            if (l1.size() != l2.size()) return false;
            for (int n = 0; n < l1.size(); n++) {
                if (!l1.get(n).equals(l2.get(n))) return false;
            }
        }

        // same name?
        return m.getDisplayName().equals(m2.getDisplayName());
    }

    /**
     * Check if player is holding an item
     * @param p Player
     * @param i Item
     * @return If the player is holding that item (true), or not (false)
     */
    public boolean hasItemInHand(Player p, ItemStack i) {
        for (ItemStack item : this.getItemInHand(p)) {
            if (this.sameItem(i, item)) return true;
        }

        return false;
    }
}
