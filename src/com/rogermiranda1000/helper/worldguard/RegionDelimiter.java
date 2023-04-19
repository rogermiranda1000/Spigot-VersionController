package com.rogermiranda1000.helper.worldguard;

import org.bukkit.Location;

import java.util.Collection;
import java.util.function.BinaryOperator;

public interface RegionDelimiter {
    /**
     * Check if a block is inside a region
     * @param target Block to check
     * @param regionNames Regions to check
     * @return If the block is inside any of the mentioned regions (true) or not (false)
     */
    boolean isInsideRegion(Location target, Collection<String> regionNames);

    /**
     * Adds a boolean flag
     * @param flagName Flag name
     * @param defaultValue Default value
     */
    void setupFlag(String flagName, boolean defaultValue);

    Boolean getFlagValue(Location target, String flag, BinaryOperator<Boolean> reduce);
}
