package uk.pattern.IllegalItems.Osnova;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ItemFilter {
    public static int MAX_BYTES;
    public static int MAX_NBT_SIZE_KB;

    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        MAX_BYTES = plugin.getConfig().getInt("max_bytes", 2048);
        MAX_NBT_SIZE_KB = plugin.getConfig().getInt("max_nbt_size_kb", 100);

    }

    @SuppressWarnings("unused")
    public static boolean isBad(ItemStack item, Plugin plugin) {
        if (item == null || item.getType().isAir()) return false;

        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            BlockState state = meta.getBlockState();
            if (state instanceof InventoryHolder holder) {
                for (ItemStack content : holder.getInventory().getContents()) {
                    if (content == null || content.getType().isAir()) continue;
                    if (isBad(content, plugin)) return true; // Рекурсия!
                }
            }
        }

        return hasMagic(item) || isTooBig(item) || hasIllegalSignData(item);
    }

    public static boolean hasMagic(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        var meta = item.getItemMeta();
        List<Component> components = new ArrayList<>();

        try {
            if (meta.hasDisplayName()) {
                Component name = meta.displayName();
                if (name != null) components.add(name);
            }
            if (meta.hasLore()) {
                List<Component> lore = meta.lore();
                if (lore != null) components.addAll(lore);
            }
        } catch (Throwable t) {
            return true;
        }

        return containsMagic(components);
    }

    private static boolean containsMagic(List<Component> components) {
        if (components == null) return false;

        for (Component comp : components) {
            if (comp == null) continue;
            try {
                String legacy = LegacyComponentSerializer.legacySection().serialize(comp);
                if (legacy.contains("§k")) return true;
            } catch (Throwable ignored) {
                return true;
            }
        }

        return false;
    }


    @SuppressWarnings({"unused", "deprecation"})
    public static boolean isTooBig(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        try {
            NBTItem nbti = new NBTItem(item);
            String fullNbtString = nbti.toString();
            int fullSizeBytes = fullNbtString.getBytes(StandardCharsets.UTF_8).length;
            int fullSizeKb = fullSizeBytes / 1024;

            if (fullSizeKb > MAX_NBT_SIZE_KB) return true;

            try {
                var nms = CraftItemStack.asNMSCopy(item);
                var tag = new NBTTagCompound();
                nms.b(tag);
                var out = new ByteArrayOutputStream();
                NBTCompressedStreamTools.a(tag, out);
                int nmsSize = out.size();
                int nmsSizeKb = nmsSize / 1024;


                return nmsSize > MAX_BYTES || nmsSizeKb > MAX_NBT_SIZE_KB;
            } catch (Throwable ignored) {
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean containsNestedInventoryHolder(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        ItemStack single = item.clone();
        single.setAmount(1);

        if (!(single.getItemMeta() instanceof BlockStateMeta meta)) return false;

        BlockState state = meta.getBlockState();
        if (!(state instanceof InventoryHolder holder)) return false;

        for (ItemStack content : holder.getInventory().getContents()) {
            if (content == null || content.getType().isAir()) continue;

            if (content.getItemMeta() instanceof BlockStateMeta) {
                return true;
            }

            if (containsNestedInventoryHolder(content)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInventoryHolder(ItemStack item) {
        return item != null && !item.getType().isAir() && item.getItemMeta() instanceof BlockStateMeta;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean trimBooksInHolder(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        boolean changed = false;

        if (item.getItemMeta() instanceof BookMeta bookMeta) {
            List<Component> pages = bookMeta.pages();
            if (pages.size() > 1) {
                bookMeta.pages(List.of(pages.get(0)));
                item.setItemMeta(bookMeta);
                changed = true;
            }
        }

        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            BlockState state = meta.getBlockState();
            if (state instanceof InventoryHolder holder) {
                for (ItemStack content : holder.getInventory().getContents()) {
                    if (content == null || content.getType().isAir()) continue;

                    boolean nestedChanged = trimBooksInHolder(content);
                    if (nestedChanged) changed = true;
                }

                if (changed) {
                    state.update(true, false);
                    meta.setBlockState(state);
                    item.setItemMeta(meta);
                }
            }
        }

        return changed;
    }


    @SuppressWarnings("unused")
    public static boolean cleanHolderIfTooBig(ItemStack holderItem, ItemStack addingItem, Plugin plugin) {

        if (holderItem == null || holderItem.getType().isAir()) return false;
        if (!(holderItem.getItemMeta() instanceof BlockStateMeta meta)) return false;
        BlockState state = meta.getBlockState();
        if (!(state instanceof InventoryHolder holder)) return false;

        for (ItemStack content : holder.getInventory().getContents()) {
            if (isTooBig(content)) return true;
        }

        return isTooBig(addingItem);
    }

    @SuppressWarnings("deprecation")
    public static boolean hasIllegalSignData(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        Material type = item.getType();
        if (!type.name().contains("_SIGN")) return false;

        try {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.hasKey("BlockEntityTag");
        } catch (Throwable ignored) {
            return false;
        }
    }
}














