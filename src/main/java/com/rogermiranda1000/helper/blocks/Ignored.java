package com.rogermiranda1000.helper.blocks;

import org.bukkit.Location;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Ignored {
    public static class StoreIgnored implements StoreConversion<Ignored> {
        public StoreIgnored() {}

        @Override
        public BiFunction<Ignored, Location, String> storeName() {
            return (in,loc)->"";
        }

        @Override
        public BiFunction<String, Location, Ignored> loadName() {
            return (in,loc)->Ignored.get();
        }
    }

    public Ignored() { }

    private static Ignored instance = null;
    public static Ignored get() {
        if (Ignored.instance == null) Ignored.instance = new Ignored();
        return Ignored.instance;
    }
}
