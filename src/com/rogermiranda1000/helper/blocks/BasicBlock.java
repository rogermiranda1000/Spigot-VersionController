package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;

public class BasicBlock {
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final String object;

    public BasicBlock(Location loc, String object) {
        this.world = (loc.getWorld() != null) ? loc.getWorld().getName() : null;
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.object = object;
    }
}
