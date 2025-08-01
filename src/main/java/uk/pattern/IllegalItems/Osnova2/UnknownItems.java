package uk.pattern.IllegalItems.Osnova2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import uk.pattern.IllegalItems.Test20065;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        ItemStack item = e.getItem().getItemStack();
        if (isIllegalItem(item) || CAttributeCheck.hasBadAttributes(item)) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        ItemStack item = e.getItem();
        if (isIllegalItem(item)) {
            e.setCancelled(true);
            e.getSource().removeItem(item);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isIllegalItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        FileConfiguration config = plugin.getConfig();
        List<String> allowedPotions = new ArrayList<>(config.getStringList("allowed-potions"));
        if (allowedPotions.isEmpty()) {
            allowedPotions.add("WATER");
        }

        if (item.getItemMeta() instanceof PotionMeta meta) {
            PotionData data = meta.getBasePotionData();
            String potionKey = data.getType().name().toUpperCase();

            if (allowedPotions.contains(potionKey)) {
                return !meta.getCustomEffects().isEmpty();
            }

            String potionNBTName = data.getType().name().toLowerCase();

            if (potionNBTName.equals("uncraftable") && allowedPotions.contains("EMPTY")) {
                return !meta.getCustomEffects().isEmpty();
            }

            return true;
        }

        for (Map.Entry<Enchantment, Integer> enchant : item.getEnchantments().entrySet()) {
            if (!EnchantmentChecker.isEnchantLevelAllowed(enchant.getKey(), enchant.getValue())) {
                return true;
            }
        }

        return false;
    }
}


