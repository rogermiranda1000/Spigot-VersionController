package com.rogermiranda1000.helper.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CustomBlock with a HashMap to save the T
 * Solves some O(n) problems that CustomBlock has
 * @param <T>   The block information to save
 *              Note: T must have implemented a valid 'equals' and 'hash' functions
 */
public abstract class ComplexCachedCustomBlock<T,O> extends CustomBlock<T> {
    private HashMap<O, List<CustomBlocksEntry<T>>> cache;
    private final boolean preserveObjects;
    private final Function<T,O> cacheByKey;

    /**
     * @param id File save name
     * @param preserveObjects Even if the last location is removed, preserve. The methods removeAllBlocksArtificially, addObject and removeObject overrides this functionality
     */
    public ComplexCachedCustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects, Function<T,O> cacheByKey) {
        super(plugin, id, isTheSameCustomBlock, overrideProtections, onEventSuceedRemove, storeFunctions);
        this.preserveObjects = preserveObjects;
        this.cacheByKey = cacheByKey;
        // removeAllBlocksArtificially called by super
    }

    /**
     * @param id File save name
     */
    public ComplexCachedCustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects, Function<T,O> cacheByKey) {
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
    synchronized public void addObject(O toAdd) {
        if (!this.cache.containsKey(toAdd)) this.cache.put(toAdd, new ArrayList<>());
    }

    /**
     * Useful while using preserveObjects
     */
    synchronized public void removeObject(O toRemove) {
        this.cache.remove(toRemove);
    }

    /**
     * Get all the locations by value.
     * O(n); not recommended using
     * @param val           Value
     * @param blockConsumer Function to call for each value found
     */
    @Override
    public void getAllBlocksByValue(@NotNull final T val, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        List<CustomBlocksEntry<T>> locations = this.getAllBlocksByValue(this.cacheByKey.apply(val));
        if (locations == null) return; // however, it shouldn't happen
        locations.forEach(blockConsumer);
    }

    /**
     * Get all the locations by value.
     * @param val           Value
     * @return              Values found
     */
    @Nullable
    synchronized public List<CustomBlocksEntry<T>> getAllBlocksByValue(@NotNull final O val) {
        return this.cache.get(val);
    }

    @Override
    public void placeBlockArtificially(T add, Location loc) {
        super.placeBlockArtificially(add, loc);
        synchronized (this) {
            O toAdd = this.cacheByKey.apply(add);
            List<CustomBlocksEntry<T>> s = this.cache.get(toAdd);
            if (s == null) {
                s = new ArrayList<>();
                this.cache.put(toAdd, s);
            }

            s.add(new CustomBlocksEntry<>(add,loc));
        }
    }

    @Override
    protected void removeBlockArtificially(final Location loc, @NotNull T rem) {
        super.removeBlockArtificially(loc, rem);
        synchronized (this) {
            O toRemove = this.cacheByKey.apply(rem);
            List<CustomBlocksEntry<T>> s = this.cache.get(toRemove);
            s.removeIf(e -> e.getValue().equals(loc));
            if (!this.preserveObjects && s.isEmpty()) this.cache.remove(toRemove); // the last element was removed
        }
    }

    @Override
    synchronized public void removeBlocksArtificiallyByValue(@NotNull final T val, @Nullable final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        O toRemove = this.cacheByKey.apply(val);
        List<CustomBlocksEntry<T>> s = this.cache.get(toRemove);
        if (s == null) return;
        for (CustomBlocksEntry<T> e : s) {
            Location loc = e.getValue();
            this.blocks = this.blocks.delete(val, CustomBlock.getPoint(loc));
            if (blockConsumer != null) blockConsumer.accept(e);
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
