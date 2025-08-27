package de.eindaniel.playerShops.shop;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.entity.ShopEntityManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class ShopManager {

    private final Main plugin;
    private final ShopStorage storage;

    private final Map<String, PlayerShop> shops = new HashMap<>();

    public ShopManager(Main plugin, ShopStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public Optional<PlayerShop> getByKey(String key) { return Optional.ofNullable(shops.get(key)); }
    public Collection<PlayerShop> all() { return Collections.unmodifiableCollection(shops.values()); }

    public PlayerShop create(Player owner, Location base, Material mat, int amountPerTrade,
                             double buyPrice, double sellPrice) {
        PlayerShop shop = new PlayerShop(owner.getUniqueId(), base, mat, amountPerTrade, buyPrice, sellPrice);
        shops.put(shop.key(), shop);
        return shop;
    }

    public boolean delete(PlayerShop shop) {
        return shops.remove(shop.key()) != null;
    }

    public void put(PlayerShop shop) { shops.put(shop.key(), shop); }

    public void clear() { shops.clear(); }
}
