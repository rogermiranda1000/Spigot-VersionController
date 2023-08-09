package com.rogermiranda1000.helper.reflection;


import java.lang.reflect.InvocationTargetException;

public interface OverridedEvent {
    public static interface OverridedMethod {
        public void run() throws SecurityException, InvocationTargetException, IllegalArgumentException, IllegalAccessException;
    }

    public void onEvent(OverridedMethod run);
}
