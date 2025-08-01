package uk.pattern.IllegalItems.Osnova2;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnchantmentChecker {

    private static Map<Enchantment, Integer> enchantLimits;

    @SuppressWarnings("deprecation")
    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        var section = config.getConfigurationSection("enchant-limits");

        if (section == null || section.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("Missing or empty 'enchant-limits' section in config. The section will be filled in automatically.");

            Map<String, Integer> defaultLimits = Map.of("protection", 4);
            defaultLimits.forEach((key, value) -> config.set("enchant-limits." + key, value));
            plugin.saveConfig();

            section = config.getConfigurationSection("enchant-limits");

            if (section == null) {
                enchantLimits = Map.of();
                return;
            }
        }

        final var safeSection = section;

        enchantLimits = safeSection.getKeys(false).stream()
                .map(key -> {
                    Enchantment enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                    if (enchant == null) {
                        plugin.getLogger().warning("Invalid enchantment key in config: " + key + ", skipping...");
                        return null;
                    }
                    return Map.entry(enchant, safeSection.getInt(key)); //
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }



    public static boolean isEnchantLevelAllowed(Enchantment enchant, int level) {
        if (enchantLimits == null) return true;
        Integer maxLevel = enchantLimits.get(enchant);
        return maxLevel == null || level <= maxLevel;
    }
}
