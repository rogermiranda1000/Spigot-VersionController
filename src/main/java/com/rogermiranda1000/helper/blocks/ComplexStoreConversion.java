package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;

import java.util.function.BiFunction;

public interface ComplexStoreConversion<T,O> {
    /**
     * @pre object must be converted into string by gson
     */
    public BiFunction<T,Location,O> storeName();

    /**
     * @pre object must be converted from string by gson
     */
    public BiFunction<O,Location,T> loadName();

    // TODO via reflection
    public Class<O> getOutputClass();
}
