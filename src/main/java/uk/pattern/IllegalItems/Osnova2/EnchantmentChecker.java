package uk.pattern.IllegalItems.Osnova2;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.stream.Collectors;

public class EnchantmentChecker {

    private static Map<Enchantment, Integer> enchantLimits;

    @SuppressWarnings("deprecation")
    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        var section = config.getConfigurationSection("enchant-limits");
        if (section == null) {
            plugin.getLogger().warning("Missing 'enchant-limits' section in config");
            enchantLimits = Map.of();
            return;
        }

        enchantLimits = section.getKeys(false).stream()
                .flatMap(key -> {
                    Enchantment enchant = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                    if (enchant == null) {
                        plugin.getLogger().warning("Invalid enchantment key in config: " + key + ", skipping...");
                        return java.util.stream.Stream.empty();
                    }
                    return java.util.stream.Stream.of(Map.entry(enchant, section.getInt(key)));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    public static boolean isEnchantLevelAllowed(Enchantment enchant, int level) {
        if (enchantLimits == null) return true;
        Integer maxLevel = enchantLimits.get(enchant);
        return maxLevel == null || level <= maxLevel;
    }
}
