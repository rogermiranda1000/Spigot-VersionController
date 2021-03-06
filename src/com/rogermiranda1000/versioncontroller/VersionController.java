package com.rogermiranda1000.versioncontroller;

import com.rogermiranda1000.versioncontroller.blocks.BlockManager;
import com.rogermiranda1000.versioncontroller.blocks.BlockPost13;
import com.rogermiranda1000.versioncontroller.blocks.BlockPre13;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import com.rogermiranda1000.versioncontroller.entities.EntityManager;
import com.rogermiranda1000.versioncontroller.entities.EntityPaper;
import com.rogermiranda1000.versioncontroller.entities.EntitySpigot;
import com.rogermiranda1000.versioncontroller.items.ItemManager;
import com.rogermiranda1000.versioncontroller.items.ItemPost9;
import com.rogermiranda1000.versioncontroller.items.ItemPre9;
import com.rogermiranda1000.versioncontroller.particles.ParticleEntity;
import com.rogermiranda1000.versioncontroller.particles.ParticleManager;
import com.rogermiranda1000.versioncontroller.particles.ParticlePost9;
import com.rogermiranda1000.versioncontroller.particles.ParticlePre9;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Singleton object for cross-version compatibility
 */
public class VersionController extends ItemManager implements BlockManager, ParticleManager, EntityManager {
    private static VersionController versionController = null;
    public static final Version version = VersionController.getVersion();
    public static final boolean isPaper = VersionController.getMCPaper();
    public static final String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
    public static final String nmsPackage = bukkitPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
    public static final String runningJarPath = VersionController.getJarPath();

    private static final BlockManager blockManager = (VersionController.version.compareTo(Version.MC_1_13) < 0) ? new BlockPre13() : new BlockPost13();
    private static final ItemManager itemManager = (VersionController.version.compareTo(Version.MC_1_9) < 0) ? new ItemPre9() : new ItemPost9();
    private static final ParticleManager particleManager = (VersionController.version.compareTo(Version.MC_1_9) < 0) ? new ParticlePre9() : new ParticlePost9();
    private static final EntityManager entityManager = (VersionController.isPaper) ? new EntityPaper() : new EntitySpigot();

    /**
     * Get the current minecraft version
     * @return version (1.XX)
     */
    private static Version getVersion() {
        // TODO get the full version
        return new Version(1, Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]), 0);
    }

    /**
     * @author https://www.codegrepper.com/code-examples/java/java+get+location+of+jar+file
     * @author https://mkyong.com/java/java-get-the-name-or-path-of-a-running-jar-file/
     */
    private static String getJarPath() {
        Properties p = System.getProperties();
        try {
            String jar = p.getProperty("java.class.path");
            if (jar == null || jar.equals("")) throw new NullPointerException();
            return jar;
        } catch (NullPointerException ex) {
            System.err.println("Couldn't get server's jar path. List of properties:");
            p.list(System.err);
            return "";
        }
    }

    /**
     * Get if Paper is running (or, by cons, Spigot)
     * https://www.spigotmc.org/threads/how-do-i-detect-if-a-server-is-running-paper.499064/
     * @author Gadse
     * @return Paper (true), Spigot (false)
     */
    private static boolean getMCPaper() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder"); // a package from paper
            return true;
        } catch (ClassNotFoundException ignored) { }
        return false;
    }

    public static VersionController get() {
        if (VersionController.versionController == null) VersionController.versionController = new VersionController();
        return VersionController.versionController;
    }

    @Nullable
    public BlockType getMaterial(String type) {
        return VersionController.blockManager.getMaterial(type);
    }

    public BlockType getObject(@NotNull Block block) {
        return VersionController.blockManager.getObject(block);
    }

    @Override
    public BlockType getObject(@NotNull ItemStack item) {
        return VersionController.blockManager.getObject(item);
    }

    public boolean isPassable(@NotNull Block block) {
        return VersionController.blockManager.isPassable(block);
    }

    /**
     * It generates a copy of an ItemStack as default (only the type)
     * @param item ItemStack to copy
     * @return ItemStack clone
     */
    public ItemStack cloneItemStack(ItemStack item) {
        return this.getObject(item).getItemStack(false);
    }

    @Override
    public ItemStack[] getItemInHand(PlayerInventory playerInventory) {
        return VersionController.itemManager.getItemInHand(playerInventory);
    }

    @Override
    public void setItemInHand(PlayerInventory playerInventory, ItemStack item) {
        VersionController.itemManager.setItemInHand(playerInventory, item);
    }

    @Override
    public boolean isItem(ItemStack item) {
        return VersionController.itemManager.isItem(item);
    }

    @Override
    public ParticleEntity getParticle(String particle) throws IllegalArgumentException {
        return VersionController.particleManager.getParticle(particle);
    }

    @Override
    public @NotNull Vector getVelocity(Entity e) {
        return VersionController.entityManager.getVelocity(e);
    }

    @Override
    public @NotNull Vector getVelocity(PlayerMoveEvent e) {
        return VersionController.entityManager.getVelocity(e);
    }
}
