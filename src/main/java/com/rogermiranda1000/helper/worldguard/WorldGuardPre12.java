package com.rogermiranda1000.helper.worldguard;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.Collection;

public class WorldGuardPre12 implements RegionDelimiter {
    @Override
    public boolean isInsideRegion(Location target, Collection<String> regionNames) {
        try {
            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
            Object vector = vectorClass.getConstructor(double.class, double.class, double.class)
                                    .newInstance(target.getX(), target.getY(), target.getZ());
            ApplicableRegionSet regions = (ApplicableRegionSet)RegionManager.class.getMethod("getApplicableRegions", vectorClass)
                                                .invoke(WGBukkit.getRegionManager(target.getWorld()), vector);
            for (ProtectedRegion pr : regions) {
                if (regionNames.contains(pr.getId())) return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
