package com.rogermiranda1000.versioncontroller.entities;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface EntityManager {
    @NotNull
    public default Vector getVelocity(Entity e) {
        return e.getVelocity();
    }

    @NotNull
    public default Vector getVelocity(PlayerMoveEvent e) {
        return this.getVelocity(e.getPlayer());
    }

    public BoundingBox getBoundingBox(Entity e);
}
