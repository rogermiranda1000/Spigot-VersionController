package com.rogermiranda1000.helper;

import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public interface Reporter {
    public void reportException(Exception ex);

    public void reportException(String err);

    public void userReport(@Nullable String contact, String message);
}
