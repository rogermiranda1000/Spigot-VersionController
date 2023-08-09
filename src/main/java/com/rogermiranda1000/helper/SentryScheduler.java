package com.rogermiranda1000.helper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public class SentryScheduler implements BukkitScheduler {
    private final Function<Runnable,Runnable> secureRun;

    public SentryScheduler(Reporter reporter) {
        this.secureRun = ((r)->{
            return ()->{
                try {
                    r.run();
                } catch (Throwable ex) {
                    reporter.reportRepeatedException(ex);
                }
            };
        });
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin var1, @NotNull Runnable var2, long var3) {
        return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(var1, this.secureRun.apply(var2), var3);
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin var1, @NotNull Runnable var2) {
        return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(var1, this.secureRun.apply(var2));
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin var1, @NotNull Runnable var2, long var3, long var5) {
        return Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(var1, this.secureRun.apply(var2), var3, var5);
    }

    @Override
    @NotNull
    public <T> Future<T> callSyncMethod(@NotNull Plugin var1, @NotNull Callable<T> var2) {
        return null; // TODO
    }

    @Override
    public void cancelTask(int var1) {
        Bukkit.getServer().getScheduler().cancelTask(var1);
    }

    @Override
    public void cancelTasks(@NotNull Plugin var1) {
        Bukkit.getServer().getScheduler().cancelTasks(var1);
    }

    @Override
    public boolean isCurrentlyRunning(int var1) {
        return Bukkit.getServer().getScheduler().isCurrentlyRunning(var1);
    }

    @Override
    public boolean isQueued(int var1) {
        return Bukkit.getServer().getScheduler().isQueued(var1);
    }

    @Override
    @NotNull
    public List<BukkitWorker> getActiveWorkers() {
        return Bukkit.getServer().getScheduler().getActiveWorkers();
    }

    @Override
    @NotNull
    public List<BukkitTask> getPendingTasks() {
        return Bukkit.getServer().getScheduler().getPendingTasks();
    }

    @Override
    @NotNull
    public BukkitTask runTask(@NotNull Plugin var1, @NotNull Runnable var2) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTask(var1, this.secureRun.apply(var2));
    }

    @Override
    public void runTask(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2) throws IllegalArgumentException {
        // TODO
    }

    @Override
    @NotNull
    public BukkitTask runTaskAsynchronously(@NotNull Plugin var1, @NotNull Runnable var2) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTaskAsynchronously(var1, this.secureRun.apply(var2));
    }

    @Override
    public void runTaskAsynchronously(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2) throws IllegalArgumentException {
        // TODO
    }

    @Override
    @NotNull
    public BukkitTask runTaskLater(@NotNull Plugin var1, @NotNull Runnable var2, long var3) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTaskLater(var1, this.secureRun.apply(var2), var3);
    }

    @Override
    public void runTaskLater(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2, long var3) throws IllegalArgumentException {
        // TODO
    }

    @Override
    @NotNull
    public BukkitTask runTaskLaterAsynchronously(@NotNull Plugin var1, @NotNull Runnable var2, long var3) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(var1, this.secureRun.apply(var2), var3);
    }

    @Override
    public void runTaskLaterAsynchronously(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2, long var3) throws IllegalArgumentException {
        // TODO
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimer(@NotNull Plugin var1, @NotNull Runnable var2, long var3, long var5) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTaskTimer(var1, this.secureRun.apply(var2), var3, var5);
    }

    @Override
    public void runTaskTimer(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2, long var3, long var5) throws IllegalArgumentException {
        // TODO
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimerAsynchronously(@NotNull Plugin var1, @NotNull Runnable var2, long var3, long var5) throws IllegalArgumentException {
        return Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(var1, this.secureRun.apply(var2), var3, var5);
    }

    public void runTaskTimerAsynchronously(@NotNull Plugin var1, @NotNull Consumer<BukkitTask> var2, long var3, long var5) throws IllegalArgumentException {
        // TODO
    }

    /* DEPRECATED; DO NOT USE */

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin var1, @NotNull BukkitRunnable var2) {return 0;}

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3) {return 0;}

    @Deprecated
    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3, long var5) {return 0;}

    @Deprecated
    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin var1, @NotNull Runnable var2, long var3) {return 0;}

    @Deprecated
    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin var1, @NotNull Runnable var2) {return 0;}

    @Deprecated
    @Override
    public int scheduleAsyncRepeatingTask(@NotNull Plugin var1, @NotNull Runnable var2, long var3, long var5) {return 0;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTask(@NotNull Plugin var1, @NotNull BukkitRunnable var2) throws IllegalArgumentException {return null;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTaskAsynchronously(@NotNull Plugin var1, @NotNull BukkitRunnable var2) throws IllegalArgumentException {return null;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTaskLater(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3) throws IllegalArgumentException {return null;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTaskLaterAsynchronously(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3) throws IllegalArgumentException {return null;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTaskTimer(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3, long var5) throws IllegalArgumentException {return null;}

    @Override
    @Deprecated
    @NotNull
    public BukkitTask runTaskTimerAsynchronously(@NotNull Plugin var1, @NotNull BukkitRunnable var2, long var3, long var5) throws IllegalArgumentException {return null;}
}
