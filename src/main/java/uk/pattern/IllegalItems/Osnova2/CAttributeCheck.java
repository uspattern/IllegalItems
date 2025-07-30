package uk.pattern.IllegalItems.Osnova2;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

public class CAttributeCheck implements Listener {

    @EventHandler
    public void onCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (hasBadAttributes(item)) {
            event.setCancelled(true);
            event.setCursor(new ItemStack(Material.AIR));
        }

        if (event.getWhoClicked() instanceof Player player) {
            checkArmor(player);
        }
    }


    /**
     * Code from:
     * <a href="https://github.com/dniym/IllegalStack/blob/master/src/main/java/main/java/me/dniym/checks/BadAttributeCheck.java">...</a>
     */
    public static void checkArmor(Player player) {
        ItemStack[] armor = {
                player.getInventory().getBoots(),
                player.getInventory().getLeggings(),
                player.getInventory().getChestplate(),
                player.getInventory().getHelmet()
        };

        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (hasBadAttributes(item)) {
                removeAttributes(item);

                switch (i) {
                    case 0 -> player.getInventory().setBoots(new ItemStack(Material.AIR));
                    case 1 -> player.getInventory().setLeggings(new ItemStack(Material.AIR));
                    case 2 -> player.getInventory().setChestplate(new ItemStack(Material.AIR));
                    case 3 -> player.getInventory().setHelmet(new ItemStack(Material.AIR));
                }
            }
        }
    }

    public static boolean hasBadAttributes(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasAttributeModifiers();
    }

    public static void removeAttributes(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta.hasAttributeModifiers()) {
            if (meta.getAttributeModifiers() != null) {
                Set<Attribute> toRemove = new HashSet<>(meta.getAttributeModifiers().keySet());
                for (Attribute attr : toRemove) {
                    meta.removeAttributeModifier(attr);
                }
            }
        }

        for (ItemFlag flag : meta.getItemFlags()) {
            meta.removeItemFlags(flag);
        }

        item.setItemMeta(meta);
    }
}
