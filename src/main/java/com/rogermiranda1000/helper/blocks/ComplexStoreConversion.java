package com.rogermiranda1000.helper.blocks;

import java.util.function.Function;

public interface ComplexStoreConversion<T,O> {
    /**
     * @pre object must be converted into string by gson
     */
    public Function<T,O> storeName();

    /**
     * @pre object must be converted from string by gson
     */
    public Function<O,T> loadName();

    // TODO via reflection
    public Class<O> getOutputClass();
}
