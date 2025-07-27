package uk.pattern.IllegalItems;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import uk.pattern.IllegalItems.Osnova.ItemFilter;
import uk.pattern.IllegalItems.Osnova.ItemProtectionListener;
import uk.pattern.IllegalItems.Osnova2.UnknownItems;

import java.io.File;

public class Test20065 extends JavaPlugin {

    private FileConfiguration itemsConfig;

    @Override
    public void onEnable() {
        loadItemsConfig();
        saveDefaultConfig();
        ItemFilter.loadConfig(this);

        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new UnknownItems(this), this);
    }

    public void loadItemsConfig() {
        File file = new File(getDataFolder(), "items.yml");
        if (!file.exists()) {
            saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getItemsConfig() {
        return itemsConfig;
    }
}