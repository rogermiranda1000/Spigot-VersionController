package com.rogermiranda1000.helper;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;
import com.github.davidmoten.rtreemulti.internal.EntryDefault;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A block with special meaning
 * @param <T> The block information to save
 */
public abstract class CustomBlock<T> implements Listener {
    private RTree<T, Point> blocks;
    private final Function<Block, Boolean> isTheSameCustomBlock;
    private final boolean storeInFile;

    private static Point getPoint(Location loc) {
        if (loc.getWorld() == null) return Point.create(0,0,loc.getX(), loc.getY(), loc.getZ());

        return Point.create(Double.longBitsToDouble(loc.getWorld().getUID().getMostSignificantBits()),
                Double.longBitsToDouble(loc.getWorld().getUID().getLeastSignificantBits()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    public CustomBlock(@NotNull Function<Block, Boolean> isTheSameCustomBlock, boolean storeInFile) {
        this.isTheSameCustomBlock = isTheSameCustomBlock;
        this.storeInFile = storeInFile;
    }

    public CustomBlock(@NotNull final BlockType block, boolean storeInFile) {
        this((b)->block.equals(VersionController.get().getObject(b)), storeInFile);
    }

    public void load() {
        this.blocks = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
        if (!this.storeInFile) return;

        // TODO load
    }

    public void save() {
        if (!this.storeInFile) return;
        // TODO save
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (!this.isTheSameCustomBlock.apply(b)) return;

        T add = this.onCustomBlockPlace(e, this);
        if (e.isCancelled() || add == null) return;
        this.placeBlockArtificially(add, b);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (!this.isTheSameCustomBlock.apply(b)) return;

        T rem = this.getBlock(b.getLocation());
        if (rem == null) throw new RuntimeException("Expecting element at position " + b.getLocation().toString() + ", but instead 'null' found");

        this.onCustomBlockBreak(e, this, rem);
        if (e.isCancelled()) return;
        this.blocks = this.blocks.delete(rem, CustomBlock.getPoint(b.getLocation()));
    }

    // TODO onUse, onStep

    public void placeBlockArtificially(T add, Location loc) {
        this.blocks = this.blocks.add(add, CustomBlock.getPoint(loc));
    }

    public void placeBlocksArtificially(T add, Iterable<Location> loc) {
        loc.forEach(l -> this.placeBlockArtificially(add, l));
    }

    public void placeBlockArtificially(T add, Block block) {
        this.placeBlockArtificially(add, block.getLocation());
    }

    /*public void placeBlocksArtificially(T add, Iterable<Block> blocks) {
        blocks.forEach(b -> this.placeBlockArtificially(add, b));
    }*/

    /**
     * Get all the placed blocks of this type
     * @param blockConsumer Function to execute for each block
     */
    public void getAllBlocks(final Consumer<T> blockConsumer) {
        this.blocks.entries().forEach(e -> blockConsumer.accept(e.value()));
    }

    /**
     * Get the custom block (if any) in the location loc
     * @param loc   Location to search the block
     * @return      Custom block; null if none
     */
    @Nullable
    public T getBlock(Location loc) {
        Iterator<Entry<T, Point>> results = this.blocks.search(CustomBlock.getPoint(loc)).iterator();

        if (!results.hasNext()) return null;
        return results.next().value();
    }

    public boolean removeBlockArtificially(Location loc) {
        T rem = this.getBlock(loc);
        if (rem == null) return false;

        Point pos = CustomBlock.getPoint(loc);
        this.blocks = this.blocks.delete(rem, pos);
        return true;
    }

    /**
     * Remove from the list all the occurrences of val
     * Note: T must have implemented a valid 'equals' function
     * @param val Object to remove
     */
    public void removeBlocksArtificiallyByValue(@NotNull final T val) {
        ArrayList<Entry<T,Point>> rem = new ArrayList<>();
        this.blocks.entries().forEach(e -> {
            if (val.equals(e.value())) rem.add( new EntryDefault<>(e.value(), e.geometry()) );
        });
        for (Entry<T,Point> r : rem) this.blocks = this.blocks.delete(r);
    }

    /**
     * /!\\ Called BEFORE the object is added to the list /!\\
     * It won't be called if placeBlockArtificially
     * @return the object to add (can't be null)
     */
    abstract public T onCustomBlockPlace(BlockPlaceEvent e, CustomBlock<T> cb);

    /**
     * /!\\ Called BEFORE the object is removed from the list /!\\
     * It won't be called if removeBlockArtificially
     */
    abstract public void onCustomBlockBreak(BlockBreakEvent e, CustomBlock<T> cb, T element);
}
