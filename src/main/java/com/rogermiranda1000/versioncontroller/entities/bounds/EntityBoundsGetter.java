package com.rogermiranda1000.versioncontroller.entities.bounds;

import org.bukkit.entity.Entity;

public interface EntityBoundsGetter {
    public BoundingBox getBoundingBox(Entity e);
}
