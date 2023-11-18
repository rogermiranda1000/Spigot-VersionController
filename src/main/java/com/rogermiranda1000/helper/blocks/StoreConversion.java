package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface StoreConversion<T> {
    public BiFunction<T,Location,String> storeName();
    public BiFunction<String,Location,T> loadName();
}
