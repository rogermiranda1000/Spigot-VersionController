package com.rogermiranda1000.helper.blocks;

import java.util.function.Function;

public class Ignored {
    public static class StoreIgnored implements StoreConversion<Ignored> {
        public StoreIgnored() {}

        @Override
        public Function<Ignored, String> storeName() {
            return (in)->"";
        }

        @Override
        public Function<String, Ignored> loadName() {
            return (in)->Ignored.get();
        }
    }

    public Ignored() { }

    private static Ignored instance = null;
    protected static Ignored get() {
        if (Ignored.instance == null) Ignored.instance = new Ignored();
        return Ignored.instance;
    }
}
