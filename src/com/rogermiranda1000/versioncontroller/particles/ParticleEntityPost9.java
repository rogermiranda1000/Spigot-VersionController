package com.rogermiranda1000.versioncontroller.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ParticleEntityPost9 extends ParticleEntity {
    private final Particle particle;

    public ParticleEntityPost9(Particle particle) {
        this.particle = particle;
    }

    @Override
    void playParticle(World world, Location loc) {
        world.spawnParticle(this.particle, loc.getX(), loc.getY(), loc.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    void playParticle(Player ply, Location loc) {
        ply.spawnParticle(this.particle, loc.getX(), loc.getY(), loc.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}
