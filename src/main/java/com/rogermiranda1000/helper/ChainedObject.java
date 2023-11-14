package com.rogermiranda1000.helper;

import java.util.ArrayList;
import java.util.Collection;

public class ChainedObject<T> {
    private T current;
    private ChainedObject<T> next;

    public void add(T next) {
        if (this.current == null) this.current = next;
        else {
            if (this.next == null) this.next = new ChainedObject<T>();
            this.next.add(next);
        }
    }

    public Collection<T> get() {
        ArrayList<T> r = new ArrayList<>();
        this.get(r);
        return r;
    }

    private Collection<T> get(Collection<T> array) {
        if (this.current != null) array.add(this.current);
        if (this.next != null) this.next.get(array);
        return array;
    }
}
