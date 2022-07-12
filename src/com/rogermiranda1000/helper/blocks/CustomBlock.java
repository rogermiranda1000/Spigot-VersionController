package com.rogermiranda1000.helper.blocks;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;
import com.github.davidmoten.rtreemulti.geometry.internal.PointDouble;
import com.github.davidmoten.rtreemulti.internal.EntryDefault;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A block with special meaning
 * @param <T> The block information to save
 */
public abstract class CustomBlock<T> implements Listener {
    private static final String FILE_NAME = "CustomBlocks.yml";

    private final Gson gson;
    private final RogerPlugin plugin;
    private RTree<T, Point> blocks;
    private final Function<Block, Boolean> isTheSameCustomBlock;
    @Nullable private final StoreConversion<T> storeFunctions;

    private static Point getPoint(Location loc) {
        if (loc.getWorld() == null) return Point.create(0,0,loc.getX(), loc.getY(), loc.getZ());

        return Point.create(Double.longBitsToDouble(loc.getWorld().getUID().getMostSignificantBits()),
                Double.longBitsToDouble(loc.getWorld().getUID().getLeastSignificantBits()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converts a point returned by getPoint into the original location
     */
    private static Location getLocation(Point p) throws IllegalArgumentException {
        if (!(p instanceof PointDouble)) throw new IllegalArgumentException("Point must be instance of PointDouble!");
        double []values = ((PointDouble)p).mins();
        if (values.length != 5) throw new IllegalArgumentException("Point must have 5 elements!");
        UUID world = new UUID(Double.doubleToRawLongBits(values[0]), Double.doubleToRawLongBits(values[1]));
        return new Location(Bukkit.getWorld(world), values[2], values[3], values[4]);
    }

    public CustomBlock(RogerPlugin plugin, Function<Block, Boolean> isTheSameCustomBlock, @Nullable StoreConversion<T> storeFunctions) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.plugin = plugin;
        this.isTheSameCustomBlock = isTheSameCustomBlock;
        this.storeFunctions = storeFunctions;
    }

    public boolean willSave() {
        return this.storeFunctions != null;
    }

    public CustomBlock(RogerPlugin plugin, @NotNull final BlockType block, @Nullable StoreConversion<T> storeFunctions) {
        this(plugin, (b)->block.equals(VersionController.get().getObject(b)), storeFunctions);
    }

    public void load() {
        synchronized (this) {
            this.blocks = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
        }
        if (!this.willSave()) return;

        // TODO load
    }

    public void save() throws IOException {
        if (!this.willSave()) return;

        // get the output
        final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
        synchronized (this) {
            this.blocks.entries().forEach(e -> basicBlocks.add(getBasicBlock(e)));
        }

        // write
        File file = new File(this.plugin.getDataFolder(), CustomBlock.FILE_NAME);
        FileWriter fw = new FileWriter(file);
        this.gson.toJson(basicBlocks, fw);
        fw.close();
    }

    /**
     * @pre storeFunctions != null
     */
    private BasicBlock getBasicBlock(Entry<T,Point> e) {
        return new BasicBlock(CustomBlock.getLocation(e.geometry()),
                this.storeFunctions.storeName().apply(e.value()));
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
        if (rem == null) {
            this.plugin.printConsoleWarningMessage("Expecting element at position " + b.getLocation().toString() + ", but instead 'null' found");
            return;
        }

        this.onCustomBlockBreak(e, this, rem);
        if (e.isCancelled()) return;
        synchronized (this) {
            this.blocks = this.blocks.delete(rem, CustomBlock.getPoint(b.getLocation()));
        }
    }

    // TODO onUse, onStep

    synchronized public void placeBlockArtificially(T add, Location loc) {
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
    synchronized public void getAllBlocks(final Consumer<T> blockConsumer) {
        this.blocks.entries().forEach(e -> blockConsumer.accept(e.value()));
    }

    /**
     * Get the custom block (if any) in the location loc
     * @param loc   Location to search the block
     * @return      Custom block; null if none
     */
    @Nullable
    synchronized public T getBlock(Location loc) {
        Iterator<Entry<T, Point>> results = this.blocks.search(CustomBlock.getPoint(loc)).iterator();

        if (!results.hasNext()) return null;
        return results.next().value();
    }

    public boolean removeBlockArtificially(Location loc) {
        T rem = this.getBlock(loc);
        if (rem == null) return false;

        Point pos = CustomBlock.getPoint(loc);
        synchronized (this) {
            this.blocks = this.blocks.delete(rem, pos);
        }
        return true;
    }

    /**
     * Remove from the list all the occurrences of val
     * Note: T must have implemented a valid 'equals' function
     * @param val Object to remove
     */
    synchronized public void removeBlocksArtificiallyByValue(@NotNull final T val) {
        ArrayList<Entry<T,Point>> rem = new ArrayList<>();
        this.blocks.entries().forEach(e -> {
            if (val.equals(e.value())) rem.add(new EntryDefault<>(e.value(), e.geometry()));
        });
        for (Entry<T, Point> r : rem) this.blocks = this.blocks.delete(r);
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
