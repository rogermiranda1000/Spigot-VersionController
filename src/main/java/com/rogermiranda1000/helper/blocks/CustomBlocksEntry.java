package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomBlocksEntry<T> implements Map.Entry<T, Location> {
    @NotNull private final T key;
    @NotNull private final Location value;

    public CustomBlocksEntry(@NotNull T key, @NotNull Location value) {
        this.key = key;
        this.value = value;
    }

    @Override
    @NotNull
    public T getKey() {
        return this.key;
    }

    @Override
    @NotNull
    public Location getValue() {
        return this.value;
    }

    @Override
    public Location setValue(Location value) {
        return this.getValue();
    }
}
