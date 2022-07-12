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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A block with special meaning
 * @param <T> The block information to save
 */
public abstract class CustomBlock<T> implements Listener {
    protected static Point getPoint(Location loc) {
        if (loc.getWorld() == null) return Point.create(0,0,loc.getX(), loc.getY(), loc.getZ());

        return Point.create(Double.longBitsToDouble(loc.getWorld().getUID().getMostSignificantBits()),
                Double.longBitsToDouble(loc.getWorld().getUID().getLeastSignificantBits()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converts a point returned by getPoint into the original location
     */
    protected static Location getLocation(Point p) throws IllegalArgumentException {
        if (!(p instanceof PointDouble)) throw new IllegalArgumentException("Point must be instance of PointDouble!");
        double []values = ((PointDouble)p).mins();
        if (values.length != 5) throw new IllegalArgumentException("Point must have 5 elements!");
        UUID world = new UUID(Double.doubleToRawLongBits(values[0]), Double.doubleToRawLongBits(values[1]));
        return new Location(Bukkit.getWorld(world), values[2], values[3], values[4]);
    }

    private final Gson gson;
    private final RogerPlugin plugin;
    private final String id;
    protected RTree<T, Point> blocks;
    private final CustomBlockComparer isTheSameCustomBlock;
    @Nullable private final StoreConversion<T> storeFunctions;
    private boolean overrideProtections;

    /**
     * @param id File save name
     * @param overrideProtections Launch ProtectionOverrider
     *                            /!\\ Must be called from a plugin with dependencies/soft-dependencies of WorldGuard and Residence /!\\
     */
    public CustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, @Nullable StoreConversion<T> storeFunctions) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.plugin = plugin;
        this.id = id;
        this.isTheSameCustomBlock = isTheSameCustomBlock;
        this.storeFunctions = storeFunctions;

        this.overrideProtections = overrideProtections;
    }

    /**
     * @param id File save name
     */
    public CustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, @Nullable StoreConversion<T> storeFunctions) {
        this(plugin, id, (e)->{
            if (!(e instanceof BlockEvent)) return false;
            return block.equals(VersionController.get().getObject(((BlockEvent)e).getBlock()));
        }, overrideProtections, storeFunctions);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        if (overrideProtections) ProtectionOverrider.instantiate(plugin, this);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
        if (overrideProtections) ProtectionOverrider.deinstantiate(this);
    }

    public boolean willSave() {
        return this.storeFunctions != null;
    }

    public void load() {
        this.removeAllBlocksArtificially();
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
        File file = new File(this.plugin.getDataFolder(), this.id + ".json");
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
        if (!this.isTheSameCustomBlock.isSameCustomBlock(e)) return;

        T add = this.onCustomBlockPlace(e);
        if (e.isCancelled() || add == null) return;
        this.placeBlockArtificially(add, e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!this.isTheSameCustomBlock.isSameCustomBlock(e)) {
            if (this.overrideProtections) ProtectionOverrider.shouldOccurs(this);
            return;
        }

        Block b = e.getBlock();
        T rem = this.getBlock(b.getLocation());
        if (rem == null) {
            this.plugin.printConsoleWarningMessage("Expecting element at position " + b.getLocation().toString() + ", but instead 'null' found");
            if (this.overrideProtections) ProtectionOverrider.shouldOccurs(this);
            return;
        }

        boolean shouldOverride = this.onCustomBlockBreak(e, rem);
        if (!shouldOverride && this.overrideProtections) ProtectionOverrider.shouldOccurs(this);
        if (e.isCancelled()) return;
        this.removeBlockArtificially(b.getLocation(), rem);
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
    synchronized public void getAllBlocks(final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        this.blocks.entries().forEach(e -> blockConsumer.accept(new CustomBlocksEntry<>(e.value(), CustomBlock.getLocation(e.geometry()))));
    }

    /**
     * Get all the locations by value.
     * O(n); not recommended using
     * Note: T must have implemented a valid 'equals' function
     * @param val           Value
     * @param blockConsumer Function to call for each value found
     */
    synchronized public void getAllBlocksByValue(@NotNull final T val, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        this.getAllBlocks((e) -> {
            if (val.equals(e.getKey())) blockConsumer.accept(e);
        });
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

    protected void removeBlockArtificially(Location loc, @NotNull T rem) {
        Point pos = CustomBlock.getPoint(loc);
        synchronized (this) {
            this.blocks = this.blocks.delete(rem, pos);
        }
    }

    @Nullable
    public T removeBlockArtificially(Location loc) {
        T rem = this.getBlock(loc);
        if (rem == null) return null;
        this.removeBlockArtificially(loc, rem);
        return rem;
    }

    /**
     * Remove from the list all the occurrences of val
     * Note: T must have implemented a valid 'equals' function
     * @param val           Object to remove
     * @param blockConsumer Get the removed objects (null if not desired)
     */
    synchronized public void removeBlocksArtificiallyByValue(@NotNull final T val, @Nullable final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        // /!\\ FOR OPTIMIZATION REASON, THIS CODE IS DUPLICATED IN CachedCustomBlock; CHANGE THAT CODE TOO /!\\
        ArrayList<Entry<T,Point>> rem = new ArrayList<>();
        this.blocks.entries().forEach(e -> {
            if (val.equals(e.value())) {
                rem.add(new EntryDefault<>(e.value(), e.geometry()));
                if (blockConsumer != null) blockConsumer.accept(new CustomBlocksEntry<>(e.value(), CustomBlock.getLocation(e.geometry())));
            }
        });
        for (Entry<T, Point> r : rem) this.blocks = this.blocks.delete(r);
    }

    synchronized public void removeAllBlocksArtificially() {
        this.blocks = RTree.star().dimensions(5).create(); // MSB[world], LSB[world], x, y, z
    }

    /**
     * Remove from the list all the occurrences of val
     * Note: T must have implemented a valid 'equals' function
     * @param val           Object to remove
     */
    public void removeBlocksArtificiallyByValue(@NotNull final T val) {
        this.removeBlocksArtificiallyByValue(val, null);
    }

    /**
     * /!\\ Called BEFORE the object is added to the list /!\\
     * It won't be called if placeBlockArtificially
     * @return the object to add
     */
    @NotNull
    abstract public T onCustomBlockPlace(BlockPlaceEvent e);

    /**
     * /!\\ Called BEFORE the object is removed from the list /!\\
     * It won't be called if removeBlockArtificially
     * @return if overrideProtections, if the protection should be overridden (if not it's ignored)
     */
    abstract public boolean onCustomBlockBreak(BlockBreakEvent e, T element);
}
