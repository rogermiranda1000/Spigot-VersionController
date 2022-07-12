package com.rogermiranda1000.versioncontroller.particles;

import org.bukkit.Particle;

/**
 * ParticleManager for version >= 1.9
 */
public class ParticlePost9 implements ParticleManager {
    @Override
    public ParticleEntity getParticle(String particle) throws IllegalArgumentException {
        return new ParticleEntityPost9(Particle.valueOf(particle));
    }
}
