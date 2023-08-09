package com.rogermiranda1000.helper.blocks;

import java.util.function.Function;

public interface StoreConversion<T> {
    public Function<T,String> storeName();
    public Function<String,T> loadName();
}
