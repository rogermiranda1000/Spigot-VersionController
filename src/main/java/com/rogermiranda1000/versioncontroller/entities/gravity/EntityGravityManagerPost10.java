package com.rogermiranda1000.versioncontroller.entities.gravity;

import org.bukkit.entity.Entity;

public class EntityGravityManagerPost10 implements EntityGravityManager {
    @Override
    public void disableGravity(Entity e) {
        e.setGravity(false);
    }
}
