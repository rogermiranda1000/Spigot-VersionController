package com.rogermiranda1000.versioncontroller.particles;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ParticleEntityPre9 extends ParticleEntity {
    private static final int RADIUS = 35;

    @Nullable
    private static final Method playWorldEffectMethod = ParticleEntityPre9.getPlayWorldEffectMethod();
    @Nullable
    private static final Method playPlayerEffectMethod = ParticleEntityPre9.getPlayPlayerEffectMethod();

    @Nullable
    private static Method getPlayPlayerEffectMethod() {
        try {
            return Player.Spigot.class.getMethod("playEffect", Location.class, Effect.class, int.class, int.class, float.class, float.class, float.class, float.class, int.class, int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    private static Method getPlayWorldEffectMethod() {
        try {
            return World.Spigot.class.getMethod("playEffect", Location.class, Effect.class, int.class, int.class, float.class, float.class, float.class, float.class, int.class, int.class);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private final Effect particle;

    public ParticleEntityPre9(Effect particle) {
        this.particle = particle;
    }

    @Override
    public void playParticle(World world, Location loc) {
        try {
            ParticleEntityPre9.playWorldEffectMethod.invoke(world.spigot(), loc, this.particle, 0, 0, 0.f, 0.f, 0.f, 0.f, 1, ParticleEntityPre9.RADIUS);
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void playParticle(Player ply, Location loc) {
        try {
            ParticleEntityPre9.playPlayerEffectMethod.invoke(ply.spigot(), loc, this.particle, 0, 0, 0.f, 0.f, 0.f, 0.f, 1, ParticleEntityPre9.RADIUS);
        } catch (IllegalAccessException | NullPointerException | InvocationTargetException e) {
            //e.printStackTrace();
        }
    }
}
