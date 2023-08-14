package com.rogermiranda1000.versioncontroller.entities.bounds;

import org.bukkit.util.Vector;

public class BoundingBox {
    private final double minX, minY, minZ,
            maxX, maxY, maxZ;

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public Vector getMin() {
        return new Vector(this.minX, this.minY, this.minZ);
    }

    public Vector getMax() {
        return new Vector(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", minZ=" + minZ +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", maxZ=" + maxZ +
                '}';
    }
}
