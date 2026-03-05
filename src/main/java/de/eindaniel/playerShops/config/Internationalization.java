package de.eindaniel.playerShops.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Internationalization {

    private final JavaPlugin plugin;
    private FileConfiguration langConfig;

    public Internationalization(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File langFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!langFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            Reader defReader = new InputStreamReader(defStream, StandardCharsets.UTF_8);
            FileConfiguration defaults = YamlConfiguration.loadConfiguration(defReader);
            langConfig.setDefaults(defaults);
            langConfig.options().copyDefaults(true);
            try {
                langConfig.save(langFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Konnte messages.yml nicht speichern: " + e.getMessage());
            }
        }
    }

    public String get(String key, Object... args) {
        String value = langConfig.getString(key);
        if (value == null) {
            plugin.getLogger().warning("[i18n] Fehlender Key: " + key);
            return "<red>[missing: " + key + "]</red>";
        }
        for (int i = 0; i < args.length; i++) {
            value = value.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return value;
    }

    public FileConfiguration getLanguageConfig() {
        return langConfig;
    }
}
