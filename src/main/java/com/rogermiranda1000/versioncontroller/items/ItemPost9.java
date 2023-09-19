package com.rogermiranda1000.versioncontroller.items;

import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * ItemManager for version >= 1.9
 */
public class ItemPost9 extends ItemManager {
    @Override
    public boolean isItem(ItemStack item) {
        return item.getType().isItem(); // TODO will work in <1.13?
    }

    @Override
    public int getDurability(ItemStack item) throws IllegalArgumentException {
        if (VersionController.version.compareTo(Version.MC_1_13) < 0) return new ItemPre9().getDurability(item);
        else {
            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable)) throw new IllegalArgumentException(item.getType().name() + " is not damageable");
            return ((Damageable) meta).getDamage();
        }
    }

    @Override
    public void setDurability(ItemStack item, int damage) throws IllegalArgumentException {
        if (VersionController.version.compareTo(Version.MC_1_13) < 0) new ItemPre9().setDurability(item, damage);
        else {
            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable)) throw new IllegalArgumentException(item.getType().name() + " is not damageable");
            ((Damageable) meta).setDamage(damage);
            item.setItemMeta(meta);
        }
    }

    @Override
    public ItemStack setUnbreakable(ItemStack item) {
        if (VersionController.version.compareTo(Version.MC_1_11) < 0) return new ItemPre9().setUnbreakable(item);
        else {
            ItemMeta meta = item.getItemMeta();
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
            return item;
        }
    }
}
