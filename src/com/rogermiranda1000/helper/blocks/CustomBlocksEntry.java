package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;

import java.util.Map;

public class CustomBlocksEntry<T> implements Map.Entry<T, Location> {
    private final T key;
    private final Location value;

    public CustomBlocksEntry(T key, Location value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public T getKey() {
        return this.key;
    }

    @Override
    public Location getValue() {
        return this.value;
    }

    @Override
    public Location setValue(Location value) {
        return this.getValue();
    }
}
