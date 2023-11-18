package com.rogermiranda1000.helper.blocks;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.jetbrains.annotations.NotNull;

public abstract class CachedCustomBlock<T> extends ComplexCachedCustomBlock<T,T> {
    public CachedCustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects) {
        super(plugin, id, isTheSameCustomBlock, overrideProtections, onEventSuceedRemove, storeFunctions, preserveObjects, e->e);
    }

    /**
     * @param id File save name
     */
    public CachedCustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions, boolean preserveObjects) {
        super(plugin, id, block, overrideProtections, onEventSuceedRemove, storeFunctions, preserveObjects, e->e);
    }
}
