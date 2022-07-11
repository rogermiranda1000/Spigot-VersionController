package com.rogermiranda1000.helper.reflection;

import org.bukkit.event.Event;

public interface OnServerEvent<T extends Event> {
    /**
     * @retval TRUE     An error has occurred
     * @retval FALSE    All ok
     */
    public boolean onEvent(T e);
}
