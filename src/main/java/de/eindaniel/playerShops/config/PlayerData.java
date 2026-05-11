package de.eindaniel.playerShops.config;

import de.eindaniel.playerShops.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.UUID;

public class PlayerData {
    private final Main plugin;
    private final File playerDataFolder;

    public PlayerData(Main plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
            plugin.getLogger().warning("Playerdata folder could not be installed: " + playerDataFolder.getPath());
        } else {
            plugin.getLogger().info("Playerdata folder has been created: " + playerDataFolder.getPath());
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(playerDataFolder, uuid.toString() + ".yml");
    }

    public int getShopAmount(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        return config.getInt("shops.amount", 0);
    }

    public void addShopAmount(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("shops.amount", getShopAmount(uuid)+1);
    }

    public void removeShopAmount(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("shops.amount", getShopAmount(uuid)-1);
    }
}
