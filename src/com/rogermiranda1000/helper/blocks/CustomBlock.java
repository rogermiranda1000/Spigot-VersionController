package com.rogermiranda1000.helper.blocks;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;
import com.github.davidmoten.rtreemulti.geometry.Rectangle;
import com.github.davidmoten.rtreemulti.geometry.internal.PointDouble;
import com.github.davidmoten.rtreemulti.internal.EntryDefault;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.helper.blocks.file.BasicBlock;
import com.rogermiranda1000.helper.blocks.file.BasicLocation;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final boolean overrideProtections, onEventSuceedRemove;

    /**
     * @param id File save name
     * @param overrideProtections Launch ProtectionOverrider
     *                            /!\\ Must be called from a plugin with dependencies/soft-dependencies of WorldGuard and Residence /!\\
     * @param onEventSuceedRemove After a successful event (not canceled) remove the block from the list
     */
    public CustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions) {
        this.gson = new GsonBuilder()/*.setPrettyPrinting()*/.create();
        this.plugin = plugin;
        this.id = id;
        this.isTheSameCustomBlock = isTheSameCustomBlock;
        this.storeFunctions = storeFunctions;
        this.overrideProtections = overrideProtections;
        this.onEventSuceedRemove = onEventSuceedRemove;

        this.removeAllBlocksArtificially();
    }

    public <O> CustomBlock(RogerPlugin plugin, String id, CustomBlockComparer isTheSameCustomBlock, boolean overrideProtections, boolean onEventSuceedRemove, final @Nullable ComplexStoreConversion<T,O> storeFunctions) {
        this(plugin, id, isTheSameCustomBlock, overrideProtections, onEventSuceedRemove, new StoreConversion<T>(){
            private final Gson gson = new Gson();

            public Function<T,String> storeName() {
                return in->this.gson.toJson((O) storeFunctions.storeName().apply((T) in));
            }

            public Function<String,T> loadName() {
                return in->storeFunctions.loadName().apply(this.gson.fromJson(in, storeFunctions.getOutputClass()));
            }
        });
    }

    /**
     * @param id File save name
     */
    public CustomBlock(RogerPlugin plugin, String id, @NotNull final BlockType block, boolean overrideProtections, boolean onEventSuceedRemove, @Nullable StoreConversion<T> storeFunctions) {
        this(plugin, id, (e)->{
            if (!(e instanceof BlockEvent)) return false;
            return block.equals(VersionController.get().getObject(((BlockEvent)e).getBlock()));
        }, overrideProtections, onEventSuceedRemove, storeFunctions);
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

    private File getCustomBlockFile() {
        File folder = this.plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();

        folder = new File(folder.getPath() + File.separatorChar + "CustomBlocks");
        if (!folder.exists()) {
            folder.mkdirs();

            // first time creating the folder; create a warning
            File readme = new File(folder, "README.txt");
            try (FileWriter fw = new FileWriter(readme)) {
                fw.write("This are internal-use only files; do not modify unless specifically suggested.");
            } catch (IOException ignored) {}
        }

        return new File(folder, this.id + ".json");
    }

    public String getId() {
        return id;
    }

    public void load() throws IOException {
        if (!this.willSave()) return;

        try {
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(this.getCustomBlockFile());
            while (scanner.hasNextLine()) sb.append(scanner.nextLine());
            scanner.close();

            CustomBlocksEntry<T> []blocks = BasicBlock.getEntries(this.gson.fromJson(sb.toString(), BasicBlock[].class), this.storeFunctions.loadName());
            for (CustomBlocksEntry<T> e : blocks) this.placeBlockArtificially(e);
        } catch (JsonSyntaxException ex) {
            throw new IOException(ex.getMessage());
        } catch (FileNotFoundException ignore) {
            return; // no file, no blocks :)
        }
    }

    public void save() throws IOException {
        if (!this.willSave()) return;

        // get the output
        final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
        this.getAllBlocks(e -> basicBlocks.add(new BasicBlock(e.getValue(), storeFunctions.storeName().apply(e.getKey()))));

        if (basicBlocks.size() > 0) {
            // write
            try (FileWriter fw = new FileWriter(this.getCustomBlockFile())) {
                this.gson.toJson(basicBlocks, fw);
            }
        }
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
        Block b = e.getBlock();
        T rem;
        if (this.blocks == null || !this.isTheSameCustomBlock.isSameCustomBlock(e) || (rem = this.getBlock(b.getLocation())) == null) {
            if (this.overrideProtections) ProtectionOverrider.shouldOccurs(e, this);
            return;
        }

        boolean shouldOverride = this.onCustomBlockBreak(e, rem);
        if (!shouldOverride && this.overrideProtections) ProtectionOverrider.shouldOccurs(e, this);
        if (!e.isCancelled() && this.onEventSuceedRemove) this.removeBlockArtificially(b.getLocation(), rem);
    }

    // TODO onUse, onStep

    /**
     * Lacking some coordinates, locate all the blocks in that area
     */
    public void getBlocksLackingCoordinate(@Nullable World world, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        double w1_min = -Double.MAX_VALUE, w1_max = Double.MAX_VALUE,
                w2_min = -Double.MAX_VALUE, w2_max = Double.MAX_VALUE,
                x_min = -Double.MAX_VALUE, x_max = Double.MAX_VALUE,
                y_min = -Double.MAX_VALUE, y_max = Double.MAX_VALUE,
                z_min = -Double.MAX_VALUE, z_max = Double.MAX_VALUE;

        if (world != null) {
            w1_min = w1_max = Double.longBitsToDouble(world.getUID().getMostSignificantBits());
            w2_min = w2_max = Double.longBitsToDouble(world.getUID().getLeastSignificantBits());
        }
        if (x != null) x_min = x_max = x;
        if (y != null) y_min = y_max = y;
        if (z != null) z_min = z_max = z;

        this.blocks.search(Rectangle.create(w1_min, w2_min, x_min, y_min, z_min,
                w1_max, w2_max, x_max, y_max, z_max)).forEach(e -> blockConsumer.accept(new CustomBlocksEntry<T>(e.value(), CustomBlock.getLocation(e.geometry()))));
    }

    synchronized public void placeBlockArtificially(T add, Location loc) {
        this.blocks = this.blocks.add(add, CustomBlock.getPoint(loc));
    }

    public void placeBlockArtificially(CustomBlocksEntry<T> add) {
        this.placeBlockArtificially(add.getKey(), add.getValue());
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
    public void getAllBlocks(final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        List<CustomBlocksEntry<T>> r = new ArrayList<>();
        synchronized (this) {
            this.blocks.entries().forEach(e -> r.add(new CustomBlocksEntry<>(e.value(), CustomBlock.getLocation(e.geometry()))));
        }

        for (CustomBlocksEntry<T> e : r) blockConsumer.accept(e);
    }

    synchronized public int getNumBlocks() {
        return this.blocks.size();
    }

    /**
     * Get all the locations by value.
     * O(n); not recommended using
     * Note: T must have implemented a valid 'equals' function
     * @param val           Value
     * @param blockConsumer Function to call for each value found
     */
    public void getAllBlocksByValue(@NotNull final T val, final Consumer<CustomBlocksEntry<T>> blockConsumer) {
        this.getAllBlocks((e) -> {
            if (val.equals(e.getKey())) blockConsumer.accept(e);
        });
    }

    /**
     * Get all values.
     * O(n); not recommended using
     * Note: T must have implemented a valid 'equals' function
     */
    public Set<T> getAllValues() {
        final Set<T> r = new HashSet<>();
        this.getAllBlocks(e -> r.add(e.getKey()));
        return r;
    }

    /**
     * Get the number of different values.
     * O(n); not recommended using
     * Note: T must have implemented a valid 'equals' function
     */
    public int getDifferentValuesNum() {
        return this.getAllValues().size();
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
