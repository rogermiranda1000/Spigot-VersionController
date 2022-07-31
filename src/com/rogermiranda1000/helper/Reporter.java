package com.rogermiranda1000.helper;

import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public interface Reporter {
    public void reportException(Throwable ex);

    /**
     * Repeated exceptions are exceptions inside a loop (that may occur multiple times each millisecond)
     * @param ex Exception
     */
    public void reportRepeatedException(Throwable ex);

    public void reportException(String err);

    public void userReport(@Nullable String contact, @Nullable String name, String message);
}
