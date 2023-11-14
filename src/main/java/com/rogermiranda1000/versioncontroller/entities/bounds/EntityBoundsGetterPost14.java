package com.rogermiranda1000.versioncontroller.entities.bounds;

import org.bukkit.entity.Entity;

public class EntityBoundsGetterPost14 implements EntityBoundsGetter {
    @Override
    public BoundingBox getBoundingBox(Entity e) {
        org.bukkit.util.BoundingBox boundingBox = e.getBoundingBox();
        return new BoundingBox(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
    }
}
