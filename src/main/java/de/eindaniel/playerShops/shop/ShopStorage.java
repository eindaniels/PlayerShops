package de.eindaniel.playerShops.shop;

import de.eindaniel.playerShops.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static de.eindaniel.playerShops.util.ItemSerializer.itemFromBase64;
import static de.eindaniel.playerShops.util.ItemSerializer.itemToBase64;

public class ShopStorage {

    private final Main plugin;
    private final File file;

    public ShopStorage(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
    }

    public void loadAll() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        plugin.shops().clear();
        if (!yaml.isConfigurationSection("shops")) return;

        int loaded = 0;
        int migrated = 0;
        int failed = 0;

        for (String key : yaml.getConfigurationSection("shops").getKeys(false)) {
            ConfigurationSection sec = yaml.getConfigurationSection("shops." + key);
            if (sec == null) {
                failed++;
                continue;
            }

            String world = sec.getString("world");
            int x = sec.getInt("x");
            int y = sec.getInt("y");
            int z = sec.getInt("z");
            World w = Bukkit.getWorld(world);
            if (w == null) {
                plugin.getLogger().warning("Shop " + key + " übersprungen: Welt '" + world + "' nicht gefunden");
                failed++;
                continue;
            }

            Location base = new Location(w, x + 0.5, y, z + 0.5);
            int amountPerTrade = sec.getInt("amountPerTrade");
            double buy = sec.getDouble("buyPrice");
            double sell = sec.getDouble("sellPrice");
            boolean buyEnabled = sec.getBoolean("buyEnabled", true);
            boolean sellEnabled = sec.getBoolean("sellEnabled", true);
            UUID owner = UUID.fromString(sec.getString("owner"));

            ItemStack displayItem = null;
            boolean needsMigration = false;

            if (sec.isString("displayItem")) {
                String displayItemEncoded = sec.getString("displayItem");
                try {
                    displayItem = itemFromBase64(displayItemEncoded);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Konnte displayItem für Shop " + key + " nicht laden: " + ex.getMessage());
                }
            }

            if (displayItem == null && sec.isString("material")) {
                String matName = sec.getString("material");
                var mat = Optional.ofNullable(matName).map(org.bukkit.Material::matchMaterial).orElse(null);

                if (mat != null) {
                    displayItem = new ItemStack(mat, 1);
                    needsMigration = true;
                    plugin.getLogger().info("Migriere Shop " + key + " von Material zu ItemStack");
                }
            }

            if (displayItem == null) {
                plugin.getLogger().warning("Shop " + key + " übersprungen: Kein gültiges displayItem/material");
                failed++;
                continue;
            }

            PlayerShop shop = new PlayerShop(owner, base, displayItem, amountPerTrade, buy, sell);
            shop.setBuyEnabled(buyEnabled);
            shop.setSellEnabled(sellEnabled);

            List<String> stashEncoded = sec.getStringList("stashItems");
            for (String encoded : stashEncoded) {
                try {
                    ItemStack item = itemFromBase64(encoded);
                    if (item != null) {
                        shop.getStashItems().add(item);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("Konnte Item in Shop " + key + " nicht laden: " + ex.getMessage());
                }
            }

            plugin.shops().put(shop);
            loaded++;

            if (needsMigration) {
                migrated++;
            }
        }

        plugin.getLogger().info("Geladen: " + loaded + " PlayerShops.");
        if (migrated > 0) {
            plugin.getLogger().info("  → " + migrated + " Shops von altem Format migriert");
            plugin.getLogger().info("  → Speichere migrierte Shops...");
            saveAll();
        }
        if (failed > 0) {
            plugin.getLogger().warning("  → " + failed + " Shops konnten nicht geladen werden");
        }
    }

    public void saveAll() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (PlayerShop shop : plugin.shops().all()) {
            String key = shop.key();
            String path = "shops." + key;

            yaml.set(path + ".owner", shop.getOwner().toString());
            yaml.set(path + ".world", shop.getBaseLocation().getWorld().getName());
            yaml.set(path + ".x", shop.getBaseLocation().getBlockX());
            yaml.set(path + ".y", shop.getBaseLocation().getBlockY());
            yaml.set(path + ".z", shop.getBaseLocation().getBlockZ());
            yaml.set(path + ".amountPerTrade", shop.getAmountPerTrade());
            yaml.set(path + ".buyPrice", shop.getBuyPrice());
            yaml.set(path + ".sellPrice", shop.getSellPrice());
            yaml.set(path + ".buyEnabled", shop.isBuyEnabled());
            yaml.set(path + ".sellEnabled", shop.isSellEnabled());

            try {
                String displayItemEncoded = itemToBase64(shop.getDisplayItem());
                yaml.set(path + ".displayItem", displayItemEncoded);
            } catch (Exception e) {
                plugin.getLogger().severe("Fehler beim Speichern von displayItem für Shop " + key + ": " + e.getMessage());
            }

            List<String> stashEncoded = new ArrayList<>();
            for (ItemStack item : shop.getStashItems()) {
                if (item != null && !item.getType().isAir()) {
                    try {
                        stashEncoded.add(itemToBase64(item));
                    } catch (Exception e) {
                        plugin.getLogger().warning("Konnte Stash-Item nicht speichern: " + e.getMessage());
                    }
                }
            }
            yaml.set(path + ".stashItems", stashEncoded);
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Shops: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void createBackupIfNeeded() {
        if (!file.exists()) return;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        if (!yaml.isConfigurationSection("shops")) return;

        boolean needsMigration = false;
        for (String key : yaml.getConfigurationSection("shops").getKeys(false)) {
            ConfigurationSection sec = yaml.getConfigurationSection("shops." + key);
            if (sec == null) continue;

            if (sec.isString("material") && !sec.isString("displayItem")) {
                needsMigration = true;
                break;
            }
        }

        if (!needsMigration) {
            plugin.getLogger().info("Keine Shop-Migration erforderlich.");
            return;
        }

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        File backupFile = new File(file.getParent(), "shops.yml.backup-" + timestamp);

        try {
            java.nio.file.Files.copy(file.toPath(), backupFile.toPath());
            plugin.getLogger().info("╔═══════════════════════════════════════════════════════╗");
            plugin.getLogger().info("║  Shop-Migration wird durchgeführt                     ║");
            plugin.getLogger().info("║  Backup erstellt: " + String.format("%-32s", backupFile.getName()) + "║");
            plugin.getLogger().info("╚═══════════════════════════════════════════════════════╝");
        } catch (IOException e) {
            plugin.getLogger().severe("╔═══════════════════════════════════════════════════════╗");
            plugin.getLogger().severe("║  FEHLER: Konnte kein Backup erstellen!               ║");
            plugin.getLogger().severe("║  Migration wird ABGEBROCHEN aus Sicherheitsgründen   ║");
            plugin.getLogger().severe("╚═══════════════════════════════════════════════════════╝");
            e.printStackTrace();
            throw new RuntimeException("Backup fehlgeschlagen - Migration abgebrochen!");
        }
    }
}
