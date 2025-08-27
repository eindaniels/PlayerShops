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

        for (String key : yaml.getConfigurationSection("shops").getKeys(false)) {
            ConfigurationSection sec = yaml.getConfigurationSection("shops." + key);
            if (sec == null) continue;
            String world = sec.getString("world");
            int x = sec.getInt("x");
            int y = sec.getInt("y");
            int z = sec.getInt("z");
            World w = Bukkit.getWorld(world);
            if (w == null) continue;

            Location base = new Location(w, x + 0.5, y, z + 0.5);
            String matName = sec.getString("material");
            int amountPerTrade = sec.getInt("amountPerTrade");
            double buy = sec.getDouble("buyPrice");
            double sell = sec.getDouble("sellPrice");
            boolean buyEnabled = sec.getBoolean("buyEnabled", true);
            boolean sellEnabled = sec.getBoolean("sellEnabled", true);
            UUID owner = UUID.fromString(sec.getString("owner"));

            var mat = Optional.ofNullable(matName).map(org.bukkit.Material::matchMaterial).orElse(null);
            if (mat == null) continue;

            PlayerShop shop = new PlayerShop(owner, base, mat, amountPerTrade, buy, sell);
            shop.setBuyEnabled(buyEnabled);
            shop.setSellEnabled(sellEnabled);

            List<String> stashEncoded = sec.getStringList("stashItems");
            for (String encoded : stashEncoded) {
                try {
                    shop.getStashItems().add(itemFromBase64(encoded));
                } catch (Exception ex) {
                    plugin.getLogger().warning("Konnte Item in Shop " + key + " nicht laden: " + ex.getMessage());
                }
            }

            plugin.shops().put(shop);
        }
        plugin.getLogger().info("Geladen: " + plugin.shops().all().size() + " PlayerShops.");
    }

    public void saveAll() throws IOException {
        YamlConfiguration out = new YamlConfiguration();

        for (PlayerShop shop : plugin.shops().all()) {
            String key = shop.key();
            ConfigurationSection sec = out.createSection("shops." + key);
            sec.set("owner", shop.getOwner().toString());
            sec.set("world", shop.getBaseLocation().getWorld().getName());
            sec.set("x", shop.getBaseLocation().getBlockX());
            sec.set("y", shop.getBaseLocation().getBlockY());
            sec.set("z", shop.getBaseLocation().getBlockZ());
            sec.set("material", shop.getMaterial().name());
            sec.set("amountPerTrade", shop.getAmountPerTrade());
            sec.set("buyPrice", shop.getBuyPrice());
            sec.set("sellPrice", shop.getSellPrice());
            sec.set("buyEnabled", shop.isBuyEnabled());
            sec.set("sellEnabled", shop.isSellEnabled());

            List<String> stashEncoded = new ArrayList<>();
            for (ItemStack is : shop.getStashItems()) {
                if (is != null && is.getType() != Material.AIR) {
                    try {
                        stashEncoded.add(itemToBase64(is));
                    } catch (Exception ex) {
                        plugin.getLogger().warning("Konnte Item in Shop " + key + " nicht speichern: " + ex.getMessage());
                    }
                }
            }
            sec.set("stashItems", stashEncoded);
        }

        out.save(file);
    }
}
