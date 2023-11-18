package com.rogermiranda1000.helper;

import java.util.Iterator;

public class IteratorIterator<T> implements Iterator<T> {
    private final Iterator<T> is[];
    private int current;

    @SafeVarargs
    public IteratorIterator(Iterator<T>... iterators) {
        is = iterators;
        current = 0;
    }

    @Override
    public boolean hasNext() {
        while ( current < is.length && !is[current].hasNext() )
            current++;

        return current < is.length;
    }

    @Override
    public T next() {
        while ( current < is.length && !is[current].hasNext() )
            current++;

        return is[current].next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Unimplemented");
    }
}