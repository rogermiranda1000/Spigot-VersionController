package com.rogermiranda1000.versioncontroller;

import com.rogermiranda1000.versioncontroller.blocks.BlockManager;
import com.rogermiranda1000.versioncontroller.blocks.BlockPost13;
import com.rogermiranda1000.versioncontroller.blocks.BlockPre13;
import com.rogermiranda1000.versioncontroller.blocks.BlockType;
import com.rogermiranda1000.versioncontroller.entities.*;
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

import java.io.FileNotFoundException;
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
    private static final EntityManager entityManager = (VersionController.isPaper) ? new EntityPaper()
            : ((VersionController.version.compareTo(Version.MC_1_14) < 0) ? new EntitySpigotPre14() : new EntitySpigotPost14());

    /**
     * Get the current minecraft version
     * @return version (1.XX)
     */
    private static Version getVersion() {
        String[] numbers = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        return new Version(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), (numbers.length < 3) ? 0 : Integer.parseInt(numbers[2]));
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
            StringBuilder error = new StringBuilder()
                    .append("Couldn't get server's jar path. List of properties:");
            for (Object property : p.values()) error.append(property.toString());
            throw new RuntimeException(error.toString());
        }
    }

    /**
     * Get if Paper is running (or, by cons, Spigot)
     * https://www.spigotmc.org/threads/how-do-i-detect-if-a-server-is-running-paper.499064/
     * @author Gadse
     * @return Paper (true), Spigot (false)
     */
    private static boolean getMCPaper() {
        return Bukkit.getName().equals("Paper");
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
    public int getDurability(ItemStack item) throws IllegalArgumentException {
        return VersionController.itemManager.getDurability(item);
    }

    /**
     * /!\\ item's meta changes /!\\
     */
    @Override
    public void setDurability(ItemStack item, int damage) throws IllegalArgumentException {
        VersionController.itemManager.setDurability(item, damage);
    }

    /**
     * /!\\ item's meta changes /!\\
     */
    @Override
    public ItemStack setUnbreakable(ItemStack item) {
        return VersionController.itemManager.setUnbreakable(item);
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

    @Override
    public BoundingBox getBoundingBox(Entity e) {
        return VersionController.entityManager.getBoundingBox(e);
    }
}
