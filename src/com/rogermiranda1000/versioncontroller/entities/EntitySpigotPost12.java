package com.rogermiranda1000.versioncontroller.entities;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntitySpigotPost12 implements EntityManager {
    @Override
    public @NotNull Vector getVelocity(Entity e) {
        return e.getVelocity();
    }

    @Override
    public @NotNull Vector getVelocity(PlayerMoveEvent e) {
        return e.getPlayer().getVelocity();
    }

    @Override
    public BoundingBox getBoundingBox(Entity e) {
        org.bukkit.util.BoundingBox boundingBox = e.getBoundingBox();
        return new BoundingBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
}
