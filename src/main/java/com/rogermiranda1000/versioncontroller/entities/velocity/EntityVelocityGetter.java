package com.rogermiranda1000.versioncontroller.entities.velocity;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public interface EntityVelocityGetter {
    @NotNull
    public Vector getVelocity(Entity e);

    @NotNull
    public Vector getVelocity(PlayerMoveEvent e);
}
