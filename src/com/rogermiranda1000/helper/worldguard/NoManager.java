package com.rogermiranda1000.helper.worldguard;

import org.bukkit.Location;

import java.util.Collection;
import java.util.function.BinaryOperator;

/**
 * WorldGuard not present; no region will return
 */
public class NoManager implements RegionDelimiter {
    @Override
    public boolean isInsideRegion(Location target, Collection<String> regionNames) {
        return false;
    }

    @Override
    public void setupFlag(String flagName, boolean defaultValue) { }

    @Override
    public Boolean getFlagValue(Location target, String flag, BinaryOperator<Boolean> reduce) {
        return null;
    }
}
