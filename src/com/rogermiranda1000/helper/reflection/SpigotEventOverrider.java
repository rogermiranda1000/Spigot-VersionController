package com.rogermiranda1000.helper.reflection;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpigotEventOverrider {
    /**
     * Finds the desired listener and remove it and returns it
     * @param plugin Plugin registering the listener
     * @param match Class registering the listener
     * @param event Event class to override
     * @return Event to launch
     * @throws ListenerNotFoundException Not found
     */
    public static <T extends Event> OnServerEvent<T> overrideListener(final @NotNull Plugin plugin, Class<?> match, Class<T> event) throws ListenerNotFoundException {
        final Listener lis = getListener(plugin, match);
        if (lis == null) throw new ListenerNotFoundException("Unable to override " + plugin.getName() + " event priority: Listener not found");

        HandlerList.unregisterAll(lis); // all the RegisteredListener on reload are the same Listener

        Method r = null;
        for (final Method m: match.getDeclaredMethods()) {
            // is it an event?
            if (m.getParameterCount() != 1) continue;
            if (!Event.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
            EventHandler eventHandler = m.getAnnotation(EventHandler.class);
            if (eventHandler == null) continue;
            final Class<? extends Event> type = m.getParameterTypes()[0].asSubclass(Event.class);
            if (!type.equals(event)) continue;

            // register again the event, but with the desired priority
            Bukkit.getPluginManager().registerEvent(type, lis, eventHandler.priority(), (l, e) -> {
                try {
                    try {
                        m.invoke(l, type.cast(e));
                    } catch (ClassCastException ignore) {}
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    System.err.println("Error while overriding " + plugin + " event (" + lis.getClass().getName() + "#" + m.getName() + ")");
                    ex.printStackTrace();
                    // TODO send error back to the plugin?
                }
            }, plugin, eventHandler.ignoreCancelled());
        }

        if (r == null) throw new ListenerNotFoundException();

        final Method rCpy = r;
        return (e) -> {
            try {
                rCpy.invoke(lis, e);
                return false;
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.err.println("Error while overriding " + plugin + " event (" + lis.getClass().getName() + "#" + rCpy.getName() + ")");
                ex.printStackTrace();
                // TODO send error back to the plugin?
                return true;
            }
        };
    }

    private static Listener getListener(@NotNull Plugin plugin, Class<?> match) {
        Listener lis = null;
        for (RegisteredListener l : HandlerList.getRegisteredListeners(plugin)) {
            if (l.getListener().getClass().equals(match)) {
                lis = l.getListener();
                break;
            }
        }
        return lis;
    }
}
