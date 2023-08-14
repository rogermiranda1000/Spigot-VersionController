package com.rogermiranda1000.versioncontroller.entities.velocity;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityVelocityGetterSpigot implements EntityVelocityGetter {
    @Override
    public @NotNull Vector getVelocity(Entity e) {
        return e.getVelocity();
    }

    @Override
    public @NotNull Vector getVelocity(PlayerMoveEvent e) {
        return this.getVelocity(e.getPlayer());
    }
}
