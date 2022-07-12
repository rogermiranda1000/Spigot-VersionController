package com.rogermiranda1000.versioncontroller.particles;

import org.bukkit.Effect;

/**
 * ParticleManager for version < 1.9
 */
public class ParticlePre9 implements ParticleManager {
    @Override
    public ParticleEntity getParticle(String particle) throws IllegalArgumentException {
        return new ParticleEntityPre9(Effect.valueOf(particle));
    }
}
