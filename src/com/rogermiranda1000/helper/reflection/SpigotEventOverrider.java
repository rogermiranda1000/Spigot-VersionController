package com.rogermiranda1000.helper.reflection;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SpigotEventOverrider {
     /**
     * Finds the desired listener and remove it and returns it
     * @param plugin Plugin registering the listener
     * @param match Class registering the listener
     * @param event Event class to override
     * @param <T>   Event type
     * @return Event to launch
     * @throws ListenerNotFoundException Not found
     */
    public static <T extends Event> OnServerEvent<T> overrideListener(final @NotNull Plugin plugin, Class<?> match, Class<T> event) throws ListenerNotFoundException {
        final Listener lis = getListener(plugin, match);
        if (lis == null) throw new ListenerNotFoundException("Unable to override " + plugin.getName() + " event priority: Listener not found");

        HandlerList.unregisterAll(lis); // all the RegisteredListener on reload are the same Listener

        Method r = null;
        for (final Method m: match.getDeclaredMethods()) {
            final Class<? extends Event> type = SpigotEventOverrider.getEventType(m);
            if (type == null) continue; // not an event
            EventHandler eventHandler = m.getAnnotation(EventHandler.class);

            if (type.equals(event)) r = m;
            else {
                // register again the event
                Bukkit.getPluginManager().registerEvent(type, lis, eventHandler.priority(), (l, e) -> {
                    try {
                        try {
                            m.invoke(l, type.cast(e));
                        } catch (ClassCastException ignore) {}
                    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.err.println("Error while overriding " + plugin + " event (" + lis.getClass().getName() + "#" + m.getName() + ")");
                        ex.printStackTrace();
                        // TODO send report to Sentry
                    }
                }, plugin, eventHandler.ignoreCancelled());
            }
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
                // TODO send report to Sentry
                return true;
            }
        };
    }

    public static void wrapListeners(final @NotNull Plugin plugin, Listener lis, final OverridedEvent wrapper) {
        //HandlerList.unregisterAll(lis);

        Class<?> c = lis.getClass();
        while (c != null) {
            for (final Method m : c.getDeclaredMethods()) {
                final Class<? extends Event> type = SpigotEventOverrider.getEventType(m);
                if (type == null) continue; // not an event
                EventHandler eventHandler = m.getAnnotation(EventHandler.class);

                Bukkit.getPluginManager().registerEvent(type, lis, eventHandler.priority(), (l, e) -> wrapper.onEvent(() -> {
                    try {
                        m.invoke(l, type.cast(e));
                    } catch (ClassCastException ignore) {
                    }
                }), plugin, eventHandler.ignoreCancelled());
            }

            c = c.getSuperclass();
        }
    }

    /**
     * Get the type of that method
     * @param m Method
     * @return Method type (null if not aplicable)
     */
    @Nullable
    private static Class<? extends Event> getEventType(Method m) {
        // is it an event?
        if (m.getParameterCount() != 1) return null;
        if (!Event.class.isAssignableFrom(m.getParameterTypes()[0])) return null;
        EventHandler eventHandler = m.getAnnotation(EventHandler.class);
        if (eventHandler == null) return null;
        return m.getParameterTypes()[0].asSubclass(Event.class);
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
