package uk.pattern.IllegalItems.Osnova;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import uk.pattern.IllegalItems.Test20065;
import org.bukkit.event.inventory.ClickType;

public class ItemProtectionListener implements Listener {
    private final Test20065 plugin;

    public ItemProtectionListener(Test20065 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        ClickType type = e.getClick();

        Inventory clickedInventory = e.getClickedInventory();
        PlayerInventory playerInventory = player.getInventory();
        boolean isPlayerInventory = clickedInventory != null && clickedInventory.equals(playerInventory);

        if (!cursor.getType().isAir()) {
            if (ItemFilter.cleanHolderIfTooBig(cursor, null, plugin)
                    || ItemFilter.containsNestedInventoryHolder(cursor)
                    || ItemFilter.trimBooksInHolder(cursor)
                    || ItemFilter.isTooBig(cursor)
                    || ItemFilter.isBad(cursor, plugin)) {
                e.setCursor(null);
                e.setCancelled(true);
            }
        }

        if (type == ClickType.NUMBER_KEY || type == ClickType.DROP || type == ClickType.CONTROL_DROP
                || type == ClickType.SWAP_OFFHAND || type == ClickType.MIDDLE) {

            ItemStack itemToCheck = switch (type) {
                case NUMBER_KEY -> player.getInventory().getItem(e.getHotbarButton());
                case SWAP_OFFHAND -> player.getInventory().getItemInOffHand();
                default -> current;
            };

            if (itemToCheck != null && !itemToCheck.getType().isAir()) {
                if (ItemFilter.cleanHolderIfTooBig(itemToCheck, null, plugin)
                        || ItemFilter.containsNestedInventoryHolder(itemToCheck)
                        || ItemFilter.isInventoryHolder(itemToCheck)
                        || ItemFilter.isBad(itemToCheck, plugin)) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (type == ClickType.NUMBER_KEY && !isPlayerInventory) {
                if (ItemFilter.containsNestedInventoryHolder(current) || ItemFilter.isInventoryHolder(current)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e.isShiftClick()) {
            if (ItemFilter.cleanHolderIfTooBig(current, null, plugin)
                    || ItemFilter.containsNestedInventoryHolder(current)
                    || ItemFilter.isInventoryHolder(current)
                    || ItemFilter.isBad(current, plugin)) {
                e.setCancelled(true);
                return;
            }
            return;
        }

        if (!isPlayerInventory) {
            if (ItemFilter.containsNestedInventoryHolder(current) && cursor.getType().isAir()) {
                e.setCancelled(true);
                e.setCurrentItem(null);
                return;
            }
            if (ItemFilter.isInventoryHolder(current) && cursor.getType().isAir()) {
                e.setCancelled(true);
                e.setCurrentItem(null);
                return;
            }
            if (ItemFilter.isInventoryHolder(cursor)) {
                e.setCancelled(true);
                e.setCursor(null);
                return;
            }
        }

        if (ItemFilter.isBad(cursor, plugin)) {
            e.setCancelled(true);
            e.setCursor(null);
            return;
        }

        if (ItemFilter.isBad(current, plugin)) {
            e.setCancelled(true);

        }

        if (ItemFilter.cleanHolderIfTooBig(current, cursor, plugin)) {
            e.setCancelled(true);
            e.setCursor(null);
        }
    }


    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inventory = e.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null || item.getType().isAir()) continue;

            if (ItemFilter.containsNestedInventoryHolder(item)
                    || ItemFilter.cleanHolderIfTooBig(item, null, plugin)
                    || ItemFilter.isTooBig(item)
                    || ItemFilter.isBad(item, plugin)) {
                inventory.setItem(i, null);
            } else if (ItemFilter.trimBooksInHolder(item)) {
                inventory.setItem(i, item);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        ItemStack item = e.getItem().getItemStack();

        boolean changed = ItemFilter.trimBooksInHolder(item);
        if (changed) {
            e.getItem().setItemStack(item);
        }

        if (ItemFilter.cleanHolderIfTooBig(item, null, plugin) || ItemFilter.isTooBig(item) || ItemFilter.isBad(item, plugin)) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

    @EventHandler
    public void onAutoMove(InventoryMoveItemEvent e) {
        ItemStack item = e.getItem();
        if (ItemFilter.cleanHolderIfTooBig(item, null, plugin) || ItemFilter.isTooBig(item) || ItemFilter.isInventoryHolder(item) || ItemFilter.isBad(item, plugin)) {
            e.setCancelled(true);
        }
    }
}
