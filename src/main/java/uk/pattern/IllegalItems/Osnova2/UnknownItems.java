package uk.pattern.IllegalItems.Osnova2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import uk.pattern.IllegalItems.Test20065;

import java.util.List;

public class UnknownItems implements Listener {

    private final Test20065 plugin;

    public UnknownItems(Test20065 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();

        if (isIllegalItem(clickedItem)) {
            e.setCancelled(true);
            e.setCurrentItem(null);
        }

        if (isIllegalItem(cursorItem)) {
            e.setCancelled(true);
            player.setItemOnCursor(null);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isIllegalItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        FileConfiguration config = plugin.getItemsConfig();

        int maxEnchantLevel = config.getInt("max-enchant-level", 5);
        List<String> allowedPotions = config.getStringList("allowed-potions");
        if (allowedPotions.isEmpty()) {
            allowedPotions.add("WATER");
        }

        if (item.getItemMeta() instanceof PotionMeta meta) {
            PotionType baseType = meta.getBasePotionData().getType();
            if (!allowedPotions.contains(baseType.name())) return true;
            if (!meta.getCustomEffects().isEmpty()) return true;
        }

        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (item.getEnchantmentLevel(ench) > maxEnchantLevel) return true;
        }

        if (item.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) {
            for (Enchantment ench : bookMeta.getStoredEnchants().keySet()) {
                if (bookMeta.getStoredEnchantLevel(ench) > maxEnchantLevel) return true;
            }
        }

        return false;
    }
}
