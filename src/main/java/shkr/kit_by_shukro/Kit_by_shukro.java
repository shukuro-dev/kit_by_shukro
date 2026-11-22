package shkr.kit_by_shukro;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import shkr.kit_by_shukro.commands.KitCommand;
import shkr.kit_by_shukro.commands.KitTabCompleter;
import shkr.kit_by_shukro.listeners.KitMenuListener;
import shkr.kit_by_shukro.managers.KitManager;

public class Kit_by_shukro extends JavaPlugin {

    private KitManager kitManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.kitManager = new KitManager(this);

        getServer().getPluginManager().registerEvents(new KitMenuListener(this), this);

        PluginCommand kitCmd = getCommand("kit");
        if (kitCmd != null) {
            kitCmd.setExecutor(new KitCommand(this, kitManager));
            kitCmd.setTabCompleter(new KitTabCompleter(kitManager));
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (kitManager != null) {
            kitManager.loadKitsFromDisk();
        }
    }

    @Override
    public void onDisable() {
        if (kitManager != null) {
            kitManager.clearAll();
        }
    }
}