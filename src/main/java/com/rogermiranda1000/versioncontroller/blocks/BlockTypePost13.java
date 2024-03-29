package com.rogermiranda1000.versioncontroller.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockTypePost13 extends BlockType {
    private static final Pattern blockDataData = Pattern.compile("minecraft:[^\\[]+(\\[(.+)\\])?");
    private final BlockData data;
    private final int hash;

    public BlockTypePost13(ItemStack type) {
        BlockData data;
        try {
            data = BlockTypePost13.loadBlockData(type);
        }
        catch (IllegalArgumentException ex) {
            data = type.getType().createBlockData();
        }
        this.data = data;

        this.hash = this.data.getMaterial().ordinal(); // TODO there will be collisions between different BlockData of the same type
    }

    public BlockTypePost13(Block block) {
        this.data = block.getBlockData().clone();

        this.hash = this.data.getMaterial().ordinal(); // TODO there will be collisions between different BlockData of the same type
    }

    public BlockTypePost13(String str) throws IllegalArgumentException {
        BlockData data;
        try {
            data = Bukkit.createBlockData(str);
        } catch (IllegalArgumentException ex) {
            Material mat = Material.getMaterial(str);
            if (mat == null) throw new IllegalArgumentException(str + " is not a Material, nor BlockData");
            data = mat.createBlockData();
        }
        this.data = data;

        this.hash = this.data.getMaterial().ordinal(); // TODO there will be collisions between different BlockData of the same type
    }

    @Override
    public String getName() {
        return this.data.getAsString();
    }

    @Override
    public String getFriendlyName() {
        return this.data.getMaterial().name().toLowerCase();
    }

    @Override
    public void setType(@NotNull Block block) {
        block.setBlockData(this.data);
    }

    @Override
    public ItemStack getItemStack(boolean verbose) {
        ItemStack item = new ItemStack(this.data.getMaterial());

        List<String> data;
        if (verbose && (data = BlockTypePost13.getNonStandardDataList(this.data)).size() > 0) item = BlockTypePost13.saveBlockData(this.data.getMaterial(), data);

        return item;
    }

    private static String []getDataList(BlockData data) {
        Matcher m = BlockTypePost13.blockDataData.matcher(data.getAsString());
        if (!m.find()) throw new IllegalArgumentException("Expecting block data to be 'minecraft:...[...]', found '" + data.getAsString() + "' instead.");
        String match = m.group(2);
        if (match == null) return new String[]{};
        return match.split(",");
    }

    private static List<String> getNonStandardDataList(BlockData data) throws IllegalArgumentException {
        String []current = BlockTypePost13.getDataList(data),
                original = BlockTypePost13.getDataList(data.getMaterial().createBlockData());
        ArrayList<String> r = new ArrayList<>();
        if (!isItem(data)) {
            // we can't give the block as an item
            r.add(getBlockDataMaterial(data.getAsString()));
        }
        for (String e : current) {
            if (Arrays.stream(original).noneMatch(e::equals)) r.add(e);
        }
        return r;
    }

    private static String getBlockDataMaterial(String blockData) throws IllegalArgumentException {
        Pattern p = Pattern.compile("minecraft:([^\\[]+)"); // regex to get the block name
        Matcher m = p.matcher(blockData);
        if (!m.find()) throw new IllegalArgumentException("Can't get block nor item for " + blockData);
        return m.group(1);
    }

    private static boolean isItem(BlockData bd) throws IllegalArgumentException {
        return Material.valueOf(getBlockDataMaterial(bd.getAsString()).toUpperCase()).isItem();
    }

    public boolean defaultMaterial() {
        return this.data.matches(this.data.getMaterial().createBlockData());
    }

    /**
     * Save the BlockData in the item's lore
     * @param expectedItemMaterial Material expected of the item to save the info
     * @param data BlockData info
     * @return Re-created item, this time with the appropriate lore data
     */
    private static ItemStack saveBlockData(Material expectedItemMaterial, List<String> data) {
        data.add(0, "-- BlockData --");
        data.add("-------------");

        // if the first (now second) row doesn't contain a '=' it means that it specifies the item because `expectedItemMaterial`
        // is not a givable item
        ItemStack r = data.get(1).contains("=") ? new ItemStack(expectedItemMaterial) : new ItemStack(Material.STONE);

        // add lore
        ItemMeta meta = r.getItemMeta();
        meta.setLore(data);
        r.setItemMeta(meta);

        return r;
    }

    /**
     * Get an item saved by saveBlockData and extract the BlockData
     * @param item Item to extract the dada
     * @return BlockData
     * @throws IllegalArgumentException item not generated by saveBlockData
     */
    private static BlockData loadBlockData(ItemStack item) throws IllegalArgumentException {
        List<String> lore;
        if (item.getItemMeta() == null || (lore = item.getItemMeta().getLore()) == null || lore.size() == 0 || !lore.get(0).equals("-- BlockData --")) return item.getType().createBlockData(); // default block

        StringBuilder sb = new StringBuilder();
        sb.append("minecraft:");
        boolean blockTypeOnLore = (lore.size() > 2) && !lore.get(1).contains("=");
        sb.append(blockTypeOnLore ? lore.get(1) : item.getType().name().toLowerCase());
        sb.append('[');
        for (int i = blockTypeOnLore ? 2 : 1; i < lore.size() && !lore.get(i).startsWith("--"); i++) sb.append(lore.get(i) + ",");
        sb.setLength(sb.length()-1); // remove last ','
        sb.append(']');
        return Bukkit.createBlockData(sb.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockTypePost13)) return false;

        if (this == o) return true;
        return this.data.matches(((BlockTypePost13)o).data);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
}
