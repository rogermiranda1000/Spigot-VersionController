package com.rogermiranda1000.helper.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.function.Function;

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

    /**
     * @throws InvalidParameterException loader returned null
     */
    public static <T> CustomBlocksEntry<T> []getEntries(@NotNull BasicBlock []basicBlocks, @NotNull Function<String,T> loader) throws InvalidParameterException {
        @SuppressWarnings("unchecked")
        CustomBlocksEntry<T>[] r = new CustomBlocksEntry[basicBlocks.length];
        for (int i = 0; i < r.length; i++) {
            BasicBlock o = basicBlocks[i];
            T object = loader.apply(o.object);
            if (object == null) throw new InvalidParameterException("Loader returns null while processing " + o.toString());
            r[i] = new CustomBlocksEntry<>(object, new Location(Bukkit.getWorld(o.world), o.x, o.y, o.z));
        }
        return r;
    }

    @Override
    public String toString() {
        return "BasicBlock{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", object='" + object + '\'' +
                '}';
    }
}
