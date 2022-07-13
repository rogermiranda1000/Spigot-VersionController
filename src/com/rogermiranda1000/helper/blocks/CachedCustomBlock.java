package com.rogermiranda1000.helper.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * CustomBlock with a HashMap to save the T
 * @param <T> The block information to save
 */
public abstract class CachedCustomBlock<T> extends CustomBlock<T> {
    private final HashMap<T, Set<Location>> cache;

    /**
     * @param id File save name
     */
    public CachedCustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions) {
        super(plugin, id, isTheSameCustomBlock, overrideProtections, onEventSuceedRemove, storeFunctions);
        this.cache = new HashMap<>();
    }

    /**
     * @param id File save name
     */
    public CachedCustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions) {
        super(plugin, id, block, overrideProtections, onEventSuceedRemove, storeFunctions);
        this.cache = new HashMap<>();
    }

    public Set<T> getAllValues() {
        return this.cache.keySet();
    }

    public int getDifferentValuesNum() {
        return this.cache.size();
    }

    @Override
    public void load() throws IOException {
        super.load();

        // synchronized by father
        super.getAllBlocks(e -> {
            Set<Location> s = this.cache.get(e.getKey());
            if (s == null) {
                s = new HashSet<>();
                this.cache.put(e.getKey(), s);
            }

            s.add(e.getValue());
        });
    }

    /**
     * Get all the placed blocks of this type
     * @param blockConsumer Function to execute for each block
     */
    @Override
    synchronized public void getAllBlocks(final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        for (Map.Entry<T,Set<Location>> e : this.cache.entrySet()) {
            for (Location loc : e.getValue()) blockConsumer.accept(new CustomBlocksEntry<>(e.getKey(), loc));
        }
    }

    /**
     * Get all the locations by value.
     * Note: T must have implemented a valid 'equals' function
     * @param val           Value
     * @param blockConsumer Function to call for each value found
     */
    @Override
    public void getAllBlocksByValue(@NotNull final T val, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        Set<Location> locations = this.getAllBlocksByValue(val);
        if (locations == null) return; // however, it shouldn't happen
        locations.forEach(l -> blockConsumer.accept(new CustomBlocksEntry<>(val, l)));
    }

    @Nullable
    synchronized public Set<Location> getAllBlocksByValue(@NotNull final T val) {
        return this.cache.get(val);
    }

    @Override
    public void placeBlockArtificially(T add, Location loc) {
        super.placeBlockArtificially(add, loc);
        synchronized (this) {
            Set<Location> s = this.cache.get(add);
            if (s == null) {
                s = new HashSet<>();
                this.cache.put(add, s);
            }

            s.add(loc);
        }
    }

    @Override
    protected void removeBlockArtificially(Location loc, @NotNull T rem) {
        super.removeBlockArtificially(loc, rem);
        synchronized (this) {
            Set<Location> s = this.cache.get(rem);
            s.remove(loc);
            if (s.size() == 0) this.cache.remove(rem); // the last element was removed
        }
    }

    @Override
    synchronized public void removeBlocksArtificiallyByValue(@NotNull final T val, @Nullable final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        Set<Location> s = this.cache.get(val);
        if (s == null) return;
        for (Location loc : s) {
            this.blocks = this.blocks.delete(val, CustomBlock.getPoint(loc));
            if (blockConsumer != null) blockConsumer.accept(new CustomBlocksEntry<>(val, loc));
        }
        this.cache.remove(val);
    }

    @Override
    public void removeAllBlocksArtificially() {
        super.removeAllBlocksArtificially();
        synchronized (this) {
            this.cache.clear();
        }
    }
}
