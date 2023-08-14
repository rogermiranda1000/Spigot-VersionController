package com.rogermiranda1000.versioncontroller.entities.gravity;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.SentryScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EntityGravityManagerPre10 implements EntityGravityManager {
    private static final HashMap<Entity,Location> targetLocatons = new HashMap<>();

    public void setLocation(Entity e, Location l) {
        e.teleport(l);
        synchronized (EntityGravityManagerPre10.targetLocatons) {
            if (EntityGravityManagerPre10.targetLocatons.containsKey(e)) {
                // not null; means it has the gravity disabled
                EntityGravityManagerPre10.targetLocatons.put(e, l);
            }
        }
    }

    @Override
    public void disableGravity(Entity e) {
        synchronized (EntityGravityManagerPre10.targetLocatons) {
            EntityGravityManagerPre10.targetLocatons.put(e, e.getLocation());
        }
    }

    @Override
    public void registerListeners(RogerPlugin plugin) {
        Runnable updateGravitylessEntities = () -> {
            Set<Map.Entry<Entity, Location>> entities;
            synchronized (EntityGravityManagerPre10.targetLocatons) {
                EntityGravityManagerPre10.targetLocatons.entrySet().removeIf(e -> !e.getKey().isValid()); // Entity no loger exists
                entities = EntityGravityManagerPre10.targetLocatons.entrySet();
            }

            for (Map.Entry<Entity, Location> e : entities) {
                e.getKey().teleport(e.getValue());
            }
        };

        new SentryScheduler(plugin).runTaskTimer(plugin, updateGravitylessEntities, 0, 1);
    }
}
