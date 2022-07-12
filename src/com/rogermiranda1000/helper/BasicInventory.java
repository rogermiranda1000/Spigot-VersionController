package com.rogermiranda1000.helper;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class BasicInventory implements Listener {
    private final Plugin plugin;
    private final boolean cancelAllEvents;
    private Inventory inv;
    private final ArrayList<HumanEntity> playersWithOpenInventory;
    private boolean swappingInventories;

    /**
     * It initializes the players with the current inventory opened list
     */
    public BasicInventory(Plugin plugin, boolean cancelAllEvents) {
        this.plugin = plugin;
        this.cancelAllEvents = cancelAllEvents;
        this.playersWithOpenInventory = new ArrayList<>();
        this.swappingInventories = false;
    }

    /**
     * /!\\ only for sons of BasicInventory /!\\
     */
    public Inventory getInventory() {
        return this.inv;
    }

    /**
     * /!\\ only for sons of BasicInventory /!\\
     */
    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    /**
     * It registers the inventories event (click & close)
     */
    public void registerEvent() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public void openInventory(HumanEntity p) {
        p.closeInventory();

        // run on next tick
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, ()-> {
            p.openInventory(this.inv);
            synchronized (this.playersWithOpenInventory) {
                this.playersWithOpenInventory.add(p);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(this.inv)) return;
        if (!this.inv.equals(e.getClickedInventory())) return;

        if (this.cancelAllEvents) e.setCancelled(true);

        this.inventoryClickedEvent(e);
    }

    abstract public void inventoryClickedEvent(InventoryClickEvent e);

    /**
     * /!\\ only for sons of BasicInventory /!\\
     * Changes all the opened inventories to the new one
     * @param other New inventory
     */
    public void newInventory(@NotNull Inventory other) {
        synchronized (this) {
            this.swappingInventories = true;

            for (HumanEntity player : this.playersWithOpenInventory) player.closeInventory();
        }

        // run on next tick
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, ()->{
            synchronized (this) {
                for (HumanEntity player : this.playersWithOpenInventory) player.openInventory(other);
                this.swappingInventories = false;
            }
        }, 1L);
    }

    public void closeInventories() {
        ArrayList<HumanEntity> copy;
        synchronized (this) {
            copy = new ArrayList<>(this.playersWithOpenInventory);
        }
        for (HumanEntity player : copy) player.closeInventory();
    }

    @EventHandler
    public synchronized void onInventoryClose(InventoryCloseEvent e) {
        if (this.swappingInventories) return; // ignore (it will close and open immediately)

        if (e.getInventory().equals(this.inv)) this.playersWithOpenInventory.remove(e.getPlayer());
    }
}
