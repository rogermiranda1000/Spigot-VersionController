package com.rogermiranda1000.versioncontroller.entities.gravity;

import com.rogermiranda1000.helper.RogerPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface EntityGravityManager {
    public default void setLocation(Entity e, Location l) {
        e.teleport(l);
    }

    public void disableGravity(Entity e);

    public void enableGravity(Entity e);

    public default void registerListeners(RogerPlugin plugin) {}
}
