package com.rogermiranda1000.helper.blocks;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.reflection.OnServerEvent;
import com.rogermiranda1000.helper.reflection.SpigotEventOverrider;
import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import com.sk89q.worldguard.bukkit.listener.WorldGuardBlockListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Overrides residence's and worldguard's OnBlockBreak
 */
public class ProtectionOverrider {
    private static ProtectionOverrider instance = null;
    private final ArrayList<OnServerEvent<BlockBreakEvent>> blockBreakOverrider;
    private final RogerPlugin plugin;

    /**
     * If all the overriders tells to allow the protection events, it will be allowed
     */
    private final ArrayList<Object> overriders;

    /**
     * Initially it's empty; if at the end it have the same size than overriders then the overrided protection methods must be launched
     */
    private final HashMap<BlockBreakEvent, Set<Object>> allowQueue;

    public ProtectionOverrider(RogerPlugin plugin) {
        this.blockBreakOverrider = new ArrayList<>();
        this.overriders = new ArrayList<>();
        this.allowQueue = new HashMap<>();
        this.plugin = plugin;
    }

    // TODO allow more event types
    // TODO save priorities (sorted list) & ignoreCancelled
    private void overrideProtections() {
        PluginManager pm = Bukkit.getPluginManager();

        Plugin residence = pm.getPlugin("Residence");
        if (residence != null) {
            this.plugin.getLogger().info("Residence plugin detected.");
            this.blockBreakOverrider.add(SpigotEventOverrider.overrideListener(residence, ResidenceBlockListener.class, BlockBreakEvent.class));
        }

        Plugin worldguard = pm.getPlugin("WorldGuard");
        if (worldguard != null) {
            this.plugin.getLogger().info("WorldGuard plugin detected.");
            this.blockBreakOverrider.add(SpigotEventOverrider.overrideListener(worldguard, WorldGuardBlockListener.class, BlockBreakEvent.class));
            this.blockBreakOverrider.add(SpigotEventOverrider.overrideListener(worldguard, EventAbstractionListener.class, BlockBreakEvent.class));
        }
    }

    public void runProtectionEvents(BlockBreakEvent e) {
        for (int n = 0; n < this.blockBreakOverrider.size() && !e.isCancelled(); n++) {
            OnServerEvent<BlockBreakEvent> prot = this.blockBreakOverrider.get(n);
            // launch other protection event
            boolean err = prot.onEvent(e);
            if (err) {
                this.plugin.printConsoleErrorMessage("Protection override failure, removing from list. Notice this may involve players being able to remove protected regions, so report this error immediately.");
                this.blockBreakOverrider.remove(n);
                n--; // the n++ will leave the same index on the next iteration
            }
        }
    }

    protected static class OverriddenProtectionsPlayer implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockBreak(BlockBreakEvent e) {
            synchronized (ProtectionOverrider.class) {
                Set<Object> petitions = ProtectionOverrider.instance.allowQueue.remove(e);
                if (petitions == null) return; // any petition
                if (petitions.size() != ProtectionOverrider.instance.overriders.size()) return; // not enough requests
            }

            ProtectionOverrider.instance.runProtectionEvents(e);
        }
    }

    /**
     * Specifies the caller as overrider
     * /!\\ Must be called from a plugin with dependencies/soft-dependencies of WorldGuard and Residence /!\\
     */
    public static void instantiate(RogerPlugin plugin, Object overrider) {
        plugin.getLogger().info("Overriding protection plugins...");
        synchronized (ProtectionOverrider.class) {
            if (ProtectionOverrider.instance != null) {
                ProtectionOverrider.instance.overriders.add(overrider);

                plugin.getLogger().info("Protection plugins already overridden.");
                return;
            }

            ProtectionOverrider.instance = new ProtectionOverrider(plugin);
            ProtectionOverrider.instance.overrideProtections();
            ProtectionOverrider.instance.overriders.add(overrider);
        }

        Bukkit.getPluginManager().registerEvents(new OverriddenProtectionsPlayer(), plugin);
    }

    /**
     * @pre at least one object must call instantiate
     */
    synchronized public static void deinstantiate(Object overrider) {
        ProtectionOverrider.instance.overriders.remove(overrider);
    }

    /**
     * If the overridden events should occur
     * @pre at least one object must call instantiate
     */
    synchronized public static void shouldOccurs(BlockBreakEvent e, Object overrider) {
        Set<Object> petitions = ProtectionOverrider.instance.allowQueue.get(e);
        if (petitions == null) {
            petitions = new HashSet<>();
            ProtectionOverrider.instance.allowQueue.put(e, petitions);
        }
        petitions.add(overrider);
    }
}
