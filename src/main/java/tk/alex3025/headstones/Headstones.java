package tk.alex3025.headstones;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import tk.alex3025.headstones.commands.HeadstonesCommand;
import tk.alex3025.headstones.commands.subcommands.*;
import tk.alex3025.headstones.listeners.BlockBreakListener;
import tk.alex3025.headstones.listeners.PlayerDeathListener;
import tk.alex3025.headstones.listeners.RightClickListener;
import tk.alex3025.headstones.utils.ConfigFile;

public final class Headstones extends JavaPlugin {

    private static Headstones instance;

    private ConfigFile config;
    private ConfigFile messages;
    private ConfigFile database;

    @Override
    public void onEnable() {
        instance = this;

        this.loadConfigurationFiles();
        this.cleanupOldUnplacedHeadstones();
        this.registerListeners();
        this.registerCommands();
    }

    private void cleanupOldUnplacedHeadstones() {
        org.bukkit.configuration.ConfigurationSection unplaced = this.database.getConfigurationSection("unplaced_headstones");
        if (unplaced != null) {
            long now = System.currentTimeMillis();
            long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;
            boolean modified = false;

            for (String key : unplaced.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection hs = unplaced.getConfigurationSection(key);
                if (hs != null) {
                    long timestamp = hs.getLong("timestamp", 0);
                    if (now - timestamp > sevenDaysInMillis) {
                        unplaced.set(key, null);
                        modified = true;
                    }
                }
            }

            if (modified) {
                this.database.save();
                getLogger().info("Cleaned up old unplaced headstones.");
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfigurationFiles() {
        this.config = new ConfigFile(this,"config.yml");
        this.messages = new ConfigFile(this,"messages.yml");
        this.database = new ConfigFile(this,"database.yml");
    }

    private void registerListeners() {
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerDeathListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new RightClickListener(), this);
    }

    private void registerCommands() {
        new HeadstonesCommand();

        // Subcommands
        new ClearDatabaseCommand();
        new ReloadConfigCommand();
        new ListHeadstonesCommand();
        new TeleportHeadstoneCommand();
        new DestroyHeadstoneCommand();
        new UnplacedHeadstonesCommand();
    }

    public static Headstones getInstance() {
        return instance;
    }

    // Config getters
    @Override
    public @NotNull ConfigFile getConfig() {
        return config;
    }

    public ConfigFile getMessages() {
        return messages;
    }

    public ConfigFile getDatabase() {
        return database;
    }

}
