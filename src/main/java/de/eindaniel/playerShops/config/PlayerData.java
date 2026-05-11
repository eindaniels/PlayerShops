package de.eindaniel.playerShops.config;

import de.eindaniel.playerShops.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerData {
    private final Main plugin;
    private final File playerDataFolder;

    public PlayerData(Main plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists() && !playerDataFolder.mkdirs()) {
            plugin.getLogger().warning("Playerdata folder could not be installed: " + playerDataFolder.getPath());
        }
    }


    private void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            File playerFile = getPlayerFile(uuid);
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            config.set("shops.amount", getShopAmount(uuid)+1);
            saveConfig(config, playerFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeShopAmount(UUID uuid) {
        File playerFile = getPlayerFile(uuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("shops.amount", getShopAmount(uuid)-1);
        saveConfig(config, playerFile);
    }
}
