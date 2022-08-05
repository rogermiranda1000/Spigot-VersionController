package com.rogermiranda1000.helper.blocks.file;

import com.rogermiranda1000.helper.blocks.CustomBlocksEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.security.InvalidParameterException;
import java.util.function.Function;

public class BasicBlock {
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final String object;

    public BasicBlock(Location loc, String object) {
        this.world = (loc.getWorld() != null) ? loc.getWorld().getName() : null;
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.object = object;
    }

    /**
     * @throws InvalidParameterException loader returned null
     */
    public static <T> CustomBlocksEntry<T>[]getEntries(@NotNull BasicBlock []basicBlocks, @NotNull Function<String,T> loader) throws InvalidParameterException {
        @SuppressWarnings("unchecked")
        CustomBlocksEntry<T>[] r = new CustomBlocksEntry[basicBlocks.length];
        for (int i = 0; i < r.length; i++) {
            BasicBlock o = basicBlocks[i];
            T object = loader.apply(o.object);
            if (object == null) /*throw new InvalidParameterException("Loader returns null while processing " + o.toString())*/ continue;
            r[i] = new CustomBlocksEntry<>(object, new Location(Bukkit.getWorld(o.world), (double)o.x, (double)o.y, (double)o.z));
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
