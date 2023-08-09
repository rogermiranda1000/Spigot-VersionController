package com.rogermiranda1000.versioncontroller.particles;

public interface ParticleManager {
    /**
     * Get the particle
     * @param particle Particle's name
     * @return Particle
     * @throws IllegalArgumentException Particle not found
     */
    ParticleEntity getParticle(String particle) throws IllegalArgumentException;
}
