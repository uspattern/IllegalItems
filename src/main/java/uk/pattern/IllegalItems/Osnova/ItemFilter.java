package uk.pattern.IllegalItems.Osnova;

import net.minecraft.nbt.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ItemFilter {
    public static int MAX_BYTES;

    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        MAX_BYTES = plugin.getConfig().getInt("max_bytes", 2048);
    }

    public static boolean isBad(ItemStack item, Plugin plugin) {
        if (item == null || item.getType().isAir()) return false;

        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            BlockState state = meta.getBlockState();
            if (state instanceof InventoryHolder holder) {
                for (ItemStack content : holder.getInventory().getContents()) {
                    if (isBad(content, plugin)) return true;
                }
            }
        }

        return hasMagic(item) || isTooBig(item, plugin);
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
            return false;
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
                return false;
            }
        }

        return false;
    }

    public static boolean isTooBig(ItemStack item, Plugin plugin) {
        try {
            var nms = CraftItemStack.asNMSCopy(item);
            var tag = new NBTTagCompound();
            nms.b(tag);
            var out = new ByteArrayOutputStream();
            NBTCompressedStreamTools.a(tag, out);
            int size = out.size();
            return size > MAX_BYTES;
        } catch (Throwable t) {
            plugin.getLogger().warning("NBT size check failed: " + t.getMessage());
            return true;
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
}






