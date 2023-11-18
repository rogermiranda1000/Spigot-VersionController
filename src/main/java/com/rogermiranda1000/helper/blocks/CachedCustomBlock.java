package com.rogermiranda1000.helper.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CustomBlock with a HashMap to save the T
 * Solves some O(n) problems that CustomBlock has
 * @param <T>   The block information to save
 *              Note: T must have implemented a valid 'equals' and 'hash' functions
 */
public abstract class CachedCustomBlock<T,O> extends CustomBlock<T> {
    private HashMap<O, List<Location>> cache;
    private final boolean preserveObjects;
    private final Function<T,O> cacheByKey;

    /**
     * @param id File save name
     * @param preserveObjects Even if the last location is removed, preserve. The methods removeAllBlocksArtificially, addObject and removeObject overrides this functionality
     */
    public CachedCustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects, Function<T,O> cacheByKey) {
        super(plugin, id, isTheSameCustomBlock, overrideProtections, onEventSuceedRemove, storeFunctions);
        this.preserveObjects = preserveObjects;
        this.cacheByKey = cacheByKey;
        // removeAllBlocksArtificially called by super
    }

    /**
     * @param id File save name
     */
    public CachedCustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects, Function<T,O> cacheByKey) {
        super(plugin, id, block, overrideProtections, onEventSuceedRemove, storeFunctions);
        this.preserveObjects = preserveObjects;
        this.cacheByKey = cacheByKey;
        // removeAllBlocksArtificially called by super
    }

    synchronized public Set<O> getAllCValues() {
        return this.cache.keySet();
    }

    @Override
    synchronized public int getDifferentValuesNum() {
        return this.cache.size(); // optimized
    }

    /**
     * Useful while using preserveObjects
     */
    synchronized public void addObject(T obj) {
        O toAdd = this.cacheByKey.apply(obj);
        if (!this.cache.containsKey(toAdd)) this.cache.put(toAdd, new ArrayList<>());
    }

    /**
     * Useful while using preserveObjects
     */
    synchronized public void removeObject(T obj) {
        this.cache.remove(this.cacheByKey.apply(obj));
    }

    /**
     * Get all the locations by value.
     * O(n); not recommended using
     * @param val           Value
     * @param blockConsumer Function to call for each value found
     */
    @Override
    public void getAllBlocksByValue(@NotNull final T val, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        List<Location> locations = this.getAllBlocksByValue(val);
        if (locations == null) return; // however, it shouldn't happen
        locations.forEach(l -> blockConsumer.accept(new CustomBlocksEntry<>(val, l)));
    }

    /**
     * Get all the locations by value.
     * @param val           Value
     * @return              Values found
     */
    @Nullable
    synchronized public List<Location> getAllBlocksByValue(@NotNull final T val) {
        return this.cache.get(this.cacheByKey.apply(val));
    }

    @Override
    public void placeBlockArtificially(T add, Location loc) {
        super.placeBlockArtificially(add, loc);
        synchronized (this) {
            O toAdd = this.cacheByKey.apply(add);
            List<Location> s = this.cache.get(toAdd);
            if (s == null) {
                s = new ArrayList<>();
                this.cache.put(toAdd, s);
            }

            s.add(loc);
        }
    }

    @Override
    protected void removeBlockArtificially(Location loc, @NotNull T rem) {
        super.removeBlockArtificially(loc, rem);
        synchronized (this) {
            O toRemove = this.cacheByKey.apply(rem);
            List<Location> s = this.cache.get(toRemove);
            s.remove(loc);
            if (!this.preserveObjects && s.isEmpty()) this.cache.remove(toRemove); // the last element was removed
        }
    }

    @Override
    synchronized public void removeBlocksArtificiallyByValue(@NotNull final T val, @Nullable final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        O toRemove = this.cacheByKey.apply(val);
        List<Location> s = this.cache.get(toRemove);
        if (s == null) return;
        for (Location loc : s) {
            this.blocks = this.blocks.delete(val, CustomBlock.getPoint(loc));
            if (blockConsumer != null) blockConsumer.accept(new CustomBlocksEntry<>(val, loc));
        }
        if (!this.preserveObjects) this.cache.remove(toRemove);
    }

    @Override
    public void removeAllBlocksArtificially() {
        super.removeAllBlocksArtificially();
        synchronized (this) {
            this.cache = new HashMap<>();
        }
    }
}
