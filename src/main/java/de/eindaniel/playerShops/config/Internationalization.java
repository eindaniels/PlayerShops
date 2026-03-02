package de.eindaniel.playerShops.config;

import de.cytooxien.realms.api.PlayerInformationProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Internationalization {
    private final FileConfiguration defaultLangConfig;
    private final JavaPlugin plugin;

    public Internationalization(JavaPlugin plugin) {
        this.plugin = plugin;
        defaultLangConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "DE_DE.yml"));
    }

    public FileConfiguration getLanguageConfig(Player player) {
        PlayerInformationProvider playerInformationProvider = Bukkit.getServicesManager().load(PlayerInformationProvider.class);
        File langFile = new File(plugin.getDataFolder(), playerInformationProvider.language(player.getUniqueId()).name() + ".yml");
        if (langFile.exists()) {
            return YamlConfiguration.loadConfiguration(langFile);
        }
        return defaultLangConfig;
    }
}
