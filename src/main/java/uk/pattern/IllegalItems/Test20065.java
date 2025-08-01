package uk.pattern.IllegalItems;

import org.bukkit.plugin.java.JavaPlugin;
import uk.pattern.IllegalItems.Osnova.ItemFilter;
import uk.pattern.IllegalItems.Osnova.ItemProtectionListener;
import uk.pattern.IllegalItems.Osnova2.CAttributeCheck;
import uk.pattern.IllegalItems.Osnova2.EnchantmentChecker;
import uk.pattern.IllegalItems.Osnova2.UnknownItems;

public class Test20065 extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ItemFilter.loadConfig(this);
        EnchantmentChecker.loadConfig(this);

        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new UnknownItems(this), this);
        getServer().getPluginManager().registerEvents(new CAttributeCheck(), this);
    }
}