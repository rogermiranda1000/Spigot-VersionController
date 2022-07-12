package com.rogermiranda1000.versioncontroller.particles;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public abstract class ParticleEntity {
    /**
     * Plays a particle
     * @param world Location's world
     * @param loc Particle's location
     */
    abstract void playParticle(World world, Location loc);

    /**
     * Plays a particle to only one player
     * @param ply Player who the particle will be shown
     * @param loc Location to show the particle
     */
    abstract void playParticle(Player ply, Location loc);
}
